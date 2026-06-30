package com.imnaiyar.skytimes.theme

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.clash_bold
import skytimes.shared.generated.resources.clash_regular
import androidx.compose.material3.Typography

@Composable
fun appTypography(): Typography {
    val appFont = FontFamily(
        Font(Res.font.clash_regular, FontWeight.Bold),
        Font(Res.font.clash_bold, FontWeight.Bold)
    )
    return Typography(
        displayLarge = Typography().displayLarge.copy(fontFamily = appFont),
        displayMedium = Typography().displayMedium.copy(fontFamily = appFont),
        displaySmall = Typography().displaySmall.copy(fontFamily = appFont),
        headlineLarge = Typography().headlineLarge.copy(fontFamily = appFont),
        headlineMedium = Typography().headlineMedium.copy(fontFamily = appFont),
        headlineSmall = Typography().headlineSmall.copy(fontFamily = appFont),
        titleLarge = Typography().titleLarge.copy(fontFamily = appFont),
        titleMedium = Typography().titleMedium.copy(fontFamily = appFont),
        titleSmall = Typography().titleSmall.copy(fontFamily = appFont),
        bodyLarge = Typography().bodyLarge.copy(fontFamily = appFont),
        bodyMedium = Typography().bodyMedium.copy(fontFamily = appFont),
        bodySmall = Typography().bodySmall.copy(fontFamily = appFont),
        labelLarge = Typography().labelLarge.copy(fontFamily = appFont),
        labelMedium = Typography().labelMedium.copy(fontFamily = appFont),
        labelSmall = Typography().labelSmall.copy(fontFamily = appFont)
    )
}