package id.dionix.kiro.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import id.dionix.kiro.adapter.DeviceAdapter
import id.dionix.kiro.adapter.DeviceConnectionAdapter
import id.dionix.kiro.databinding.DialogDeviceBinding
import id.dionix.kiro.model.DeviceConnection
import id.dionix.kiro.utility.*

class DeviceDialog(
    onDismiss: () -> Unit = {}
) : AppCompatDialogFragment() {

    private val mOnDismiss = onDismiss

    private lateinit var mBinding: DialogDeviceBinding

    private val devices = mutableListOf<DeviceConnection>()
    private var currentDevice = DeviceConnection()

    private var isRefreshing = false
        set(value) {
            field = value
            mBinding.pbRefresh.visibility = if (value) View.VISIBLE else View.GONE
            mBinding.ivRefresh.visibility = if (value) View.GONE else View.VISIBLE
        }

    private val deviceAdapter = DeviceAdapter {
        currentDevice = it
        mBinding.vpConnection.deviceConnectionAdapter.update(it)
        mBinding.llDevice.visibility = View.GONE
        mBinding.llConnection.visibility = View.VISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        UDP.rebind()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(300.dip, 400.dip)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mOnDismiss()
        UDP.detach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogDeviceBinding.inflate(inflater, container, false)

        mBinding.llDevice.visibility = View.VISIBLE
        mBinding.llConnection.visibility = View.GONE

        mBinding.rvDevice.apply {
            setHasFixedSize(true)
            adapter = deviceAdapter
        }

        val mediator =
            TabLayoutMediator(mBinding.tlConnection, mBinding.vpConnection) { tab, position ->
                tab.text = mBinding.vpConnection.deviceConnectionAdapter.getTabTitles()[position]
            }

        mBinding.vpConnection.apply {
            adapter = DeviceConnectionAdapter(
                requireActivity(),
                onTabsUpdated = {
                    if (it.size > 1) {
                        mBinding.tlConnection.visibility = View.VISIBLE
                        mediator.detach()
                        mediator.attach()
                    } else {
                        mBinding.tlConnection.visibility = View.GONE
                    }
                },
                onSuccessful = {
                    dismiss()
                }
            )

            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        mBinding.cvRefresh.scaleOnClick {
            if (!isRefreshing) {
                isRefreshing = true
                devices.clear()
                scanWiFi()
                scanLocal()
            }
        }

        isRefreshing = true
        scanWiFi()

        return mBinding.root
    }

    private fun updateDevices() {
        if (devices.isEmpty()) {
            mBinding.rvDevice.visibility = View.GONE
            mBinding.llNoDevice.visibility = View.VISIBLE
        } else {
            mBinding.rvDevice.visibility = View.VISIBLE
            mBinding.llNoDevice.visibility = View.GONE
            deviceAdapter.setDevices(devices)
        }
        isRefreshing = false
    }

    private fun scanWiFi() {
        WiFi.scan { results ->
            runMain {
                results.map { scanResult ->
                    val ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        scanResult.wifiSsid.toString()
                    } else {
                        @Suppress("DEPRECATION")
                        scanResult.SSID
                    }

                    val device = devices.find { it.name == ssid }

                    if (device == null) {
                        devices.add(
                            DeviceConnection(
                                id = scanResult.BSSID,
                                name = ssid,
                                mac = scanResult.BSSID,
                                isWifi = true
                            )
                        )
                    } else {
                        device.mac = scanResult.BSSID
                        device.isWifi = true
                    }
                }
                updateDevices()
            }
        }
    }

    private fun scanLocal() {
        UDP.scan { name: String, ip: String ->
            runMain {
                val device = devices.find { it.name == name }

                if (device == null) {
                    devices.add(
                        DeviceConnection(
                            name = name,
                            ip = ip,
                            isLan = true
                        )
                    )
                } else {
                    device.ip = ip
                    device.isLan = true
                }
                updateDevices()
            }
        }
    }

    private val ViewPager2.deviceConnectionAdapter: DeviceConnectionAdapter get() = adapter as DeviceConnectionAdapter

}