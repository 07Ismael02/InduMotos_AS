package pe.indumotos.mobile.di
import org.koin.dsl.module
import org.koin.core.module.Module
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.indumotos.mobile.db.OfflineDb
actual val platformModule:Module=module{
 single<HttpClientEngine>{Darwin.create()}
 single<SqlDriver>{NativeSqliteDriver(OfflineDb.Schema,"indumotos-offline.db")}
}
