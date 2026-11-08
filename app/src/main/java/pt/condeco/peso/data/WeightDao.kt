package pt.condeco.peso.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 1")
    fun getLatestEntry(): Flow<WeightEntry?>

    @Query("SELECT COUNT(*) FROM weight_entries WHERE date >= :since")
    suspend fun countSince(since: Long): Int

    @Query("SELECT * FROM weight_entries ORDER BY date ASC")
    suspend fun getAllOnce(): List<WeightEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WeightEntry>)

    @Delete
    suspend fun delete(entry: WeightEntry)

    @Query("DELETE FROM weight_entries")
    suspend fun deleteAll()
}
