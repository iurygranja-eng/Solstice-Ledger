package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.UserProfile
import com.example.domain.AvatarEvolutionEngine
import com.example.domain.AvatarMascotTier
import com.example.domain.AvatarWisdomStage
import com.example.domain.mapWisdomLevelToVisualAssets
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.CinzelTitleStyle
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.GoldBadge
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightBase
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.SolarMagenta
import com.example.ui.theme.SolarSecondary

/**
 * Dynamic 32-bit Avatar cluster rendered inside Taverna Central.
 * Dynamically composes:
 * 1) Wisdom Aura Halo (for Wisdom levels >= 2)
 * 2) Hero Portrait + Incremental Costume & Accessory overlays mapped from Wisdom level ranges
 * 3) Companion Mascot miniature pedestal docked alongside the portrait
 */
@Composable
fun TavernaDynamicAvatarCluster(
    profile: UserProfile,
    wisdomLevel: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loadout = remember(wisdomLevel) { mapWisdomLevelToVisualAssets(wisdomLevel) }
    val evolution = remember(profile) { AvatarEvolutionEngine.compute(profile) }
    val activeCostume = evolution.costume
    val activeAccessory = evolution.accessory
    val activeMascot = evolution.mascot

    val stageAccent = when (loadout.wisdomStage) {
        AvatarWisdomStage.APRENDIZ -> LunarCyan
        AvatarWisdomStage.ESCRIBA -> LunarCyan
        AvatarWisdomStage.GUARDIAO -> LunarPrimary
        AvatarWisdomStage.ESTRATEGA -> SolarMagenta
        AvatarWisdomStage.ARQUIMAGO -> GoldBadge
    }

    val infiniteTransition = rememberInfiniteTransition(label = "taverna_avatar_motion")
    val bobPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )
    val haloSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Box(
        modifier = modifier
            .size(width = 74.dp, height = 64.dp)
            .clickable(onClick = onClick)
            .testTag("taverna_dynamic_avatar"),
        contentAlignment = Alignment.CenterStart
    ) {
        // Component Layer 1: Animated Wisdom Rune Halo
        Box(
            modifier = Modifier.size(62.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loadout.auraRingEnabled) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val r = size.minDimension * 0.47f
                    rotate(degrees = haloSpin, pivot = c) {
                        drawCircle(
                            color = stageAccent.copy(alpha = 0.48f),
                            radius = r,
                            center = c,
                            style = Stroke(
                                width = if (wisdomLevel >= 5) 2.2f else 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )
                        )
                    }
                }
            }

            // Component Layer 2: Portrait + Clothing + Accessory overlays
            PixelHeroAvatar(
                hairIndex = profile.avatarHair,
                outfitIndex = profile.avatarOutfit,
                auraKey = profile.avatarAura,
                size = 54.dp,
                wisdomLevel = wisdomLevel,
                netWorthCents = evolution.netWorthCents,
                costumeOverride = activeCostume,
                accessoryOverride = activeAccessory,
                wisdomStageOverride = loadout.wisdomStage
            )
        }

        // Component Layer 3: Dynamic Companion Mascot Pedestal (rendered when unlocked or level range >= 2, else astral egg)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-2f * bobPhase).dp)
                .size(29.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MidnightVoid.copy(alpha = 0.94f))
                .border(
                    1.2.dp,
                    if (activeMascot == AvatarMascotTier.SEALED_EGG) BevelBorderIndigo else stageAccent.copy(alpha = 0.85f),
                    RoundedCornerShape(7.dp)
                )
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            MascotSpriteCanvas(
                tier = activeMascot,
                bobFraction = bobPhase,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Compact 1-line strip in Taverna Central showing the dynamic components currently equipped
 * for the user's Sabedoria Financeira tier.
 */
@Composable
fun TavernaLoadoutBadgeStrip(
    profile: UserProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val evolution = remember(profile) { AvatarEvolutionEngine.compute(profile) }
    val loadout = evolution.wisdomLoadout
    val stageAccent = when (loadout.wisdomStage) {
        AvatarWisdomStage.APRENDIZ, AvatarWisdomStage.ESCRIBA -> LunarCyan
        AvatarWisdomStage.GUARDIAO -> LunarPrimary
        AvatarWisdomStage.ESTRATEGA -> SolarMagenta
        AvatarWisdomStage.ARQUIMAGO -> GoldBadge
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MidnightVoid.copy(alpha = 0.82f))
            .border(1.dp, stageAccent.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag("taverna_loadout_strip")
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(stageAccent.copy(alpha = 0.18f))
                .border(1.dp, stageAccent.copy(alpha = 0.55f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = loadout.levelRangeLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = stageAccent,
                maxLines = 1,
                softWrap = false
            )
        }
        Text(
            text = "${evolution.costume.titlePt} • ${evolution.accessory.titlePt} • ${evolution.mascot.titlePt}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            color = OnSurfaceText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Ver",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = LunarCyan,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Interactive Dialog explaining how each Sabedoria Financeira level range maps incrementally
 * to clothing, accessories, and companion mascots rendered on the Taverna avatar.
 */
@Composable
fun TavernaWisdomLoadoutDialog(
    profile: UserProfile,
    onDismiss: () -> Unit
) {
    val evolution = remember(profile) { AvatarEvolutionEngine.compute(profile) }
    val activeLoadout = evolution.wisdomLoadout

    Dialog(onDismissRequest = onDismiss) {
        RpgPsxWindow(
            borderColor = GoldBadge.copy(alpha = 0.65f),
            bracketColor = GoldBadge,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sabedoria Financeira • Nv.${evolution.wisdomLevel}",
                        style = CinzelTitleStyle.copy(fontSize = 14.sp),
                        color = GoldBadge,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(text = activeLoadout.levelRangeLabel, color = LunarCyan)
                }

                // Active 3 Dynamic Components Card
                RpgInsetWell(borderColor = LunarCyan.copy(alpha = 0.45f), contentPadding = PaddingValues(10.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "COMPONENTES ATIVOS (${activeLoadout.wisdomStage.titlePt.uppercase()})",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = LunarPrimary
                        )
                        ActiveComponentLine(
                            icon = Icons.Default.Checkroom,
                            slotLabel = "Traje",
                            value = evolution.costume.titlePt,
                            accent = SolarMagenta
                        )
                        ActiveComponentLine(
                            icon = Icons.Default.AutoAwesome,
                            slotLabel = "Acessório",
                            value = evolution.accessory.titlePt,
                            accent = GoldBadge
                        )
                        ActiveComponentLine(
                            icon = Icons.Default.Pets,
                            slotLabel = "Mascote",
                            value = evolution.mascot.titlePt,
                            accent = LunarCyan
                        )
                    }
                }

                Text(
                    text = "MAPA INCREMENTAL POR FAIXA DE NÍVEL",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    color = OnSurfaceMuted
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AvatarEvolutionEngine.allWisdomRangeLoadouts.forEach { rangeLoadout ->
                        val isCurrentRange = rangeLoadout.tierIndex == activeLoadout.tierIndex
                        val unlocked = evolution.wisdomLevel >= rangeLoadout.wisdomStage.minLevel &&
                            rangeLoadout.tierIndex <= activeLoadout.tierIndex
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCurrentRange) CosmicIndigo else MidnightVoid,
                            border = BorderStroke(
                                width = if (isCurrentRange) 1.4.dp else 1.dp,
                                color = if (isCurrentRange) GoldBadge else if (unlocked) EmeraldSafe.copy(alpha = 0.45f) else BevelBorderIndigo
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MascotSpriteCanvas(
                                    tier = rangeLoadout.mascot,
                                    bobFraction = 0.5f,
                                    modifier = Modifier.size(30.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${rangeLoadout.levelRangeLabel} • ${rangeLoadout.wisdomStage.titlePt}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isCurrentRange) GoldBadge else LunarPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isCurrentRange) {
                                            StatusChip(text = "ATUAL", color = GoldBadge)
                                        }
                                    }
                                    Text(
                                        text = "${rangeLoadout.costume.titlePt} + ${rangeLoadout.accessory.titlePt} + ${rangeLoadout.mascot.titlePt}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceMuted
                                    )
                                }
                            }
                        }
                    }
                }

                LunarCoreButton(
                    text = "Fechar",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "close_wisdom_loadout_dialog"
                )
            }
        }
    }
}

@Composable
private fun ActiveComponentLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    slotLabel: String,
    value: String,
    accent: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(15.dp))
        Text(
            text = "$slotLabel:",
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = OnSurfaceText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
