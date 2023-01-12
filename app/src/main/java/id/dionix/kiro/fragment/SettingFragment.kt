package id.dionix.kiro.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import id.dionix.kiro.R
import id.dionix.kiro.adapter.SettingAdapter
import id.dionix.kiro.database.DataViewModel
import id.dionix.kiro.databinding.FragmentSettingBinding
import id.dionix.kiro.dialog.*
import id.dionix.kiro.model.Notification
import id.dionix.kiro.model.Setting
import id.dionix.kiro.model.SettingGroup
import id.dionix.kiro.utility.*

class SettingFragment : Fragment() {

    private lateinit var mBinding: FragmentSettingBinding

    private var settingGroups = listOf<SettingGroup>()
    private var mIsOpenDialog = false
    private var mIsWaitingResponse = false

    private val mDataViewModel by activityViewModels<DataViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSettingBinding.inflate(inflater, container, false)

        mBinding.tvTitle.apply {
            val insets = WindowInsetsCompat
                .toWindowInsetsCompat(requireActivity().window.decorView.rootWindowInsets)
                .getInsets(WindowInsetsCompat.Type.statusBars())

            val params = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = 20.dip + insets.top
            }
            layoutParams = params
        }

        val settingAdapter = SettingAdapter { setting ->
            when (setting.type) {
                Setting.Type.String,
                Setting.Type.Float,
                Setting.Type.Integer -> {
                    val group = settingGroups.find { group ->
                        group.settings.find { it.id == setting.id } != null
                    } ?: return@SettingAdapter

                    if (!mIsOpenDialog) {
                        mIsOpenDialog = true

                        ValueDialog(
                            group.name,
                            setting,
                            onSave = { newValue ->
                                mIsWaitingResponse = true
                                mResponseTimer.start()
                                mDataViewModel.sendSettingGroup(
                                    group.copy(
                                        settings = listOf(
                                            newValue
                                        )
                                    )
                                )
                            },
                            onDismiss = {
                                mIsOpenDialog = false
                            }
                        ).show(requireActivity().supportFragmentManager, "dialog_location")
                    }
                }
                Setting.Type.Time,
                Setting.Type.Date -> {
                    val dateTimeGroup = settingGroups.find { group ->
                        group.settings.find { it.id == setting.id } != null
                    } ?: return@SettingAdapter

                    val time = dateTimeGroup.settings.find {
                        it.type == Setting.Type.Time
                    } ?: return@SettingAdapter

                    val date = dateTimeGroup.settings.find {
                        it.type == Setting.Type.Date
                    } ?: return@SettingAdapter

                    if (!mIsOpenDialog) {
                        mIsOpenDialog = true

                        DateTimeDialog(
                            time,
                            date,
                            onSave = { newTime, newDate ->
                                mIsWaitingResponse = true
                                mResponseTimer.start()
                                mDataViewModel.sendSettingGroup(
                                    dateTimeGroup.copy(settings = listOf(newTime, newDate))
                                )
                            },
                            onDismiss = {
                                mIsOpenDialog = false
                            }
                        ).show(requireActivity().supportFragmentManager, "dialog_location")
                    }
                }
                Setting.Type.WiFi -> {
                    val wifiGroup = settingGroups.find { group ->
                        group.settings.find { it.id == setting.id } != null
                    } ?: return@SettingAdapter

                    val ssid = wifiGroup.settings.find {
                        it.type == Setting.Type.WiFi && !it.isConfidential
                    } ?: return@SettingAdapter

                    val password = wifiGroup.settings.find {
                        it.type == Setting.Type.WiFi && it.isConfidential
                    } ?: return@SettingAdapter

                    if (!mIsOpenDialog) {
                        mIsOpenDialog = true

                        WiFiDialog(
                            ssid,
                            password,
                            onSave = { newSsid, newPassword ->
                                mIsWaitingResponse = true
                                mResponseTimer.start()
                                mDataViewModel.sendSettingGroup(
                                    wifiGroup.copy(settings = listOf(newSsid, newPassword))
                                )
                            },
                            onDismiss = {
                                mIsOpenDialog = false
                            }
                        ).show(requireActivity().supportFragmentManager, "dialog_wifi")
                    }
                }
                Setting.Type.Latitude,
                Setting.Type.Longitude,
                Setting.Type.Elevation -> {
                    val locationGroup = settingGroups.find { group ->
                        group.settings.find { it.id == setting.id } != null
                    } ?: return@SettingAdapter

                    val latitude = locationGroup.settings.find {
                        it.type == Setting.Type.Latitude
                    } ?: return@SettingAdapter

                    val longitude = locationGroup.settings.find {
                        it.type == Setting.Type.Longitude
                    } ?: return@SettingAdapter

                    val elevation = locationGroup.settings.find {
                        it.type == Setting.Type.Elevation
                    } ?: return@SettingAdapter

                    if (!mIsOpenDialog) {
                        mIsOpenDialog = true

                        LocationDialog(
                            latitude,
                            longitude,
                            elevation,
                            onSave = { newLatitude, newLongitude, newElevation ->
                                mIsWaitingResponse = true
                                mResponseTimer.start()
                                mDataViewModel.sendSettingGroup(
                                    locationGroup.copy(
                                        settings = listOf(
                                            newLatitude,
                                            newLongitude,
                                            newElevation
                                        )
                                    )
                                )
                            },
                            onDismiss = {
                                mIsOpenDialog = false
                            }
                        ).show(requireActivity().supportFragmentManager, "dialog_location")
                    }
                }
                else -> {
                    // Do nothing
                }
            }
        }

        mDataViewModel.device.observe(viewLifecycleOwner) {
            it?.let { device ->
                settingAdapter.setDevice(device)
            }

            mBinding.recyclerView.visibility = if (it == null) View.GONE else View.VISIBLE
            mBinding.llDeviceNotConnected.visibility = if (it == null) View.VISIBLE else View.GONE
        }

        mBinding.cvFindDevice.scaleOnClick {
            if (!mIsOpenDialog) {
                mIsOpenDialog = true
                DeviceDialog {
                    mIsOpenDialog = false
                }.show(requireActivity().supportFragmentManager, "dialog_device")
            }
        }

        mDataViewModel.settingGroups.observe(viewLifecycleOwner) {
            settingGroups = it
            settingAdapter.setSettingGroups(it)

            if (mIsWaitingResponse) {
                mIsWaitingResponse = false
                mResponseTimer.cancel()
                mDataViewModel.setNotification(
                    Notification(getString(R.string.data_has_been_saved_successfully))
                )
            }
        }

        mDataViewModel.isConnected.observe(viewLifecycleOwner) {
            settingAdapter.setAction(
                SettingAdapter.Action(
                    tag = "connection",
                    label = getString(if (it) R.string.disconnect else R.string.connect),
                    color = requireContext().getColor(if (it) R.color.red else R.color.secondary)
                ) {
                    if (it) {
                        Config.resetDevice()
                        mDataViewModel.leave()
                        WiFi.disconnect()
                    } else {
                        mDataViewModel.rejoin()
                    }
                }
            )
        }

        mBinding.recyclerView.apply {
            adapter = settingAdapter
            itemAnimator = null
            addOnScrollListener(object : OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    when {
                        dy > 10.dip && mBinding.cvHeader.cardElevation == 0f -> {
                            mBinding.cvHeader.cardElevation = 4.dp
                        }
                        dy < 2.dip && mBinding.cvHeader.cardElevation != 0f -> {
                            mBinding.cvHeader.cardElevation = 0f
                        }
                    }
                }
            })
        }

        return mBinding.root
    }

    private val mResponseTimer = makeTimer(5000) {
        mIsWaitingResponse = false
        mDataViewModel.setNotification(Notification(
            getString(R.string.cannot_connect_to_device),
            true
        ))
    }

}