package com.imnaiyar.skytimes.constants

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone

val RoundedCorner = RoundedCornerShape(16.dp);

val GameTimeZone = TimeZone.of("America/Los_Angeles")
val LocalTimeZone = TimeZone.currentSystemDefault()

const val SkyHelperApi = "https://api.skyhelper.xyz";

const val SkyHelperCdn = "https://cdn.skyhelper.xyz"

const val dateDisclaimer = "The displayed date reflects the game's reset date at midnight," +
        " which is in LA timezone where TGC is based on. It may differ from your actual local date"

const val MinFlowRowWidth = 400