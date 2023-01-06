package id.dionix.kiro.database

import android.app.Application
import androidx.lifecycle.*
import com.codedillo.rttp.model.Message
import com.codedillo.rttp.model.Value
import com.codedillo.tinydb.TinyDB
import id.dionix.kiro.model.*
import id.dionix.kiro.utility.Config
import id.dionix.kiro.utility.parseDate
import id.dionix.kiro.utility.runMain
import id.dionix.kiro.utility.secondsToTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class DataViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DataRepository()

    private val mMutableTime = MutableLiveData(LocalTime.now())
    val time: LiveData<LocalTime> get() = mMutableTime

    private val mMutableDate = MutableLiveData(LocalDate.now())
    val date: LiveData<LocalDate> get() = mMutableDate

    private val mMutableDevice = MutableLiveData<Device?>(null)
    val device: LiveData<Device?> get() = mMutableDevice

    private val mMutablePrayerGroup = MutableLiveData(PrayerGroup())
    val prayerGroup: LiveData<PrayerGroup> get() = mMutablePrayerGroup

    private val mMutablePrayerOngoing = MutableLiveData(Prayer())
    val prayerOngoing: LiveData<Prayer> = mMutablePrayerOngoing

    private val mMutableQiroGroups = MutableLiveData(listOf<QiroGroup>())
    val qiroGroups: LiveData<List<QiroGroup>> = mMutableQiroGroups

    private val mMutableQiroOngoing = MutableLiveData(Qiro())
    val qiroOngoing: LiveData<Qiro> = mMutableQiroOngoing

    private val mMutableSurahOngoing = MutableLiveData(SurahAudio())
    val surahOngoing: LiveData<SurahAudio> get() = mMutableSurahOngoing

    private val mMutableSurahPreview = MutableLiveData<SurahAudio?>(null)
    val surahPreview: LiveData<SurahAudio?> = mMutableSurahPreview

    private val mMutableSettingGroups = MutableLiveData(listOf<SettingGroup>())
    val settingGroups: LiveData<List<SettingGroup>> = mMutableSettingGroups

    private val mMutableSurahCollection = MutableLiveData(SurahCollection())
    val surahCollection: LiveData<SurahCollection> = mMutableSurahCollection

    private val mMutableLastSurahSearch = MutableLiveData(listOf<SurahProperties>())
    val lastSurahSearch: LiveData<List<SurahProperties>> = mMutableLastSurahSearch

    private val mMutableIsUpdatingSurahCollection = MutableLiveData(false)
    val isUpdatingSurahCollection: LiveData<Boolean> = mMutableIsUpdatingSurahCollection

    private val mMutableNotification = MutableLiveData<Notification?>(null)
    val notification: LiveData<Notification?> = mMutableNotification

    val isConnected = repository.isConnected
    val isAuthenticated = repository.isAuthenticated

    fun initialize() {
        CoroutineScope(Dispatchers.IO).launch {
            val list = TinyDB.getInstance().getList(KEY_SURAH_SEARCH, SurahProperties::class.java)
            runMain {
                mMutableLastSurahSearch.value = list
            }
        }
    }

    fun sendPrayerTimeOffset(prayerTimeOffset: PrayerTimeOffset) {
        repository.send(TOPIC_PRAYER_OFFSET, Message.Action.Set, Value(prayerTimeOffset))
    }

    fun sendQiroGroup(qiroGroup: QiroGroup) {
        repository.send(
            when (qiroGroup.dayOfWeek) {
                DayOfWeek.MONDAY -> TOPIC_QIRO_MONDAY
                DayOfWeek.TUESDAY -> TOPIC_QIRO_TUESDAY
                DayOfWeek.WEDNESDAY -> TOPIC_QIRO_WEDNESDAY
                DayOfWeek.THURSDAY -> TOPIC_QIRO_THURSDAY
                DayOfWeek.FRIDAY -> TOPIC_QIRO_FRIDAY
                DayOfWeek.SATURDAY -> TOPIC_QIRO_SATURDAY
                else -> TOPIC_QIRO_SUNDAY
            },
            Message.Action.Set,
            Value(qiroGroup)
        )
    }

    fun sendSettingGroup(settingGroup: SettingGroup) {
        repository.send(TOPIC_SETTING_GROUP, Message.Action.Set, Value(settingGroup))
    }

    fun sendSurahPreview(surah: SurahAudio) {
        repository.send(TOPIC_SURAH_PREVIEW, Message.Action.Set, Value(surah))
    }

    fun sendSurahForceStopCommand() {
        repository.send(TOPIC_SURAH_FORCE_STOP, Message.Action.Set, Value(true))
    }

    fun setNotification(notification: Notification?) {
        mMutableNotification.value = notification
    }

    fun fetchSurahList() {
        if (isUpdatingSurahCollection.value == true) {
            return
        }

        mIsPendingSurahCollection = true
        repository.send(TOPIC_SURAH_COLLECTION, Message.Action.Get, Value(0))
    }

    fun setSurahCollection(collection: SurahCollection) {
        mMutableSurahCollection.value = collection
    }

    fun addSurahSearchResult(surahProperties: SurahProperties) {
        var list = mMutableLastSurahSearch.value?.toMutableList() ?: mutableListOf()
        list.add(0, surahProperties)

        if (list.size > 10) {
            list = list.subList(0, 10)
        }

        CoroutineScope(Dispatchers.IO).launch {
            TinyDB.getInstance().putListObject(KEY_SURAH_SEARCH, list)
        }

        runMain {
            mMutableLastSurahSearch.value = list
        }
    }

    fun join(password: String) {
        repository.join(password)
    }

    fun leave() {
        repository.leave()
        mMutableDevice.value = null
    }

    fun setClientID(name: String, id: String) {
        repository.setClientID(name, id)
    }

    fun setServer(host: String, port: Int) {
        repository.setServer(host, port)
    }

    fun attachToLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(repository)
    }

    init {
        repository.onTopic(TOPIC_DEVICE) {
            if (it.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            val device = it.payload.toObject { Device() }
            if (device.isValid) {
                mMutableDevice.value = device
            } else {
                mMutableDevice.value = null
            }
        }

        repository.onTopic(TOPIC_PRAYER_GROUP) {
            if (it.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            val group = it.payload.toObject { PrayerGroup() }
            if (group.isValid) {
                runMain {
                    mMutablePrayerGroup.value = group
                }
            }
        }

        repository.onTopic(TOPIC_PRAYER_ONGOING) {
            if (it.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            val prayer = it.payload.toObject { Prayer() }
            if (prayer.isValid) {
                runMain {
                    mMutablePrayerOngoing.value = prayer
                }
            }
        }

        repository.onTopic(TOPIC_QIRO_SUNDAY, ::onQiroGroup)
        repository.onTopic(TOPIC_QIRO_MONDAY, ::onQiroGroup)
        repository.onTopic(TOPIC_QIRO_TUESDAY, ::onQiroGroup)
        repository.onTopic(TOPIC_QIRO_WEDNESDAY, ::onQiroGroup)
        repository.onTopic(TOPIC_QIRO_THURSDAY, ::onQiroGroup)
        repository.onTopic(TOPIC_QIRO_FRIDAY, ::onQiroGroup)
        repository.onTopic(TOPIC_QIRO_SATURDAY, ::onQiroGroup)

        repository.onTopic(TOPIC_QIRO_ONGOING) {
            if (it.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            val qiro = it.payload.toObject { Qiro() }
            if (qiro.isValid) {
                runMain {
                    mMutableQiroOngoing.value = qiro
                }
            }
        }

        repository.onTopic(TOPIC_SETTING_GROUP) { message ->
            if (message.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            val group = message.payload.toObject { SettingGroup() }
            if (group.isValid) {
                val groups = mMutableSettingGroups.value?.toMutableList() ?: return@onTopic
                val idx = groups.indexOfFirst { it.name == group.name }
                if (idx != -1) {
                    groups[idx] = group
                    runMain {
                        mMutableSettingGroups.value = groups
                    }
                }
            }
        }

        repository.onTopic(TOPIC_SETTING_ALL) { message ->
            if (message.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            if (!message.payload.isArray()) {
                return@onTopic
            }

            val settings = message.payload.toList { it.toObject { SettingGroup() } }
            settings.forEach {
                if (!it.isValid) {
                    return@onTopic
                }
            }

            runMain {
                mMutableSettingGroups.value = settings
                settings.forEach {
                    val newTime = it.getSetting(Setting.Type.Time) ?: return@runMain
                    val newDate = it.getSetting(Setting.Type.Date) ?: return@runMain

                    mMutableTime.value = newTime.value.toInt().secondsToTime()
                    mMutableDate.value = newDate.value.toString().parseDate("dd-MM-yyyy")
                }
            }
        }

        repository.onTopic(TOPIC_SURAH_PREVIEW) {
            if (it.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            val audio = it.payload.toObject { SurahAudio() }
            if (audio.isValid) {
                runMain {
                    mMutableSurahPreview.value = audio
                }
            } else {
                mMutableSurahPreview.value = null
            }
        }

        repository.onTopic(TOPIC_SURAH_ONGOING) {
            if (it.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            val surah = it.payload.toObject { SurahAudio() }
            if (surah.isValid) {
                runMain {
                    mMutableSurahOngoing.value = surah
                }
            }
        }

        repository.onTopic(TOPIC_SURAH_COLLECTION) {
            if (it.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            if (!it.payload.isString()) {
                return@onTopic
            }

            runMain {
                mMutableSurahCollection.value = mMutableSurahCollection.value?.copy(name = it.payload.toString())
            }

            if (mIsPendingSurahCollection) {
                mIsPendingSurahCollection = false
                mIsPendingSurahListSize = true
                repository.send(TOPIC_SURAH_LIST_SIZE, Message.Action.Get, Value(0))
            }
        }

        repository.onTopic(TOPIC_SURAH_LIST_SIZE) {
            if (it.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            if (!it.payload.isNumber()) {
                return@onTopic
            }

            runMain {
                mMutableSurahCollection.value = mMutableSurahCollection.value?.copy(
                    total = it.payload.toInt(),
                    progress = 0
                )
            }

            if (mIsPendingSurahListSize) {
                mIsPendingSurahListSize = false
                mMutableIsUpdatingSurahCollection.value = true

                repository.send(TOPIC_SURAH_LIST, Message.Action.Get, Value(0))

                mMutableSurahCollection.value?.let { collection ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (collection != Config.collection) {
                            Config.updateCollection(collection.name, collection.total, collection.progress)
                            AppDatabase.getInstance().surahDao.deleteAll()
                            repository.send(TOPIC_SURAH_LIST, Message.Action.Get, Value(0))
                        }
                    }
                }
            }
        }

        repository.onTopic(TOPIC_SURAH_LIST) { message ->
            if (message.senderId != Message.SERVER_ID) {
                return@onTopic
            }

            if (!message.payload.isArray()) {
                return@onTopic
            }

            val surahList = message.payload.toList { it.toObject { SurahProperties() } }
            surahList.forEach {
                if (!it.isValid) {
                    return@onTopic
                }
            }

            viewModelScope.launch {
                AppDatabase.getInstance().surahDao.insertAll(*surahList.toTypedArray())
            }

            runMain {
                val surahCollection = mMutableSurahCollection.value ?: SurahCollection()
                mMutableSurahCollection.value = surahCollection.copy(progress = surahCollection.progress + surahList.size)

                if (mMutableSurahCollection.value?.isFinished == true) {
                    mMutableIsUpdatingSurahCollection.value = false
                }
            }
        }
    }

    private fun onQiroGroup(message: Message) {
        if (message.senderId != Message.SERVER_ID) {
            return
        }

        val group = message.payload.toObject { QiroGroup() }
        if (group.isValid) {
            runMain {
                val groups = mMutableQiroGroups.value?.toMutableList()
                if (groups == null) {
                    mMutableQiroGroups.value = listOf(group)
                } else {
                    val idx = groups.indexOfFirst { it.dayOfWeek == group.dayOfWeek }
                    if (idx == -1) {
                        groups.add(group)
                    } else {
                        groups[idx] = group
                    }

                    groups.sortBy { it.dayOfWeek }
                    mMutableQiroGroups.value = groups
                }
            }
        }
    }

    private var mIsPendingSurahCollection = false
    private var mIsPendingSurahListSize = false

    companion object {
        private const val TOPIC_DEVICE = "device"
        private const val TOPIC_PRAYER_GROUP = "prayer-group"
        private const val TOPIC_PRAYER_OFFSET = "prayer-offset"
        private const val TOPIC_PRAYER_ONGOING = "prayer-ongoing"
        private const val TOPIC_QIRO_SUNDAY = "qiro-sunday"
        private const val TOPIC_QIRO_MONDAY = "qiro-monday"
        private const val TOPIC_QIRO_TUESDAY = "qiro-tuesday"
        private const val TOPIC_QIRO_WEDNESDAY = "qiro-wednesday"
        private const val TOPIC_QIRO_THURSDAY = "qiro-thursday"
        private const val TOPIC_QIRO_FRIDAY = "qiro-friday"
        private const val TOPIC_QIRO_SATURDAY = "qiro-saturday"
        private const val TOPIC_QIRO_ONGOING = "qiro-ongoing"
        private const val TOPIC_SETTING_GROUP = "setting-group"
        private const val TOPIC_SETTING_ALL = "setting-all"
        private const val TOPIC_SURAH_LIST = "surah-list"
        private const val TOPIC_SURAH_LIST_SIZE = "surah-list-size"
        private const val TOPIC_SURAH_COLLECTION = "surah-collection"
        private const val TOPIC_SURAH_PREVIEW = "surah-preview"
        private const val TOPIC_SURAH_ONGOING = "surah-ongoing"
        private const val TOPIC_SURAH_FORCE_STOP = "surah-force-stop"

        private const val KEY_SURAH_SEARCH = "surah_search"
    }

}