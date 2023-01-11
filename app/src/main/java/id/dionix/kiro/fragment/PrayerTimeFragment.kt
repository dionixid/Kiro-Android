package id.dionix.kiro.fragment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import id.dionix.kiro.R
import id.dionix.kiro.adapter.PrayerTimeAdapter
import id.dionix.kiro.database.DataViewModel
import id.dionix.kiro.databinding.FragmentPrayerTimeBinding
import id.dionix.kiro.dialog.ConfirmationDialog
import id.dionix.kiro.dialog.DeviceDialog
import id.dionix.kiro.dialog.PrayerTimeDialog
import id.dionix.kiro.utility.dip
import id.dionix.kiro.utility.format
import id.dionix.kiro.utility.scaleOnClick
import java.time.LocalDate

class PrayerTimeFragment : Fragment() {

    private lateinit var mBinding: FragmentPrayerTimeBinding

    private var mIsOpenDialog = false

    private val mDataViewModel by activityViewModels<DataViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = FragmentPrayerTimeBinding.inflate(inflater, container, false)

        mDataViewModel.time.observe(viewLifecycleOwner) {
            mBinding.tvTime.text = it.format("HH:mm")
        }

        mDataViewModel.date.observe(viewLifecycleOwner) {
            mBinding.tvDate.text = it.format("dd")
            mBinding.tvMonth.text = it.format("MMMM")
            mBinding.tvYear.text = it.format("yyyy")
        }

        mBinding.cvDevice.scaleOnClick {
            if (!mIsOpenDialog) {
                mIsOpenDialog = true
                DeviceDialog {
                    mIsOpenDialog = false
                }.show(requireActivity().supportFragmentManager, "dialog_device")
            }
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

        val prayerTimeAdapter = PrayerTimeAdapter(
            onForceStop = {
                if (!mIsOpenDialog) {
                    mIsOpenDialog = true

                    ConfirmationDialog(
                        it.name,
                        getString(R.string.surah_force_stop_description),
                        getString(R.string.stop),
                        requireContext().getColor(R.color.red),
                        onConfirm = {
                            mDataViewModel.sendSurahForceStopCommand()
                        },
                        onDismiss = {
                            mIsOpenDialog = false
                        }
                    )
                }
            },
            onItemSelected = {
                if (!mIsOpenDialog) {
                    mIsOpenDialog = true

                    PrayerTimeDialog(
                        it,
                        onSave = { offset ->
                            mDataViewModel.sendPrayerTimeOffset(offset)
                        },
                        onDismiss = {
                            mIsOpenDialog = false
                        }
                    ).show(requireActivity().supportFragmentManager, "dialog_prayer_time_offset")
                }
            }
        )

        mDataViewModel.surahOngoing.observe(viewLifecycleOwner) {
            prayerTimeAdapter.surahAudio = it
        }

        mDataViewModel.prayerGroup.observe(viewLifecycleOwner) {
            prayerTimeAdapter.setPrayerGroup(it)
        }

        mDataViewModel.prayerOngoing.observe(viewLifecycleOwner) {
            prayerTimeAdapter.setOngoingPrayer(it)
        }

        mDataViewModel.qiroOngoing.observe(viewLifecycleOwner) {
            prayerTimeAdapter.setOngoingQiro(it)
        }

        mDataViewModel.qiroGroups.observe(viewLifecycleOwner) {
            it.forEach { group ->
                if (LocalDate.now().dayOfWeek == group.dayOfWeek) {
                    prayerTimeAdapter.setQiroGroup(group)
                    return@observe
                }
            }
        }

        mDataViewModel.device.observe(viewLifecycleOwner) {
            mBinding.tvDevice.text = it?.name ?: getString(R.string.device)
            mBinding.marginSlider.maxMargin = if (it == null) 16.dip else (-100).dip
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

        mDataViewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            mDataViewModel.device.value?.let {
                mBinding.ivErrorNotConnected.visibility = if (isConnected) View.GONE else View.VISIBLE
            }
        }

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