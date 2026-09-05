package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FootballDao {
    // Master Clips
    @Query("SELECT * FROM master_clips ORDER BY createdAt DESC")
    fun getAllMasterClips(): Flow<List<MasterClip>>

    @Query("SELECT * FROM master_clips WHERE id = :id LIMIT 1")
    suspend fun getMasterClipById(id: Long): MasterClip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasterClip(clip: MasterClip): Long

    @Query("DELETE FROM master_clips WHERE id = :id")
    suspend fun deleteMasterClip(id: Long)

    // Moments
    @Query("SELECT * FROM moment_candidates WHERE masterClipId = :masterClipId ORDER BY startSec ASC")
    fun getMomentsForClip(masterClipId: Long): Flow<List<MomentCandidate>>

    @Query("SELECT * FROM moment_candidates ORDER BY priorityScore DESC")
    fun getAllMoments(): Flow<List<MomentCandidate>>

    @Query("SELECT * FROM moment_candidates WHERE id = :id LIMIT 1")
    suspend fun getMomentById(id: Long): MomentCandidate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(moment: MomentCandidate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoments(moments: List<MomentCandidate>)

    @Update
    suspend fun updateMoment(moment: MomentCandidate)

    @Query("UPDATE moment_candidates SET status = :status WHERE id = :id")
    suspend fun updateMomentStatus(id: Long, status: String)

    @Query("DELETE FROM moment_candidates WHERE id = :id")
    suspend fun deleteMoment(id: Long)

    // Render Jobs
    @Query("SELECT * FROM clip_render_jobs ORDER BY createdAt DESC")
    fun getAllRenderJobs(): Flow<List<ClipRenderJob>>

    @Query("SELECT * FROM clip_render_jobs WHERE id = :id LIMIT 1")
    suspend fun getRenderJobById(id: Long): ClipRenderJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRenderJob(job: ClipRenderJob): Long

    @Update
    suspend fun updateRenderJob(job: ClipRenderJob)

    @Query("UPDATE clip_render_jobs SET progress = :progress WHERE id = :id")
    suspend fun updateRenderJobProgress(id: Long, progress: Int)

    @Query("UPDATE clip_render_jobs SET status = :status WHERE id = :id")
    suspend fun updateRenderJobStatus(id: Long, status: String)

    @Query("DELETE FROM clip_render_jobs WHERE id = :id")
    suspend fun deleteRenderJob(id: Long)

    // Schedule Templates
    @Query("SELECT * FROM schedule_templates ORDER BY id ASC")
    fun getAllScheduleTemplates(): Flow<List<ScheduleTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleTemplate(template: ScheduleTemplate): Long

    @Update
    suspend fun updateScheduleTemplate(template: ScheduleTemplate)

    @Query("DELETE FROM schedule_templates WHERE id = :id")
    suspend fun deleteScheduleTemplate(id: Long)

    // Social Post Records
    @Query("SELECT * FROM social_post_records ORDER BY timestamp DESC")
    fun getAllPostRecords(): Flow<List<SocialPostRecord>>

    @Query("SELECT * FROM social_post_records WHERE momentId = :momentId ORDER BY timestamp DESC")
    fun getPostRecordsForMoment(momentId: Long): Flow<List<SocialPostRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostRecord(record: SocialPostRecord): Long

    @Query("DELETE FROM social_post_records WHERE id = :id")
    suspend fun deletePostRecord(id: Long)
}
