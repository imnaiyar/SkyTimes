package com.imnaiyar.skytimes.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.imnaiyar.skytimes.constants.RoundedCorner
import com.imnaiyar.skytimes.di.LocalAppContainer
import com.imnaiyar.skytimes.theme.DefaultThemeColor
import com.imnaiyar.skytimes.ui.BackScaffold
import com.imnaiyar.skytimes.ui.ConfirmDialogue
import com.imnaiyar.skytimes.ui.Grid
import com.imnaiyar.skytimes.ui.GridType
import com.imnaiyar.skytimes.ui.SlidingToggle
import com.materialkolor.Contrast
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePage(onNavigateBack: () -> Unit) {
    val appContainer = LocalAppContainer.current
    val themeController = appContainer.themeController
    val settingsRep = appContainer.settingsRepository
    val settings by settingsRep.settings.collectAsState()

    val (initialH, initialS, initialV) = remember(settings.themeColor) {
        colorToHsv(
            Color(
                settings.themeColor
            )
        )
    }


    var hue by remember(initialH) { mutableStateOf(initialH) }
    var sat by remember(initialS) { mutableStateOf(initialS) }
    var value by remember(initialV) { mutableStateOf(initialV) }


    var contrast by remember(settings.themeContrast) { mutableStateOf(settings.themeContrast) }


    val unsaved by themeController.hasUnsavedChanges.collectAsState()

    val preview = { h: Float, s: Float, v: Float ->
        hue = h
        sat = s
        value = v
        themeController.preview(hsvToColor(h, s, v).toArgb(), contrast)
    }

    val reset = {
        themeController.discardPreview()
        val (h, s, v) = colorToHsv(
            Color(
                settings.themeColor
            )
        )
        hue = h
        sat = s
        value = v
        contrast = settings.themeContrast
    }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        themeController.discardPreview()
    }

    BackScaffold(
        title = "Theme",
        onNavigateBack = onNavigateBack,
        actions = {
            val enabled =
                settings.themeColor != DefaultThemeColor.toInt() || settings.themeContrast != Contrast.Default
            AnimatedVisibility(visible = enabled) {
                ConfirmDialogue(
                    message = "Are you sure you want to reset theme settings to default?",
                    onConfirm = {
                        appContainer.applicationScope.launch {
                            settingsRep.setTheme(
                                DefaultThemeColor.toInt(),
                                Contrast.Default
                            )
                        }.invokeOnCompletion { themeController.discardPreview() }
                    }
                ) {
                    OutlinedButton(
                        onClick = it,
                        enabled = enabled,
                        modifier = Modifier.padding(end = 16.dp),
                        shape = RoundedCorner
                    ) { Text("Reset to default") }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = reset,
                        enabled = unsaved,
                        modifier = Modifier.weight(1f)
                    ) { Text("Reset") }

                    Button(
                        onClick = {
                            themeController.commit()
                        },
                        enabled = unsaved,
                        modifier = Modifier.weight(1f)
                    ) { Text("Apply") }
                }
            }
        }
    ) { padding ->
        Grid(
            type = GridType.GRID,
            columns = GridCells.Adaptive(500.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp),
            contentPadding = padding
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { SectionLabel("Color") }
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    SaturationValuePanel(
                        hue = hue,
                        saturation = sat,
                        value = value,
                        onChange = { s, v -> preview(hue, s, v) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    HueSlider(
                        hue = hue,
                        onHueChange = { preview(it, sat, value) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ColorInput(
                        hue,
                        sat,
                        value,
                        onColorChange = preview
                    )

                    SectionLabel("Contrast")
                    ContrastSelector(
                        current = contrast,
                        onChange = { c ->
                            contrast = c
                            preview(hue, sat, value)
                        }
                    )

                }
            }
        }
    }
}

// Sub-components
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (s: Float, v: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val hueColor = hsvToColor(hue, 1f, 1f)

    Canvas(
        modifier = modifier.pointerInput(hue) {
            awaitEachGesture {
                val down = awaitFirstDown()

                fun updateSat(offset: Offset) {
                    val s = (offset.x / size.width).coerceIn(0f, 1f)
                    val v = (1f - offset.y / size.height).coerceIn(0f, 1f)
                    onChange(s, v)
                }

                // Update immediately on tap/down
                updateSat(down.position)

                drag(down.id) { change ->
                    updateSat(change.position)
                    change.consume()
                }
            }
        }
    ) {
        // Saturation: white -> hue color (left to right)
        drawRect(
            brush = Brush.horizontalGradient(listOf(Color.White, hueColor))
        )
        // Value/brightness: transparent -> black (top to bottom)
        drawRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
        )

        val thumbCenter = Offset(
            x = saturation * size.width,
            y = (1f - value) * size.height
        )
        drawCircle(
            color = Color.White,
            radius = 10.dp.toPx(),
            center = thumbCenter,
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.4f),
            radius = 10.dp.toPx(),
            center = thumbCenter,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Composable
private fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val hueColors = remember {
        (0..360 step 30).map { hsvToColor(it.toFloat().coerceAtMost(359.999f), 1f, 1f) }
    }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()

                fun updateHue(x: Float) {
                    onHueChange(
                        (x / size.width * 360f)
                            .coerceIn(0f, 359.999f)
                    )
                }

                // Update immediately on tap/down
                updateHue(down.position.x)

                drag(down.id) { change ->
                    updateHue(change.position.x)
                    change.consume()
                }
            }
        }
    ) {
        drawRect(brush = Brush.horizontalGradient(hueColors))

        val thumbX = (hue / 360f) * size.width
        drawCircle(
            color = Color.White,
            radius = size.height / 2f,
            center = Offset(thumbX, size.height / 2f),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

@Composable
private fun ContrastSelector(
    current: Contrast,
    onChange: (Contrast) -> Unit
) {
    val options = Contrast.entries.toList()
    val index = options.indexOf(current).toFloat()

    Column {
        Slider(
            value = index,
            onValueChange = { raw ->
                val snapped = options[raw.roundToInt().coerceIn(0, options.lastIndex)]
                if (snapped != current) onChange(snapped)
            },
            valueRange = 0f..(options.size - 1).toFloat(),
            steps = options.size - 2 // number of steps BETWEEN endpoints
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            options.forEach { opt ->
                Text(
                    text = opt.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (opt == current)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private enum class ColorInputMode { HEX, RGB, HSV }

@Composable
private fun ColorInput(
    hue: Float,
    saturation: Float,
    value: Float,
    onColorChange: (h: Float, s: Float, v: Float) -> Unit,
) {
    var mode by remember { mutableStateOf(ColorInputMode.HEX) }
    val currentColor = remember(hue, saturation, value) { hsvToColor(hue, saturation, value) }

    var hexText by remember { mutableStateOf(colorToHex(currentColor)) }
    var rText by remember { mutableStateOf((currentColor.red * 255).roundToInt().toString()) }
    var gText by remember { mutableStateOf((currentColor.green * 255).roundToInt().toString()) }
    var bText by remember { mutableStateOf((currentColor.blue * 255).roundToInt().toString()) }
    var hText by remember { mutableStateOf(hue.roundToInt().toString()) }
    var sText by remember { mutableStateOf((saturation * 100).roundToInt().toString()) }
    var vText by remember { mutableStateOf((value * 100).roundToInt().toString()) }

    LaunchedEffect(hue, saturation, value) {
        if (colorToHex(currentColor) != hexText) hexText = colorToHex(currentColor)
        if ((currentColor.red * 255).roundToInt().toString() != rText) rText =
            (currentColor.red * 255).roundToInt().toString()
        if ((currentColor.green * 255).roundToInt().toString() != gText) gText =
            (currentColor.green * 255).roundToInt().toString()
        if ((currentColor.blue * 255).roundToInt().toString() != bText) bText =
            (currentColor.blue * 255).roundToInt().toString()
        if (hue.roundToInt().toString() != hText) hText = hue.roundToInt().toString()
        if ((saturation * 100).roundToInt().toString() != sText) sText =
            (saturation * 100).roundToInt().toString()
        if ((value * 100).roundToInt().toString() != vText) vText =
            (value * 100).roundToInt().toString()
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(currentColor)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            Spacer(Modifier.width(10.dp))
            SlidingToggle(
                ColorInputMode.entries.map { it.name },
                selectedIndex = mode.ordinal,
                itemHeight = 30.dp,
                itemWidth = 50.dp,
                roundedCornerIndicator = RoundedCornerShape(8.dp),
                useHaptics = true,
                onSelectedChange = { mode = ColorInputMode.entries[it] }
            )
        }

        when (mode) {
            ColorInputMode.HEX -> OutlinedTextField(
                value = hexText,
                onValueChange = { input ->
                    val clean = input.trimStart('#').take(6)
                    hexText = clean
                    hexToRgb(clean)?.let { (r, g, b) ->
                        val (h, s, v) = colorToHsv(Color(r / 255f, g / 255f, b / 255f))
                        onColorChange(h, s, v)
                    }
                },
                label = { Text("Hex") },
                leadingIcon = { Text("#") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ColorInputMode.RGB -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("R", rText, 0..255, Modifier.weight(1f)) { text, n ->
                    rText = text
                    n?.let {
                        val (h, s, v) = colorToHsv(
                            Color(
                                it / 255f,
                                gText.toIntOrNull()?.div(255f) ?: 0f,
                                bText.toIntOrNull()?.div(255f) ?: 0f
                            )
                        )
                        onColorChange(h, s, v)
                    }
                }
                NumberField("G", gText, 0..255, Modifier.weight(1f)) { text, n ->
                    gText = text
                    n?.let {
                        val (h, s, v) = colorToHsv(
                            Color(
                                rText.toIntOrNull()?.div(255f) ?: 0f,
                                it / 255f,
                                bText.toIntOrNull()?.div(255f) ?: 0f
                            )
                        )
                        onColorChange(h, s, v)
                    }
                }
                NumberField("B", bText, 0..255, Modifier.weight(1f)) { text, n ->
                    bText = text
                    n?.let {
                        val (h, s, v) = colorToHsv(
                            Color(
                                rText.toIntOrNull()?.div(255f) ?: 0f,
                                gText.toIntOrNull()?.div(255f) ?: 0f,
                                it / 255f
                            )
                        )
                        onColorChange(h, s, v)
                    }
                }
            }

            ColorInputMode.HSV -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NumberField("H°", hText, 0..360, Modifier.weight(1f)) { text, n ->
                    hText = text
                    n?.let { onColorChange(it.toFloat().coerceIn(0f, 359.999f), saturation, value) }
                }
                NumberField("S%", sText, 0..100, Modifier.weight(1f)) { text, n ->
                    sText = text
                    n?.let { onColorChange(hue, it / 100f, value) }
                }
                NumberField("V%", vText, 0..100, Modifier.weight(1f)) { text, n ->
                    vText = text
                    n?.let { onColorChange(hue, saturation, it / 100f) }
                }
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    text: String,
    range: IntRange,
    modifier: Modifier = Modifier,
    onChange: (text: String, parsed: Int?) -> Unit
) {
    OutlinedTextField(
        value = text,
        onValueChange = { input ->
            val digitsOnly = input.filter { it.isDigit() }
            val parsed = digitsOnly.toIntOrNull()?.takeIf { it in range }
            onChange(digitsOnly, parsed)
        },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier
    )
}


// helpers
private fun hsvToColor(h: Float, s: Float, v: Float, alpha: Float = 1f): Color {
    val c = v * s
    val x = c * (1 - kotlin.math.abs((h / 60f) % 2 - 1))
    val m = v - c
    val (r1, g1, b1) = when {
        h < 60 -> Triple(c, x, 0f)
        h < 120 -> Triple(x, c, 0f)
        h < 180 -> Triple(0f, c, x)
        h < 240 -> Triple(0f, x, c)
        h < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    return Color(r1 + m, g1 + m, b1 + m, alpha)
}

private fun colorToHsv(color: Color): Triple<Float, Float, Float> {
    val r = color.red
    val g = color.green
    val b = color.blue
    val maxC = max(r, max(g, b))
    val minC = min(r, min(g, b))
    val delta = maxC - minC

    val h = when {
        delta == 0f -> 0f
        maxC == r -> 60f * (((g - b) / delta) % 6f)
        maxC == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }.let { if (it < 0) it + 360f else it }

    val s = if (maxC == 0f) 0f else delta / maxC
    val v = maxC
    return Triple(h, s, v)
}

private fun hexToRgb(hex: String): Triple<Int, Int, Int>? {
    if (hex.length != 6 || hex.any { it !in "0123456789abcdefABCDEF" }) return null
    val r = hex.substring(0, 2).toInt(16)
    val g = hex.substring(2, 4).toInt(16)
    val b = hex.substring(4, 6).toInt(16)
    return Triple(r, g, b)
}

private fun colorToHex(color: Color): String {
    fun channel(f: Float) = (f * 255).roundToInt().coerceIn(0, 255)
        .toString(16).padStart(2, '0').uppercase()
    return "${channel(color.red)}${channel(color.green)}${channel(color.blue)}"
}
