package pe.indumotos.mobile

import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.GuidesViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class GuideWorkflowTest {
    @BeforeTest fun setup(){Dispatchers.setMain(StandardTestDispatcher())}
    @AfterTest fun cleanup(){Dispatchers.resetMain()}
    private val draft=MotorcycleDraft(1,"Honda","Wave","Rojo","CHASIS-123456789","MOTOR123456","DUA","1")
    private val arrival=Arrival(10,"GUIA-10","2026-11-23",1,1)

    @Test fun finalizationRejectsUnsavedChangesAndMissingPositions() {
        val registration=GuideRegistration(false,1,0,listOf(draft))
        assertTrue(GuideRules.canFinish(registration,listOf(draft)))
        assertFalse(GuideRules.canFinish(registration,listOf(draft.copy(numeroMotor="OTRO"))))
        assertFalse(GuideRules.canFinish(registration,listOf(draft.copy(posicion=2))))
        assertFalse(GuideRules.canFinish(registration.copy(terminado=true),listOf(draft)))
    }
    @Test fun onlySavedDraftCreatesFinalInventory()=runTest {
        val repo=Fake();val vm=GuidesViewModel(repo,repo)
        vm.select(arrival,true);advanceUntilIdle();vm.edit(draft);vm.finish();advanceUntilIdle()
        assertEquals(0,repo.finishes)
        vm.save(draft);advanceUntilIdle();assertFalse(vm.state.value.registration!!.terminado)
        vm.finish();advanceUntilIdle();assertEquals(1,repo.finishes);assertTrue(vm.state.value.registration!!.terminado)
    }
    @Test fun ambiguousUploadDoesNotRetryOrInventArrival()=runTest {
        val repo=Fake();repo.failUpload=true;val vm=GuidesViewModel(repo,repo)
        vm.register("GUIA",1,1,"",listOf(GuidePhoto("foto","image/png",byteArrayOf(1))));advanceUntilIdle()
        assertEquals(1,repo.uploads);assertNull(vm.state.value.selected);assertTrue(vm.state.value.error!!.contains("antes de repetir"))
    }
    @Test fun doubleTapCannotFinalizeTwice()=runTest {
        val repo=Fake();val vm=GuidesViewModel(repo,repo);vm.select(arrival,true);advanceUntilIdle();vm.edit(draft);vm.save(draft);advanceUntilIdle()
        repo.gate=CompletableDeferred();vm.finish();runCurrent();vm.finish();runCurrent();assertEquals(1,repo.finishes)
        repo.gate!!.complete(Unit);advanceUntilIdle();assertTrue(vm.state.value.registration!!.terminado)
    }
    @Test fun resetCancelsOldAccountWork()=runTest {
        val repo=Fake();repo.gate=CompletableDeferred();val vm=GuidesViewModel(repo,repo)
        vm.select(arrival,true);advanceUntilIdle();vm.edit(draft);vm.save(draft);advanceUntilIdle();vm.finish();runCurrent();vm.reset()
        repo.gate!!.complete(Unit);advanceUntilIdle();assertNull(vm.state.value.selected);assertNull(vm.state.value.registration)
    }
    @Test fun newOcrSuggestionRequiresReviewAgain()=runTest {
        val repo=Fake();val vm=GuidesViewModel(repo,repo);vm.select(arrival,true);advanceUntilIdle();vm.ocr();advanceUntilIdle()
        vm.reviewed(true);vm.useSuggestion(0,1);assertFalse(vm.state.value.ocrReviewed)
        vm.save(vm.state.value.drafts.first());advanceUntilIdle();assertTrue(repo.current.borradores.isEmpty())
    }
    private inner class Fake:GuideRepository,CatalogRepository {
        override suspend fun detail(id:Long)=ArrivalDetail(arrival,emptyList(),0,1)
        var current=GuideRegistration(false,1,0,emptyList());var finishes=0;var uploads=0;var failUpload=false;var gate:CompletableDeferred<Unit>?=null
        override suspend fun arrivals(guide:String,state:String,page:Int)=ArrivalPage(listOf(arrival),0,1,1)
        override suspend fun register(guide:String,declared:Int,received:Int,note:String,photos:List<GuidePhoto>):Arrival{uploads++;if(failUpload)throw AppException(AppError.Network);return arrival}
        override suspend fun images(id:Long)=emptyList<GuideImage>()
        override suspend fun image(id:Long,image:Long)=byteArrayOf()
        override suspend fun registration(id:Long)=current
        override suspend fun saveDraft(id:Long,draft:MotorcycleDraft):GuideRegistration{current=current.copy(borradores=listOf(draft));return current}
        override suspend fun finish(id:Long):GuideRegistration{finishes++;gate?.await();current=current.copy(terminado=true,registradas=1,borradores=emptyList());return current}
        override suspend fun ocr(id:Long)=OcrResult(1,listOf(OcrSuggestion(1,"Honda","Wave","Rojo","CHASIS-123456789","MOTOR123456","DUA","1")))
        override suspend fun catalog()=listOf(CatalogOption("Honda","Wave","Rojo"))
        override suspend fun requestOptions()=catalog()
        override suspend fun addCatalog(option:CatalogOption)=option
    }
}
