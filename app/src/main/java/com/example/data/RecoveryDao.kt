package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecoveryDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressDirect(): UserProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UserProgress)

    @Query("SELECT * FROM hard_effort_log ORDER BY timestamp DESC")
    fun getAllHardEfforts(): Flow<List<HardEffortLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHardEffort(log: HardEffortLog)

    @Query("SELECT * FROM oxytocin_intimacy ORDER BY timestamp DESC")
    fun getAllOxytocinTasks(): Flow<List<OxytocinIntimacy>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOxytocinTask(task: OxytocinIntimacy)

    @Update
    suspend fun updateOxytocinTask(task: OxytocinIntimacy)

    @Query("SELECT * FROM sos_session ORDER BY timestamp DESC")
    fun getAllSosSessions(): Flow<List<SosSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSosSession(session: SosSession)
}
