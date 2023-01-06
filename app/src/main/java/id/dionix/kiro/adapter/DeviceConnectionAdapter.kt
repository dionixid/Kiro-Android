package id.dionix.kiro.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import id.dionix.kiro.model.DeviceConnection
import id.dionix.kiro.fragment.DeviceConnectionFragment

class DeviceConnectionAdapter(
    fragment: FragmentActivity,
    onSuccessful: () -> Unit,
    onTabsUpdated: (tabTitles: List<String>) -> Unit
) : FragmentStateAdapter(fragment) {

    private val mOnSuccessful = onSuccessful
    private val mOnTabsUpdated = onTabsUpdated

    private val mTabs: ArrayList<Tab> = ArrayList()

    fun getTabTitles(): List<String> {
        return mTabs.map { it.title }
    }

    override fun getItemCount(): Int {
        return mTabs.size
    }

    override fun createFragment(position: Int): Fragment {
        return mTabs[position].fragment
    }

    fun update(device: DeviceConnection) {
        var isUpdated = false

        if (device.isWifi && mTabs.find { it.title == "WiFi" } == null) {
            mTabs.add(
                0,
                Tab(
                    "WiFi",
                    DeviceConnectionFragment(
                        name = device.name,
                        address = device.mac,
                        type = DeviceConnectionFragment.ConnectionType.WIFI,
                        onSuccessful = mOnSuccessful
                    )
                )
            )

            isUpdated = true
            notifyItemInserted(0)
        } else if (!device.isWifi && mTabs.find { it.title == "WiFi" } != null) {
            val idx = mTabs.indexOfFirst { it.title == "WiFi" }

            mTabs.removeIf { it.title == "WiFi" }

            if (idx >= 0) {
                isUpdated = true
                notifyItemRemoved(idx)
            }
        }

        if (device.isLan && mTabs.find { it.title == "Local" } == null) {
            val idx = if (device.isWifi) 1 else 0

            mTabs.add(
                idx,
                Tab(
                    "Local",
                    DeviceConnectionFragment(
                        name = device.name,
                        address = device.ip,
                        type = DeviceConnectionFragment.ConnectionType.LAN,
                        onSuccessful = mOnSuccessful
                    )
                )
            )

            isUpdated = true
            notifyItemInserted(idx)
        } else if (!device.isLan && mTabs.find { it.title == "Local" } != null) {
            val idx = mTabs.indexOfFirst { it.title == "Local" }
            mTabs.removeIf { it.title == "Local" }

            if (idx >= 0) {
                isUpdated = true
                notifyItemRemoved(idx)
            }
        }

        if (device.isInternet && mTabs.find { it.title == "Internet" } == null) {
            var idx = 0

            if (device.isWifi) {
                idx++
            }

            if (device.isLan) {
                idx++
            }

            mTabs.add(
                idx,
                Tab(
                    "Internet",
                    DeviceConnectionFragment(
                        name = device.name,
                        address = device.id,
                        type = DeviceConnectionFragment.ConnectionType.INTERNET,
                        onSuccessful = mOnSuccessful
                    )
                )
            )

            isUpdated = true
            notifyItemInserted(idx)
        } else if (!device.isLan && mTabs.find { it.title == "Internet" } != null) {
            val idx = mTabs.indexOfFirst { it.title == "Internet" }
            mTabs.removeIf { it.title == "Internet" }

            if (idx >= 0) {
                isUpdated = true
                notifyItemRemoved(idx)
            }
        }

        if (isUpdated) {
            mOnTabsUpdated(mTabs.map { it.title })
        }
    }

    data class Tab(
        var title: String,
        var fragment: DeviceConnectionFragment
    )

}