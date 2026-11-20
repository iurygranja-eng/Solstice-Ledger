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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.data.formatBrl
import com.example.domain.AvatarAccessoryTier
import com.example.domain.AvatarCostumeTier
import com.example.domain.AvatarEvolutionState
import com.example.domain.AvatarMascotTier
import com.example.domain.AvatarWisdomStage
import com.example.ui.theme.AmberWarn
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
import com.example.ui.theme.StatCurrencyCompact
import com.example.ui.theme.TertiaryIndigo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HeroEvolutionAltarSection(
    profile: UserProfile,
    evolution: AvatarEvolutionState,
    modifier: Modifier = Modifier
) {
    var previewWisdomStage by remember(evolution.wisdomStage) { mutableStateOf<AvatarWisdomStage?>(null) }
    var previewCostume by remember(evolution.costume) { mutableStateOf<AvatarCostumeTier?>(null) }
    var previewAccessory by remember(evolution.accessory) { mutableStateOf<AvatarAccessoryTier?>(null) }
    var previewMascot by remember(evolution.mascot) { mutableStateOf<AvatarMascotTier?>(null) }
    var selectedSlotTab by remember { mutableIntStateOf(0) } // 0 = Trajes, 1 = Acessórios, 2 = Mascotes

    val isPreviewing = previewWisdomStage != null ||
        previewCostume != null ||
        previewAccessory != null ||
        previewMascot != null

    val shownWisdomStage = previewWisdomStage ?: evolution.wisdomStage
    val shownCostume = previewCostume ?: evolution.costume
    val shownAccessory = previewAccessory ?: evolution.accessory
    val shownMascot = previewMascot ?: evolution.mascot

    val stageAccent = when (shownWisdomStage) {
        AvatarWisdomStage.APRENDIZ -> LunarCyan
        AvatarWisdomStage.ESCRIBA -> LunarCyan
        AvatarWisdomStage.GUARDIAO -> LunarPrimary
        AvatarWisdomStage.ESTRATEGA -> SolarMagenta
        AvatarWisdomStage.ARQUIMAGO -> GoldBadge
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hero_evolution_altar")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val haloAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_rotation"
    )

    RpgPsxWindow(
        modifier = modifier,
        borderColor = stageAccent.copy(alpha = 0.65f),
        bracketColor = stageAccent,
        contentPadding = PaddingValues(14.dp)
    ) {
        // Header Row: Evolution Title + Mode Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = stageAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "EVOLUÇÃO DO AVATAR",
                    style = CinzelTitleStyle.copy(fontSize = 13.5.sp),
                    color = LunarPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(
                text = if (isPreviewing) "PRÉVIA ATIVA" else "${evolution.totalUnlockedItems}/${evolution.totalAvailableItems} ITENS",
                color = if (isPreviewing) AmberWarn else stageAccent
            )
        }

        Spacer(Modifier.height(10.dp))

        // 32-Bit Hero & Mascot Stage Diorama
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MidnightVoid,
                            CosmicIndigo.copy(alpha = 0.45f),
                            MidnightBase
                        )
                    )
                )
                .border(1.2.dp, stageAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
        ) {
            // Astral starlight backdrop & floor pedestal lines
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            stageAccent.copy(alpha = 0.16f + pulsePhase * 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.34f, h * 0.48f),
                        radius = w * 0.42f
                    )
                )
                // Floor horizon line
                val horizonY = h * 0.84f
                drawLine(
                    color = stageAccent.copy(alpha = 0.25f),
                    start = Offset(0f, horizonY),
                    end = Offset(w, horizonY),
                    strokeWidth = 1.4f
                )
                // Subtle RPG stars
                val stars = listOf(
                    Offset(w * 0.08f, h * 0.16f),
                    Offset(w * 0.22f, h * 0.10f),
                    Offset(w * 0.62f, h * 0.14f),
                    Offset(w * 0.86f, h * 0.19f),
                    Offset(w * 0.93f, h * 0.38f)
                )
                stars.forEachIndexed { idx, pos ->
                    val alpha = if (idx % 2 == 0) 0.35f + pulsePhase * 0.35f else 0.55f - pulsePhase * 0.25f
                    drawCircle(color = LunarPrimary.copy(alpha = alpha), radius = 1.8f, center = pos)
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Hero Altar (Portrait + Animated Wisdom Rune Halo)
                    Box(
                        modifier = Modifier.size(116.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (shownWisdomStage.minLevel >= 2) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                val outerR = size.minDimension * 0.47f
                                rotate(degrees = haloAngle, pivot = Offset(cx, cy)) {
                                    drawCircle(
                                        color = stageAccent.copy(alpha = 0.35f + pulsePhase * 0.2f),
                                        radius = outerR,
                                        center = Offset(cx, cy),
                                        style = Stroke(
                                            width = if (shownWisdomStage.minLevel >= 4) 2.2f else 1.5f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                        )
                                    )
                                }
                                if (shownWisdomStage.minLevel >= 4) {
                                    rotate(degrees = -haloAngle * 0.7f, pivot = Offset(cx, cy)) {
                                        val innerR = size.minDimension * 0.43f
                                        drawCircle(
                                            color = GoldBadge.copy(alpha = 0.35f),
                                            radius = innerR,
                                            center = Offset(cx, cy),
                                            style = Stroke(
                                                width = 1.2f,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 10f), 0f)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        PixelHeroAvatar(
                            hairIndex = profile.avatarHair,
                            outfitIndex = profile.avatarOutfit,
                            auraKey = profile.avatarAura,
                            size = 90.dp,
                            wisdomLevel = shownWisdomStage.minLevel,
                            netWorthCents = evolution.netWorthCents,
                            costumeOverride = shownCostume,
                            accessoryOverride = shownAccessory,
                            wisdomStageOverride = shownWisdomStage
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(4.dp))
                                .background(stageAccent)
                                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "SABEDORIA NV.${shownWisdomStage.minLevel}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MidnightBase,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Right Companion Mascot Pedestal + Equipped Gear Summary
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MidnightVoid.copy(alpha = 0.78f))
                                    .border(
                                        1.dp,
                                        if (shownMascot == AvatarMascotTier.SEALED_EGG) BevelBorderIndigo
                                        else LunarCyan.copy(alpha = 0.55f),
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                MascotSpriteCanvas(
                                    tier = shownMascot,
                                    bobFraction = pulsePhase,
                                    modifier = Modifier
                                        .size(58.dp)
                                        .offset(y = ((-2f) * pulsePhase).dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = shownWisdomStage.titlePt,
                                    style = CinzelTitleStyle.copy(fontSize = 13.sp),
                                    color = stageAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = shownMascot.titlePt,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (shownMascot == AvatarMascotTier.SEALED_EGG) OnSurfaceMuted else LunarPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = "Traje: ${shownCostume.titlePt}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = OnSurfaceText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Item: ${shownAccessory.titlePt}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = SolarSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (isPreviewing) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(AmberWarn.copy(alpha = 0.14f))
                            .border(1.dp, AmberWarn.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Visualizando estética de evolução futura",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberWarn,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(
                            onClick = {
                                previewWisdomStage = null
                                previewCostume = null
                                previewAccessory = null
                                previewMascot = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.testTag("evolution_reset_preview_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = LunarCyan, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Atual", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = LunarCyan)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Patrimônio Líquido & Next Cosmetic Milestone Gauge
        RpgInsetWell(
            borderColor = EmeraldSafe.copy(alpha = 0.40f),
            contentPadding = PaddingValues(11.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PATRIMÔNIO LÍQUIDO",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = OnSurfaceMuted
                        )
                        Text(
                            text = formatBrl(evolution.netWorthCents),
                            style = StatCurrencyCompact,
                            color = if (evolution.netWorthCents >= 0L) EmeraldSafe else AmberWarn,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Caixa + Reserva − Fatura",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = OnSurfaceMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                val milestone = evolution.nextMilestone
                if (milestone != null) {
                    RpgProgressBar(
                        progress = milestone.progressRatio,
                        barColor = GoldBadge,
                        secondaryColor = LunarCyan,
                        height = 10.dp
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Próx. item: ${milestone.titlePt}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GoldBadge
                        )
                        Text(
                            text = "Faltam ${formatBrl(milestone.missingCents)} (${formatBrl(milestone.requiredNetWorthCents)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = LunarCyan
                        )
                    }
                } else {
                    RpgProgressBar(
                        progress = 1f,
                        barColor = GoldBadge,
                        secondaryColor = EmeraldSafe,
                        height = 10.dp
                    )
                    Text(
                        text = "Todos os trajes, acessórios e mascotes lendários foram desbloqueados!",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GoldBadge
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Sabedoria Financeira Evolution Path (5 Wisdom Stages)
        Text(
            text = "AURA DE SABEDORIA FINANCEIRA (TOQUE PARA PRÉVIA)",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
            color = TertiaryIndigo
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            AvatarWisdomStage.entries.forEach { stage ->
                val unlocked = evolution.wisdomLevel >= stage.minLevel
                val activeShown = shownWisdomStage == stage
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            previewWisdomStage = if (stage == evolution.wisdomStage) null else stage
                        }
                        .testTag("wisdom_stage_${stage.minLevel}"),
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        activeShown -> CosmicIndigo
                        unlocked -> MidnightVoid
                        else -> MidnightBase.copy(alpha = 0.65f)
                    },
                    border = BorderStroke(
                        width = if (activeShown) 1.5.dp else 1.dp,
                        color = when {
                            activeShown -> GoldBadge
                            unlocked -> LunarCyan.copy(alpha = 0.55f)
                            else -> BevelBorderIndigo.copy(alpha = 0.7f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (unlocked) Icons.Default.Star else Icons.Default.Lock,
                            contentDescription = null,
                            tint = when {
                                activeShown -> GoldBadge
                                unlocked -> LunarCyan
                                else -> OnSurfaceMuted.copy(alpha = 0.6f)
                            },
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Nv.${stage.minLevel}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (unlocked) LunarPrimary else OnSurfaceMuted,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "${shownWisdomStage.titlePt}: ${shownWisdomStage.auraDescPt}",
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(12.dp))

        // Wardrobe & Mascot Tabs (Trajes / Acessórios / Mascotes)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .clip(RoundedCornerShape(8.dp))
                .background(MidnightVoid)
                .border(1.dp, BevelBorderIndigo, RoundedCornerShape(8.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            EvolutionTabChip(
                label = "Trajes (${evolution.unlockedCostumesCount}/${AvatarCostumeTier.entries.size})",
                icon = Icons.Default.Checkroom,
                selected = selectedSlotTab == 0,
                accent = SolarMagenta,
                onClick = { selectedSlotTab = 0 },
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("evolution_tab_trajes")
            )
            EvolutionTabChip(
                label = "Itens (${evolution.unlockedAccessoriesCount}/${AvatarAccessoryTier.entries.size})",
                icon = Icons.Default.AutoAwesome,
                selected = selectedSlotTab == 1,
                accent = GoldBadge,
                onClick = { selectedSlotTab = 1 },
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("evolution_tab_acessorios")
            )
            EvolutionTabChip(
                label = "Mascotes (${evolution.unlockedMascotsCount}/${AvatarMascotTier.entries.size - 1})",
                icon = Icons.Default.Pets,
                selected = selectedSlotTab == 2,
                accent = LunarCyan,
                onClick = { selectedSlotTab = 2 },
                modifier = Modifier.weight(1f).fillMaxHeight().testTag("evolution_tab_mascotes")
            )
        }

        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            when (selectedSlotTab) {
                0 -> AvatarCostumeTier.entries.forEach { tier ->
                    val unlocked = evolution.netWorthCents >= tier.requiredNetWorthCents
                    val active = shownCostume == tier
                    CosmeticTierRow(
                        title = tier.titlePt,
                        lore = tier.lorePt,
                        requirementText = if (tier.requiredNetWorthCents == 0L) "Inicial" else formatBrl(tier.requiredNetWorthCents),
                        unlocked = unlocked,
                        active = active,
                        accent = SolarMagenta,
                        badgeContent = {
                            Icon(
                                imageVector = Icons.Default.Checkroom,
                                contentDescription = null,
                                tint = if (unlocked) SolarSecondary else OnSurfaceMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            previewCostume = if (tier == evolution.costume) null else tier
                        },
                        modifier = Modifier.testTag("evolution_item_${tier.name.lowercase()}")
                    )
                }

                1 -> AvatarAccessoryTier.entries.forEach { tier ->
                    val unlocked = evolution.netWorthCents >= tier.requiredNetWorthCents
                    val active = shownAccessory == tier
                    CosmeticTierRow(
                        title = tier.titlePt,
                        lore = tier.lorePt,
                        requirementText = if (tier.requiredNetWorthCents == 0L) "Inicial" else formatBrl(tier.requiredNetWorthCents),
                        unlocked = unlocked,
                        active = active,
                        accent = GoldBadge,
                        badgeContent = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (unlocked) GoldBadge else OnSurfaceMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            previewAccessory = if (tier == evolution.accessory) null else tier
                        },
                        modifier = Modifier.testTag("evolution_item_${tier.name.lowercase()}")
                    )
                }

                else -> AvatarMascotTier.entries.filter { it != AvatarMascotTier.SEALED_EGG }.forEach { tier ->
                    val unlocked = evolution.netWorthCents >= tier.requiredNetWorthCents
                    val active = shownMascot == tier
                    CosmeticTierRow(
                        title = tier.titlePt,
                        lore = tier.lorePt,
                        requirementText = formatBrl(tier.requiredNetWorthCents),
                        unlocked = unlocked,
                        active = active,
                        accent = LunarCyan,
                        badgeContent = {
                            MascotSpriteCanvas(
                                tier = tier,
                                bobFraction = 0.5f,
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        onClick = {
                            previewMascot = if (tier == evolution.mascot) null else tier
                        },
                        modifier = Modifier.testTag("evolution_item_${tier.name.lowercase()}")
                    )
                }
            }
        }
    }
}

@Composable
private fun EvolutionTabChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(6.dp),
        color = if (selected) CosmicIndigo else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, accent.copy(alpha = 0.75f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accent else OnSurfaceMuted,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) LunarPrimary else OnSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CosmeticTierRow(
    title: String,
    lore: String,
    requirementText: String,
    unlocked: Boolean,
    active: Boolean,
    accent: Color,
    badgeContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RpgInsetWell(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        borderColor = when {
            active -> accent
            unlocked -> accent.copy(alpha = 0.45f)
            else -> BevelBorderIndigo
        },
        contentPadding = PaddingValues(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (unlocked) CosmicIndigo.copy(alpha = 0.7f)
                        else MidnightBase
                    )
                    .border(
                        1.dp,
                        if (active) accent else if (unlocked) accent.copy(alpha = 0.45f) else BevelBorderIndigo,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                badgeContent()
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (unlocked) LunarPrimary else OnSurfaceText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = if (unlocked) Icons.Default.Verified else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (unlocked) EmeraldSafe else OnSurfaceMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (active) "NO ALTAR" else requirementText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (active) accent else if (unlocked) EmeraldSafe else OnSurfaceMuted,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = lore,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 32-Bit RPG Companion / Familiar sprite renderer drawn in crisp Canvas artwork.
 */
@Composable
fun MascotSpriteCanvas(
    tier: AvatarMascotTier,
    bobFraction: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Ground shadow ellipse
        drawOval(
            color = Color.Black.copy(alpha = 0.45f),
            topLeft = Offset(w * 0.22f, h * 0.85f),
            size = Size(w * 0.56f, h * 0.11f)
        )

        when (tier) {
            AvatarMascotTier.SEALED_EGG -> {
                // Mystical Astral Egg with cyan rune band
                val egg = Path().apply {
                    moveTo(w * 0.50f, h * 0.16f)
                    cubicTo(w * 0.78f, h * 0.18f, w * 0.80f, h * 0.78f, w * 0.50f, h * 0.82f)
                    cubicTo(w * 0.20f, h * 0.78f, w * 0.22f, h * 0.18f, w * 0.50f, h * 0.16f)
                    close()
                }
                drawPath(
                    egg,
                    brush = Brush.verticalGradient(listOf(CosmicIndigo, MidnightBase))
                )
                drawPath(egg, color = LunarCyan.copy(alpha = 0.55f), style = Stroke(width = 2f))
                // Rune zigzag crack
                val rune = Path().apply {
                    moveTo(w * 0.36f, h * 0.48f)
                    lineTo(w * 0.46f, h * 0.42f)
                    lineTo(w * 0.54f, h * 0.52f)
                    lineTo(w * 0.64f, h * 0.45f)
                }
                drawPath(rune, color = LunarCyan.copy(alpha = 0.5f + bobFraction * 0.4f), style = Stroke(width = 2f, cap = StrokeCap.Round))
            }

            AvatarMascotTier.LUNAR_SLIME -> {
                // Translucent glossy RPG Slime
                val body = Path().apply {
                    moveTo(w * 0.50f, h * 0.16f)
                    lineTo(w * 0.56f, h * 0.28f)
                    cubicTo(w * 0.86f, h * 0.38f, w * 0.88f, h * 0.80f, w * 0.50f, h * 0.82f)
                    cubicTo(w * 0.12f, h * 0.80f, w * 0.14f, h * 0.38f, w * 0.44f, h * 0.28f)
                    close()
                }
                drawPath(
                    body,
                    brush = Brush.verticalGradient(listOf(LunarPrimary, LunarCyan, Color(0xFF0369A1)))
                )
                drawPath(body, color = Color.White.copy(alpha = 0.85f), style = Stroke(width = 1.8f, join = StrokeJoin.Round))
                // Gloss highlight
                drawOval(
                    color = Color.White.copy(alpha = 0.70f),
                    topLeft = Offset(w * 0.30f, h * 0.38f),
                    size = Size(w * 0.14f, h * 0.10f)
                )
                // Eyes & smile
                drawCircle(color = MidnightVoid, radius = w * 0.055f, center = Offset(w * 0.40f, h * 0.56f))
                drawCircle(color = MidnightVoid, radius = w * 0.055f, center = Offset(w * 0.60f, h * 0.56f))
                drawCircle(color = Color.White, radius = w * 0.02f, center = Offset(w * 0.38f, h * 0.54f))
                drawCircle(color = Color.White, radius = w * 0.02f, center = Offset(w * 0.58f, h * 0.54f))
                drawArc(
                    color = MidnightVoid,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(w * 0.42f, h * 0.60f),
                    size = Size(w * 0.16f, h * 0.10f),
                    style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                )
            }

            AvatarMascotTier.STAR_MOOGLE -> {
                // Winged Moogle companion with magenta glowing pompom
                val leftWing = Path().apply {
                    moveTo(w * 0.30f, h * 0.56f)
                    lineTo(w * 0.10f, h * 0.42f)
                    lineTo(w * 0.14f, h * 0.64f)
                    close()
                }
                val rightWing = Path().apply {
                    moveTo(w * 0.70f, h * 0.56f)
                    lineTo(w * 0.90f, h * 0.42f)
                    lineTo(w * 0.86f, h * 0.64f)
                    close()
                }
                drawPath(leftWing, color = TertiaryIndigo)
                drawPath(rightWing, color = TertiaryIndigo)
                // Pompom antenna
                drawLine(
                    color = OnSurfaceText,
                    start = Offset(w * 0.50f, h * 0.32f),
                    end = Offset(w * 0.50f, h * 0.17f),
                    strokeWidth = 2f
                )
                drawCircle(color = SolarMagenta.copy(alpha = 0.4f), radius = w * 0.11f, center = Offset(w * 0.50f, h * 0.15f))
                drawCircle(color = SolarMagenta, radius = w * 0.07f, center = Offset(w * 0.50f, h * 0.15f))
                // Body/Head
                drawOval(
                    brush = Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFCBD5E1))),
                    topLeft = Offset(w * 0.25f, h * 0.30f),
                    size = Size(w * 0.50f, h * 0.52f)
                )
                drawOval(
                    color = LunarPrimary,
                    topLeft = Offset(w * 0.25f, h * 0.30f),
                    size = Size(w * 0.50f, h * 0.52f),
                    style = Stroke(width = 1.6f)
                )
                // Ears
                drawPath(
                    Path().apply {
                        moveTo(w * 0.29f, h * 0.36f)
                        lineTo(w * 0.24f, h * 0.22f)
                        lineTo(w * 0.38f, h * 0.31f)
                        close()
                    },
                    color = Color(0xFFE2E8F0)
                )
                drawPath(
                    Path().apply {
                        moveTo(w * 0.71f, h * 0.36f)
                        lineTo(w * 0.76f, h * 0.22f)
                        lineTo(w * 0.62f, h * 0.31f)
                        close()
                    },
                    color = Color(0xFFE2E8F0)
                )
                // Cute closed happy eyes & red nose
                drawLine(color = MidnightVoid, start = Offset(w * 0.36f, h * 0.48f), end = Offset(w * 0.43f, h * 0.48f), strokeWidth = 2f, cap = StrokeCap.Round)
                drawLine(color = MidnightVoid, start = Offset(w * 0.57f, h * 0.48f), end = Offset(w * 0.64f, h * 0.48f), strokeWidth = 2f, cap = StrokeCap.Round)
                drawCircle(color = SolarMagenta, radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.55f))
            }

            AvatarMascotTier.VAULT_OWL -> {
                // Arcane Golden Vault Owl with cyan rune eyes
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(GoldBadge, Color(0xFFB45309))),
                    topLeft = Offset(w * 0.25f, h * 0.24f),
                    size = Size(w * 0.50f, h * 0.58f),
                    cornerRadius = CornerRadius(w * 0.20f, w * 0.20f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.75f),
                    topLeft = Offset(w * 0.25f, h * 0.24f),
                    size = Size(w * 0.50f, h * 0.58f),
                    cornerRadius = CornerRadius(w * 0.20f, w * 0.20f),
                    style = Stroke(width = 1.6f)
                )
                // Ear tufts
                drawPath(
                    Path().apply {
                        moveTo(w * 0.27f, h * 0.30f)
                        lineTo(w * 0.20f, h * 0.16f)
                        lineTo(w * 0.38f, h * 0.24f)
                        close()
                    },
                    color = GoldBadge
                )
                drawPath(
                    Path().apply {
                        moveTo(w * 0.73f, h * 0.30f)
                        lineTo(w * 0.80f, h * 0.16f)
                        lineTo(w * 0.62f, h * 0.24f)
                        close()
                    },
                    color = GoldBadge
                )
                // Chest emblem
                drawOval(color = CosmicIndigo, topLeft = Offset(w * 0.36f, h * 0.54f), size = Size(w * 0.28f, h * 0.22f))
                // Glowing cyan rune eyes + beak
                drawCircle(color = MidnightBase, radius = w * 0.09f, center = Offset(w * 0.39f, h * 0.40f))
                drawCircle(color = MidnightBase, radius = w * 0.09f, center = Offset(w * 0.61f, h * 0.40f))
                drawCircle(color = LunarCyan, radius = w * 0.048f, center = Offset(w * 0.39f, h * 0.40f))
                drawCircle(color = LunarCyan, radius = w * 0.048f, center = Offset(w * 0.61f, h * 0.40f))
                drawPath(
                    Path().apply {
                        moveTo(w * 0.46f, h * 0.44f)
                        lineTo(w * 0.54f, h * 0.44f)
                        lineTo(w * 0.50f, h * 0.51f)
                        close()
                    },
                    color = AmberWarn
                )
            }

            AvatarMascotTier.SOLSTICE_DRAGON -> {
                // Celestial Solstice Hatchling Dragon (turquoise & gold crest)
                // Wings
                drawPath(
                    Path().apply {
                        moveTo(w * 0.32f, h * 0.52f)
                        lineTo(w * 0.10f, h * 0.30f)
                        lineTo(w * 0.18f, h * 0.62f)
                        close()
                    },
                    color = GoldBadge.copy(alpha = 0.85f)
                )
                drawPath(
                    Path().apply {
                        moveTo(w * 0.68f, h * 0.52f)
                        lineTo(w * 0.90f, h * 0.30f)
                        lineTo(w * 0.82f, h * 0.62f)
                        close()
                    },
                    color = GoldBadge.copy(alpha = 0.85f)
                )
                // Crystal horns
                drawPath(
                    Path().apply {
                        moveTo(w * 0.38f, h * 0.28f)
                        lineTo(w * 0.28f, h * 0.12f)
                        lineTo(w * 0.44f, h * 0.24f)
                        close()
                    },
                    color = GoldBadge
                )
                drawPath(
                    Path().apply {
                        moveTo(w * 0.62f, h * 0.28f)
                        lineTo(w * 0.72f, h * 0.12f)
                        lineTo(w * 0.56f, h * 0.24f)
                        close()
                    },
                    color = GoldBadge
                )
                // Dragon body + head
                drawOval(
                    brush = Brush.verticalGradient(listOf(EmeraldSafe, LunarCyan, Color(0xFF0F766E))),
                    topLeft = Offset(w * 0.28f, h * 0.24f),
                    size = Size(w * 0.44f, h * 0.58f)
                )
                drawOval(
                    color = Color.White.copy(alpha = 0.82f),
                    topLeft = Offset(w * 0.28f, h * 0.24f),
                    size = Size(w * 0.44f, h * 0.58f),
                    style = Stroke(width = 1.6f)
                )
                // Belly plate
                drawOval(color = GoldBadge.copy(alpha = 0.75f), topLeft = Offset(w * 0.38f, h * 0.54f), size = Size(w * 0.24f, h * 0.24f))
                // Eyes & gem crest
                drawCircle(color = GoldBadge, radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.32f))
                drawCircle(color = MidnightVoid, radius = w * 0.05f, center = Offset(w * 0.39f, h * 0.42f))
                drawCircle(color = MidnightVoid, radius = w * 0.05f, center = Offset(w * 0.61f, h * 0.42f))
                drawCircle(color = SolarSecondary, radius = w * 0.022f, center = Offset(w * 0.39f, h * 0.42f))
                drawCircle(color = SolarSecondary, radius = w * 0.022f, center = Offset(w * 0.61f, h * 0.42f))
            }
        }
    }
}
