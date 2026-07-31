package com.imnaiyar.skytimes.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class UnderlineStyle { Solid, Dashed, Dotted }

/**
 * Since dashed underline decoration is not available
 */
@Composable
fun DecoratedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
    style: TextStyle = LocalTextStyle.current,
    textDecoration: TextDecoration = TextDecoration.None,
    underlineStyle: UnderlineStyle = UnderlineStyle.Dotted,
    underlineColor: Color = color,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 2.dp,
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val density = LocalDensity.current

    Text(
        text = text,
        style = style,
        color = color,
        textDecoration = textDecoration,
        onTextLayout = { layoutResult = it },
        modifier = modifier.drawWithContent {
            drawContent()

            val layout = layoutResult ?: return@drawWithContent
            val strokeWidthPx = with(density) { strokeWidth.toPx() }

            val pathEffect = when (underlineStyle) {
                UnderlineStyle.Solid -> null
                UnderlineStyle.Dashed -> PathEffect.dashPathEffect(
                    floatArrayOf(
                        with(density) { dashLength.toPx() },
                        with(density) { gapLength.toPx() }
                    ),
                    phase = 0f
                )

                UnderlineStyle.Dotted -> PathEffect.dashPathEffect(
                    floatArrayOf(
                        0.1f, // near-zero dash -> renders as a dot with round cap
                        with(density) { gapLength.toPx() }
                    ),
                    phase = 0f
                )
            }

            for (lineIndex in 0 until layout.lineCount) {
                val lineBottom = layout.getLineBottom(lineIndex)
                val lineLeft = layout.getLineLeft(lineIndex)
                val lineRight = layout.getLineRight(lineIndex)
                val y = lineBottom /*+ gapPx*/

                drawLine(
                    color = underlineColor,
                    start = Offset(lineLeft, y),
                    end = Offset(lineRight, y),
                    strokeWidth = strokeWidthPx,
                    pathEffect = pathEffect,
                    cap = if (underlineStyle == UnderlineStyle.Dotted) StrokeCap.Round else StrokeCap.Butt
                )
            }
        }
    )
}


@Composable
@Preview
fun TextPrev() {
    DecoratedText("This is a text!")
}