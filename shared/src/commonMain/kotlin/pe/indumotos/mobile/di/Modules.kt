package pe.indumotos.mobile.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.indumotos.mobile.data.RemoteRepository
import pe.indumotos.mobile.data.createHttpClient
import pe.indumotos.mobile.domain.*
import pe.indumotos.mobile.presentation.LogisticsViewModel
import pe.indumotos.mobile.presentation.operations.OperationsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.KoinApplication
import pe.indumotos.mobile.db.OfflineDb

expect val platformModule: Module
val appModule = module {
    single { OfflineDb(get()) }
    single<OfflineStore> { pe.indumotos.mobile.data.SqlOfflineStore(get()) }
    single { pe.indumotos.mobile.data.OfflineSession(get()) }
    single { createHttpClient(get()) }
    single { RemoteRepository(get(), get(), get()) }
    single<LogisticsRepository> { get<RemoteRepository>() }
    single<LocationRepository> { pe.indumotos.mobile.data.LocationRemoteRepository(get()) }
    single { pe.indumotos.mobile.data.GuideRemoteRepository(get()) }
    single {
        pe.indumotos.mobile.data.DraftSynchronizer(
            get(),
            get(),
            pe.indumotos.mobile.data.GuideRemoteRepository(get(), true)
        ) {
            val remote = get<RemoteRepository>();

            val session = get<pe.indumotos.mobile.data.OfflineSession>()
            val before = requireNotNull(session.current)
            val access = remote.profile()
            session.confirmOnline(access, before);
            access
        }
    }
    single<GuideRepository> {
        pe.indumotos.mobile.data.OfflineGuideRepository(
            get<pe.indumotos.mobile.data.GuideRemoteRepository>(),
            pe.indumotos.mobile.data.GuideRemoteRepository(get(), true),
            get(),
            get(),
            get()
        )
    }
    single<OfflineWork> {
        pe.indumotos.mobile.data.OfflineService(
            get(),
            get<pe.indumotos.mobile.data.GuideRemoteRepository>(),
            get(),
            get(),
            get<VehicleRepository>()
        )
    }
    single<VehicleRepository> { pe.indumotos.mobile.data.VehicleRemoteRepository(get()) }
    viewModel { pe.indumotos.mobile.presentation.inventory.VehicleViewModel(get()) }
    viewModel { pe.indumotos.mobile.presentation.guides.GuidesViewModel(get(), get()) }
    single<CatalogRepository> { pe.indumotos.mobile.data.CatalogRemoteRepository(get()) }
    single<OperationsRepository> { pe.indumotos.mobile.data.OperationsRemoteRepository(get()) }
    viewModel { OperationsViewModel(get(), get(), get(), get()) }
    factory { LogisticsUseCase(get()) }
    viewModel { LogisticsViewModel(get(), get(), get(), get(), get(), get()) }
}

fun initKoin(configure: KoinApplication.() -> Unit = {}) =
    startKoin {
        configure();
        modules(platformModule, appModule)
    }
