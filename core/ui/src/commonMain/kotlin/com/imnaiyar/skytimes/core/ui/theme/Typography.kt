package com.imnaiyar.skytimes.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.imnaiyar.skytimes.core.ui.generated.resources.Res
import com.imnaiyar.skytimes.core.ui.generated.resources.clash_bold
import com.imnaiyar.skytimes.core.ui.generated.resources.clash_regular
import org.jetbrains.compose.resources.Font

private val default = Typography()

@Composable
fun clashFontFamily(): FontFamily = FontFamily(
    Font(Res.font.clash_regular, FontWeight.Normal),
    Font(Res.font.clash_bold, FontWeight.Bold)
)

@Composable
fun appTypography(): Typography {

    val appFont = clashFontFamily()

    fun TextStyle.withAppFont(sizeReduction: TextUnit = 2.sp) = copy(
        fontFamily = appFont,
        fontSize = (fontSize.value - sizeReduction.value).sp
    )
    return default.copy(
        headlineLarge = default.headlineLarge.withAppFont(),
        headlineMedium = default.headlineMedium.withAppFont(),
        headlineSmall = default.headlineSmall.withAppFont(),
        titleLarge = default.titleLarge.withAppFont(),
        titleMedium = default.titleMedium.withAppFont(),
        titleSmall = default.titleSmall.withAppFont(),
        titleLargeEmphasized = default.titleLargeEmphasized.withAppFont(),
        titleSmallEmphasized = default.titleSmallEmphasized.withAppFont(),
        titleMediumEmphasized = default.titleMediumEmphasized.withAppFont(),
        headlineLargeEmphasized = default.headlineLargeEmphasized.withAppFont(),
        headlineMediumEmphasized = default.headlineMediumEmphasized.withAppFont(),
        headlineSmallEmphasized = default.headlineSmallEmphasized.withAppFont(),
    )
}

val Typography.labelTiny: TextStyle
    get() = labelSmall.copy(
        fontSize = (labelSmall.fontSize.value - 1.5).sp,
        lineHeight = 11.sp
    )

fun Typography.titleTiny(sizeReduction: TextUnit = 2.sp): TextStyle = titleSmall.copy(
    fontSize = (titleSmall.fontSize.value - sizeReduction.value).sp
)
