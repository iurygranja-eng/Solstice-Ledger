package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.audio.AudioPreferencesStore
import com.example.audio.AudioSettings
import com.example.audio.RpgAudioEngine
import com.example.data.CyclePreferencesStore
import com.example.data.formatBrl
import com.example.data.repo.FinanceRepository
import com.example.data.repo.OracleAction
import com.example.data.repo.authStateFlow
import com.example.data.repo.signOut
import com.example.domain.AvatarEvolutionEngine
import com.example.domain.NotificationMilestoneInput
import com.example.domain.RpgNotificationEngine
import com.example.notifications.NotificationPreferencesStore
import com.example.notifications.NotificationSettings
import com.example.notifications.RpgNotificationDispatcher
import com.example.notifications.RpgReminderScheduler
import com.example.ui.components.CrossedSwordsGlyph
import com.example.ui.components.ExplainerContent
import com.example.ui.components.ExplainerModal
import com.example.ui.components.FirstMissionTourModal
import com.example.ui.components.JustInTimeTipBanner
import com.example.ui.components.LunarCoreButton
import com.example.ui.components.RpgBevelCard
import com.example.ui.components.TwilightBackground
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ConfiguracoesScreen
import com.example.ui.screens.ForjaModalSheet
import com.example.ui.screens.GuildaScreen
import com.example.ui.screens.HeroCreationScreen
import com.example.ui.screens.MapaEstrelasScreen
import com.example.ui.screens.OraculoScreen
import com.example.ui.screens.TavernaCentralScreen
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CrimsonRisk
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LunarCyan
import com.example.ui.theme.LunarDeep
import com.example.ui.theme.LunarPrimary
import com.example.ui.theme.MidnightBase
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.OnSurfaceMuted
import com.example.ui.theme.OnSurfaceText
import com.example.ui.theme.SolarMagenta
import com.example.viewmodel.LedgerUiState
import com.example.viewmodel.LedgerViewModel
import com.example.viewmodel.OraculoViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

const val ROUTE_TAVERNA = "taverna"
const val ROUTE_MAPA = "mapa"
const val ROUTE_GUILDA = "guilda"
const val ROUTE_ORACULO = "oraculo"
const val ROUTE_CONFIG = "configuracoes"

@Composable
fun AppNavigation(
    auth: FirebaseAuth = Firebase.auth,
    deepLinkTarget: String? = null,
    onConsumeDeepLink: () -> Unit = {}
) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedAppShell(
                currentUserId = user.uid,
                defaultDisplayName = user.displayName ?: "Aeloria",
                deepLinkTarget = deepLinkTarget,
                onConsumeDeepLink = onConsumeDeepLink
            )
        }
    }
}

@Composable
private fun AuthenticatedAppShell(
    currentUserId: String,
    defaultDisplayName: String,
    deepLinkTarget: String?,
    onConsumeDeepLink: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val ledgerViewModel: LedgerViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                LedgerViewModel(FinanceRepository(db), currentUserId)
            }
        }
    )

    val oraculoViewModel: OraculoViewModel = viewModel(key = "oracle_$currentUserId")

    val uiState by ledgerViewModel.uiState.collectAsStateWithLifecycle()
    val viewMode by ledgerViewModel.viewMode.collectAsStateWithLifecycle()
    val isSubmitting by ledgerViewModel.isSubmitting.collectAsStateWithLifecycle()
    val statusMessage by ledgerViewModel.statusMessage.collectAsStateWithLifecycle()
    val jitTip by ledgerViewModel.justInTimeTip.collectAsStateWithLifecycle()
    val replayTour by ledgerViewModel.replayTourRequested.collectAsStateWithLifecycle()

    val oracleMessages by oraculoViewModel.messages.collectAsStateWithLifecycle()
    val oracleStreaming by oraculoViewModel.isStreaming.collectAsStateWithLifecycle()

    val audioPrefs = remember(context) { AudioPreferencesStore(context) }
    val audioSettings by audioPrefs.settingsFlow.collectAsStateWithLifecycle(initialValue = AudioSettings())

    val notificationPrefs = remember(context) { NotificationPreferencesStore(context) }
    val loadedNotificationSettings by notificationPrefs.settingsFlow.collectAsStateWithLifecycle(initialValue = null)
    val notificationSettings = loadedNotificationSettings ?: NotificationSettings()

    val cyclePrefs = remember(context) { CyclePreferencesStore(context) }
    val savedCycleStartDay by cyclePrefs.cycleStartDayFlow.collectAsStateWithLifecycle(initialValue = 1)
    LaunchedEffect(savedCycleStartDay) {
        ledgerViewModel.setCycleStartDay(savedCycleStartDay)
    }

    var currentTab by remember { mutableStateOf(ROUTE_TAVERNA) }
    var previousTab by remember { mutableStateOf(ROUTE_TAVERNA) }
    var showForjaSheet by remember { mutableStateOf(false) }
    var explainerContent by remember { mutableStateOf<ExplainerContent?>(null) }
    var prefilledOraclePrompt by remember { mutableStateOf<String?>(null) }

    val openSettingsTab: () -> Unit = {
        RpgAudioEngine.playUiTap(audioSettings)
        if (currentTab != ROUTE_CONFIG) {
            previousTab = currentTab
        }
        currentTab = ROUTE_CONFIG
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        val msg = statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            ledgerViewModel.clearStatusMessage()
        }
    }

    if (currentTab != ROUTE_TAVERNA) {
        BackHandler {
            currentTab = if (currentTab == ROUTE_CONFIG && previousTab != ROUTE_CONFIG) {
                previousTab
            } else {
                ROUTE_TAVERNA
            }
        }
    }

    when (val state = uiState) {
        LedgerUiState.Loading -> {
            TwilightBackground {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LunarCyan)
                        Spacer(Modifier.height(12.dp))
                        Text("Abrindo o Livro-Razão Celestial...", color = LunarPrimary)
                    }
                }
            }
        }

        LedgerUiState.NeedsHeroSetup -> {
            Box(modifier = Modifier.fillMaxSize()) {
                HeroCreationScreen(
                    isSubmitting = isSubmitting,
                    defaultDisplayName = defaultDisplayName,
                    onCreateHero = { name, hair, outfit, aura, fc, income, demo ->
                        ledgerViewModel.createHero(
                            heroName = name,
                            avatarHair = hair,
                            avatarOutfit = outfit,
                            avatarAura = aura,
                            financialClass = fc,
                            monthlyNetIncomeCents = income,
                            includeDemoData = demo
                        )
                    }
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }

        is LedgerUiState.Error -> {
            TwilightBackground {
                Box(
                    Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RpgBevelCard(borderColor = CrimsonRisk) {
                        Text("Falha ao sincronizar", style = MaterialTheme.typography.titleMedium, color = CrimsonRisk)
                        Spacer(Modifier.height(6.dp))
                        Text(state.message, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceText)
                        Spacer(Modifier.height(12.dp))
                        LunarCoreButton(
                            text = "Sair e tentar novamente",
                            onClick = { signOut(credentialManager, scope) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        is LedgerUiState.Ready -> {
            val showTour = !state.profile.hasSeenTour || replayTour

            LaunchedEffect(deepLinkTarget) {
                when (deepLinkTarget) {
                    RpgNotificationEngine.TARGET_FORJA -> {
                        currentTab = ROUTE_TAVERNA
                        showForjaSheet = true
                        onConsumeDeepLink()
                    }
                    RpgNotificationEngine.TARGET_GUILDA -> {
                        currentTab = ROUTE_GUILDA
                        onConsumeDeepLink()
                    }
                    RpgNotificationEngine.TARGET_TAVERNA -> {
                        currentTab = ROUTE_TAVERNA
                        onConsumeDeepLink()
                    }
                }
            }

            LaunchedEffect(
                loadedNotificationSettings?.remindersEnabled,
                loadedNotificationSettings?.reminderHour,
                loadedNotificationSettings?.reminderMinute,
                state.profile.heroName,
                state.stats.safeToSpendDailyCents,
                state.profile.languageLevel
            ) {
                val settings = loadedNotificationSettings ?: return@LaunchedEffect
                RpgNotificationDispatcher.ensureChannels(context)
                notificationPrefs.syncHeroContext(
                    heroName = state.profile.heroName,
                    safeToSpendCents = state.stats.safeToSpendDailyCents,
                    languageLevel = state.profile.languageLevel
                )
                if (settings.remindersEnabled && RpgNotificationDispatcher.hasNotificationPermission(context)) {
                    RpgReminderScheduler.scheduleDailyReminder(
                        context = context,
                        hour = settings.reminderHour,
                        minute = settings.reminderMinute
                    )
                } else {
                    RpgReminderScheduler.cancelDailyReminder(context)
                }
            }

            LaunchedEffect(
                loadedNotificationSettings,
                state.profile,
                state.breakEven,
                state.stats,
                state.guild
            ) {
                val settings = loadedNotificationSettings ?: return@LaunchedEffect
                val evolution = AvatarEvolutionEngine.compute(state.profile)
                val input = NotificationMilestoneInput(
                    profile = state.profile,
                    breakEven = state.breakEven,
                    stats = state.stats,
                    evolution = evolution,
                    guild = state.guild
                )
                if (currentUserId !in settings.seededUids) {
                    val baselineIds = RpgNotificationEngine.computeCurrentMilestoneIds(input)
                    notificationPrefs.markUidSeeded(currentUserId, baselineIds)
                } else if (settings.goalAlertsEnabled && RpgNotificationDispatcher.hasNotificationPermission(context)) {
                    val prefix = "$currentUserId:"
                    val seenForUser = settings.seenMilestoneIds
                        .filter { it.startsWith(prefix) }
                        .map { it.removePrefix(prefix) }
                        .toSet()
                    val newlyAchieved = RpgNotificationEngine.detectAchievedMilestones(
                        input = input,
                        alreadySentIds = seenForUser
                    )
                    if (newlyAchieved.isNotEmpty()) {
                        val dispatchedIds = mutableListOf<String>()
                        newlyAchieved.forEach { payload ->
                            if (RpgNotificationDispatcher.dispatch(context, payload)) {
                                dispatchedIds += payload.id
                            }
                        }
                        if (dispatchedIds.isNotEmpty()) {
                            notificationPrefs.markMilestonesSeen(currentUserId, dispatchedIds)
                        }
                    }
                }
            }

            TwilightBackground {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth >= 720.dp

                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = if (isWideScreen) {
                            WindowInsets.safeDrawing
                        } else {
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                        },
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                        bottomBar = {
                            if (!isWideScreen) {
                                SolsticeBottomNavBar(
                                    currentTab = currentTab,
                                    onSelectTab = { tab ->
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        currentTab = tab
                                    },
                                    onOpenForja = {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        showForjaSheet = true
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isWideScreen) {
                                SolsticeNavRail(
                                    currentTab = currentTab,
                                    onSelectTab = { tab ->
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        if (tab == ROUTE_CONFIG && currentTab != ROUTE_CONFIG) {
                                            previousTab = currentTab
                                        }
                                        currentTab = tab
                                    },
                                    onOpenForja = {
                                        RpgAudioEngine.playUiTap(audioSettings)
                                        showForjaSheet = true
                                    }
                                )
                            }

                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    jitTip?.let { tip ->
                                        JustInTimeTipBanner(
                                            tip = tip,
                                            onDismiss = { ledgerViewModel.dismissJustInTimeTip() }
                                        )
                                    }

                                    when (currentTab) {
                                        ROUTE_TAVERNA -> TavernaCentralScreen(
                                            state = state,
                                            viewMode = viewMode,
                                            audioSettings = audioSettings,
                                            onSetViewMode = { ledgerViewModel.setViewMode(it) },
                                            onOpenSettings = openSettingsTab,
                                            onOpenExplainer = { explainerContent = it },
                                            onAskOracleDirect = { prompt ->
                                                prefilledOraclePrompt = prompt
                                                currentTab = ROUTE_ORACULO
                                            },
                                            onPayOpenBill = { ledgerViewModel.payOpenCreditCardBill() },
                                            onReplayTour = { ledgerViewModel.requestReplayTour() },
                                            onSignOut = { signOut(credentialManager, scope) }
                                        )

                                        ROUTE_MAPA -> MapaEstrelasScreen(
                                            state = state,
                                            audioSettings = audioSettings,
                                            onOpenExplainer = { explainerContent = it },
                                            onUnlockLesson = { id, xp -> ledgerViewModel.unlockLesson(id, xp) },
                                            onAskOracle = { prompt ->
                                                prefilledOraclePrompt = prompt
                                                currentTab = ROUTE_ORACULO
                                            },
                                            onOpenSettings = openSettingsTab
                                        )

                                        ROUTE_GUILDA -> GuildaScreen(
                                            state = state,
                                            isSubmitting = isSubmitting,
                                            audioSettings = audioSettings,
                                            onUpdateGuildSettings = { priv, split ->
                                                ledgerViewModel.updateGuildSettings(priv, split)
                                            },
                                            onSettleGuild = { ledgerViewModel.settleGuildMonth() },
                                            onJoinGuildCode = { code -> ledgerViewModel.joinGuildByCode(code) },
                                            onAskOracle = { prompt ->
                                                prefilledOraclePrompt = prompt
                                                currentTab = ROUTE_ORACULO
                                            },
                                            onOpenSettings = openSettingsTab
                                        )

                                        ROUTE_ORACULO -> OraculoScreen(
                                            ledgerState = state,
                                            messages = oracleMessages,
                                            isStreaming = oracleStreaming,
                                            prefilledPrompt = prefilledOraclePrompt,
                                            onConsumePrefilled = { prefilledOraclePrompt = null },
                                            onSendMessage = { prompt ->
                                                oraculoViewModel.sendMessage(prompt, state.toContextSnapshot())
                                            },
                                            onExecuteAction = { action ->
                                                when (action) {
                                                    is OracleAction.ReallocateBudget -> {
                                                        ledgerViewModel.reallocateBudgetByName(
                                                            fromName = action.fromCategoryName,
                                                            toName = action.toCategoryName,
                                                            shiftCents = action.amountCents
                                                        )
                                                    }
                                                    is OracleAction.SimulateInstallment -> {
                                                        val perMonth = action.totalCents / action.months.coerceAtLeast(1)
                                                        oraculoViewModel.sendMessage(
                                                            "Simule detalhadamente como ${action.months}x de ${formatBrl(perMonth)} afetaria meus próximos 3 meses e meu Ponto de Equilíbrio.",
                                                            state.toContextSnapshot()
                                                        )
                                                    }
                                                }
                                            },
                                            onClearChat = { oraculoViewModel.clearConversation() },
                                            onOpenSettings = openSettingsTab
                                        )

                                        ROUTE_CONFIG -> ConfiguracoesScreen(
                                            state = state,
                                            audioSettings = audioSettings,
                                            notificationSettings = notificationSettings,
                                            onBack = {
                                                currentTab = if (previousTab != ROUTE_CONFIG) previousTab else ROUTE_TAVERNA
                                            },
                                            onSelectLanguageLevel = { lvl -> ledgerViewModel.setLanguageLevel(lvl) },
                                            onSelectCycleStartDay = { day -> scope.launch { cyclePrefs.setCycleStartDay(day) } },
                                            onToggleMasterMute = { muted -> scope.launch { audioPrefs.setMasterMuted(muted) } },
                                            onToggleSfx = { enabled -> scope.launch { audioPrefs.setSfxEnabled(enabled) } },
                                            onChangeSfxVolume = { vol -> scope.launch { audioPrefs.setSfxVolume(vol) } },
                                            onToggleAmbience = { enabled -> scope.launch { audioPrefs.setAmbienceEnabled(enabled) } },
                                            onChangeAmbienceVolume = { vol -> scope.launch { audioPrefs.setAmbienceVolume(vol) } },
                                            onToggleHaptics = { enabled -> scope.launch { audioPrefs.setHapticsEnabled(enabled) } },
                                            onSelectSoundProfile = { prof -> scope.launch { audioPrefs.setSoundProfile(prof) } },
                                            onResetAudioDefaults = { scope.launch { audioPrefs.resetDefaults() } },
                                            onToggleReminders = { enabled -> scope.launch { notificationPrefs.setRemindersEnabled(enabled) } },
                                            onToggleGoalAlerts = { enabled -> scope.launch { notificationPrefs.setGoalAlertsEnabled(enabled) } },
                                            onSelectReminderTime = { h, m -> scope.launch { notificationPrefs.setReminderTime(h, m) } },
                                            onNotificationFeedback = { msg ->
                                                scope.launch { snackbarHostState.showSnackbar(msg) }
                                            },
                                            onReplayTour = { ledgerViewModel.requestReplayTour() },
                                            onSignOut = { signOut(credentialManager, scope) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Forja Quick Entry + Installment Simulator + OFX/CSV Sheet
                    if (showForjaSheet) {
                        ForjaModalSheet(
                            state = state,
                            isSubmitting = isSubmitting,
                            onDismiss = { showForjaSheet = false },
                            onSubmitManual = { desc, cents, type, method, catId, tags, inst, shared ->
                                ledgerViewModel.addManualTransaction(
                                    description = desc,
                                    amountCents = cents,
                                    type = type,
                                    paymentMethod = method,
                                    categoryId = catId,
                                    tags = tags,
                                    installments = inst,
                                    sharedGuild = shared,
                                    onComplete = {
                                        RpgAudioEngine.playSuccessChime(audioSettings)
                                        showForjaSheet = false
                                    }
                                )
                            },
                            onConfirmImport = { items ->
                                ledgerViewModel.importParsedTransactions(
                                    items = items,
                                    onComplete = {
                                        RpgAudioEngine.playSuccessChime(audioSettings)
                                        showForjaSheet = false
                                    }
                                )
                            },
                            onAskOracle = { prompt ->
                                showForjaSheet = false
                                prefilledOraclePrompt = prompt
                                currentTab = ROUTE_ORACULO
                            }
                        )
                    }

                    // 3-Block "?" Explainer Modal
                    explainerContent?.let { content ->
                        ExplainerModal(
                            content = content,
                            onDismiss = { explainerContent = null },
                            onAskOracle = { prompt ->
                                prefilledOraclePrompt = prompt
                                currentTab = ROUTE_ORACULO
                            }
                        )
                    }

                    // 3-Step "Primeira Missão" Tour
                    if (showTour) {
                        FirstMissionTourModal(
                            onFinish = { ledgerViewModel.finishTour() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SolsticeBottomNavBar(
    currentTab: String,
    onSelectTab: (String) -> Unit,
    onOpenForja: () -> Unit
) {
    // Wrapper Box reserves 14.dp above the cyan HUD top border so the glowing Forjar orb
    // floats gracefully above the bottom bar without any layout clipping.
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Dark translucent HUD bar backdrop fills behind the system gesture area smoothly
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(top = 14.dp)
                .background(Color(0xF5070B18))
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                LunarCyan.copy(alpha = 0.25f),
                                LunarCyan.copy(alpha = 0.58f),
                                LunarCyan.copy(alpha = 0.25f)
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.height(62.dp))
        }

        // 5-Slot HUD Row: Taverna | Mapa | [Forjar Orb] | Guilda | Oráculo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            NavTabItem(
                label = "Taverna",
                icon = Icons.Default.Castle,
                selected = currentTab == ROUTE_TAVERNA,
                onClick = { onSelectTab(ROUTE_TAVERNA) },
                testTag = "nav_taverna",
                modifier = Modifier.weight(1f)
            )

            NavTabItem(
                label = "Mapa",
                icon = Icons.Default.Explore,
                selected = currentTab == ROUTE_MAPA,
                onClick = { onSelectTab(ROUTE_MAPA) },
                testTag = "nav_mapa",
                modifier = Modifier.weight(1f)
            )

            // Center Floating 32-bit Glowing Forjar Orb with Crossed Swords
            Column(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-4).dp)
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onOpenForja)
                    .testTag("nav_forja_fab")
                    .padding(horizontal = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            ambientColor = LunarCyan,
                            spotColor = LunarCyan
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(LunarCyan, LunarDeep, CosmicIndigo)
                            )
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF102246), Color(0xFF0A1226))
                            )
                        )
                        .border(1.dp, LunarPrimary.copy(alpha = 0.48f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CrossedSwordsGlyph(
                        tint = LunarPrimary,
                        modifier = Modifier.size(21.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Forjar",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = LunarPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            NavTabItem(
                label = "Guilda",
                icon = Icons.Default.Shield,
                selected = currentTab == ROUTE_GUILDA,
                onClick = { onSelectTab(ROUTE_GUILDA) },
                testTag = "nav_guilda",
                modifier = Modifier.weight(1f)
            )

            NavTabItem(
                label = "Oráculo",
                icon = Icons.Default.AutoAwesome,
                selected = currentTab == ROUTE_ORACULO,
                showAlertDot = true,
                onClick = { onSelectTab(ROUTE_ORACULO) },
                testTag = "nav_oraculo",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    showAlertDot: Boolean = false
) {
    Column(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) {
                    Brush.verticalGradient(listOf(CosmicIndigo.copy(alpha = 0.65f), Color.Transparent))
                } else {
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .then(
                if (selected) Modifier.border(1.dp, LunarCyan.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) LunarCyan else OnSurfaceMuted,
                modifier = Modifier.size(20.dp)
            )
            if (showAlertDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(6.dp)
                        .shadow(4.dp, CircleShape, spotColor = SolarMagenta)
                        .clip(CircleShape)
                        .background(SolarMagenta)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp
            ),
            color = if (selected) LunarPrimary else OnSurfaceMuted,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SolsticeNavRail(
    currentTab: String,
    onSelectTab: (String) -> Unit,
    onOpenForja: () -> Unit
) {
    NavigationRail(
        containerColor = MidnightVoid,
        contentColor = OnSurfaceText,
        header = {
            Spacer(Modifier.height(12.dp))
            FloatingActionButton(
                onClick = onOpenForja,
                containerColor = Color(0xFF0A1226),
                contentColor = LunarPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .border(2.dp, LunarCyan, CircleShape)
                    .testTag("rail_forja_fab")
            ) {
                CrossedSwordsGlyph(tint = LunarPrimary, modifier = Modifier.size(22.dp))
            }
        }
    ) {
        Spacer(Modifier.height(16.dp))
        listOf(
            Triple(ROUTE_TAVERNA, "Taverna", Icons.Default.Castle),
            Triple(ROUTE_MAPA, "Mapa", Icons.Default.Explore),
            Triple(ROUTE_GUILDA, "Guilda", Icons.Default.Shield),
            Triple(ROUTE_ORACULO, "Oráculo", Icons.Default.AutoAwesome),
            Triple(ROUTE_CONFIG, "Config", Icons.Default.Settings)
        ).forEach { (route, label, icon) ->
            NavigationRailItem(
                selected = currentTab == route,
                onClick = { onSelectTab(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = LunarCyan,
                    selectedTextColor = LunarPrimary,
                    indicatorColor = CosmicIndigo,
                    unselectedIconColor = OnSurfaceMuted,
                    unselectedTextColor = OnSurfaceMuted
                )
            )
        }
    }
}
