package id.dionix.kiro.database

import androidx.annotation.WorkerThread
import id.dionix.kiro.model.SurahProperties
import kotlinx.coroutines.flow.Flow

class SurahRepository(
    private val dao: SurahDao
) {

    fun getAllSurah() : Flow<List<SurahProperties>> {
        return dao.getAll()
    }

    suspend fun size() : Int {
        return dao.size()
    }

    suspend fun getSurahProperties(id: Int): SurahProperties? {
        return dao.getSurahProperties(id)
    }

    @WorkerThread
    suspend fun insertAll(surahList: List<SurahProperties>) {
        dao.insertAll(*surahList.toTypedArray())
    }

    @WorkerThread
    suspend fun deleteAll() {
        dao.deleteAll()
    }
}