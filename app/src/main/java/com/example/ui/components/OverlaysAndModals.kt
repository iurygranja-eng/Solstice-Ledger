package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.AstralSurface
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.FrameOuterShadow
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarDeep
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.SolarMagenta
import com.example.viewmodel.JustInTimeTip

data class ExplainerContent(
    val title: String,
    val whatItShows: String,
    val currentStatus: String,
    val oraclePrompt: String
)

@Composable
fun ExplainerModal(
    content: ExplainerContent,
    onDismiss: () -> Unit,
    onAskOracle: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        RpgBevelCard(
            borderColor = LunarCyan,
            containerColor = CosmicIndigo
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SampledDrawableImage(
                        resId = R.drawable.img_oracle_orb_1794969169050,
                        contentDescription = null,
                        maxDimPx = 192,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, LunarCyan.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = content.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LunarPrimary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_explainer_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar explicação", tint = OnSurfaceMuted)
                }
            }

            Spacer(Modifier.height(10.dp))
            RpgInsetWell(
                borderColor = LunarCyan.copy(alpha = 0.45f),
                contentPadding = PaddingValues(11.dp)
            ) {
                Column {
                    Text(
                        text = "1. O que este indicador mostra",
                        style = MaterialTheme.typography.labelMedium,
                        color = LunarCyan
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = content.whatItShows,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceText
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            RpgInsetWell(
                borderColor = EmeraldSafe.copy(alpha = 0.5f),
                contentPadding = PaddingValues(11.dp)
            ) {
                Column {
                    Text(
                        text = "2. Como está sua situação agora",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldSafe
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = content.currentStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceText
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            SolarCompanionButton(
                text = "Perguntar ao Oráculo sobre isso",
                onClick = {
                    onDismiss()
                    onAskOracle(content.oraclePrompt)
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "ask_oracle_from_explainer"
            )
        }
    }
}

@Composable
fun FirstMissionTourModal(
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    val steps = listOf(
        Triple(
            "Passo 1/3 • Limite Seguro Diário",
            "Bússola de Decisão Rápida",
            "Este valor mostra quanto você pode gastar hoje sem prejudicar suas contas fixas, faturas do cartão ou sua meta de reserva do mês."
        ),
        Triple(
            "Passo 2/3 • Linha de Equilíbrio Mensal",
            "A Estrada até a Zona de Conquista",
            "A barra compara sua renda do mês com o mínimo necessário para vencer contas fixas, faturas e essenciais. Passou da linha? Você entra na Zona de Conquista!"
        ),
        Triple(
            "Passo 3/3 • Forja de Lançamentos (< 5s)",
            "Registro Instantâneo & Importação OFX/CSV",
            "Toque no botão central '+' para registrar gastos em 3 toques no teclado rápido ou importar seu extrato bancário (.ofx/.csv) sem duplicatas."
        )
    )
    val (header, subtitle, body) = steps[step]

    Dialog(onDismissRequest = onFinish) {
        RpgBevelCard(
            borderColor = SolarMagenta,
            containerColor = CosmicIndigo
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SampledDrawableImage(
                    resId = R.drawable.img_oracle_orb_1794969169050,
                    contentDescription = null,
                    maxDimPx = 192,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, SolarMagenta.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Tour Primeira Missão",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberWarn
                    )
                    Text(
                        text = header,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = LunarPrimary
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = SolarMagenta
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MidnightVoid.copy(alpha = 0.8f))
                    .border(1.dp, BevelBorderIndigo, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceText
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.testTag("skip_tour_button")
                ) {
                    Text("Pular", color = OnSurfaceMuted)
                }
                LunarCoreButton(
                    text = if (step < 2) "Próximo" else "Iniciar Jornada!",
                    onClick = {
                        if (step < 2) step++ else onFinish()
                    },
                    modifier = Modifier.width(160.dp),
                    testTag = "tour_next_button"
                )
            }
        }
    }
}

@Composable
fun JustInTimeTipBanner(
    tip: JustInTimeTip,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        color = CosmicIndigo,
        border = BorderStroke(1.5.dp, AmberWarn.copy(alpha = 0.75f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = AmberWarn,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = AmberWarn
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tip.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceText
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp).testTag("dismiss_jit_tip")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Dispensar dica", tint = OnSurfaceMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun LanguageSelectorRow(
    currentLevel: Int,
    onSelectLevel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(1 to "1. Fácil", 2 to "2. Médio", 3 to "3. RPG 32-Bit")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(androidx.compose.ui.graphics.Color(0xFF060A17))
            .border(1.dp, LunarCyan.copy(alpha = 0.24f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = null,
                tint = LunarCyan,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "TOM:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = LunarCyan.copy(alpha = 0.9f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { (lvl, label) ->
                val selected = currentLevel == lvl
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(
                                    listOf(LunarCyan.copy(alpha = 0.45f), CosmicIndigo.copy(alpha = 0.85f))
                                )
                            } else {
                                Brush.horizontalGradient(listOf(AstralSurface.copy(alpha = 0.25f), MidnightVoid))
                            }
                        )
                        .then(
                            if (selected) Modifier.border(1.dp, LunarCyan.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                            else Modifier
                        )
                        .clickable { onSelectLevel(lvl) }
                        .testTag("lang_level_$lvl")
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) LunarPrimary else OnSurfaceMuted
                        )
                    )
                }
            }
        }
    }
}
