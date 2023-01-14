package id.dionix.kiro.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
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
import com.codedillo.numberpicker.NumberPicker
import com.codedillo.rttp.model.Value
import id.dionix.kiro.R
import id.dionix.kiro.databinding.DialogDateTimeBinding
import id.dionix.kiro.model.Setting
import id.dionix.kiro.utility.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.*

class DateTimeDialog(
    time: Setting,
    date: Setting,
    onSave: (time: Setting, date: Setting) -> Unit = { _, _ -> },
    onDismiss: () -> Unit = {}
) : AppCompatDialogFragment() {

    private var mTimeSetting = time.copy()
    private var mDateSetting = date.copy()

    private val mOnSave = onSave
    private val mOnDismiss = onDismiss

    private var mTime: LocalTime = time.value.toInt().secondsToTime()
    private var mDate: LocalDate = date.value.toString().parseDate("dd-MM-yyyy") ?: LocalDate.now()

    private lateinit var mBinding: DialogDateTimeBinding

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
        mBinding = DialogDateTimeBinding.inflate(inflater, container, false)

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

        mBinding.root.apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                setPadding(0, 0, 0, navBar.bottom)
                return@setOnApplyWindowInsetsListener insets
            }
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

        fun setupNumberSet(numberSet: NumberPicker.NumberSet) {
            numberSet.apply {
                divider.color = requireContext().getColor(R.color.disabled)
                separator.appearance.color = requireContext().getColor(R.color.text_default)
                separator.appearance.typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.dm_sans) ?: Typeface.DEFAULT
                value.appearance.color = requireContext().getColor(R.color.text_default)
                value.appearance.colorFocus = requireContext().getColor(R.color.secondary)
                value.appearance.typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.dm_sans) ?: Typeface.DEFAULT
            }
        }

        setupNumberSet(hourSet)
        setupNumberSet(minuteSet)
        setupNumberSet(secondSet)
        setupNumberSet(dateSet)
        setupNumberSet(monthSet)
        setupNumberSet(yearSet)

        hourSet.apply {
            value.currentValue = mTime.hour.toFloat()
            value.setOnValueChangedListener { value, fromUser ->
                mTime = mTime.withHour(value.toInt())
                if (fromUser) {
                    mBinding.cbSystemTime.isChecked = false
                }
            }
        }

        minuteSet.apply {
            value.currentValue = mTime.minute.toFloat()
            value.setOnValueChangedListener { value, fromUser ->
                mTime = mTime.withMinute(value.toInt())
                if (fromUser) {
                    mBinding.cbSystemTime.isChecked = false
                }
            }
        }

        secondSet.apply {
            value.currentValue = mTime.second.toFloat()
            value.setOnValueChangedListener { value, fromUser ->
                mTime = mTime.withSecond(value.toInt())
                if (fromUser) {
                    mBinding.cbSystemTime.isChecked = false
                }
            }
        }

        dateSet.apply {
            value.currentValue = mDate.dayOfMonth.toFloat()
            value.count = YearMonth.from(mDate).atEndOfMonth().dayOfMonth
            value.setOnValueChangedListener { value, fromUser ->
                mDate = mDate.withDayOfMonth(value.toInt())
                if (fromUser) {
                    mBinding.cbSystemTime.isChecked = false
                }
            }
        }

        monthSet.apply {
            value.currentValue = mDate.monthValue.toFloat()
            value.setOnValueChangedListener { value, fromUser ->
                mDate = mDate.withMonth(value.toInt())

                val oldCount = dateSet.value.count
                dateSet.value.count = YearMonth.from(mDate).atEndOfMonth().dayOfMonth

                if (oldCount != dateSet.value.count) {
                    dateSet.value.invalidate()
                }

                if (fromUser) {
                    mBinding.cbSystemTime.isChecked = false
                }
            }
        }

        yearSet.apply {
            value.currentValue = mDate.year.toFloat()
            value.setOnValueChangedListener { value, fromUser ->
                mDate = mDate.withYear(value.toInt())

                val oldCount = dateSet.value.count
                dateSet.value.count = YearMonth.from(mDate).atEndOfMonth().dayOfMonth

                if (oldCount != dateSet.value.count) {
                    dateSet.value.invalidate()
                }

                if (fromUser) {
                    mBinding.cbSystemTime.isChecked = false
                }
            }
        }

        mBinding.npTime.apply {
            addNumberSet(hourSet)
            addNumberSet(minuteSet)
            addNumberSet(secondSet)
        }

        mBinding.npDate.apply {
            addNumberSet(dateSet)
            addNumberSet(monthSet)
            addNumberSet(yearSet)
        }

        mBinding.cbSystemTime.setOnCheckedChangeListener {
            if (it) {
                mTime = LocalTime.now()
                mDate = LocalDate.now()

                val oldCount = dateSet.value.count
                dateSet.value.count = YearMonth.from(mDate).atEndOfMonth().dayOfMonth

                if (oldCount != dateSet.value.count) {
                    dateSet.value.invalidate()
                }

                hourSet.value.setValue(mTime.hour.toFloat())
                minuteSet.value.setValue(mTime.minute.toFloat())
                secondSet.value.setValue(mTime.second.toFloat())
                dateSet.value.setValue(mDate.dayOfMonth.toFloat())
                monthSet.value.setValue(mDate.monthValue.toFloat())
                yearSet.value.setValue(mDate.year.toFloat())
            }
        }

        mBinding.cvSave.scaleOnClick {
            if (mBinding.cbSystemTime.isChecked) {
                mTimeSetting.value = Value(LocalTime.now().toSecondOfDay())
                mDateSetting.value = Value(LocalDate.now().format("dd-MM-yyyy"))
            } else {
                mTimeSetting.value = Value(mTime.toSecondOfDay())
                mDateSetting.value = Value(mDate.format("dd-MM-yyyy"))
            }
            mOnSave(mTimeSetting, mDateSetting)
            dismiss()
        }

        mBinding.cvBack.scaleOnClick {
            dismiss()
        }

        return mBinding.root
    }

    private fun NumberPicker.NumberSet.commonSetup() {
        value.appearance.size = 14.dp
        width = 80.dip
        divider.width = 50.dip
        separator.appearance.size = 16.dp
        isEndlessModeEnabled = true
    }

    private val hourSet: NumberPicker.NumberSet = NumberPicker.NumberSet().apply {
        commonSetup()
        separator.text = ":"
        value.count = 24
        value.setFormatter { String.format(Locale.US, "%02d", it.toInt()) }
    }

    private val minuteSet: NumberPicker.NumberSet = NumberPicker.NumberSet().apply {
        commonSetup()
        separator.text = ":"
        value.count = 60
        value.setFormatter { String.format(Locale.US, "%02d", it.toInt()) }
    }

    private val secondSet: NumberPicker.NumberSet = NumberPicker.NumberSet().apply {
        commonSetup()
        separator.text = ":"
        value.count = 60
        value.setFormatter { String.format(Locale.US, "%02d", it.toInt()) }
    }

    private val dateSet: NumberPicker.NumberSet = NumberPicker.NumberSet().apply {
        commonSetup()
        separator.text = "/"
        value.min = 1f
        value.count = 31
        value.setFormatter { String.format(Locale.US, "%02d", it.toInt()) }
    }

    private val monthSet: NumberPicker.NumberSet = NumberPicker.NumberSet().apply {
        commonSetup()
        separator.text = "/"
        value.min = 1f
        value.currentValue = 1f
        value.count = 12
        value.setFormatter { String.format(Locale.US, "%02d", it.toInt()) }
    }

    private val yearSet: NumberPicker.NumberSet = NumberPicker.NumberSet().apply {
        commonSetup()
        separator.text = "/"
        value.min = 1970f
        value.currentValue = 2022f
        value.count = 130
        value.setFormatter { String.format(Locale.US, "%d", it.toInt()) }
    }

}