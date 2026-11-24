package pe.indumotos.mobile.di
import org.koin.dsl.module
import org.koin.core.module.Module
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import pe.indumotos.mobile.db.OfflineDb
actual val platformModule:Module=module{
 single<HttpClientEngine>{OkHttp.create()}
 single<SqlDriver>{AndroidSqliteDriver(OfflineDb.Schema,androidContext(),"indumotos-offline.db")}
}
