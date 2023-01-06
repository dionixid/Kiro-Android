package id.dionix.kiro.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import id.dionix.kiro.model.SurahProperties
import kotlinx.coroutines.flow.Flow

@Dao
interface SurahDao {

    @Query("SELECT * FROM surah")
    fun getAll(): Flow<List<SurahProperties>>

    @Query("SELECT COUNT(id) FROM surah")
    suspend fun size(): Int

    @Query("SELECT * FROM surah WHERE id = :id LIMIT 1")
    suspend fun getSurahProperties(id: Int): SurahProperties?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg surahProperties: SurahProperties)

    @Query("DELETE FROM surah")
    suspend fun deleteAll()

}