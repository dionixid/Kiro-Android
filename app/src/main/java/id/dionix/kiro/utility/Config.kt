package id.dionix.kiro.utility

import android.content.Context
import com.codedillo.tinydb.TinyDB
import id.dionix.kiro.model.AppConfig
import id.dionix.kiro.model.DeviceConfig
import id.dionix.kiro.model.SurahCollection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object Config {
    var device: DeviceConfig = DeviceConfig()
        private set

    var app: AppConfig = AppConfig()
        private set

    var collection: SurahCollection = SurahCollection()
        private set

    fun updateDevice(
        id: String = device.id,
        ip: String = device.ip,
        name: String = device.name,
        isLocal: Boolean = device.isLocal,
        key: String = device.key
    ) {
        device = DeviceConfig(id, ip, name, isLocal, key)

        CoroutineScope(Dispatchers.IO).launch {
            TinyDB.getInstance().put(KEY_DEVICE, device)
        }
    }

    fun resetDevice() {
        device.apply {
            isLocal = false
            name = ""
            key = ""
            id = ""
            ip = ""
        }

        CoroutineScope(Dispatchers.IO).launch {
            TinyDB.getInstance().put(KEY_DEVICE, device)
        }
    }

    fun loadDevice(onReady: (device: DeviceConfig) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            device =
                TinyDB.getInstance().get(KEY_DEVICE, DeviceConfig::class.java) ?: DeviceConfig()
            runMain {
                onReady(device)
            }
        }
    }

    fun loadApp(context: Context, onReady: (app: AppConfig) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val config = TinyDB.getInstance().get(KEY_APP, AppConfig::class.java)

            if (config == null) {
                app = AppConfig(getDeviceName(context), UUID.randomUUID().toString())
                CoroutineScope(Dispatchers.IO).launch {
                    TinyDB.getInstance().put(KEY_APP, app)
                }
            }

            runMain {
                onReady(app)
            }
        }
    }

    fun updateCollection(
        name: String = collection.name,
        total: Int = collection.totalSize,
        progress: Int = collection.progress
    ) {
        collection = SurahCollection(name, total, progress)

        CoroutineScope(Dispatchers.IO).launch {
            TinyDB.getInstance().put(KEY_COLLECTION, collection)
        }
    }

    fun loadCollection(onReady: (collection: SurahCollection) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            collection = TinyDB.getInstance().get(KEY_COLLECTION, SurahCollection::class.java) ?: SurahCollection()
            runMain {
                onReady(collection)
            }
        }
    }

    private const val KEY_DEVICE = "device"
    private const val KEY_APP = "app"
    private const val KEY_COLLECTION = "collection"
}