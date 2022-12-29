package id.dionix.kiro.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import id.dionix.kiro.R
import id.dionix.kiro.adapter.ScheduleAdapter
import id.dionix.kiro.databinding.FragmentScheduleBinding
import id.dionix.kiro.model.Prayer
import id.dionix.kiro.utility.dip

class ScheduleFragment: Fragment() {

    private lateinit var mBinding: FragmentScheduleBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentScheduleBinding.inflate(inflater, container, false)

        mBinding.clContainer.apply {
            val insets = WindowInsetsCompat
                .toWindowInsetsCompat(requireActivity().window.decorView.rootWindowInsets)
                .getInsets(WindowInsetsCompat.Type.statusBars())

            val params = (layoutParams as MarginLayoutParams).apply {
                height += insets.top
            }
            layoutParams = params
            setPadding(16.dip, 16.dip + insets.top, 16.dip, 16.dip)
        }

        val scheduleAdapter = ScheduleAdapter()

        fun updatePrayer(name: Prayer.Name) {
            mBinding.tvPrayerName.text = requireContext().getString(
                when (name) {
                    Prayer.Name.Fajr -> R.string.fajr
                    Prayer.Name.Dhuhr -> R.string.dhuhr
                    Prayer.Name.Asr -> R.string.asr
                    Prayer.Name.Maghrib -> R.string.maghrib
                    Prayer.Name.Isha -> R.string.isha
                }
            )

            mBinding.clContainer.background = ContextCompat.getDrawable(
                requireContext(),
                when (name) {
                    Prayer.Name.Fajr -> R.drawable.bg_fajr
                    Prayer.Name.Dhuhr -> R.drawable.bg_dhuhr
                    Prayer.Name.Asr -> R.drawable.bg_asr
                    Prayer.Name.Maghrib -> R.drawable.bg_maghrib
                    Prayer.Name.Isha -> R.drawable.bg_isha
                }
            )

            mBinding.ivIcon.setImageResource(
                when (name) {
                    Prayer.Name.Fajr -> R.drawable.ic_fajr
                    Prayer.Name.Dhuhr -> R.drawable.ic_dhuhr
                    Prayer.Name.Asr -> R.drawable.ic_asr
                    Prayer.Name.Maghrib -> R.drawable.ic_maghrib
                    Prayer.Name.Isha -> R.drawable.ic_isha
                }
            )

            mBinding.ivCloud.setImageResource(
                when (name) {
                    Prayer.Name.Fajr -> R.drawable.ic_cloud_fajr
                    Prayer.Name.Dhuhr -> R.drawable.ic_cloud_dhuhr
                    Prayer.Name.Asr -> R.drawable.ic_cloud_asr
                    Prayer.Name.Maghrib -> R.drawable.ic_cloud_maghrib
                    Prayer.Name.Isha -> R.drawable.ic_cloud_isha
                }
            )
        }

        updatePrayer(scheduleAdapter.currentPrayer)

        mBinding.clContainer.setOnClickListener {
            scheduleAdapter.currentPrayer = scheduleAdapter.currentPrayer.next()
            updatePrayer(scheduleAdapter.currentPrayer)
        }

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = scheduleAdapter

            addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State,
                ) {
                    if (parent.getChildAdapterPosition(view) == state.itemCount - 1) {
                        outRect.bottom = 20.dip
                    } else {
                        outRect.bottom = 0
                    }
                }
            })
        }

        return mBinding.root
    }

}