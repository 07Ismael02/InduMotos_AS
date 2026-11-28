package pe.indumotos.mobile

import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.GuidesViewModel
import kotlinx.serialization.json.*

@OptIn(ExperimentalCoroutinesApi::class)
class GuideWorkflowTest {
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    private val draft =
        MotorcycleDraft(1, "Honda", "Wave", "Rojo", "CHASIS-123456789", "MOTOR123456", "DUA", "1")
    private val arrival = Arrival(10, "GUIA-10", "2026-11-23", 1, 1)

    @Test
    fun finalizationRejectsUnsavedChangesAndMissingPositions() {
        val registration = GuideRegistration(false, 1, 0, listOf(draft))
        assertTrue(GuideRules.canFinish(registration, listOf(draft)))
        assertFalse(GuideRules.canFinish(registration, listOf(draft.copy(numeroMotor = "OTRO"))))
        assertFalse(GuideRules.canFinish(registration, listOf(draft.copy(posicion = 2))))
        assertFalse(GuideRules.canFinish(registration.copy(terminado = true), listOf(draft)))
    }

    @Test
    fun onlySavedDraftCreatesFinalInventory() = runTest {
        val repo = Fake();

        val vm = GuidesViewModel(repo, repo)
        vm.select(
            arrival,
            true
        );
        advanceUntilIdle();
        vm.edit(draft);
        vm.finish();
        advanceUntilIdle()
        assertEquals(0, repo.finishes)
        vm.save(draft);
        advanceUntilIdle();
        assertFalse(vm.state.value.registration!!.terminado)
        vm.finish();
        advanceUntilIdle();
        assertEquals(
            1,
            repo.finishes
        );
        assertTrue(vm.state.value.registration!!.terminado)
    }

    @Test
    fun guideNumberFollowsSunatSeriesAndNumber() {
        assertEquals("T001-00012345", GuideRules.guideNumber("t001-12345"))
        assertEquals("EG07-00000123", GuideRules.guideNumber(" EG07-123 "))
        assertEquals("0001-00000042", GuideRules.guideNumber("0001-42"))
        listOf("1737jfj17", "T001", "T01-123", "T001-", "T001-123456789", "T001-0000").forEach {
            assertFailsWith<IllegalArgumentException>(it) { GuideRules.guideNumber(it) }
        }
        assertEquals("T001-12", GuideRules.guideNumberTyping("t0 01-1a2"))
        assertEquals("EG07", GuideRules.guideNumberTyping("eg07x"))
        assertEquals("T001-12345678", GuideRules.guideNumberTyping("T001-123456789-"))
    }

    @Test
    fun invalidGuideIsNotSentAndValidOneIsNormalized() = runTest {
        val repo = Fake()
        val vm = GuidesViewModel(repo, repo)
        val photo = listOf(GuidePhoto("foto", "image/png", byteArrayOf(1)))
        vm.register("1737jfj17", 1, 1, "", photo)
        advanceUntilIdle()
        assertEquals(0, repo.uploads)
        vm.register("t001-123", 1, 1, "", photo)
        advanceUntilIdle()
        assertEquals("T001-00000123", repo.lastGuide)
    }

    @Test
    fun ambiguousUploadDoesNotRetryOrInventArrival() = runTest {
        val repo = Fake();
        repo.failUpload = true;

        val vm = GuidesViewModel(repo, repo)
        vm.register(
            "T001-1",
            1,
            1,
            "",
            listOf(GuidePhoto("foto", "image/png", byteArrayOf(1)))
        );
        advanceUntilIdle()
        assertEquals(
            1,
            repo.uploads
        );
        assertNull(vm.state.value.selected);
        assertTrue(vm.state.value.error!!.contains("antes de repetir"))
    }

    @Test
    fun doubleTapCannotFinalizeTwice() = runTest {
        val repo = Fake();

        val vm = GuidesViewModel(repo, repo);
        vm.select(arrival, true);
        advanceUntilIdle();
        vm.edit(
            draft
        );
        vm.save(draft);
        advanceUntilIdle()
        repo.gate =
            CompletableDeferred();
        vm.finish();
        runCurrent();
        vm.finish();
        runCurrent();
        assertEquals(
            1,
            repo.finishes
        )
        repo.gate!!.complete(Unit);
        advanceUntilIdle();
        assertTrue(vm.state.value.registration!!.terminado)
    }

    @Test
    fun resetCancelsOldAccountWork() = runTest {
        val repo = Fake();
        repo.gate = CompletableDeferred();

        val vm = GuidesViewModel(repo, repo)
        vm.select(
            arrival,
            true
        );
        advanceUntilIdle();
        vm.edit(draft);
        vm.save(draft);
        advanceUntilIdle();
        vm.finish();
        runCurrent();
        vm.reset()
        repo.gate!!.complete(Unit);
        advanceUntilIdle();
        assertNull(vm.state.value.selected);
        assertNull(
            vm.state.value.registration
        )
    }

    @Test
    fun newOcrSuggestionRequiresReviewAgain() = runTest {
        val repo = Fake();

        val vm = GuidesViewModel(repo, repo);
        vm.select(
            arrival,
            true
        );
        advanceUntilIdle();
        vm.ocr();
        advanceUntilIdle()
        vm.reviewed(true);
        vm.useSuggestion(0, 1);
        assertFalse(vm.state.value.ocrReviewed)
        vm.save(vm.state.value.drafts.first());
        advanceUntilIdle();
        assertTrue(repo.current.borradores.isEmpty())
    }

    @Test
    fun linkedMotorcyclesBelongToSelectedGuideAndClearOnExit() = runTest {
        val repo = Fake();
        repo.linked =
            listOf(buildJsonObject {
                put("id", 77);
                put("llegadaId", arrival.id)
            })
        val vm = GuidesViewModel(repo, repo);
        vm.select(arrival, false);
        advanceUntilIdle()
        assertEquals(77, vm.state.value.detail!!.fichas.single()["id"]!!.jsonPrimitive.int)
        vm.list();
        advanceUntilIdle();
        assertNull(vm.state.value.detail)
    }

    private inner class Fake : GuideRepository, CatalogRepository {
        var linked: List<JsonObject> = emptyList()
        override suspend fun detail(id: Long) =
            ArrivalDetail(arrival, linked, linked.size, 1 - linked.size)

        var current = GuideRegistration(false, 1, 0, emptyList());

        var finishes = 0;

        var uploads = 0;

        var failUpload = false;
        var lastGuide: String? = null

        var gate: CompletableDeferred<Unit>? = null
        override suspend fun arrivals(guide: String, state: String, page: Int) =
            ArrivalPage(listOf(arrival), 0, 1, 1)

        override suspend fun register(
            guide: String,
            declared: Int,
            received: Int,
            note: String,
            photos: List<GuidePhoto>
        ): Arrival {
            uploads++;
            lastGuide = guide;
            if (failUpload) throw AppException(AppError.Network);
            return arrival
        }

        override suspend fun images(id: Long) = emptyList<GuideImage>()
        override suspend fun image(id: Long, image: Long) = byteArrayOf()
        override suspend fun registration(id: Long) = current
        override suspend fun saveDraft(id: Long, draft: MotorcycleDraft): GuideRegistration {
            current = current.copy(borradores = listOf(draft));
            return current
        }

        override suspend fun finish(id: Long): GuideRegistration {
            finishes++;
            gate?.await();
            current = current.copy(
                terminado = true,
                registradas = 1,
                borradores = emptyList()
            );
            return current
        }

        override suspend fun ocr(id: Long) = OcrResult(
            1,
            listOf(
                OcrSuggestion(
                    1,
                    "Honda",
                    "Wave",
                    "Rojo",
                    "CHASIS-123456789",
                    "MOTOR123456",
                    "DUA",
                    "1"
                )
            )
        )

        override suspend fun catalog() = listOf(CatalogOption("Honda", "Wave", "Rojo"))
        override suspend fun requestOptions() = catalog()
    }
}
