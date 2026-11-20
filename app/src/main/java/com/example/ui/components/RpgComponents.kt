package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.domain.AvatarAccessoryTier
import com.example.domain.AvatarCostumeTier
import com.example.domain.AvatarWisdomStage
import com.example.domain.LanguageDictionary
import com.example.domain.TermKey
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.AstralSurface
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.BevelHighlightWhite
import com.example.ui.theme.CardGradientBottom
import com.example.ui.theme.CardGradientMid
import com.example.ui.theme.CardGradientTop
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CrimsonRisk
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.FrameInnerShadow
import com.example.ui.theme.FrameMetallicLight
import com.example.ui.theme.FrameOuterShadow
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.InsetWellBottom
import com.example.ui.theme.InsetWellTop
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarDeep
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightBase
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import com.example.ui.theme.PsxDangerBottom
import com.example.ui.theme.PsxDangerMid
import com.example.ui.theme.PsxDangerTop
import com.example.ui.theme.PsxWindowBottom
import com.example.ui.theme.PsxWindowMid
import com.example.ui.theme.PsxWindowTop
import com.example.ui.theme.RoseHazard
import com.example.ui.theme.SolarDeep
import com.example.ui.theme.SolarMagenta
import com.example.ui.theme.SolarSecondary

@Composable
fun TwilightBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MidnightBase,
                        MidnightVoid,
                        CosmicIndigo.copy(alpha = 0.88f),
                        AstralSurface,
                        MidnightBase
                    )
                )
            )
            .drawWithCache {
                val w = size.width
                val h = size.height

                val lunarBloom = Brush.radialGradient(
                    colors = listOf(LunarCyan.copy(alpha = 0.16f), LunarDeep.copy(alpha = 0.05f), Color.Transparent),
                    center = Offset(w * 0.16f, h * 0.12f),
                    radius = (w * 0.75f).coerceAtLeast(1f)
                )
                val solarBloom = Brush.radialGradient(
                    colors = listOf(SolarMagenta.copy(alpha = 0.13f), SolarDeep.copy(alpha = 0.04f), Color.Transparent),
                    center = Offset(w * 0.88f, h * 0.36f),
                    radius = (w * 0.65f).coerceAtLeast(1f)
                )
                val indigoBloom = Brush.radialGradient(
                    colors = listOf(CosmicIndigo.copy(alpha = 0.25f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.78f),
                    radius = (w * 0.8f).coerceAtLeast(1f)
                )

                val latticePath = Path().apply {
                    val step = 64f
                    var x = -h
                    while (x < w + h) {
                        moveTo(x, 0f)
                        lineTo(x + h * 0.45f, h)
                        x += step
                    }
                }

                val scanlinePath = Path().apply {
                    var sy = 0f
                    while (sy < h) {
                        moveTo(0f, sy)
                        lineTo(w, sy)
                        sy += 12f
                    }
                }

                val nodes = listOf(
                    Offset(w * 0.12f, h * 0.08f),
                    Offset(w * 0.28f, h * 0.14f),
                    Offset(w * 0.45f, h * 0.10f),
                    Offset(w * 0.76f, h * 0.06f),
                    Offset(w * 0.89f, h * 0.18f)
                )

                val stars = listOf(
                    Triple(0.12f, 0.08f, 7f),
                    Triple(0.28f, 0.14f, 5f),
                    Triple(0.76f, 0.06f, 8f),
                    Triple(0.89f, 0.18f, 5.5f),
                    Triple(0.45f, 0.10f, 5f),
                    Triple(0.18f, 0.36f, 5.5f),
                    Triple(0.84f, 0.44f, 6.5f),
                    Triple(0.08f, 0.62f, 5f),
                    Triple(0.92f, 0.72f, 6f),
                    Triple(0.54f, 0.85f, 5f)
                )

                val orbitCenter = Offset(w * 0.5f, h * 0.26f)
                val orbitRadii = listOf(w * 0.38f, w * 0.62f, w * 0.88f)

                onDrawBehind {
                    drawCircle(brush = lunarBloom, center = Offset(w * 0.16f, h * 0.12f), radius = w * 0.75f)
                    drawCircle(brush = solarBloom, center = Offset(w * 0.88f, h * 0.36f), radius = w * 0.65f)
                    drawCircle(brush = indigoBloom, center = Offset(w * 0.5f, h * 0.78f), radius = w * 0.8f)

                    orbitRadii.forEachIndexed { idx, radius ->
                        drawCircle(
                            color = (if (idx % 2 == 0) LunarCyan else FrameMetallicLight).copy(alpha = 0.045f),
                            radius = radius,
                            center = orbitCenter,
                            style = Stroke(width = 1.4f)
                        )
                    }

                    drawPath(latticePath, color = FrameMetallicLight.copy(alpha = 0.025f), style = Stroke(width = 1f))

                    for (i in 0 until nodes.size - 1) {
                        if (i != 2) {
                            drawLine(
                                color = FrameMetallicLight.copy(alpha = 0.12f),
                                start = nodes[i],
                                end = nodes[i + 1],
                                strokeWidth = 1.4f
                            )
                        }
                    }

                    stars.forEachIndexed { idx, (px, py, r) ->
                        val cx = w * px
                        val cy = h * py
                        val starColor = if (idx % 2 == 0) LunarCyan else SolarMagenta
                        drawCircle(
                            color = starColor.copy(alpha = 0.20f),
                            radius = r * 2.0f,
                            center = Offset(cx, cy)
                        )
                        drawLine(starColor.copy(alpha = 0.65f), Offset(cx - r, cy), Offset(cx + r, cy), strokeWidth = 1.8f)
                        drawLine(starColor.copy(alpha = 0.65f), Offset(cx, cy - r), Offset(cx, cy + r), strokeWidth = 1.8f)
                        drawCircle(color = Color.White.copy(alpha = 0.88f), radius = 2f, center = Offset(cx, cy))
                    }

                    drawPath(scanlinePath, color = FrameOuterShadow.copy(alpha = 0.12f), style = Stroke(width = 1.4f))
                }
            }
    ) {
        content()
    }
}

@Composable
fun RpgBevelCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BevelBorderIndigo,
    containerColor: Color = AstralSurface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    val topTone = if (containerColor == AstralSurface) CardGradientTop else containerColor
    val midTone = if (containerColor == AstralSurface) CardGradientMid else Color(
        red = (containerColor.red * 0.84f).coerceIn(0f, 1f),
        green = (containerColor.green * 0.84f).coerceIn(0f, 1f),
        blue = (containerColor.blue * 0.88f).coerceIn(0f, 1f),
        alpha = containerColor.alpha
    )
    val bottomTone = if (containerColor == AstralSurface) CardGradientBottom else Color(
        red = (containerColor.red * 0.56f).coerceIn(0f, 1f),
        green = (containerColor.green * 0.56f).coerceIn(0f, 1f),
        blue = (containerColor.blue * 0.64f).coerceIn(0f, 1f),
        alpha = containerColor.alpha
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = FrameOuterShadow,
                spotColor = borderColor.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(topTone, midTone, bottomTone)
                )
            )
            .border(BorderStroke(2.dp, FrameOuterShadow), shape)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val inset = 3.5f

                val weavePath = Path().apply {
                    val texStep = 28f
                    var tx = 0f
                    while (tx < w) {
                        moveTo(tx, 0f)
                        lineTo(tx, h)
                        tx += texStep
                    }
                }
                val sheenHeight = (h * 0.45f).coerceAtLeast(48f)
                val sheenBrush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.09f), LunarCyan.copy(alpha = 0.03f), Color.Transparent),
                    start = Offset(0f, 0f),
                    end = Offset(w * 0.65f, sheenHeight)
                )
                val rimBrush = Brush.linearGradient(
                    colors = listOf(FrameMetallicLight.copy(alpha = 0.85f), borderColor, FrameOuterShadow.copy(alpha = 0.9f))
                )
                val topRidgeBrush = Brush.horizontalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.55f), BevelHighlightWhite, Color.Transparent)
                )
                val leftRidgeBrush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.42f), BevelHighlightWhite.copy(alpha = 0.14f), Color.Transparent)
                )
                val cx = w - 9f
                val cy = 9f
                val d = 3.2f
                val diamond = Path().apply {
                    moveTo(cx, cy - d)
                    lineTo(cx + d, cy)
                    lineTo(cx, cy + d)
                    lineTo(cx - d, cy)
                    close()
                }
                val bracketLen = 11f
                val bInset = 4.5f
                val cornerSilver = FrameMetallicLight.copy(alpha = 0.65f)
                val bracketsPath = Path().apply {
                    moveTo(bInset, bInset + bracketLen); lineTo(bInset, bInset); lineTo(bInset + bracketLen, bInset)
                    moveTo(w - bInset - bracketLen, bInset); lineTo(w - bInset, bInset); lineTo(w - bInset, bInset + bracketLen)
                    moveTo(bInset, h - bInset - bracketLen); lineTo(bInset, h - bInset); lineTo(bInset + bracketLen, h - bInset)
                    moveTo(w - bInset - bracketLen, h - bInset); lineTo(w - bInset, h - bInset); lineTo(w - bInset, h - bInset - bracketLen)
                }

                onDrawBehind {
                    drawPath(weavePath, color = FrameMetallicLight.copy(alpha = 0.025f), style = Stroke(width = 1f))
                    drawRect(brush = sheenBrush, size = Size(w, sheenHeight))
                    drawRoundRect(
                        brush = rimBrush,
                        topLeft = Offset(inset, inset),
                        size = Size((w - inset * 2f).coerceAtLeast(1f), (h - inset * 2f).coerceAtLeast(1f)),
                        cornerRadius = CornerRadius(18f, 18f),
                        style = Stroke(width = 2.6f)
                    )
                    drawLine(brush = topRidgeBrush, start = Offset(10f, inset + 1.5f), end = Offset(w - 10f, inset + 1.5f), strokeWidth = 2.2f)
                    drawLine(brush = leftRidgeBrush, start = Offset(inset + 1.5f, 10f), end = Offset(inset + 1.5f, h - 10f), strokeWidth = 2f)
                    drawLine(FrameOuterShadow.copy(alpha = 0.92f), Offset(10f, h - inset - 1.5f), Offset(w - 10f, h - inset - 1.5f), strokeWidth = 2.6f)
                    drawLine(FrameOuterShadow.copy(alpha = 0.88f), Offset(w - inset - 1.5f, 10f), Offset(w - inset - 1.5f, h - 10f), strokeWidth = 2.4f)
                    val groove = 6.5f
                    drawRoundRect(
                        color = FrameMetallicLight.copy(alpha = 0.18f),
                        topLeft = Offset(groove, groove),
                        size = Size((w - groove * 2f).coerceAtLeast(1f), (h - groove * 2f).coerceAtLeast(1f)),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = 1f)
                    )
                    drawPath(bracketsPath, color = cornerSilver, style = Stroke(width = 2f))
                    drawPath(diamond, color = borderColor.copy(alpha = 0.85f))
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * 32-bit recessed bevel well for nested stat displays, transaction rows, and indicators.
 * Uses inverted directional bevel (dark shadow top/left, metallic catchlight bottom/right).
 */
@Composable
fun RpgInsetWell(
    modifier: Modifier = Modifier,
    borderColor: Color = BevelBorderIndigo,
    contentPadding: PaddingValues = PaddingValues(11.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(InsetWellTop, InsetWellBottom))
            )
            .border(1.5.dp, FrameOuterShadow, shape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            // Top & Left recessed inner shadow
            drawLine(
                color = FrameOuterShadow.copy(alpha = 0.95f),
                start = Offset(4f, 2f),
                end = Offset(w - 4f, 2f),
                strokeWidth = 2.6f
            )
            drawLine(
                color = FrameOuterShadow.copy(alpha = 0.8f),
                start = Offset(2f, 4f),
                end = Offset(2f, h - 4f),
                strokeWidth = 2.2f
            )
            // Bottom & Right subtle metallic rim catchlight
            drawLine(
                color = borderColor.copy(alpha = 0.55f),
                start = Offset(6f, h - 2f),
                end = Offset(w - 6f, h - 2f),
                strokeWidth = 1.6f
            )
            drawLine(
                color = borderColor.copy(alpha = 0.35f),
                start = Offset(w - 2f, 6f),
                end = Offset(w - 2f, h - 6f),
                strokeWidth = 1.4f
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * High-resolution 32-bit beveled icon medallion socket.
 */
@Composable
fun RpgIconMedallion(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp,
    contentDescription: String? = null
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(size)
            .shadow(4.dp, shape, spotColor = tint.copy(alpha = 0.45f))
            .clip(shape)
            .background(
                Brush.radialGradient(
                    colors = listOf(tint.copy(alpha = 0.30f), CosmicIndigo.copy(alpha = 0.65f), FrameOuterShadow)
                )
            )
            .border(1.5.dp, tint.copy(alpha = 0.72f), shape)
            .padding(1.5.dp)
            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun LunarCoreButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val shape = RoundedCornerShape(8.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MidnightVoid,
            disabledContainerColor = SurfaceContainerHighCompat,
            disabledContentColor = OnSurfaceMuted
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .shadow(if (enabled) 8.dp else 0.dp, shape, spotColor = LunarCyan.copy(alpha = 0.55f))
            .border(2.dp, FrameOuterShadow, shape)
            .background(
                brush = if (enabled) {
                    Brush.verticalGradient(listOf(Color(0xFFC8ECFF), LunarPrimary, LunarCyan, LunarDeep))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF222A3D), Color(0xFF171F33)))
                },
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.2.dp,
                    if (enabled) Color.White.copy(alpha = 0.55f) else BevelBorderIndigo,
                    shape
                )
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (enabled) {
                    Canvas(modifier = Modifier.size(11.dp)) {
                        val half = size.minDimension / 2f
                        val p = Path().apply {
                            moveTo(half, 0f)
                            lineTo(size.width, half)
                            lineTo(half, size.height)
                            lineTo(0f, half)
                            close()
                        }
                        drawPath(p, color = MidnightVoid.copy(alpha = 0.8f))
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private val SurfaceContainerHighCompat = Color(0xFF222A3D)

@Composable
fun SolarCompanionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val shape = RoundedCornerShape(8.dp)
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = SurfaceContainerHighCompat,
            disabledContentColor = OnSurfaceMuted
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .shadow(if (enabled) 8.dp else 0.dp, shape, spotColor = SolarMagenta.copy(alpha = 0.55f))
            .border(2.dp, FrameOuterShadow, shape)
            .background(
                brush = if (enabled) {
                    Brush.verticalGradient(listOf(Color(0xFFFFD1E7), SolarSecondary, SolarMagenta, SolarDeep))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF222A3D), Color(0xFF171F33)))
                },
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.2.dp, Color.White.copy(alpha = 0.50f), shape)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (enabled) {
                    Canvas(modifier = Modifier.size(11.dp)) {
                        val half = size.minDimension / 2f
                        val p = Path().apply {
                            moveTo(half, 0f)
                            lineTo(size.width, half)
                            lineTo(half, size.height)
                            lineTo(0f, half)
                            close()
                        }
                        drawPath(p, color = Color.White.copy(alpha = 0.92f))
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RpgProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    dayMarkerFraction: Float? = null,
    barColor: Color = LunarCyan,
    secondaryColor: Color = SolarMagenta,
    height: Dp = 16.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(5.dp))
            .background(FrameOuterShadow)
            .border(1.5.dp, FrameMetallicLight.copy(alpha = 0.55f), RoundedCornerShape(5.dp))
            .padding(2.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Recessed metallic ATB trough
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(FrameOuterShadow, FrameInnerShadow, MidnightVoid)
                ),
                size = size
            )

            val fillWidth = size.width * clamped
            if (fillWidth > 0f) {
                // Multi-stop luminous FF Limit/ATB energy fill
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(barColor, secondaryColor, Color.White.copy(alpha = 0.85f)),
                        startX = 0f,
                        endX = (fillWidth * 1.06f).coerceAtLeast(4f)
                    ),
                    size = Size(fillWidth, size.height)
                )
                // Cylindrical upper glass specular band
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.50f),
                            Color.White.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    ),
                    size = Size(fillWidth, size.height * 0.55f)
                )
                // Bottom recessed edge along the bar fill
                drawLine(
                    color = FrameOuterShadow.copy(alpha = 0.45f),
                    start = Offset(0f, size.height - 1f),
                    end = Offset(fillWidth, size.height - 1f),
                    strokeWidth = 2f
                )
                // Bright leading-edge energy flare
                drawLine(
                    color = Color.White.copy(alpha = 0.90f),
                    start = Offset(fillWidth, 0f),
                    end = Offset(fillWidth, size.height),
                    strokeWidth = 2.8f
                )
            }

            // Etched metallic gauge dividers
            val segments = 5
            for (i in 1 until segments) {
                val tx = size.width * (i.toFloat() / segments)
                drawLine(
                    color = FrameOuterShadow.copy(alpha = 0.82f),
                    start = Offset(tx, 0f),
                    end = Offset(tx, size.height),
                    strokeWidth = 1.8f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.12f),
                    start = Offset(tx + 1.5f, 0f),
                    end = Offset(tx + 1.5f, size.height),
                    strokeWidth = 1f
                )
            }

            // Faceted crystal day-of-month marker
            dayMarkerFraction?.let { frac ->
                val mx = (size.width * frac.coerceIn(0.03f, 0.97f))
                drawLine(
                    color = Color.White,
                    start = Offset(mx, 0f),
                    end = Offset(mx, size.height),
                    strokeWidth = 2.8f
                )
                val pin = Path().apply {
                    moveTo(mx, 0f)
                    lineTo(mx + 5f, size.height * 0.28f)
                    lineTo(mx, size.height * 0.56f)
                    lineTo(mx - 5f, size.height * 0.28f)
                    close()
                }
                drawPath(pin, color = Color.White)
            }
        }
    }
}

/**
 * 32-bit Final Fantasy style character portrait bust inside a double-beveled FF menu frame
 * with Materia crystal slot indicators and elemental aura lighting.
 */
@Composable
fun PixelHeroAvatar(
    hairIndex: Int,
    outfitIndex: Int,
    auraKey: String,
    size: Dp = 56.dp,
    wisdomLevel: Int = 1,
    netWorthCents: Long = 0L,
    costumeOverride: AvatarCostumeTier? = null,
    accessoryOverride: AvatarAccessoryTier? = null,
    wisdomStageOverride: AvatarWisdomStage? = null,
    modifier: Modifier = Modifier
) {
    val wisdomStage = wisdomStageOverride ?: AvatarWisdomStage.forLevel(wisdomLevel)
    val costumeTier = costumeOverride ?: (
        AvatarCostumeTier.entries.lastOrNull { netWorthCents >= it.requiredNetWorthCents }
            ?: AvatarCostumeTier.TRAVELER_TUNIC
        )
    val accessoryTier = accessoryOverride ?: (
        AvatarAccessoryTier.entries.lastOrNull { netWorthCents >= it.requiredNetWorthCents }
            ?: AvatarAccessoryTier.COPPER_PIN
        )

    val auraColor = when (auraKey.uppercase()) {
        "MAGENTA" -> SolarMagenta
        "EMERALD" -> EmeraldSafe
        "AMBER" -> AmberWarn
        else -> LunarCyan
    }
    val materiaColor = when (outfitIndex % 4) {
        0 -> LunarCyan
        1 -> SolarMagenta
        2 -> EmeraldSafe
        else -> AmberWarn
    }
    val stageAccent = when (wisdomStage) {
        AvatarWisdomStage.APRENDIZ -> auraColor
        AvatarWisdomStage.ESCRIBA -> LunarCyan
        AvatarWisdomStage.GUARDIAO -> LunarPrimary
        AvatarWisdomStage.ESTRATEGA -> SolarMagenta
        AvatarWisdomStage.ARQUIMAGO -> GoldBadge
    }
    val portraitRes = if (hairIndex % 2 == 0) {
        R.drawable.img_ff_avatar_lunar_1794969600168
    } else {
        R.drawable.img_ff_avatar_solar_1794969600624
    }
    val tintFilter = when (hairIndex % 4) {
        2 -> ColorFilter.tint(AmberWarn.copy(alpha = 0.16f), BlendMode.SrcAtop)
        3 -> ColorFilter.tint(EmeraldSafe.copy(alpha = 0.14f), BlendMode.SrcAtop)
        else -> null
    }

    val shape = RoundedCornerShape(10.dp)
    val frameBorderWidth = if (wisdomStage.minLevel >= 4) 2.5.dp else 2.dp
    val borderBrush = when {
        wisdomStage == AvatarWisdomStage.ARQUIMAGO -> Brush.linearGradient(
            listOf(GoldBadge, Color.White, LunarCyan, SolarMagenta, GoldBadge)
        )
        wisdomStage.minLevel >= 3 -> Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.85f), stageAccent, auraColor, CosmicIndigo)
        )
        else -> Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.72f), auraColor, CosmicIndigo, FrameOuterShadow)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(if (wisdomStage.minLevel >= 3) 12.dp else 8.dp, shape, spotColor = stageAccent.copy(alpha = 0.7f))
            .clip(shape)
            .background(FrameOuterShadow)
            .border(frameBorderWidth, borderBrush, shape)
            .padding(2.5.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                if (wisdomStage.minLevel >= 4) GoldBadge.copy(alpha = 0.65f) else FrameMetallicLight.copy(alpha = 0.55f),
                RoundedCornerShape(8.dp)
            )
    ) {
        SampledDrawableImage(
            resId = portraitRes,
            contentDescription = "Retrato do Herói estilo Final Fantasy",
            maxDimPx = 256,
            contentScale = ContentScale.Crop,
            colorFilter = tintFilter,
            modifier = Modifier.fillMaxSize()
        )

        // Elemental aura + Wisdom stage sigils + Costume pauldrons + Accessory crown/orb
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // 1. Ambient Sabedoria Aura
            val auraAlpha = 0.18f + (wisdomStage.minLevel * 0.04f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, stageAccent.copy(alpha = auraAlpha)),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = this.size.minDimension * 0.68f
                )
            )

            if (wisdomStage.minLevel >= 4) {
                drawCircle(
                    color = stageAccent.copy(alpha = 0.28f),
                    radius = this.size.minDimension * 0.43f,
                    center = Offset(w * 0.5f, h * 0.46f),
                    style = Stroke(width = 1.6f)
                )
            }

            // 2. Evolved Costume Pauldrons / Cape Collar along lower bust
            if (costumeTier != AvatarCostumeTier.TRAVELER_TUNIC) {
                val primaryArmor = when (costumeTier) {
                    AvatarCostumeTier.TRAVELER_TUNIC -> CosmicIndigo
                    AvatarCostumeTier.LUNAR_MANTLE -> Color(0xFF475569)
                    AvatarCostumeTier.MITHRIL_BRIGANDINE -> Color(0xFF64748B)
                    AvatarCostumeTier.SOLSTICE_COAT -> SolarMagenta
                    AvatarCostumeTier.CELESTIAL_ARMOR -> GoldBadge
                }
                val trimArmor = when (costumeTier) {
                    AvatarCostumeTier.LUNAR_MANTLE -> LunarCyan
                    AvatarCostumeTier.MITHRIL_BRIGANDINE -> LunarPrimary
                    AvatarCostumeTier.SOLSTICE_COAT -> GoldBadge
                    AvatarCostumeTier.CELESTIAL_ARMOR -> Color.White
                    else -> BevelBorderIndigo
                }

                // Left shoulder pauldron
                val leftShoulder = Path().apply {
                    moveTo(0f, h)
                    lineTo(0f, h * 0.72f)
                    lineTo(w * 0.28f, h * 0.80f)
                    lineTo(w * 0.36f, h)
                    close()
                }
                // Right shoulder pauldron
                val rightShoulder = Path().apply {
                    moveTo(w, h)
                    lineTo(w, h * 0.72f)
                    lineTo(w * 0.72f, h * 0.80f)
                    lineTo(w * 0.64f, h)
                    close()
                }
                drawPath(leftShoulder, color = primaryArmor.copy(alpha = 0.90f))
                drawPath(rightShoulder, color = primaryArmor.copy(alpha = 0.90f))
                drawPath(leftShoulder, color = trimArmor.copy(alpha = 0.92f), style = Stroke(width = 2.0f))
                drawPath(rightShoulder, color = trimArmor.copy(alpha = 0.92f), style = Stroke(width = 2.0f))

                // Mithril / Celestial shoulder wing flare
                if (costumeTier.ordinal >= AvatarCostumeTier.MITHRIL_BRIGANDINE.ordinal) {
                    drawLine(
                        color = trimArmor,
                        start = Offset(w * 0.04f, h * 0.78f),
                        end = Offset(w * 0.24f, h * 0.86f),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = trimArmor,
                        start = Offset(w * 0.96f, h * 0.78f),
                        end = Offset(w * 0.76f, h * 0.86f),
                        strokeWidth = 2.2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Accessory Renderings
            when (accessoryTier) {
                AvatarAccessoryTier.COPPER_PIN -> Unit
                AvatarAccessoryTier.CRYSTAL_AMULET -> {
                    val cx = w * 0.5f
                    val cy = h * 0.88f
                    val r = w * 0.065f
                    val gem = Path().apply {
                        moveTo(cx, cy - r)
                        lineTo(cx + r * 0.85f, cy)
                        lineTo(cx, cy + r)
                        lineTo(cx - r * 0.85f, cy)
                        close()
                    }
                    drawPath(gem, color = LunarCyan)
                    drawPath(gem, color = Color.White, style = Stroke(width = 1.4f))
                }
                AvatarAccessoryTier.CLARITY_DIADEM -> {
                    val cx = w * 0.5f
                    val cy = h * 0.14f
                    val tiar = Path().apply {
                        moveTo(cx - w * 0.16f, cy + h * 0.04f)
                        lineTo(cx - w * 0.07f, cy - h * 0.01f)
                        lineTo(cx, cy - h * 0.06f)
                        lineTo(cx + w * 0.07f, cy - h * 0.01f)
                        lineTo(cx + w * 0.16f, cy + h * 0.04f)
                    }
                    drawPath(tiar, color = LunarPrimary, style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawCircle(color = LunarCyan, radius = w * 0.036f, center = Offset(cx, cy - h * 0.02f))
                }
                AvatarAccessoryTier.MANA_ORBITAL -> {
                    // Diadem + floating Mana Orb at upper left
                    val cx = w * 0.5f
                    val cy = h * 0.14f
                    val tiar = Path().apply {
                        moveTo(cx - w * 0.16f, cy + h * 0.04f)
                        lineTo(cx, cy - h * 0.06f)
                        lineTo(cx + w * 0.16f, cy + h * 0.04f)
                    }
                    drawPath(tiar, color = LunarPrimary, style = Stroke(width = 2.2f, cap = StrokeCap.Round))
                    val orbCenter = Offset(w * 0.17f, h * 0.21f)
                    drawCircle(color = LunarCyan.copy(alpha = 0.35f), radius = w * 0.11f, center = orbCenter)
                    drawCircle(color = LunarCyan, radius = w * 0.065f, center = orbCenter)
                    drawCircle(color = Color.White, radius = w * 0.025f, center = Offset(orbCenter.x - w * 0.018f, orbCenter.y - w * 0.018f))
                }
                AvatarAccessoryTier.SOLAR_CROWN -> {
                    // Royal 3-peak golden crown + glowing mana orb
                    val cx = w * 0.5f
                    val base = h * 0.17f
                    val crown = Path().apply {
                        moveTo(cx - w * 0.18f, base)
                        lineTo(cx - w * 0.20f, base - h * 0.09f)
                        lineTo(cx - w * 0.08f, base - h * 0.04f)
                        lineTo(cx, base - h * 0.13f)
                        lineTo(cx + w * 0.08f, base - h * 0.04f)
                        lineTo(cx + w * 0.20f, base - h * 0.09f)
                        lineTo(cx + w * 0.18f, base)
                        close()
                    }
                    drawPath(crown, color = GoldBadge)
                    drawPath(crown, color = Color.White.copy(alpha = 0.85f), style = Stroke(width = 1.6f, join = StrokeJoin.Round))
                    drawCircle(color = SolarMagenta, radius = w * 0.032f, center = Offset(cx, base - h * 0.045f))

                    val orbCenter = Offset(w * 0.16f, h * 0.22f)
                    drawCircle(color = GoldBadge.copy(alpha = 0.38f), radius = w * 0.10f, center = orbCenter)
                    drawCircle(color = LunarCyan, radius = w * 0.055f, center = orbCenter)
                }
            }

            // 4. Corner Wisdom Sparkles for Level 3+
            if (wisdomStage.minLevel >= 3) {
                val starColor = if (wisdomStage == AvatarWisdomStage.ARQUIMAGO) GoldBadge else LunarPrimary
                val sr = w * 0.048f
                val corner = Offset(w * 0.84f, h * 0.15f)
                val star = Path().apply {
                    moveTo(corner.x, corner.y - sr)
                    lineTo(corner.x + sr * 0.32f, corner.y - sr * 0.32f)
                    lineTo(corner.x + sr, corner.y)
                    lineTo(corner.x + sr * 0.32f, corner.y + sr * 0.32f)
                    lineTo(corner.x, corner.y + sr)
                    lineTo(corner.x - sr * 0.32f, corner.y + sr * 0.32f)
                    lineTo(corner.x - sr, corner.y)
                    lineTo(corner.x - sr * 0.32f, corner.y - sr * 0.32f)
                    close()
                }
                drawPath(star, color = starColor)
            }

            // Top diagonal specular highlight
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.White.copy(alpha = 0.65f), Color.Transparent)
                ),
                start = Offset(2f, 2f),
                end = Offset(w * 0.75f, 2f),
                strokeWidth = 2f
            )
        }

        // Bottom-right Materia Crystal Slot (reflects outfit/job trim)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(3.dp)
                .size((size.value * 0.22f).coerceIn(10f, 18f).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White, materiaColor, MidnightBase)
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape)
        )
    }
}

@Composable
fun ReserveShieldBadge(
    tier: Int,
    runwayMonths: Double,
    modifier: Modifier = Modifier
) {
    val shieldColor = when (tier) {
        5 -> EmeraldSafe
        4 -> LunarCyan
        3 -> SolarMagenta
        2 -> AmberWarn
        else -> CrimsonRisk
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(shieldColor.copy(alpha = 0.24f), CosmicIndigo.copy(alpha = 0.5f), MidnightVoid)
                )
            )
            .border(1.2.dp, shieldColor.copy(alpha = 0.72f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            val w = size.width
            val h = size.height
            val fullShield = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                lineTo(w * 0.93f, h * 0.24f)
                lineTo(w * 0.82f, h * 0.72f)
                lineTo(w * 0.5f, h * 0.97f)
                lineTo(w * 0.18f, h * 0.72f)
                lineTo(w * 0.07f, h * 0.24f)
                close()
            }
            val leftHalf = Path().apply {
                moveTo(w * 0.5f, h * 0.05f)
                lineTo(w * 0.5f, h * 0.97f)
                lineTo(w * 0.18f, h * 0.72f)
                lineTo(w * 0.07f, h * 0.24f)
                close()
            }
            drawPath(fullShield, color = shieldColor.copy(alpha = 0.30f))
            drawPath(leftHalf, color = shieldColor.copy(alpha = 0.62f))
            drawPath(fullShield, color = Color.White.copy(alpha = 0.7f), style = Stroke(width = 1.8f))
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(w * 0.5f, h * 0.20f),
                end = Offset(w * 0.5f, h * 0.80f),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(w * 0.26f, h * 0.42f),
                end = Offset(w * 0.74f, h * 0.42f),
                strokeWidth = 1.4f
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(
            text = "Nv.$tier • ${String.format("%.1f", runwayMonths)}m",
            style = MaterialTheme.typography.labelSmall.copy(
                color = shieldColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun TermLabelWithTooltip(
    termKey: TermKey,
    languageLevel: Int,
    modifier: Modifier = Modifier,
    textColor: Color = OnSurfaceText,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleSmall
) {
    val display = remember(termKey, languageLevel) { LanguageDictionary.get(termKey, languageLevel) }
    var showTooltip by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = display.label,
            style = style,
            color = textColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (languageLevel == 3) {
            Spacer(Modifier.width(2.dp))
            IconButton(
                onClick = { showTooltip = true },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("tooltip_${termKey.name.lowercase()}")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Traduzir termo ${display.label}",
                    tint = LunarCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showTooltip) {
        Dialog(onDismissRequest = { showTooltip = false }) {
            RpgBevelCard(
                borderColor = LunarCyan,
                containerColor = CosmicIndigo
            ) {
                Text(
                    text = display.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = LunarPrimary
                )
                Spacer(Modifier.height(8.dp))
                RpgInsetWell {
                    Text(
                        text = display.tooltipLevel3,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceText
                    )
                }
                Spacer(Modifier.height(12.dp))
                LunarCoreButton(
                    text = "Entendi",
                    onClick = { showTooltip = false },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.56f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun ClickableHelpBadge(
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(listOf(CosmicIndigo, MidnightVoid))
                )
                .border(1.dp, LunarCyan.copy(alpha = 0.65f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                contentDescription = contentDescription,
                tint = LunarPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Classic 32-bit PS1/Saturn RPG dialogue window with specular top highlight and corner brackets.
 */
@Composable
fun RpgPsxWindow(
    modifier: Modifier = Modifier,
    borderColor: Color = LunarCyan.copy(alpha = 0.45f),
    bracketColor: Color = LunarCyan,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .shadow(12.dp, shape, ambientColor = FrameOuterShadow, spotColor = LunarCyan.copy(alpha = 0.28f))
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(PsxWindowTop, PsxWindowMid, PsxWindowBottom))
            )
            .border(1.5.dp, borderColor, shape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            // Specular horizontal highlight along top edge
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, LunarPrimary.copy(alpha = 0.85f), Color.Transparent)
                ),
                start = Offset(0f, 1.5f),
                end = Offset(w, 1.5f),
                strokeWidth = 2.2f
            )
            // Top-left & bottom-right 32-bit corner brackets
            val bLen = 14f
            drawLine(bracketColor, Offset(1f, 1f), Offset(bLen, 1f), 3.5f)
            drawLine(bracketColor, Offset(1f, 1f), Offset(1f, bLen), 3.5f)
            drawLine(bracketColor, Offset(w - bLen, h - 1f), Offset(w - 1f, h - 1f), 3.5f)
            drawLine(bracketColor, Offset(w - 1f, h - bLen), Offset(w - 1f, h - 1f), 3.5f)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * 32-bit danger/critical resonance alert window (Rose/Ruby glow).
 */
@Composable
fun RpgDangerWindow(
    modifier: Modifier = Modifier,
    accentColor: Color = RoseHazard,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .shadow(12.dp, shape, ambientColor = FrameOuterShadow, spotColor = accentColor.copy(alpha = 0.35f))
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(PsxDangerTop, PsxDangerMid, PsxDangerBottom))
            )
            .border(1.5.dp, accentColor.copy(alpha = 0.58f), shape)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, accentColor.copy(alpha = 0.8f), Color.Transparent)
                ),
                start = Offset(0f, 1.5f),
                end = Offset(size.width, 1.5f),
                strokeWidth = 2f
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Crisp 32-bit crossed swords emblem for the center Forjar orb HUD button.
 */
@Composable
fun CrossedSwordsGlyph(
    tint: Color = LunarPrimary,
    modifier: Modifier = Modifier.size(22.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeMain = Stroke(width = w * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val strokeGuard = Stroke(width = w * 0.10f, cap = StrokeCap.Round)

        // Sword 1 (bottom-left to top-right)
        drawLine(tint, Offset(w * 0.18f, h * 0.82f), Offset(w * 0.82f, h * 0.18f), strokeWidth = strokeMain.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.22f, h * 0.60f), Offset(w * 0.40f, h * 0.78f), strokeWidth = strokeGuard.width, cap = StrokeCap.Round)

        // Sword 2 (bottom-right to top-left)
        drawLine(tint, Offset(w * 0.82f, h * 0.82f), Offset(w * 0.18f, h * 0.18f), strokeWidth = strokeMain.width, cap = StrokeCap.Round)
        drawLine(tint, Offset(w * 0.60f, h * 0.78f), Offset(w * 0.78f, h * 0.60f), strokeWidth = strokeGuard.width, cap = StrokeCap.Round)

        // Center hilt specular spark
        drawCircle(color = Color.White.copy(alpha = 0.9f), radius = w * 0.06f, center = Offset(w * 0.5f, h * 0.5f))
    }
}

