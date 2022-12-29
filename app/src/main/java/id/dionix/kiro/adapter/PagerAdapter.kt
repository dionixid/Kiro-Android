package id.dionix.kiro.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import id.dionix.kiro.fragment.PrayerTimeFragment
import id.dionix.kiro.fragment.ScheduleFragment
import id.dionix.kiro.fragment.SettingFragment

class PagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    private val mFragments = listOf(
        PrayerTimeFragment(),
        ScheduleFragment(),
        SettingFragment()
    )

    override fun getItemCount(): Int {
        return mFragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragments[position]
    }

}