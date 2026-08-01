package com.walkroute.app.data.local.dao

import androidx.room.*
import com.walkroute.app.data.local.entity.WalkSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkSessionDao {
    
    @Query("SELECT * FROM walk_sessions ORDER BY startTime DESC")
    fun getAllWalkSessions(): Flow<List<WalkSessionEntity>>
    
    @Query("SELECT * FROM walk_sessions WHERE id = :id")
    suspend fun getWalkSessionById(id: Long): WalkSessionEntity?
    
    @Query("SELECT * FROM walk_sessions WHERE isCompleted = 1 ORDER BY startTime DESC")
    fun getCompletedWalkSessions(): Flow<List<WalkSessionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalkSession(session: WalkSessionEntity): Long
    
    @Update
    suspend fun updateWalkSession(session: WalkSessionEntity)
    
    @Delete
    suspend fun deleteWalkSession(session: WalkSessionEntity)
    
    @Query("DELETE FROM walk_sessions WHERE id = :id")
    suspend fun deleteWalkSessionById(id: Long)
    
    @Query("DELETE FROM walk_sessions")
    suspend fun deleteAllWalkSessions()
    
    @Query("SELECT COUNT(*) FROM walk_sessions WHERE isCompleted = 1")
    fun getTotalWalksCount(): Flow<Int>
    
    @Query("SELECT SUM(totalSteps) FROM walk_sessions WHERE isCompleted = 1")
    fun getTotalStepsSum(): Flow<Int?>
    
    @Query("SELECT SUM(totalDistanceMeters) FROM walk_sessions WHERE isCompleted = 1")
    fun getTotalDistanceSum(): Flow<Int?>
}
