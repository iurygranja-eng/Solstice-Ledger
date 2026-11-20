package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SolsticeColorScheme = darkColorScheme(
    primary = LunarPrimary,
    onPrimary = OnPrimaryDark,
    primaryContainer = LunarCyan,
    onPrimaryContainer = ColorPaletteHelper.OnPrimaryContainer,
    secondary = SolarSecondary,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SolarMagenta,
    onSecondaryContainer = OnSecondaryDark,
    tertiary = TertiaryIndigo,
    onTertiary = CosmicIndigo,
    background = MidnightBase,
    onBackground = OnSurfaceText,
    surface = AstralSurface,
    onSurface = OnSurfaceText,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceMuted,
    outline = OutlineMuted,
    outlineVariant = OutlineVariant,
    error = CrimsonRisk,
    onError = MidnightVoid
)

private object ColorPaletteHelper {
    val OnPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF002738)
}

val SolsticeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(10.dp),
    extraLarge = RoundedCornerShape(14.dp)
)

@Composable
fun SolsticeLedgerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SolsticeColorScheme,
        typography = SolsticeTypography,
        shapes = SolsticeShapes,
        content = content
    )
}
