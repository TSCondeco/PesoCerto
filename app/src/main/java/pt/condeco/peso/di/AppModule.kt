package pt.condeco.peso.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pt.condeco.peso.data.SettingsRepository
import pt.condeco.peso.data.WeightDatabase
import pt.condeco.peso.notifications.ReminderManager
import pt.condeco.peso.ui.WeightViewModel
import pt.condeco.peso.widget.WidgetUpdater

val appModule = module {
    single { WeightDatabase.getDatabase(androidContext()) }
    single { get<WeightDatabase>().weightDao() }
    single { SettingsRepository(androidContext()) }
    single { ReminderManager(androidContext()) }
    single { WidgetUpdater(androidContext()) }
    viewModel {
        val widgetUpdater: WidgetUpdater = get()
        WeightViewModel(get(), get(), get(), onEntriesChanged = widgetUpdater::refreshAll)
    }
}
