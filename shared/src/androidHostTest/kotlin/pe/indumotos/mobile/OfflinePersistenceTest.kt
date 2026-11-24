package pe.indumotos.mobile

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import java.nio.file.Files
import pe.indumotos.mobile.db.OfflineDb
import pe.indumotos.mobile.data.SqlOfflineStore
import pe.indumotos.mobile.data.OfflineSession
import pe.indumotos.mobile.domain.*

class OfflinePersistenceTest {
    private val draft=MotorcycleDraft(1,"Honda","Wave","Rojo","CHASISQA123456","MOTORQA1234","DUA-QA","1")
    private fun memory():Pair<JdbcSqliteDriver,SqlOfflineStore>{val d=JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY);OfflineDb.Schema.create(d);return d to SqlOfflineStore(OfflineDb(d))}
    @Test fun draftAndCacheSurviveDatabaseReopen()=runTest {
        val path=Files.createTempFile("indumotos-offline-", ".db")
        val url="jdbc:sqlite:$path"
        var driver=JdbcSqliteDriver(url)
        try {
            OfflineDb.Schema.create(driver);val store=SqlOfflineStore(OfflineDb(driver))
            store.put("server|1","catalog","all","catalogo-local",123)
            store.edit("server|1",8,draft,null,"PENDING");driver.close();driver=JdbcSqliteDriver(url)
            val reopened=SqlOfflineStore(OfflineDb(driver))
            assertEquals("catalogo-local",reopened.get("server|1","catalog","all")!!.payload)
            assertEquals(8L,reopened.drafts("server|1").single().guideId)
        } finally {driver.close();Files.deleteIfExists(path)}
    }
    @Test fun serverAndUserPartitionsNeverShareData()=runTest {
        val (d,s)=memory();try {
            s.put("serverA|1","ficha","8","privado-A",1);s.edit("serverA|1",8,draft,null,"PENDING")
            assertNull(s.get("serverA|2","ficha","8"));assertNull(s.get("serverB|1","ficha","8"));assertTrue(s.drafts("serverA|2").isEmpty())
        }finally{d.close()}
    }
    @Test fun oldAcknowledgmentCannotDeleteNewerLocalEdit()=runTest {
        val (d,s)=memory();try {
            val first=s.edit("s|1",8,draft,null,"PENDING")
            val second=s.edit("s|1",8,draft.copy(numeroMotor="NUEVO"),null,"PENDING")
            s.acknowledge(first,first.payload)
            val pending=s.drafts("s|1").single()
            assertEquals(second.revision,pending.revision);assertEquals(first.payload,pending.baseline)
            s.acknowledge(pending,pending.payload);assertTrue(s.drafts("s|1").isEmpty())
        }finally{d.close()}
    }
    @Test fun conflictOnOldRevisionDoesNotOverwriteNewEdit()=runTest {
        val (d,s)=memory();try {
            val first=s.edit("s|1",8,draft,null,"PENDING")
            s.edit("s|1",8,draft.copy(color="Azul"),null,"EDITING")
            s.mark(first,"CONFLICT","El servidor cambió")
            assertEquals("EDITING",s.drafts("s|1").single().state)
        }finally{d.close()}
    }
    @Test fun logoutProfileRemovalPreservesOnlyPartitionedDrafts()=runTest {
        val (d,s)=memory();try {
            val session=OfflineSession(s);session.online("http://localhost",Access(1,"qa","INVENTARIOS",emptyList()))
            val p=s.profile()!!;s.edit(p.owner,8,draft,null,"EDITING");s.profile(null);session.close()
            assertNull(session.available());assertEquals(1,s.drafts(p.owner).size)
        }finally{d.close()}
    }
    @Test fun expiredOfflineProfileCannotUnlock()=runTest {
        val (d,s)=memory();try {
            s.profile(OfflineProfile("s|1","s",Access(1,"qa","ADMIN",emptyList()),epochMillis()-86_400_001))
            assertNull(OfflineSession(s).available())
        }finally{d.close()}
    }
    @Test fun oldConnectionCheckCannotRestoreLoggedOutProfile()=runTest {
        val (d,s)=memory();try {
            val session=OfflineSession(s);val access=Access(1,"qa","INVENTARIOS",emptyList())
            session.online("http://localhost",access);val before=session.current!!
            s.profile(null);session.close()
            assertFailsWith<kotlinx.coroutines.CancellationException>{session.confirmOnline(access,before)}
            assertNull(session.current);assertNull(s.profile())
        }finally{d.close()}
    }
}
