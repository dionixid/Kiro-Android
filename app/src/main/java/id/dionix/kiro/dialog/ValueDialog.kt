package id.dionix.kiro.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.codedillo.rttp.model.Value
import id.dionix.kiro.R
import id.dionix.kiro.databinding.DialogValueBinding
import id.dionix.kiro.model.Setting
import id.dionix.kiro.utility.ContentResolver
import id.dionix.kiro.utility.Text
import id.dionix.kiro.utility.scaleOnClick

class ValueDialog(
    groupName: String,
    value: Setting,
    onSave: (value: Setting) -> Unit = {},
    onDismiss: () -> Unit = {}
) : AppCompatDialogFragment() {

    private var mGroupName = groupName
    private var mValue = value.copy()

    private val mOnSave = onSave
    private val mOnDismiss = onDismiss

    private lateinit var mBinding: DialogValueBinding

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
        mBinding = DialogValueBinding.inflate(inflater, container, false)

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

        mBinding.tvTitle.text = ContentResolver.getString(requireContext(), mGroupName)
        mBinding.tvFieldName.text = ContentResolver.getString(requireContext(), mValue.label)

        mBinding.cvVisibility.scaleOnClick {
            val selectionStart = mBinding.etValue.selectionStart
            val selectionEnd = mBinding.etValue.selectionEnd

            if (mBinding.etValue.transformationMethod == Text.passwordTransformationMethod) {
                mBinding.etValue.transformationMethod = null
                mBinding.ivVisibility.setImageResource(R.drawable.ic_round_visibility_off)
            } else {
                mBinding.etValue.transformationMethod = Text.passwordTransformationMethod
                mBinding.ivVisibility.setImageResource(R.drawable.ic_round_visibility)
            }

            mBinding.etValue.setSelection(selectionStart, selectionEnd)
        }

        mBinding.etValue.apply {
            setText(mValue.value.toString())

            when (mValue.type) {
                Setting.Type.Integer -> {
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                }
                Setting.Type.Float -> {
                    inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
                }
                Setting.Type.String -> {
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                }
                else -> {
                    // DO Nothing
                }
            }

            transformationMethod = if (mValue.isConfidential) {
                Text.passwordTransformationMethod
            } else {
                null
            }

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mBinding.etValue.clearFocus()
                }
                return@setOnEditorActionListener false
            }

            addTextChangedListener {
                it?.toString()?.let { value ->
                    if (value.isBlank()) {
                        if (mBinding.cvSave.isEnabled) {
                            mBinding.cvSave.isEnabled = false
                            mBinding.llSave.setBackgroundResource(R.color.disabled)
                        }
                    } else {
                        if (!mBinding.cvSave.isEnabled) {
                            mBinding.cvSave.isEnabled = true
                            mBinding.llSave.setBackgroundResource(R.drawable.ic_background)
                        }
                    }

                    when (mValue.type) {
                        Setting.Type.Integer -> {
                            mValue.value = Value(value.toIntOrNull() ?: mValue.value.toInt())
                        }
                        Setting.Type.Float -> {
                            mValue.value = Value(value.toFloatOrNull() ?: mValue.value.toFloat())
                        }
                        Setting.Type.String -> {
                            mValue.value = Value(value)
                        }
                        else -> {
                            // DO Nothing
                        }
                    }
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { _, insets ->
            if (insets.getInsets(WindowInsetsCompat.Type.ime()).bottom == 0) {
                mBinding.etValue.clearFocus()
            }
            return@setOnApplyWindowInsetsListener insets
        }

        mBinding.cvSave.scaleOnClick {
            mOnSave(mValue)
            dismiss()
        }

        mBinding.cvBack.scaleOnClick {
            dismiss()
        }

        return mBinding.root
    }

}