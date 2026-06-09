package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val startRecoveryTimestamp: Long,
    val neuralCapitalPoints: Int = 0,
    val sosDeescalationCount: Int = 0,
    val hasActivePartnerModule: Boolean = false,
    val partnerName: String = ""
)

@Entity(tableName = "hard_effort_log")
data class HardEffortLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // PHYSICAL, COGNITIVE, SKILL
    val timestamp: Long,
    val pointsEarned: Int,
    val notes: String = ""
)

@Entity(tableName = "oxytocin_intimacy")
data class OxytocinIntimacy(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timestamp: Long,
    val completed: Boolean = false,
    val feedback: String = ""
)

@Entity(tableName = "sos_session")
data class SosSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val durationSeconds: Int = 300,
    val success: Boolean = true
)
