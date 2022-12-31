package id.dionix.kiro.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import id.dionix.kiro.databinding.DialogWifiBinding
import id.dionix.kiro.model.Setting
import id.dionix.kiro.utility.Text
import id.dionix.kiro.utility.scaleOnClick

class WiFiDialog(
    ssid: Setting,
    password: Setting,
    onSave: (ssid: Setting, password: Setting) -> Unit = { _, _ -> },
    onDismiss: () -> Unit = {}
) : AppCompatDialogFragment() {

    private var mSsid = ssid.copy()
    private var mPassword = password.copy()

    private val mOnSave = onSave
    private val mOnDismiss = onDismiss

    private lateinit var mBinding: DialogWifiBinding

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
        mBinding = DialogWifiBinding.inflate(inflater, container, false)

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

        mBinding.cvVisibility.scaleOnClick {
            val selectionStart = mBinding.etPassword.selectionStart
            val selectionEnd = mBinding.etPassword.selectionEnd

            if (mBinding.etPassword.transformationMethod == Text.passwordTransformationMethod) {
                mBinding.etPassword.transformationMethod = null
                mBinding.ivVisibility.setImageResource(R.drawable.ic_round_visibility_off)
            } else {
                mBinding.etPassword.transformationMethod = Text.passwordTransformationMethod
                mBinding.ivVisibility.setImageResource(R.drawable.ic_round_visibility)
            }

            mBinding.etPassword.setSelection(selectionStart, selectionEnd)
        }


        mBinding.etSsid.apply {
            setText(mSsid.value.toString())

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mBinding.etSsid.clearFocus()
                    mBinding.etPassword.clearFocus()
                }
                return@setOnEditorActionListener false
            }

            addTextChangedListener {
                it?.toString()?.let { value ->
                    mSsid.value = Value(value)
                }
            }
        }

        mBinding.etPassword.apply {
            setText(mPassword.value.toString())
            transformationMethod = Text.passwordTransformationMethod

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mBinding.etSsid.clearFocus()
                    mBinding.etPassword.clearFocus()
                }
                return@setOnEditorActionListener false
            }

            addTextChangedListener {
                it?.toString()?.let { value ->
                    mPassword.value = Value(value)
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { _, insets ->
            if (insets.getInsets(WindowInsetsCompat.Type.ime()).bottom == 0) {
                mBinding.etSsid.clearFocus()
                mBinding.etPassword.clearFocus()
            }
            return@setOnApplyWindowInsetsListener insets
        }

        mBinding.cvSave.scaleOnClick {
            mOnSave(mSsid, mPassword)
            dismiss()
        }

        mBinding.cvBack.scaleOnClick {
            dismiss()
        }

        return mBinding.root
    }

}