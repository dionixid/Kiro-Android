package id.dionix.kiro.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import id.dionix.kiro.model.SurahProperties
import id.dionix.kiro.utility.runMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SurahViewModel(application: Application) : AndroidViewModel(application) {

    private val database by lazy {
        AppDatabase.initialize(application.applicationContext)
        AppDatabase.getInstance()
    }

    private val repository by lazy {
        SurahRepository(database.surahDao)
    }

    val allSurah : LiveData<List<SurahProperties>> = repository.getAllSurah().asLiveData()

    suspend fun getSize(): Int {
        return repository.size()
    }

    private val mMutableSearchResults = MutableLiveData(listOf<SurahProperties>())
    val searchResults: LiveData<List<SurahProperties>> = mMutableSearchResults

    fun filter(pattern: String) {
        if (pattern.isBlank()) {
            CoroutineScope(Dispatchers.IO).launch {
                val allSurah = allSurah.value

                runMain {
                    mMutableSearchResults.value = allSurah
                }
            }
            return
        }

        val patterns = pattern.trim().split(" ", "-").filter { it.isNotBlank() }

        CoroutineScope(Dispatchers.IO).launch {
            allSurah.value?.let { list ->
                val newSearchResults = list.filter { surah ->
                    patterns.all { token ->
                        surah.name.contains(token)
                    }
                }

                runMain {
                    mMutableSearchResults.value = newSearchResults
                }
            }
        }
    }

    suspend fun getSurahProperties(id: Int) : SurahProperties? {
        return repository.getSurahProperties(id)
    }

    fun insertAll(surahList: List<SurahProperties>) {
        viewModelScope.launch {
            repository.insertAll(surahList)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

}