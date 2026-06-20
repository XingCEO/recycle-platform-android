package com.recycle.vendor.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val VendorColors = lightColorScheme(
    primary = Emerald600,
    onPrimary = Color.White,
    primaryContainer = MintContainer,
    onPrimaryContainer = Emerald700,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = TealContainer,
    onSecondaryContainer = Teal700,
    tertiary = Amber,
    onTertiary = Color.White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = AmberDark,
    background = Color(0xFFF2F8F5),
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Slate,
    surfaceTint = Emerald,
    error = Danger,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),
    outline = Color(0xFFCBD5E1),
    outlineVariant = MistDeep,
)

private val VendorShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Brand gradients. */
val HeroGradient = Brush.verticalGradient(listOf(Emerald700, Emerald600, Teal))
val ButtonGradient = Brush.linearGradient(listOf(Emerald600, Teal))
val ScreenGradient = Brush.verticalGradient(listOf(Color(0xFFEAF7F0), Color(0xFFF6FAF8)))

@Composable
fun VendorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VendorColors,
        shapes = VendorShapes,
        content = content,
    )
}
