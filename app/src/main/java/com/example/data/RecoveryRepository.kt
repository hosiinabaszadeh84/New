package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class RecoveryRepository(private val dao: RecoveryDao) {

    val userProgress: Flow<UserProgress?> = dao.getUserProgress()
    val hardEfforts: Flow<List<HardEffortLog>> = dao.getAllHardEfforts()
    val oxytocinTasks: Flow<List<OxytocinIntimacy>> = dao.getAllOxytocinTasks()
    val sosSessions: Flow<List<SosSession>> = dao.getAllSosSessions()

    suspend fun getOrCreateProgress(): UserProgress {
        val existing = dao.getUserProgressDirect()
        if (existing != null) return existing
        
        // Setup initial default progress
        val defaultProgress = UserProgress(
            startRecoveryTimestamp = System.currentTimeMillis(),
            neuralCapitalPoints = 50, // Initial calibration bonus
            sosDeescalationCount = 0
        )
        dao.insertProgress(defaultProgress)
        return defaultProgress
    }

    suspend fun resetRecovery() {
        val current = dao.getUserProgressDirect() ?: UserProgress(startRecoveryTimestamp = System.currentTimeMillis())
        val resetProgress = current.copy(
            startRecoveryTimestamp = System.currentTimeMillis()
        )
        dao.insertProgress(resetProgress)
    }

    suspend fun addPoints(points: Int) {
        val current = dao.getUserProgressDirect() ?: getOrCreateProgress()
        val updated = current.copy(
            neuralCapitalPoints = current.neuralCapitalPoints + points
        )
        dao.insertProgress(updated)
    }

    suspend fun logHardEffort(title: String, category: String, points: Int, notes: String) {
        val log = HardEffortLog(
            title = title,
            category = category,
            timestamp = System.currentTimeMillis(),
            pointsEarned = points,
            notes = notes
        )
        dao.insertHardEffort(log)
        addPoints(points)
    }

    suspend fun completeOxytocinTask(task: OxytocinIntimacy, feedback: String) {
        val updated = task.copy(
            completed = true,
            feedback = feedback,
            timestamp = System.currentTimeMillis()
        )
        dao.updateOxytocinTask(updated)
        addPoints(15) // Reward 15 neural points for oxytocin connection activity
    }

    suspend fun registerSosDeescalation(success: Boolean) {
        val session = SosSession(
            timestamp = System.currentTimeMillis(),
            durationSeconds = 300,
            success = success
        )
        dao.insertSosSession(session)
        if (success) {
            val current = dao.getUserProgressDirect() ?: getOrCreateProgress()
            val updated = current.copy(
                sosDeescalationCount = current.sosDeescalationCount + 1,
                neuralCapitalPoints = current.neuralCapitalPoints + 30 // High-grade recovery reward
            )
            dao.insertProgress(updated)
        }
    }

    suspend fun prepopulateOxytocinTasksIfEmpty() {
        val currentTasks = dao.getAllOxytocinTasks().firstOrNull()
        if (currentTasks.isNullOrEmpty()) {
            val initial = listOf(
                OxytocinIntimacy(
                    title = "مکالمه ۱۰ دقیقه‌ای بدون واسطه دیجیتال",
                    description = "مکالمه عمیق درباره اهداف و احساسات طرفین بدون نگاه کردن به هیچ صفحه نمایش یا تلفن همراه.",
                    timestamp = System.currentTimeMillis()
                ),
                OxytocinIntimacy(
                    title = "به اشتراک‌گذاری یک آسیب‌پذیری کوچک",
                    description = "با شریک عاطفی خود صمیمانه درباره یکی از ترس‌ها یا چالش‌های ذهنی امروز خود بدون هراس صحبت کنید.",
                    timestamp = System.currentTimeMillis()
                ),
                OxytocinIntimacy(
                    title = "تنفس هماهنگ ۵ دقیقه‌ای",
                    description = "۵ دقیقه کنار یکدیگر نشسته و آگاهانه نفس‌های عمیق خود را با هم هماهنگ کنید (کاهش ضربان قلب و تنظیم کورتیزول).",
                    timestamp = System.currentTimeMillis()
                ),
                OxytocinIntimacy(
                    title = "یادداشت قدردانی بی‌پیرایه",
                    description = "یک رفتار خاص یا ویژگی اخلاقی شریکتان را که امروز واقعاً مورد احترام شما بود، روی کاغذ یا پیامک بنویسید.",
                    timestamp = System.currentTimeMillis()
                ),
                OxytocinIntimacy(
                    title = "لمس همدلانه و آغوش طولانی (۲۰ ثانیه)",
                    description = "آغوش پیوسته به مدت حداقل ۲۰ ثانیه که به لحاظ فیزیولوژیک سبب ترشح فوری اکسی‌توسین و مهار مدار آمیگدال می‌شود.",
                    timestamp = System.currentTimeMillis()
                )
            )
            for (task in initial) {
                dao.insertOxytocinTask(task)
            }
        }
    }
    
    suspend fun updatePartnerConfig(enabled: Boolean, partnerName: String) {
        val current = dao.getUserProgressDirect() ?: getOrCreateProgress()
        val updated = current.copy(
            hasActivePartnerModule = enabled,
            partnerName = partnerName
        )
        dao.insertProgress(updated)
    }
}
