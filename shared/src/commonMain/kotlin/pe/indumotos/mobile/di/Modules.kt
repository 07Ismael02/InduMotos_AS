package pe.indumotos.mobile.di
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.indumotos.mobile.data.RemoteRepository
import pe.indumotos.mobile.data.createHttpClient
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.LogisticsViewModel
import pe.indumotos.mobile.presentation.OperationsViewModel
import org.koin.core.module.dsl.viewModel
expect val platformModule:Module
val appModule = module {
    single { createHttpClient(get()) }
    single { RemoteRepository(get()) }
    single<LogisticsRepository> { get<RemoteRepository>() }
    single<LocationRepository> { get<RemoteRepository>() }
    single<GuideRepository> { pe.indumotos.mobile.data.GuideRemoteRepository(get()) }
    viewModel { pe.indumotos.mobile.presentation.GuidesViewModel(get(), get()) }
    single<CatalogRepository> { get<RemoteRepository>() }
    viewModel { pe.indumotos.mobile.presentation.CatalogViewModel(get()) }
    single<OperationsRepository> { get<RemoteRepository>() }
    viewModel { OperationsViewModel(get(), get(), get(), get()) }
    factory { LogisticsUseCase(get()) }
    viewModel { LogisticsViewModel(get(), get(), get(), get()) }
}
fun initKoin()=startKoin{modules(platformModule,appModule)}
