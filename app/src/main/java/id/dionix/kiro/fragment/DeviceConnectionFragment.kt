package id.dionix.kiro.fragment

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import id.dionix.kiro.R
import id.dionix.kiro.database.DataViewModel
import id.dionix.kiro.databinding.FragmentDeviceConnectionBinding
import id.dionix.kiro.dialog.DeviceAuthenticationDialog
import id.dionix.kiro.utility.Config
import id.dionix.kiro.utility.WiFi
import id.dionix.kiro.utility.makeTimer
import id.dionix.kiro.utility.scaleOnClick

class DeviceConnectionFragment(
    name: String,
    address: String,
    val type: ConnectionType,
    onSuccessful: () -> Unit
) : Fragment() {

    private val mName = name
    private val mAddress = address
    private val mOnSuccessful = onSuccessful

    private lateinit var mBinding: FragmentDeviceConnectionBinding

    private var mIsOpenDialog = false
    private var mAuthenticationDialog: DeviceAuthenticationDialog? = null

    private val mDataViewModel by viewModels<DataViewModel>()

    private var mIsConnecting = false
        set(value) {
            field = value
            mBinding.pbButton.visibility = if (value) View.VISIBLE else View.GONE
            mBinding.tvButton.visibility = if (value) View.GONE else View.VISIBLE
        }

    private var mIsConnected = false
        set(value) {
            field = value
            mBinding.tvButton.text = requireContext().getString(
                if (value) R.string.disconnect
                else R.string.connect
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDeviceConnectionBinding.inflate(inflater, container, false)

        mBinding.tvName.text = mName
        mBinding.tvAddress.text = mAddress

        mBinding.cvButton.scaleOnClick {
            if (mIsConnected) {
                if (type == ConnectionType.WIFI) {
                    WiFi.disconnect()
                }

                mDataViewModel.leave()
                Config.resetDevice()
                mIsConnected = false
            } else {
                if (mIsConnecting) {
                    return@scaleOnClick
                }

                when (type) {
                    ConnectionType.WIFI -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            WiFi.connect(mName, mAddress, "") { success ->
                                if (success && !mIsOpenDialog) {
                                    mIsOpenDialog = true

                                    DeviceAuthenticationDialog(
                                        onConnect = { dialog, password ->
                                            mIsConnecting = true
                                            dialog.isConnecting = true
                                            Config.updateDevice(key = password, isLocal = true)
                                            mAuthTimer.start()
                                            mDataViewModel.setServer("192.168.4.1", 80)
                                            mDataViewModel.join(password)
                                        },
                                        onDismiss = {
                                            mIsOpenDialog = false
                                        }
                                    )
                                }
                            }
                        } else {
                            if (!mIsOpenDialog) {
                                mIsOpenDialog = true

                                DeviceAuthenticationDialog(
                                    onConnect = { dialog, password ->
                                        dialog.isConnecting = true

                                        WiFi.connect(mName, mAddress, password) { success ->
                                            if (success) {
                                                mIsConnecting = true
                                                Config.updateDevice(key = password, isLocal = true)
                                                mAuthTimer.start()
                                                mDataViewModel.setServer("192.168.4.1", 80)
                                                mDataViewModel.join(password)
                                            } else {
                                                dialog.isConnecting = false
                                            }
                                        }
                                    },
                                    onDismiss = {
                                        mIsOpenDialog = false
                                    }
                                )
                            }
                        }
                    }
                    else -> {
                        if (!mIsOpenDialog) {
                            mIsOpenDialog = true

                            DeviceAuthenticationDialog(
                                onConnect = { dialog, password ->
                                    mIsConnecting = true
                                    dialog.isConnecting = true
                                    Config.updateDevice(
                                        key = password,
                                        isLocal = type != ConnectionType.INTERNET
                                    )
                                    mAuthTimer.start()
                                    mDataViewModel.setServer(mAddress, 80)
                                    mDataViewModel.join(password)
                                },
                                onDismiss = {
                                    mIsOpenDialog = false
                                }
                            )
                        }
                    }
                }

            }

        }

        mDataViewModel.isAuthenticated.observe(viewLifecycleOwner) { isAuthenticated ->
            if (!mIsConnecting) {
                return@observe
            }

            if (isAuthenticated) {
                mOnSuccessful()
            }

            mIsConnecting = false
            mAuthenticationDialog?.isConnecting = false
        }

        mBinding.ivConnectionType.setImageResource(
            when (type) {
                ConnectionType.WIFI -> R.drawable.ic_round_wifi
                ConnectionType.LAN -> R.drawable.ic_round_lan
                ConnectionType.INTERNET -> R.drawable.ic_round_language
            }
        )

        if (type == ConnectionType.INTERNET) {
            mBinding.tvAddress.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
        }

        mIsConnected = WiFi.isConnected(mName)
        return mBinding.root
    }

    private val mAuthTimer = makeTimer(10000) {
        mIsConnecting = false
        mAuthenticationDialog?.isConnecting = false
    }

    enum class ConnectionType {
        WIFI,
        LAN,
        INTERNET
    }

}