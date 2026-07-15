package com.imnaiyar.skytimes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.imnaiyar.skytimes.di.AppContainer
import com.imnaiyar.skytimes.reminders.IosReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
fun MainViewController() = ComposeUIViewController {
	val appContainer = remember {
		AppContainer { settingsRepository, reminderRepository, scope ->
			IosReminderScheduler(
				settingsRepository = settingsRepository,
				reminderRepository = reminderRepository,
				scope = scope
			)
		}
	}

	App(appContainer)
}