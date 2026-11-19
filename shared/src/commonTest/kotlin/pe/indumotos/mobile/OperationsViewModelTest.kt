package pe.indumotos.mobile

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.OperationsViewModel
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class OperationsViewModelTest {
    @BeforeTest fun setup() { Dispatchers.setMain(StandardTestDispatcher()) }
    @AfterTest fun cleanup() { Dispatchers.resetMain() }

    private val body = NewRequest(1,"Académico",listOf(NewRequestLine("Yamaha","FZ","Azul",1),NewRequestLine("Honda","Wave","Rojo",2)))

    @Test fun doubleTapDoesNotDuplicatePost() = runTest {
        val repo=Fake();repo.gate=CompletableDeferred();val vm=OperationsViewModel(repo,repo)
        vm.create(body);runCurrent();vm.create(body);runCurrent()
        assertEquals(1,repo.creations);assertTrue(vm.state.value.busy)
        repo.gate!!.complete(Unit);advanceUntilIdle()
        assertEquals(2,repo.lastBody!!.lineas.size);assertEquals(7,vm.state.value.selected!!.id)
        assertFalse(vm.state.value.busy)
    }

    @Test fun invalidSecondLineDoesNotPartiallySend() = runTest {
        val repo=Fake();val vm=OperationsViewModel(repo,repo)
        vm.create(body.copy(lineas=body.lineas+NewRequestLine("","Wave","Rojo",1)));advanceUntilIdle()
        assertEquals(0,repo.creations);assertNull(vm.state.value.selected);assertNotNull(vm.state.value.error)
    }

    @Test fun uncertainResponseIsNotRetriedOrMarkedSaved() = runTest {
        val repo=Fake();repo.failure=AppError.Network;val vm=OperationsViewModel(repo,repo)
        vm.create(body);advanceUntilIdle()
        assertEquals(1,repo.creations);assertNull(vm.state.value.selected);assertNull(vm.state.value.message)
        assertTrue(vm.state.value.error!!.contains("antes de repetir"))
    }

    @Test fun conflictPreservesRequestWithoutInventingAssignedUnit() = runTest {
        val repo=Fake();val vm=OperationsViewModel(repo,repo)
        vm.select(7);advanceUntilIdle();vm.line(repo.record.lineas.first());advanceUntilIdle()
        repo.failure=AppError.Conflict("Cupo completo")
        vm.assign(EligibleUnit(99,"CHASIS","Yamaha","FZ","Azul"));advanceUntilIdle()
        assertEquals(7,vm.state.value.selected!!.id);assertTrue(vm.state.value.assigned.isEmpty())
        assertNull(vm.state.value.message);assertEquals("Cupo completo",vm.state.value.error)
    }

    @Test fun cancellationNeedsReasonAndDoesNotRunForPreparedRequest() = runTest {
        val repo=Fake();val vm=OperationsViewModel(repo,repo)
        vm.select(7);advanceUntilIdle();vm.cancel("Motivo");advanceUntilIdle()
        assertEquals(0,repo.cancellations)
        repo.record=repo.record.copy(estado="PENDIENTE");vm.select(7);advanceUntilIdle()
        vm.cancel(" ");advanceUntilIdle();assertEquals(0,repo.cancellations)
        vm.cancel("Solicitud innecesaria");advanceUntilIdle()
        assertEquals(1,repo.cancellations);assertEquals("ANULADA",vm.state.value.selected!!.estado)
    }

    @Test fun resetCancelsPendingWorkAndClearsAccountData() = runTest {
        val repo=Fake();repo.gate=CompletableDeferred();val vm=OperationsViewModel(repo,repo)
        vm.create(body);runCurrent();vm.reset();repo.gate!!.complete(Unit);advanceUntilIdle()
        assertNull(vm.state.value.selected);assertTrue(vm.state.value.requests.isEmpty());assertFalse(vm.state.value.busy)
    }

    private class Fake:OperationsRepository,LocationRepository {
        var record=AssemblyRequest(7,"admin.indumotos",1,"2026-11-19T00:00:00Z",estado="EN_PREPARACION",lineas=listOf(RequestLine(1,"Yamaha","FZ","Azul",1)))
        var creations=0;var cancellations=0;var lastBody:NewRequest?=null;var failure:AppError?=null
        var gate:CompletableDeferred<Unit>?=null
        override suspend fun requests(page:Int)=listOf(record)
        override suspend fun request(id:Long)=record
        override suspend fun createRequest(body:NewRequest):AssemblyRequest {
            creations++;lastBody=body;gate?.await();failure?.let{throw AppException(it)}
            record=record.copy(estado="PENDIENTE",lineas=body.lineas.mapIndexed{i,l->RequestLine(i+1L,l.marca,l.modelo,l.color,l.cantidad)})
            return record
        }
        override suspend fun events(id:Long)=emptyList<RequestEvent>()
        override suspend fun advance(id:Long,status:String,reason:String){record=record.copy(estado=status)}
        override suspend fun cancel(id:Long,reason:String){cancellations++;record=record.copy(estado="ANULADA")}
        override suspend fun eligible(chassis:String)=emptyList<EligibleUnit>()
        override suspend fun assigned(id:Long,line:Long)=emptyList<AssignedUnit>()
        override suspend fun assign(id:Long,line:Long,motorcycle:Long){failure?.let{throw AppException(it)}}
        override suspend fun remove(id:Long,line:Long,association:Long,reason:String){}
        override suspend fun transfer(motorcycle:Long,destination:Long)=error("not used")
        override suspend fun transfers(motorcycle:Long)=emptyList<TransferRecord>()
        override suspend fun sites()=emptyList<Site>()
        override suspend fun currentLocation(id:Long):LocationRecord?=null
        override suspend fun registerLocation(id:Long,destinationId:Long)=error("not used")
    }
}
