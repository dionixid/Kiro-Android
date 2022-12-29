package id.dionix.kiro.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.codedillo.rttp.model.Value
import id.dionix.kiro.R
import id.dionix.kiro.adapter.SettingAdapter
import id.dionix.kiro.databinding.FragmentSettingBinding
import id.dionix.kiro.model.Device
import id.dionix.kiro.model.Setting
import id.dionix.kiro.model.SettingGroup
import id.dionix.kiro.utility.dip
import id.dionix.kiro.utility.dp

class SettingFragment: Fragment() {

    private lateinit var mBinding: FragmentSettingBinding

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


        val settingAdapter = SettingAdapter()

        settingAdapter.setDevice(Device("23d407ac-d96f-4a5a-be36-8417fe5710c7", "Kiro 2F7A"))

        settingAdapter.setSettingGroups(
            listOf(
                SettingGroup(
                    name = "Date and Time",
                    settings = listOf(
                        Setting("DT0", Setting.Type.Time, "Time", Value(36000)),
                        Setting("DT1", Setting.Type.Date, "Date", Value("28-12-2022"))
                    )
                ),
                SettingGroup(
                    name = "Location",
                    settings = listOf(
                        Setting("L0", Setting.Type.Latitude, "Latitude", Value(-7.237072)),
                        Setting("L1", Setting.Type.Longitude, "Longitude", Value(110.411327)),
                        Setting("L2", Setting.Type.Elevation, "Elevation", Value(604)),
                    )
                ),
                SettingGroup(
                    name = "WiFi",
                    settings = listOf(
                        Setting("W0", Setting.Type.Info, "Status", Value("disconnected")),
                        Setting("W1", Setting.Type.WiFi, "SSID", Value("Anindia")),
                        Setting("W2", Setting.Type.WiFi, "Password", Value(""), true)
                    )
                ),
                SettingGroup(
                    name = "Security",
                    settings = listOf(
                        Setting("S0", Setting.Type.String, "Password", Value("12345678"), true)
                    )
                ),
                SettingGroup(
                    name = "About",
                    settings = listOf(
                        Setting("A0", Setting.Type.Info, "Version", Value("1.0.0.001"))
                    )
                ),
            )
        )

        settingAdapter.setActions(
            listOf(
                SettingAdapter.Action(
                    tag = "connection",
                    label = "Disconnect",
                    color = requireContext().getColor(R.color.red)
                ) {
                    // TODO Connect or Disconnect device
                }
            )
        )

        mBinding.recyclerView.apply {
            adapter = settingAdapter
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

}