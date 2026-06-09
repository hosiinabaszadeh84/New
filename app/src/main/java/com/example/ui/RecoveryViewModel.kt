package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HardEffortLog
import com.example.data.OxytocinIntimacy
import com.example.data.RecoveryRepository
import com.example.data.SosSession
import com.example.data.UserProgress
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.exp

class RecoveryViewModel(
    application: Application,
    private val repository: RecoveryRepository
) : AndroidViewModel(application) {

    // Exposure of core datalogs from repository
    val userProgress: StateFlow<UserProgress?> = repository.userProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val hardEfforts: StateFlow<List<HardEffortLog>> = repository.hardEfforts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val oxytocinTasks: StateFlow<List<OxytocinIntimacy>> = repository.oxytocinTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sosSessions: StateFlow<List<SosSession>> = repository.sosSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // SOS Mode State
    var isSosModeActive by mutableStateOf(false)
        private set

    var sosTimerSecondsLeft by mutableStateOf(300) // 5 minutes
        private set

    // Cognitive Friction Task States
    var currentSosTaskIndex by mutableStateOf(0) // 0 to 2 for 3 phase task
        private set

    // Logic puzzle prompt & answer
    var currentPuzzleQuestion by mutableStateOf("")
    var currentPuzzleAnswer by mutableStateOf("")
    var userPuzzleInput by mutableStateOf("")
    var isPuzzleSolved by mutableStateOf(false)

    // Breathing phase state
    var breathingActionText by mutableStateOf("دم عمیق (۴ ثانیه)") // Farsi guided breathing
    var breathingProgress by mutableStateOf(0f)

    private var sosTimerJob: Job? = null
    private var breathingJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getOrCreateProgress()
            repository.prepopulateOxytocinTasksIfEmpty()
        }
    }

    // Math function to compute recovery days elapsed
    fun getRecoveryDaysElapsed(startTimestamp: Long): Int {
        val diffMs = System.currentTimeMillis() - startTimestamp
        // Minimum day 1
        return ((diffMs / (1000 * 60 * 60 * 24)).toInt() + 1).coerceAtLeast(1)
    }

    // Formula to compute Upregulation level percentage: 10% (initial) up to 100% after 180 days
    fun calculateUpregulationPercentage(days: Int): Float {
        // Logarithmic or negative exponential saturation curve: U(t) = 10 + 90 * (1 - e^(-0.02 * days))
        val exponent = -0.022 * days
        val percent = 10f + 90f * (1f - exp(exponent).toFloat())
        return percent.coerceIn(10f, 100f)
    }

    // Get stage details in Persian based on day number
    fun getStageDetails(days: Int): Pair<String, String> {
        return when {
            days <= 21 -> Pair(
                "کاهش تنظیم گیرنده‌ها و آنهدونیا",
                "روزهای ۱ تا ۲۱: فاز اول سم‌زدایی زیستی. در این مرحله مغز به علت نبود محرک شدید قبلی با مقاومت و کمبود دوپامین شدید مبارزه می‌کند تا تعادل گیرنده‌ها مجدداً آغاز شود."
            )
            days <= 60 -> Pair(
                "آغاز بازسازی عصبی",
                "روزهای ۲۲ تا ۶۰: فاز جوانه‌زنی اتصالات سیناپسی جدید. کورتکس پیش‌پیشانی به تدریج توانایی خونسازی طبیعی و مدیریت پالس‌های عصبی ارادی را به دست می‌آورد."
            )
            days <= 90 -> Pair(
                "سیم‌کشی مجدد مدارهای فرونتال",
                "روزهای ۶۱ تا ۹۰: بازگشت جریان خون کورتیکال به مقادیر بهینه طبیعی. هرس شدن سیناپس مهارکننده‌های شرطی‌شده آسیب‌رسان سرعت بیشتری می‌پذیرد."
            )
            else -> Pair(
                "تثبیت سطح دوپامین پایدار",
                "روزهای ۹۱ تا ۱۸۰: تعادل کامل سیستم لیمبیک با قشر خاکستری مغز. ترشح دوپامین از محرک‌های ساده روزمره دوباره لذت‌بخش شده و به وضعیت کالیبره‌شده بازیابی می‌گردد."
            )
        }
    }

    // Dynamic scientific text for the current recovery day
    fun getDailyInsight(days: Int): String {
        return when (days) {
            in 1..3 -> "امروز مغز شما در بهت ناشی از قطع فوران دوپامین بیرونی است. این احساس بی‌پناهی یا خستگی موقت، صرفاً پیامد فیزیولوژیک کاهش تنظیم (Downregulation) گیرنده‌هاست و نشانه‌ خرابی مغز نیست."
            in 4..7 -> "مقاومت شما در فاز اولیه سبب مهار اثر کولیدج (تنوع‌طلبی افراطی صادر از مدار لیمبیک) می‌شود. کورتکس کماکان در وضعیت انقباض خونی است؛ آب‌رسانی و تنفس عمیق را شدت ببخشید."
            in 8..14 -> "پدیده تحمل (Tolerance) به تدریج رو به افول می‌رود. مدارهای مغزی شما تلاش می‌کنند ولتاژ پایینی از پاداش طبیعی را درک کنند. تداوم پایداری در این هفته حیاتی‌ترین خشت بازسازی است."
            in 15..21 -> "آنهدونیا یا کرختی در حوالی روز بیستم به اوج می‌رسد، زیرا گیرنده‌های قدیمی ناامید شده و آماده فروپاشی برای بازسازی نهایی هستند. این همان پیچ حساس بازگشت حساسیت عصبی است."
            in 22..30 -> "تبریک علمی! کورتکس پیش‌پیشانی مجدداً شروع به بازیابی کنترل اجرایی خود کرده است. مدارهای گاباانرژیک بازدارنده در حال جوانه‌زدن در بخش خاکستری مغز هستند تا رفتارهای تکانشی را مهار کنند."
            in 31..45 -> "مغز با ترشح تدریجی استیل‌کولین و اندورفین در اثر سخت‌کوشی‌های بدنی، روند لذت واقعی را بازمعماری می‌کند. اثر پاداش فوری جایش را به لذت از تلاش‌های عمیق فیزیکی می‌دهد."
            in 46..60 -> "کاهش مداوم فانتزی‌های خودکار ناشی از وقوع پدیده هرس سیناپسی (Synaptic Pruning) است. مسیرهای ارتباط عصبی انحرافی مرتبط با اعتیاد به علت عدم استفاده طولانی تضعیف می‌شوند."
            in 61..75 -> "نرخ جریان اکسیژن‌رسانی به کورتکس فرونتال به ۸۰٪ کارایی طبیعی خود رسیده است. دیگر افکار محرک فاقد لنگرگاه‌های قدرتمند عاطفی سابق در کورتکس شما هستند."
            in 76..90 -> "مکالمات واقعی و فعالیت‌های مبتنی بر اکسی‌توسین در مهار استرس‌های ثانویه بسیار کارآمدتر از قبل عمل می‌کنند. مدارهای عاطفی شما اکنون صمیمیت انسانی را ارزشمندتر از تصاویر مجازی می‌یابند."
            in 91..120 -> "تعادل هموستاتیک ناقل‌های عصبی در حال نهایی شدن است. سطح کورتیکواستروئیدها (هورمون استرس مزمن) افت کرده و سیستم لیمبیک بار دیگر با کالیبراسیون طبیعی دوپامین همساز شده است."
            else -> "مبارزه نوروپلاستیسیته تکمیل شد. بازسازی قشر پیش‌پیشانی به ثبات تثبیت‌شده صعود کرده است. حال شما از تمرکز پایدار، اراده پولادین علمی و ارتعاشات شادابی طبیعی زندگی بهره می‌برید."
        }
    }

    // Trigger SOS mode
    fun startSosDeescalation() {
        isSosModeActive = true
        sosTimerSecondsLeft = 300
        currentSosTaskIndex = 0
        isPuzzleSolved = false
        userPuzzleInput = ""
        generateNextPuzzle()
        startSosTimer()
        startBreathingAnimation()
    }

    fun endSosDeescalation(success: Boolean) {
        viewModelScope.launch {
            repository.registerSosDeescalation(success)
            isSosModeActive = false
            sosTimerJob?.cancel()
            breathingJob?.cancel()
        }
    }

    private fun startSosTimer() {
        sosTimerJob?.cancel()
        sosTimerJob = viewModelScope.launch {
            while (sosTimerSecondsLeft > 0) {
                delay(1000)
                sosTimerSecondsLeft--
            }
            // If countdown finishes, they successfully delayed and decoupled!
            endSosDeescalation(success = true)
        }
    }

    private fun startBreathingAnimation() {
        breathingJob?.cancel()
        breathingJob = viewModelScope.launch {
            while (isSosModeActive) {
                // Phase 1: Inhale (4s)
                breathingActionText = "دم عمیق شکمی (۴ ثانیه)"
                var elapsed = 0f
                while (elapsed < 4000) {
                    delay(50)
                    elapsed += 50
                    breathingProgress = (elapsed / 4000f).coerceIn(0f, 1f)
                }
                // Phase 2: Hold (۴s)
                breathingActionText = "حبس نفس در سینه (۴ ثانیه)"
                elapsed = 0f
                while (elapsed < 4000) {
                    delay(50)
                    elapsed += 50
                    breathingProgress = (1f - (elapsed / 4000f)).coerceIn(0f, 1f)
                }
                // Phase 3: Exhale (۶s) - Physiological Sigh
                breathingActionText = "بازدم آرام با صدای آه (۶ ثانیه)"
                elapsed = 0f
                while (elapsed < 6000) {
                    delay(50)
                    elapsed += 50
                    breathingProgress = (elapsed / 6000f).coerceIn(0f, 1f)
                }
            }
        }
    }

    fun generateNextPuzzle() {
        isPuzzleSolved = false
        userPuzzleInput = ""
        val puzzles = listOf(
            Pair("حاصل عبارت علمی را بیابید:\n 17 + 29 - 8 = ؟", "38"),
            Pair("عدد مجهول سری مقابل چیست؟\n 3, 7, 15, 31, ؟", "63"),
            Pair("حاصلضرب دو فاکتور پایدار روبرو را محاسبه کنید:\n 12 × 13 = ؟", "156"),
            Pair("کدام عدد الگوی ریاضی را کامل می‌کند؟\n 2, 6, 12, 20, 30, ؟", "42"),
            Pair("تفریق منطقی را حل کنید:\n 144 - 36 - 15 = ؟", "93")
        )
        val selected = puzzles.random()
        currentPuzzleQuestion = selected.first
        currentPuzzleAnswer = selected.second
    }

    fun checkPuzzleAnswer() {
        if (userPuzzleInput.trim() == currentPuzzleAnswer) {
            isPuzzleSolved = true
            // Advance task or reward
            viewModelScope.launch {
                repository.addPoints(10) // Small analytical reward
                if (currentSosTaskIndex < 2) {
                    currentSosTaskIndex++
                    generateNextPuzzle()
                } else {
                    // All tasks completed, successfully bypassed urge!
                    endSosDeescalation(success = true)
                }
            }
        }
    }

    // Hard-effort actions
    fun logHardEffort(title: String, category: String, points: Int, notes: String) {
        viewModelScope.launch {
            repository.logHardEffort(title, category, points, notes)
        }
    }

    // Oxytocin actions
    fun completeOxytocinTask(task: OxytocinIntimacy, feedback: String) {
        viewModelScope.launch {
            repository.completeOxytocinTask(task, feedback)
        }
    }

    fun configurePartner(enabled: Boolean, partnerName: String) {
        viewModelScope.launch {
            repository.updatePartnerConfig(enabled, partnerName)
        }
    }

    fun resetTimeline() {
        viewModelScope.launch {
            repository.resetRecovery()
        }
    }
}

class RecoveryViewModelFactory(
    private val application: Application,
    private val repository: RecoveryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecoveryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecoveryViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
