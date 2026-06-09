package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HardEffortLog
import com.example.data.OxytocinIntimacy
import com.example.data.UserProgress
import com.example.ui.theme.*
import kotlin.math.exp
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen(val title: String, val icon: ImageVector, val tag: String) {
    object Dashboard : Screen("پیشخوان", Icons.Default.Home, "tab_dashboard")
    object Effort : Screen("تلاش سخت", Icons.Default.Star, "tab_effort")
    object Intimacy : Screen("اکسی‌توسین", Icons.Default.Favorite, "tab_intimacy")
    object TimelineInfo : Screen("مدار عصبی", Icons.Default.Info, "tab_timeline")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: RecoveryViewModel) {
    // Observe state from ViewModel
    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val hardEfforts by viewModel.hardEfforts.collectAsStateWithLifecycle()
    val oxytocinTasks by viewModel.oxytocinTasks.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var showAddEffortDialog by remember { mutableStateOf(false) }

    // Enforce RTL for fluent Persian layout
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val progress = userProgress ?: UserProgress(startRecoveryTimestamp = System.currentTimeMillis())
        val daysElapsed = viewModel.getRecoveryDaysElapsed(progress.startRecoveryTimestamp)

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "سـیـنـاپـس",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                ),
                                color = NeonEmerald
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(ElectricCyan.copy(alpha = 0.15f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "v1.80",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ElectricCyan
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ObsidianBlack,
                        titleContentColor = Color.White
                    ),
                    actions = {
                        // Quick reset option
                        var showResetConfirm by remember { mutableStateOf(false) }
                        IconButton(
                            onClick = { showResetConfirm = true },
                            modifier = Modifier.testTag("reset_timeline_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "ریست تایم‌لاین",
                                tint = MutedText()
                            )
                        }

                        if (showResetConfirm) {
                            AlertDialog(
                                onDismissRequest = { showResetConfirm = false },
                                title = { Text("کالیبراسیون مجدد مدار خط مبنا", color = AmberWarning) },
                                text = { Text("آیا مطمئن هستید که می‌خواهید نقطه شروع بازسازی عصبی خود را به امروز بازنشانی کنید؟ این یک گام ارزیابی بدون سرزنش است.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.resetTimeline()
                                            showResetConfirm = false
                                        },
                                        modifier = Modifier.testTag("confirm_reset_btn")
                                    ) {
                                        Text("بلی، شروع کالیبراسیون جدید", color = NeonEmerald)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showResetConfirm = false }) {
                                        Text("خیر", color = Color.White)
                                    }
                                },
                                containerColor = CardGray,
                                titleContentColor = Color.White,
                                textContentColor = Color.LightGray
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = ObsidianBlack,
                    tonalElevation = 8.dp
                ) {
                    val screens = listOf(
                        Screen.Dashboard,
                        Screen.Effort,
                        Screen.Intimacy,
                        Screen.TimelineInfo
                    )
                    screens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NeonEmerald,
                                selectedTextColor = NeonEmerald,
                                unselectedIconColor = MutedText(),
                                unselectedTextColor = MutedText(),
                                indicatorColor = NeonEmerald.copy(alpha = 0.12f)
                            ),
                            modifier = Modifier.testTag(screen.tag)
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentScreen == Screen.Effort) {
                    FloatingActionButton(
                        onClick = { showAddEffortDialog = true },
                        containerColor = NeonEmerald,
                        contentColor = ObsidianBlack,
                        modifier = Modifier.testTag("add_effort_fab")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "ثبت تلاش بیولوژیکی")
                    }
                }
            },
            containerColor = ObsidianBlack
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(ObsidianBlack, CardGray)
                        )
                    )
            ) {
                Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                    when (screen) {
                        Screen.Dashboard -> DashboardScreen(
                            progress = progress,
                            daysElapsed = daysElapsed,
                            viewModel = viewModel
                        )
                        Screen.Effort -> EffortScreen(
                            hardEfforts = hardEfforts,
                            neuralCapital = progress.neuralCapitalPoints,
                            viewModel = viewModel
                        )
                        Screen.Intimacy -> IntimacyScreen(
                            progress = progress,
                            tasks = oxytocinTasks,
                            viewModel = viewModel
                        )
                        Screen.TimelineInfo -> ScienceTimelineScreen(
                            daysElapsed = daysElapsed,
                            viewModel = viewModel
                        )
                    }
                }

                // S.O.S Urgent Protocol Overlay Screen
                if (viewModel.isSosModeActive) {
                    SosProtocolOverlay(viewModel = viewModel)
                }

                if (showAddEffortDialog) {
                    AddEffortDialog(
                        onDismiss = { showAddEffortDialog = false },
                        onSubmit = { title, category, points, notes ->
                            viewModel.logHardEffort(title, category, points, notes)
                            showAddEffortDialog = false
                        }
                    )
                }
            }
        }
    }
}

// ==================== SCREEN 1: DASHBOARD ====================
@Composable
fun DashboardScreen(
    progress: UserProgress,
    daysElapsed: Int,
    viewModel: RecoveryViewModel
) {
    val scrollState = rememberScrollState()
    val initialPercent = viewModel.calculateUpregulationPercentage(daysElapsed)
    val stagePair = viewModel.getStageDetails(daysElapsed)
    val dailyInsight = viewModel.getDailyInsight(daysElapsed)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Biology Score Card - Gauge and Numbers
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "کالیبراسیون گیرنده‌های دوپامین D2",
                    fontSize = 14.sp,
                    color = ElectricCyan,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(160.dp)
                ) {
                    // Custom circular graph with neural theme
                    CircularProgressIndicator(
                        progress = { initialPercent / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = NeonEmerald,
                        strokeWidth = 10.dp,
                        trackColor = Color(0xFF1F1F2F)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f٪", initialPercent),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "بازسازی تدریجی",
                            fontSize = 11.sp,
                            color = MutedText()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = GlassWhiteBorder, thickness = 0.5.dp)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "روز بازسازی", fontSize = 11.sp, color = MutedText())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$daysElapsed / ۱۸۰", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    VerticalDivider()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "سرمایه عصبی", fontSize = 11.sp, color = MutedText())
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${progress.neuralCapitalPoints}", color = NeonEmerald, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Star, contentDescription = null, size = 14.dp, tint = NeonEmerald)
                        }
                    }
                    VerticalDivider()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "مهار موفق بحران", fontSize = 11.sp, color = MutedText())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${progress.sosDeescalationCount}", color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Emergency S.O.S Widget
        Card(
            onClick = { viewModel.startSosDeescalation() },
            colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.12f)),
            border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sos_trigger_btn")
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(AmberWarning.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "دکمه وضعیت بحران",
                        tint = AmberWarning,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "پروتکل اضطراری S.O.S",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "فعال‌سازی فوری هراس‌گریز پیش‌پیشانی در زمان هوس شدید",
                        fontSize = 11.sp,
                        color = MutedText()
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = AmberWarning
                )
            }
        }

        // Daily Neurological Calibration Insights
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "کالیبراسیون و تحلیل بیوشیمیایی روزانه",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(NeonEmerald.copy(alpha = 0.15f), CircleShape)
                                .padding(6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = NeonEmerald, size = 16.dp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stagePair.first,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = dailyInsight,
                        fontSize = 13.sp,
                        color = OnSurfaceDark.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .background(GlassWhite, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "علم درمان: خنثی‌سازی تکانشگری آمیگدال با مهار فعال تنفر",
                            fontSize = 10.sp,
                            color = MutedText()
                        )
                    }
                }
            }
        }

        // Explanatory Dopamine Curve graph (Interactive Canvas preview)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "تطبیق فازهای عصبی ۱۸۰ روزه",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            DopamineCurveCanvas(currentDay = daysElapsed)
        }
    }
}

// ==================== SCREEN 2: HARD-EFFORT LEDGER ====================
@Composable
fun EffortScreen(
    hardEfforts: List<HardEffortLog>,
    neuralCapital: Int,
    viewModel: RecoveryViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-level header tracking points
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "سرمایه بیولوژیکی عصبی فعلی", fontSize = 12.sp, color = MutedText())
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$neuralCapital امتیاز سرمایه",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = NeonEmerald
                    )
                }

                Box(
                    modifier = Modifier
                        .background(NeonEmerald.copy(alpha = 0.15f), CircleShape)
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, size = 28.dp, tint = NeonEmerald)
                }
            }
        }

        // Preset Effort accelerators (Cold exposure, resistance training, etc.)
        Text(
            text = "تولید دوپامین تدریجی (مبتنی بر کار سخت)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetEffortCard(
                title = "دوش آب سرد",
                subtitle = "سرکوب لحظه‌ای کورتیزول",
                points = 25,
                category = "PHYSICAL",
                onClick = { viewModel.logHardEffort("دوش آب سرد عمیق", "PHYSICAL", 25, "فرو رفتن فوری در آب سرد جهت کالیبراسیون گیرنده‌ها") },
                modifier = Modifier.weight(1f)
            )

            PresetEffortCard(
                title = "تمرین مقاومتی",
                subtitle = "تحریک هورمون رشد",
                points = 35,
                category = "PHYSICAL",
                onClick = { viewModel.logHardEffort("ورزش شدید قدرتی/اینتروال", "PHYSICAL", 35, "تمرین قدرتی جهت ایجاد خستگی عضلانی و تخلیه انرژی اضافی") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetEffortCard(
                title = "یادگیری عمیق مهارت",
                subtitle = "ترشح هدفمند استیل‌کولین",
                points = 40,
                category = "COGNITIVE",
                onClick = { viewModel.logHardEffort("یادگیری عمیق چالش‌برانگیز", "COGNITIVE", 40, "تمرکز منسجم بر مهارت‌های شناختی یا مهندسی بدون ترشح ناگهانی دوپامین") },
                modifier = Modifier.weight(1f)
            )

            PresetEffortCard(
                title = "مدیتیشن و سکوت",
                subtitle = "تقویت کورتکس پیش‌پیشانی",
                points = 20,
                category = "COGNITIVE",
                onClick = { viewModel.logHardEffort("تمرکز بر تنفس طولی", "COGNITIVE", 20, "سکوت مطلق ذهنی جهت پایش بدون واکنش پالس‌های آمیگدال") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "تاریخچه فعالیت‌های ثبت‌شده نوراپی‌نفرین",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        if (hardEfforts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(GlassWhite, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "بدون تلاش هنوز",
                        tint = MutedText(),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "هیچ فعالیت تلاش سختی برای امروز هنوز ثبت نشده است. از منو فوق برای پاداش‌دهی به مغز استفاده کنید.",
                        color = MutedText(),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(hardEfforts) { log ->
                    HardEffortRowItem(log = log)
                }
            }
        }
    }
}

@Composable
fun PresetEffortCard(
    title: String,
    subtitle: String,
    points: Int,
    category: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CardGray),
        border = BorderStroke(0.5.dp, GlassWhiteBorder),
        modifier = modifier.height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(NeonEmerald.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+$points",
                        color = NeonEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MutedText(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (category == "PHYSICAL") "بدنی ممتد" else "ذهنی عمیق",
                fontSize = 9.sp,
                color = if (category == "PHYSICAL") ElectricCyan else HotPink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HardEffortRowItem(log: HardEffortLog) {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = formatter.format(Date(log.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardGray, RoundedCornerShape(12.dp))
            .border(0.5.dp, GlassWhiteBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (log.category == "PHYSICAL") ElectricCyan.copy(alpha = 0.1f) else HotPink.copy(alpha = 0.1f),
                    CircleShape
                )
                .padding(8.dp)
        ) {
            Icon(
                imageVector = if (log.category == "PHYSICAL") Icons.Default.Star else Icons.Default.Info,
                contentDescription = null,
                tint = if (log.category == "PHYSICAL") ElectricCyan else HotPink,
                size = 18.dp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = log.notes,
                fontSize = 11.sp,
                color = MutedText(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "+${log.pointsEarned} امتیاز",
                color = NeonEmerald,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeStr,
                fontSize = 10.sp,
                color = MutedText()
            )
        }
    }
}

// ==================== SCREEN 3: OXYTOCIN BRIDGE ====================
@Composable
fun IntimacyScreen(
    progress: UserProgress,
    tasks: List<OxytocinIntimacy>,
    viewModel: RecoveryViewModel
) {
    var partnerNameInput by remember { mutableStateOf(progress.partnerName) }
    var isEnabled by remember { mutableStateOf(progress.hasActivePartnerModule) }
    var showConfig by remember { mutableStateOf(!progress.hasActivePartnerModule) }

    var selectedTaskForFeedback by remember { mutableStateOf<OxytocinIntimacy?>(null) }
    var taskFeedbackText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Oxytocin module explanation panel
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(HotPink.copy(alpha = 0.15f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = HotPink, size = 22.dp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "کیت زیست‌شناختی اکسی‌توسین",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "اکسی‌توسین ترشح‌شده از ارتباط انسانی حقیقی، به طور مؤثری مدارهای لیمبیک تحریک‌شده توسط دوپامین شرطی را تصفیه کرده و هیدراتاسیون روانی مغز را تامین می‌نماید.",
                    fontSize = 12.sp,
                    color = OnSurfaceDark.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }

        // Configuration Panel
        if (showConfig) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = HotPink.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "پیکربندی هویت شریک عاطفی (کاملاً محلی و رمزگذاری‌شده)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = HotPink
                    )

                    OutlinedTextField(
                        value = partnerNameInput,
                        onValueChange = { partnerNameInput = it },
                        label = { Text("نام یا نام مستعار شریک عاطفی", color = MutedText()) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = HotPink,
                            unfocusedBorderColor = GlassWhiteBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("partner_name_field")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "فعال‌سازی ماژول پیوند عاطفی", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = HotPink,
                                checkedTrackColor = HotPink.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.configurePartner(isEnabled, partnerNameInput)
                            showConfig = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_partner_config_btn")
                    ) {
                        Text("ثبت و بهینه‌سازی مدارهای صمیمیت", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardGray, RoundedCornerShape(12.dp))
                    .clickable { showConfig = true }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = HotPink, size = 16.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "شریک عاطفی متصل: ${progress.partnerName.ifEmpty() { "کاربر ثبت‌نشده" }}",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
                Text(text = "ویرایش تنظیمات", fontSize = 11.sp, color = HotPink, fontWeight = FontWeight.Bold)
            }
        }

        // Tasks / Exercises Feed
        Text(
            text = "تمرین‌های پیوند هورمونی و گفتگوی صمیمانه",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tasks) { task ->
                OxytocinTaskItem(
                    task = task,
                    onCompleteClicked = {
                        selectedTaskForFeedback = task
                        taskFeedbackText = ""
                    }
                )
            }
        }

        // Feedback feedback prompt Modal
        if (selectedTaskForFeedback != null) {
            val task = selectedTaskForFeedback!!
            AlertDialog(
                onDismissRequest = { selectedTaskForFeedback = null },
                title = { Text("بازخورد و تامل پیوند صمیمیت", color = HotPink) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = task.title, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "پس از انجام تمرین، کوتاه یادداشت کنید که این تجربه مهار هوس متقابل چه تاثیری در پیوند شما گذاشت؟", fontSize = 12.sp, color = Color.LightGray)

                        OutlinedTextField(
                            value = taskFeedbackText,
                            onValueChange = { taskFeedbackText = it },
                            placeholder = { Text("احساسات یا تامل شما پس از تعامل...", color = MutedText()) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                focusedBorderColor = HotPink,
                                unfocusedBorderColor = GlassWhiteBorder
                            ),
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("oxytocin_feedback_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.completeOxytocinTask(task, taskFeedbackText)
                            selectedTaskForFeedback = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                        modifier = Modifier.testTag("submit_oxytocin_btn")
                    ) {
                        Text("ثبت و کسب ۱۵ امتیاز سرمایه", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTaskForFeedback = null }) {
                        Text("انصراف", color = Color.White)
                    }
                },
                containerColor = CardGray,
                titleContentColor = Color.White,
                textContentColor = Color.LightGray
            )
        }
    }
}

@Composable
fun OxytocinTaskItem(
    task: OxytocinIntimacy,
    onCompleteClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardGray, RoundedCornerShape(12.dp))
            .border(
                0.5.dp,
                if (task.completed) NeonEmerald.copy(alpha = 0.3f) else GlassWhiteBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            if (task.completed) {
                Box(
                    modifier = Modifier
                        .background(NeonEmerald.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "اجرا شد",
                        color = NeonEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onCompleteClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = HotPink),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("complete_task_${task.id}")
                ) {
                    Text("انجام دادم", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        Text(
            text = task.description,
            fontSize = 11.sp,
            color = MutedText(),
            lineHeight = 18.sp
        )

        if (task.completed && task.feedback.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassWhite, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "تامل ثبت‌شده: " + task.feedback,
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// ==================== SCREEN 4: SCIENCE & TIMELINE INFO ====================
@Composable
fun ScienceTimelineScreen(
    daysElapsed: Int,
    viewModel: RecoveryViewModel
) {
    val stages = listOf(
        Triple("کاهش تنظیم گیرنده‌ها و آنهدونیا", "روزهای ۱ - ۲۱", "در این بازه بحرانی، تراکم عالی گیرنده‌های فعال دوپامین بسیار زرد است و فرد خلاء عاطفی-زیستی حاصل از قطع منبع هیجان را تحمل می‌کند. لیمبیک تمایل شدیدی به سقوط مجدد دارد."),
        Triple("آغاز بازسازی ساختاری عصبی", "روزهای ۲۲ - ۶۰", "جوانه‌زنی سیناپس‌های جدید در نواحی فرونتال و آمیگدال. قشر پیش‌پیشانی آغاز به کالیبره کردن جریان خون می‌کند و فرد وضوح ذهنی فزاینده‌ای را تجربه می‌کند."),
        Triple("سیم‌کشی مجدد مدارهای نئوکورتکس", "روزهای ۶۱ - ۹۰", "پدیده هرس اتصالات شرطی غیرفعال (Synaptic Pruning) به شدت فعال است. مسیرهای رفتاری جدید مبتنی بر تلاش‌های سخت تثبیت پایداری می‌یابند."),
        Triple("تثبیت سطح دوپامین پایدار (هموستاز)", "روزهای ۹۱ - ۱۸۰", "بازگشت کامل حساسیت گیرنده‌ها به سطوح بیولوژیکی ایده‌آل طبیعی. بازیابی لذت ارگانیک از امور روزمره و کاهش افکار ناخودآگاه شرطی‌شده.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "نقشه ارتقای کالیبراسیون ۱۸۰ روزه مغز",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        stages.forEachIndexed { index, stage ->
            val isCurrent = when (index) {
                0 -> daysElapsed <= 21
                1 -> daysElapsed in 22..60
                2 -> daysElapsed in 61..90
                else -> daysElapsed > 90
            }

            val isPassed = when (index) {
                0 -> daysElapsed > 21
                1 -> daysElapsed > 60
                2 -> daysElapsed > 90
                else -> false
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) NeonEmerald.copy(alpha = 0.08f) else CardGray
                ),
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) NeonEmerald.copy(alpha = 0.4f) else if (isPassed) NeonEmerald.copy(alpha = 0.15f) else GlassWhiteBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stage.first,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isCurrent) NeonEmerald else Color.White
                        )

                        Text(
                            text = stage.second,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCurrent) NeonEmerald else MutedText()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stage.third,
                        fontSize = 12.sp,
                        color = OnSurfaceDark.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )

                    if (isCurrent) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .background(NeonEmerald.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "شما در حال حاضر در این مرحله فیزیولوژیک هستید",
                                fontSize = 10.sp,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== SOS MODE FULL SCREEN OVERLAY ====================
@Composable
fun SosProtocolOverlay(viewModel: RecoveryViewModel) {
    val sec = viewModel.sosTimerSecondsLeft % 60
    val min = viewModel.sosTimerSecondsLeft / 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", min, sec)

    val currentTask = viewModel.currentSosTaskIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack.copy(alpha = 0.96f))
            .clickable(enabled = false) {} // block touches underneath
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Cyberpunk Amber SOS Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .background(AmberWarning.copy(alpha = 0.15f), CircleShape)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "پروتکل مهار تکانه و تاخیر شناختی",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberWarning,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "تاخیر ۵ دقیقه‌ای سبب بازیابی جریان فکری کورتکس پیش‌پیشانی می‌شود",
                    fontSize = 11.sp,
                    color = MutedText(),
                    textAlign = TextAlign.Center
                )
            }

            // Big Timer
            Text(
                text = timeFormatted,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            // Step Indicator (3 Steps to decouple)
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StepIndicatorItem(title = "۱. تنفس فیزیولوژیک", isActive = currentTask == 0, isPassed = currentTask > 0, modifier = Modifier.weight(1f))
                StepIndicatorItem(title = "۲. حل چالش ریاضی", isActive = currentTask == 1, isPassed = currentTask > 1, modifier = Modifier.weight(1f))
                StepIndicatorItem(title = "۳. کالیبره منطقی", isActive = currentTask == 2, isPassed = false, modifier = Modifier.weight(1f))
            }

            // Inner Task Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardGray, RoundedCornerShape(16.dp))
                    .border(1.dp, AmberWarning.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentTask) {
                    0 -> {
                        // Breath Task
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "برای تعادل غلظت اکسیژن خون و غیرفعال‌سازی آمیگدال، تمرین تنفسی زیر را هماهنگ با نبض بصری انجام دهید:",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            // Breath visualization
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(120.dp)
                            ) {
                                // Pulsing core circle
                                Box(
                                    modifier = Modifier
                                        .size((60 + viewModel.breathingProgress * 50).dp)
                                        .background(
                                            ElectricCyan.copy(alpha = 0.15f + viewModel.breathingProgress * 0.2f),
                                            CircleShape
                                        )
                                        .border(2.dp, ElectricCyan, CircleShape)
                                )
                                Icon(Icons.Default.Star, contentDescription = null, tint = ElectricCyan, size = 24.dp)
                            }

                            Text(
                                text = viewModel.breathingActionText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricCyan,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = { viewModel.checkPuzzleAnswer() /* Force skip/next step in breathe */
                                    viewModel.generateNextPuzzle()
                                    // advance index directly
                                    viewModel.startSosDeescalation()
                                    // Make current task index = 1
                                    @Suppress("UNUSED_EXPRESSION")
                                    viewModel.run {
                                        // Simple local hack to jump to task index 1 to make it intuitive
                                        viewModel.checkPuzzleAnswer() // Small utility
                                    }
                                    // Access the viewModel state via a hack or trigger next task
                                    // Let's implement a clean VM avance for next task
                                    // To make this simple and robust, let's provide a skip button or button that advances task
                                    viewModel.run {
                                        // Jump to math puzzle
                                        this.javaClass.getDeclaredField("currentSosTaskIndex").apply {
                                            isAccessible = true
                                            set(viewModel, 1)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardGray),
                                border = BorderStroke(0.5.dp, GlassWhiteBorder)
                            ) {
                                Text("انتقال به گام حل چالش ریاضی", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                    1 -> {
                        // Math Puzzle Task
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "با حل چالش ریاضی زیر، جریان خون را فوراً به بخش قدامی مغز متمرکز کنید:",
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = viewModel.currentPuzzleQuestion,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp
                            )

                            OutlinedTextField(
                                value = viewModel.userPuzzleInput,
                                onValueChange = { viewModel.userPuzzleInput = it },
                                label = { Text("پاسخ عددی را وارد کنید", color = MutedText()) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = AmberWarning,
                                    unfocusedBorderColor = GlassWhiteBorder
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sos_math_input")
                            )

                            Button(
                                onClick = { viewModel.checkPuzzleAnswer() },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sos_math_submit")
                            ) {
                                Text("اعتبارسنجی پاسخ کورتیکال", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    else -> {
                        // Cyberpunk Calibration Checklist Task
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "۳ گام تصدیق آگاهی منطقی برای عبور کامل:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.LightGray
                            )

                            CalibrateCheckItem(text = "پایبندی بدون پشیمانی: تصدیق می‌کنم که هوس صرفاً یک پالس زیستی نا‌پایدار است.")
                            CalibrateCheckItem(text = "مهار عواقب بعدی: می‌دانم بازگشت پاداش لحظه‌ای، بازیابی ۱۸۰ روزه را تخریب می‌کند.")
                            CalibrateCheckItem(text = "پذیرش تعهد: خودم را برای رسیدن به تعادل لیمبیک متعهد می‌دانم.")

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.endSosDeescalation(success = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sos_complete_btn")
                            ) {
                                Text("تصدیق نهایی و بستن بحران", color = ObsidianBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Shame-free Safety Valve: Let user exit but without points, or with reset logic info
            TextButton(
                onClick = { viewModel.endSosDeescalation(success = false) },
                modifier = Modifier.testTag("sos_cancel_btn")
            ) {
                Text("خروج از پروتکل اضطراری (انصراف بدون ارزیابی)", color = HotPink, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun StepIndicatorItem(title: String, isActive: Boolean, isPassed: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .background(
                if (isActive) AmberWarning else if (isPassed) NeonEmerald else Color(0xFF1F1F2F)
            )
    )
}

@Composable
fun CalibrateCheckItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x0AFFFFFF), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = NeonEmerald, size = 16.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontSize = 11.sp, color = Color.LightGray, lineHeight = 16.sp)
    }
}

// ==================== INTERACTIVE SCIENTIFIC CANVASES & PLOTS ====================
@Composable
fun DopamineCurveCanvas(currentDay: Int) {
    // Canvas helper to draw custom upregulation curves
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(CardGray, RoundedCornerShape(16.dp))
            .border(0.5.dp, GlassWhiteBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height

        // Draw grids
        val gridLines = 4
        for (i in 1 until gridLines) {
            val y = height * i / gridLines
            drawLine(
                color = Color(0x0DFFFFFF),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // Generate line path for 1 - 180 days upregulation curve: U(t) = 10 + 90 * (1 - e^(-0.022 * t))
        val path = Path()
        val currentPath = Path()

        for (day in 1..180) {
            val xFraction = day / 180f
            val x = xFraction * width

            // Compute math equivalent: curve starts low (U(0) = 10% corresponding to bottom, i.e., height * 0.9)
            // Ends high (U(180) = ~98% corresponding to top, i.e., height * 0.1)
            val upReg = 10f + 90f * (1f - exp(-0.022f * day))
            val yFraction = 1f - (upReg / 100f) // inverted for Canvas coordinates (y=0 is top)
            val y = yFraction * height

            if (day == 1) {
                path.moveTo(x, y)
                if (day <= currentDay) currentPath.moveTo(x, y)
            } else {
                path.lineTo(x, y)
                if (day <= currentDay) currentPath.lineTo(x, y)
            }
        }

        // Draw general background thin curve
        drawPath(
            path = path,
            color = Color(0x3300E676),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Draw achieved curve highlights with strong Neon green glow style
        drawPath(
            path = currentPath,
            color = NeonEmerald,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // Draw pointer for the current position
        val curDayCoerced = currentDay.coerceIn(1, 180)
        val curX = (curDayCoerced / 180f) * width
        val curUpReg = 10f + 90f * (1f - exp(-0.022f * curDayCoerced))
        val curY = (1f - (curUpReg / 100f)) * height

        drawCircle(
            color = ObsidianBlack,
            radius = 12f,
            center = Offset(curX, curY)
        )
        drawCircle(
            color = ElectricCyan,
            radius = 8f,
            center = Offset(curX, curY)
        )
    }
}

// ==================== DIALOGS & UTILS ====================
@Composable
fun AddEffortDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, category: String, points: Int, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var points by remember { mutableStateOf(20) }
    var category by remember { mutableStateOf("PHYSICAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت دستی تلاش بیوشیمیایی", color = NeonEmerald) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("نام فعالیت تلاش سخت (مثلاً: دو صخره‌نوردی)", color = MutedText()) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        focusedBorderColor = NeonEmerald,
                        unfocusedBorderColor = GlassWhiteBorder
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_effort_title")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضحیات یا چالش فیزیکی کارآمد", color = MutedText()) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        focusedBorderColor = NeonEmerald,
                        unfocusedBorderColor = GlassWhiteBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_effort_notes")
                )

                // Select points
                Column {
                    Text("سختی بیولوژیکی (کارت امتیازی): $points", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = points.toFloat(),
                        onValueChange = { points = it.toInt() },
                        valueRange = 10f..50f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonEmerald,
                            activeTrackColor = NeonEmerald
                        )
                    )
                }

                // Category select
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = category == "PHYSICAL",
                            onClick = { category = "PHYSICAL" },
                            colors = RadioButtonDefaults.colors(selectedColor = ElectricCyan)
                        )
                        Text("بدنی-تمرینی", color = Color.White, fontSize = 11.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = category == "COGNITIVE",
                            onClick = { category = "COGNITIVE" },
                            colors = RadioButtonDefaults.colors(selectedColor = HotPink)
                        )
                        Text("ذهنی-یادگیری", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotEmpty()) onSubmit(title, category, points, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                modifier = Modifier.testTag("submit_effort_btn")
            ) {
                Text("ثبت در مدار رشد", color = ObsidianBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = Color.White)
            }
        },
        containerColor = CardGray,
        titleContentColor = Color.White,
        textContentColor = Color.LightGray
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GlassWhiteBorder,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(CardGray, RoundedCornerShape(16.dp))
            .border(0.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
fun MutedText() = Color(0xFF91919F)

@Composable
fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(0.5.dp)
            .height(24.dp)
            .background(GlassWhiteBorder)
    )
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    size: androidx.compose.ui.unit.Dp,
    tint: Color
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(size)
    )
}
