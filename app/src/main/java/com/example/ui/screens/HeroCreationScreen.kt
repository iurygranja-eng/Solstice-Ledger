package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.FinancialClass
import com.example.ui.components.LunarCoreButton
import com.example.ui.components.PixelHeroAvatar
import com.example.ui.components.RpgBevelCard
import com.example.ui.components.StatusChip
import com.example.ui.components.TwilightBackground
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.AstralSurface
import com.example.ui.theme.BevelBorderIndigo
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.SolarMagenta

@Composable
fun HeroCreationScreen(
    isSubmitting: Boolean,
    defaultDisplayName: String,
    onCreateHero: (
        heroName: String,
        avatarHair: Int,
        avatarOutfit: Int,
        avatarAura: String,
        financialClass: FinancialClass,
        monthlyNetIncomeCents: Long,
        includeDemoData: Boolean
    ) -> Unit
) {
    var heroName by remember { mutableStateOf(defaultDisplayName.ifBlank { "Aeloria" }) }
    var hairIndex by remember { mutableIntStateOf(0) }
    var outfitIndex by remember { mutableIntStateOf(0) }
    var auraKey by remember { mutableStateOf("CYAN") }
    var selectedClass by remember { mutableStateOf(FinancialClass.GUARDIAN) }
    var incomeBrlText by remember { mutableStateOf("6800") }
    var includeDemoData by remember { mutableStateOf(true) }

    val auraOptions = listOf("CYAN" to "Lua", "MAGENTA" to "Sol", "EMERALD" to "Esmeralda", "AMBER" to "Solar")

    TwilightBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatusChip(text = "NOVO JOGO • CRIAÇÃO DE AVATAR", color = SolarMagenta)
                Text(
                    text = "Forje Seu Herói Financeiro",
                    style = MaterialTheme.typography.headlineLarge,
                    color = LunarPrimary
                )
                Text(
                    text = "Personalize seu avatar e escolha a Classe que calibra os tetos iniciais do seu orçamento mensal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceMuted
                )

                // Avatar & Name Card
                RpgBevelCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixelHeroAvatar(
                            hairIndex = hairIndex,
                            outfitIndex = outfitIndex,
                            auraKey = auraKey,
                            size = 96.dp
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = heroName,
                                onValueChange = { if (it.length <= 32) heroName = it },
                                label = { Text("Nome do Herói") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LunarCyan,
                                    unfocusedBorderColor = BevelBorderIndigo,
                                    focusedContainerColor = MidnightVoid,
                                    unfocusedContainerColor = MidnightVoid,
                                    focusedTextColor = OnSurfaceText,
                                    unfocusedTextColor = OnSurfaceText
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("hero_name_input")
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallSelectChip(
                            label = "Retrato #${hairIndex + 1}",
                            onClick = { hairIndex = (hairIndex + 1) % 4 },
                            modifier = Modifier.weight(1f),
                            testTag = "cycle_hair_button"
                        )
                        SmallSelectChip(
                            label = "Materia #${outfitIndex + 1}",
                            onClick = { outfitIndex = (outfitIndex + 1) % 4 },
                            modifier = Modifier.weight(1f),
                            testTag = "cycle_outfit_button"
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("Aura Celestial", style = MaterialTheme.typography.labelMedium, color = OnSurfaceMuted)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        auraOptions.forEach { (key, label) ->
                            val selected = auraKey == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (selected) CosmicIndigo else MidnightVoid)
                                    .border(
                                        1.5.dp,
                                        if (selected) LunarCyan else BevelBorderIndigo,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .clickable { auraKey = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) LunarPrimary else OnSurfaceMuted
                                )
                            }
                        }
                    }
                }

                // Income & Financial Class
                RpgBevelCard {
                    Text(
                        text = "Renda Líquida Mensal (R$)",
                        style = MaterialTheme.typography.titleSmall,
                        color = LunarPrimary
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = incomeBrlText,
                        onValueChange = { txt ->
                            val filtered = txt.filter { it.isDigit() }
                            if (filtered.length <= 7) incomeBrlText = filtered
                        },
                        prefix = { Text("R$ ", fontFamily = JetBrainsMonoFontFamily, color = LunarCyan) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LunarCyan,
                            unfocusedBorderColor = BevelBorderIndigo,
                            focusedContainerColor = MidnightVoid,
                            unfocusedContainerColor = MidnightVoid,
                            focusedTextColor = OnSurfaceText,
                            unfocusedTextColor = OnSurfaceText
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("income_input")
                    )

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Escolha sua Classe Financeira",
                        style = MaterialTheme.typography.titleSmall,
                        color = LunarPrimary
                    )
                    Spacer(Modifier.height(8.dp))

                    FinancialClass.entries.forEach { fc ->
                        val isSelected = selectedClass == fc
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedClass = fc }
                                .testTag("class_${fc.name.lowercase()}"),
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) CosmicIndigo else MidnightVoid,
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) SolarMagenta else BevelBorderIndigo
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = fc.titlePt,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) SolarMagenta else OnSurfaceText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    StatusChip(
                                        text = "${fc.essentialPct}/${fc.lifestylePct}/${fc.reservePct}",
                                        color = if (isSelected) LunarCyan else EmeraldSafe
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = fc.subtitlePt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceMuted
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { includeDemoData = !includeDemoData },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeDemoData,
                            onCheckedChange = { includeDemoData = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = LunarCyan,
                                uncheckedColor = BevelBorderIndigo,
                                checkmarkColor = MidnightVoid
                            )
                        )
                        Column {
                            Text(
                                text = "Iniciar com dados demonstrativos realistas",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceText
                            )
                            Text(
                                text = "Pré-carrega lançamentos, parcelas, tags e Guilda para explorar na hora.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }
                }

                if (isSubmitting) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = LunarCyan, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Preparando Taverna Central...", color = LunarPrimary)
                    }
                } else {
                    LunarCoreButton(
                        text = "Entrar na Taverna Central",
                        onClick = {
                            val incomeReais = incomeBrlText.toLongOrNull()?.coerceAtLeast(500L) ?: 6800L
                            onCreateHero(
                                heroName,
                                hairIndex,
                                outfitIndex,
                                auraKey,
                                selectedClass,
                                incomeReais * 100L,
                                includeDemoData
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "start_adventure_button"
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SmallSelectChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MidnightVoid)
            .border(1.5.dp, BevelBorderIndigo, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = LunarPrimary
        )
    }
}
