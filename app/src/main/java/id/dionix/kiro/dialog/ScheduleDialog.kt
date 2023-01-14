package id.dionix.kiro.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.codedillo.numberpicker.NumberPicker
import id.dionix.kiro.R
import id.dionix.kiro.adapter.PlaylistAdapter
import id.dionix.kiro.databinding.DialogScheduleBinding
import id.dionix.kiro.model.Prayer
import id.dionix.kiro.model.Qiro
import id.dionix.kiro.model.QiroGroup
import id.dionix.kiro.utility.ContentResolver
import id.dionix.kiro.utility.dip
import id.dionix.kiro.utility.dp
import id.dionix.kiro.utility.scaleOnClick

class ScheduleDialog(
    initialPrayerName: Prayer.Name,
    qiroGroup: QiroGroup,
    onSave: (qiroGroup: QiroGroup) -> Unit = {},
    onDismiss: () -> Unit = {}
) : AppCompatDialogFragment() {

    private var mCurrentPrayerName = initialPrayerName
    private var mQiroGroup = qiroGroup.deepCopy()

    private val mOnSave = onSave
    private val mOnDismiss = onDismiss

    private lateinit var mBinding: DialogScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : AppCompatDialog(requireContext(), R.style.Theme_Kiro_Fullscreen) {
            init {
                onBackPressedDispatcher.addCallback {
                    dialog?.window?.decorView?.let {
                        it.animate()
                            .setDuration(200)
                            .translationX(it.measuredWidth.toFloat())
                            .withEndAction {
                                dismiss()
                            }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mOnDismiss()
    }

    override fun dismiss() {
        dialog?.window?.decorView?.let {
            it.animate()
                .setDuration(200)
                .translationX(it.measuredWidth.toFloat())
                .withEndAction {
                    super.dismiss()
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogScheduleBinding.inflate(inflater, container, false)

        dialog?.window?.decorView?.let {
            it.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    it.translationX = it.measuredWidth.toFloat()
                    it.post {
                        it.animate()
                            .setDuration(200)
                            .translationX(0f)
                    }
                }
            })
        }

        mBinding.llHeader.apply {
            val insets = WindowInsetsCompat
                .toWindowInsetsCompat(requireActivity().window.decorView.rootWindowInsets)
                .getInsets(WindowInsetsCompat.Type.statusBars())

            val params = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                topMargin = insets.top
            }
            layoutParams = params
        }

        mBinding.root.apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                setPadding(0, 0, 0, navBar.bottom)
                return@setOnApplyWindowInsetsListener insets
            }
        }

        val playlistAdapter = PlaylistAdapter { surahList ->
            mQiroGroup.getQiro(mCurrentPrayerName).surahList = surahList.map { it }
        }.apply {
            setQiro(mQiroGroup.getQiro(mCurrentPrayerName))
        }

        mBinding.tvTitle.text = ContentResolver.getDayName(requireContext(), mQiroGroup.dayOfWeek)

        durationSet.apply {
            divider.color = requireContext().getColor(R.color.disabled)
            separator.appearance.color = requireContext().getColor(R.color.text_default)
            separator.appearance.typeface =
                ResourcesCompat.getFont(requireContext(), R.font.dm_sans) ?: Typeface.DEFAULT
            value.appearance.color = requireContext().getColor(R.color.text_default)
            value.appearance.colorFocus = requireContext().getColor(R.color.secondary)
            value.appearance.typeface =
                ResourcesCompat.getFont(requireContext(), R.font.dm_sans) ?: Typeface.DEFAULT
            value.setFormatter {
                when {
                    it == 0f -> {
                        requireContext().getString(R.string.inactive)
                    }
                    it > 1 -> {
                        requireContext().getString(R.string.minutes_format, it.toInt())
                    }
                    else -> {
                        requireContext().getString(R.string.minute_format, it.toInt())
                    }
                }
            }
        }

        durationSet.apply {
            value.currentValue = mQiroGroup.getQiro(mCurrentPrayerName).durationMinutes.toFloat()
            value.setOnValueChangedListener { value, _ ->
                mQiroGroup.getQiro(mCurrentPrayerName).durationMinutes = value.toInt()
                playlistAdapter.setTotalDuration(value.toInt())
            }
        }

        mBinding.npDuration.apply {
            addNumberSet(durationSet)
        }

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = playlistAdapter

            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun supportsPredictiveItemAnimations(): Boolean {
                    return false
                }
            }

            addItemDecoration(object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    if (parent.getChildAdapterPosition(view) == state.itemCount - 1) {
                        outRect.bottom = 42.dip
                    } else {
                        outRect.bottom = 0
                    }
                }
            })
        }

        mBinding.cvPrayerName.scaleOnClick {
            mCurrentPrayerName = mCurrentPrayerName.next()
            playlistAdapter.setQiro(mQiroGroup.getQiro(mCurrentPrayerName))
            updatePrayerName()
            updateMaxDuration()
            durationSet.value.setValue(mQiroGroup.getQiro(mCurrentPrayerName).durationMinutes.toFloat())
        }

        mBinding.cvSave.scaleOnClick {
            mOnSave(mQiroGroup)
            dismiss()
        }

        mBinding.cvBack.scaleOnClick {
            dismiss()
        }

        updatePrayerName()
        updateMaxDuration()

        return mBinding.root
    }

    private fun updatePrayerName() {
        mBinding.tvPrayerName.text = requireContext().getString(
            when (mCurrentPrayerName) {
                Prayer.Name.Fajr -> R.string.fajr
                Prayer.Name.Dhuhr -> R.string.dhuhr
                Prayer.Name.Asr -> R.string.asr
                Prayer.Name.Maghrib -> R.string.maghrib
                Prayer.Name.Isha -> R.string.isha
            }
        )
    }

    private fun updateMaxDuration() {
        durationSet.apply {
            val oldCount = value.count
            value.count = Qiro.getMaximumDuration(mCurrentPrayerName) + 1
            if (oldCount != value.count) {
                value.invalidate()
            }
        }
    }

    private val durationSet: NumberPicker.NumberSet = NumberPicker.NumberSet().apply {
        value.appearance.size = 14.dp
        width = 100.dip
        divider.width = 80.dip
        separator.appearance.size = 16.dp
        isEndlessModeEnabled = true
        separator.text = ":"
    }

}