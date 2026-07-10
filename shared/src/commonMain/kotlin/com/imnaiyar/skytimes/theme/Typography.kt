package com.imnaiyar.skytimes.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import skytimes.shared.generated.resources.Res
import skytimes.shared.generated.resources.clash_bold
import skytimes.shared.generated.resources.clash_regular

private val default = Typography()

@Composable
fun appTypography(): Typography {

    val appFont = FontFamily(
        Font(Res.font.clash_regular, FontWeight.Normal),
        Font(Res.font.clash_bold, FontWeight.Bold)
    )

    fun TextStyle.withAppFont(sizeReduction: TextUnit = 2.sp) = copy(
        fontFamily = appFont,
        fontSize = (fontSize.value - sizeReduction.value).sp
    )
    return default.copy(
        displayLarge = default.displayLarge.withAppFont(),
        displayMedium = default.displayMedium.withAppFont(),
        displaySmall = default.displaySmall.withAppFont(),
        headlineLarge = default.headlineLarge.withAppFont(),
        headlineMedium = default.headlineMedium.withAppFont(),
        headlineSmall = default.headlineSmall.withAppFont(),
        titleLarge = default.titleLarge.withAppFont(),
        titleMedium = default.titleMedium.withAppFont(),
        titleSmall = default.titleSmall.withAppFont(),
        bodyLarge = default.bodyLarge.withAppFont(),
        bodyMedium = default.bodyMedium.withAppFont(),
        bodySmall = default.bodySmall.withAppFont(),
        labelLarge = default.labelLarge.withAppFont(),
        labelMedium = default.labelMedium.withAppFont(),
        labelSmall = default.labelSmall.withAppFont(),
        titleLargeEmphasized = default.titleLargeEmphasized.withAppFont(),
        titleSmallEmphasized = default.titleSmallEmphasized.withAppFont(),
        titleMediumEmphasized = default.labelTiny.withAppFont(),
        labelSmallEmphasized = default.labelSmall.withAppFont(),
        labelMediumEmphasized = default.labelMedium.withAppFont(),
        labelLargeEmphasized = default.labelLarge.withAppFont(),
        bodyLargeEmphasized = default.bodyLarge.withAppFont(),
        bodyMediumEmphasized = default.bodyMedium.withAppFont(),
        bodySmallEmphasized = default.bodySmall.withAppFont(),
        displayLargeEmphasized = default.displayLarge.withAppFont(),
        displayMediumEmphasized = default.displayMedium.withAppFont(),
        displaySmallEmphasized = default.displaySmall.withAppFont(),
        headlineLargeEmphasized = default.headlineLarge.withAppFont(),
        headlineMediumEmphasized = default.headlineMedium.withAppFont(),
        headlineSmallEmphasized = default.headlineSmall.withAppFont(),
    )
}

val Typography.labelTiny: TextStyle
    get() = labelSmall.copy(
        fontSize = (labelSmall.fontSize.value - 1.5).sp,
        lineHeight = 11.sp
    )