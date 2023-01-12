package id.dionix.kiro.fragment

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import id.dionix.kiro.R
import id.dionix.kiro.database.DataViewModel
import id.dionix.kiro.databinding.FragmentDeviceConnectionBinding
import id.dionix.kiro.dialog.DeviceAuthenticationDialog
import id.dionix.kiro.model.Notification
import id.dionix.kiro.utility.*

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

    private val mDataViewModel by activityViewModels<DataViewModel>()

    private var mPassword = ""

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
                        if (!mIsOpenDialog) {
                            mIsOpenDialog = true

                            mAuthenticationDialog = DeviceAuthenticationDialog(
                                onConnect = { dialog, password ->
                                    dialog.isConnecting = true
                                    mPassword = password

                                    WiFi.connect(
                                        mName,
                                        mAddress,
                                        password,
                                        onResult = { success, network ->
                                            if (success) {
                                                mIsConnecting = true
                                                dialog.dismiss()

                                                Config.updateDevice(
                                                    name = mName,
                                                    key = password,
                                                    isLocal = true,
                                                    ip = "192.168.4.1"
                                                )

                                                mAuthTimer.start()
                                                mDataViewModel.setServer("192.168.4.1", 80)
                                                mDataViewModel.bind(network)
                                                mDataViewModel.join(password)
                                            } else {
                                                dialog.isConnecting = false
                                                SingleToast.show(getString(R.string.cannot_connect_to_device))
                                            }
                                        },
                                        onLost = {
                                            mDataViewModel.setNotification(
                                                Notification(
                                                    getString(R.string.device_connection_lost),
                                                    true
                                                )
                                            )
                                            mDataViewModel.bind(null)
                                        }
                                    )
                                },
                                onDismiss = {
                                    mIsOpenDialog = false
                                }
                            )
                            mAuthenticationDialog?.show(
                                requireActivity().supportFragmentManager,
                                "dialog_authentication"
                            )
                        }
                    }
                    else -> {
                        if (!mIsOpenDialog) {
                            mIsOpenDialog = true

                            mAuthenticationDialog = DeviceAuthenticationDialog(
                                onConnect = { dialog, password ->
                                    mIsConnecting = true
                                    dialog.dismiss()
                                    mPassword = password

                                    Config.updateDevice(
                                        name = mName,
                                        key = password,
                                        isLocal = type != ConnectionType.INTERNET,
                                        ip = mAddress
                                    )

                                    mAuthTimer.start()

                                    val connectivityManager =
                                        requireContext().getSystemService(ConnectivityManager::class.java)

                                    tryRun {
                                        connectivityManager.unregisterNetworkCallback(
                                            mNetworkCallback
                                        )
                                    }

                                    tryRun {
                                        connectivityManager.registerNetworkCallback(
                                            NetworkRequest.Builder()
                                                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                                .build(),
                                            mNetworkCallback
                                        )
                                    }
                                },
                                onDismiss = {
                                    mIsOpenDialog = false
                                }
                            )
                            mAuthenticationDialog?.show(
                                requireActivity().supportFragmentManager,
                                "dialog_authentication"
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

        if (type == ConnectionType.WIFI) {
            WiFi.isConnected(mName) {
                mIsConnected = it
            }
        }

        mDataViewModel.isAuthenticated.observe(viewLifecycleOwner) { isAuthenticated ->
            if (type == ConnectionType.LAN) {
                mIsConnected = Config.device.name == mName
                        && Config.device.ip == mAddress
                        && isAuthenticated
            }
        }

        return mBinding.root
    }

    override fun onPause() {
        super.onPause()
        mAuthTimer.cancel()
    }

    private val mNetworkCallback = object : NetworkCallback() {
        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            mDataViewModel.bind(network)
            mDataViewModel.setServer(mAddress, 80)
            mDataViewModel.join(mPassword)
        }
    }

    private val mAuthTimer = makeTimer(15000) {
        mIsConnecting = false
        mAuthenticationDialog?.isConnecting = false
    }

    enum class ConnectionType {
        WIFI,
        LAN,
        INTERNET
    }

}