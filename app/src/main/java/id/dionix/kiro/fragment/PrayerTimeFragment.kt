package id.dionix.kiro.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import id.dionix.kiro.adapter.PrayerTimeAdapter
import id.dionix.kiro.databinding.FragmentPrayerTimeBinding
import id.dionix.kiro.utility.dip
import id.dionix.kiro.utility.scaleOnClick

class PrayerTimeFragment : Fragment() {

    private lateinit var mBinding: FragmentPrayerTimeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = FragmentPrayerTimeBinding.inflate(inflater, container, false)

        mBinding.tvTime.text = "00:00"
        mBinding.tvDate.text = "26"
        mBinding.tvMonth.text = "Desember"
        mBinding.tvYear.text = "2022"

        mBinding.cvDevice.scaleOnClick {
            // TODO Open device dialog
        }

        mBinding.ivLogo.apply {
            val insets = WindowInsetsCompat
                .toWindowInsetsCompat(requireActivity().window.decorView.rootWindowInsets)
                .getInsets(WindowInsetsCompat.Type.statusBars())

            val params = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = 20.dip + insets.top
            }
            layoutParams = params
        }

        mBinding.marginSlider.setOnInterceptEvent {
            !mBinding.recyclerView.canScrollVertically(-1)
        }

        val prayerTimeAdapter = PrayerTimeAdapter()

        mBinding.recyclerView.apply {
            adapter = prayerTimeAdapter
            setHasFixedSize(true)

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