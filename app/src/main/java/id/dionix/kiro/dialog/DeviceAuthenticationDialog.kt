package id.dionix.kiro.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import id.dionix.kiro.R
import id.dionix.kiro.databinding.DialogDeviceAuthenticationBinding
import id.dionix.kiro.utility.Text
import id.dionix.kiro.utility.dip
import id.dionix.kiro.utility.scaleOnClick

class DeviceAuthenticationDialog(
    private val password: String = "",
    private val onConnect: (dialog: DeviceAuthenticationDialog, password: String) -> Unit = { _, _ -> },
    private val onDismiss: () -> Unit = {}
) : AppCompatDialogFragment() {

    private lateinit var mBinding: DialogDeviceAuthenticationBinding

    var isConnecting = false
        set(value) {
            field = value
            mBinding.pbButton.visibility = if (value) View.VISIBLE else View.GONE
            mBinding.tvButton.visibility = if (value) View.GONE else View.VISIBLE
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(300.dip, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogDeviceAuthenticationBinding.inflate(inflater, container, false)

        mBinding.etValue.apply {
            transformationMethod = Text.passwordTransformationMethod
            setText(password)
        }

        mBinding.ivVisibility.apply {
            scaleOnClick {
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
        }

        mBinding.cvButton.scaleOnClick {
            if (!isConnecting) {
                mBinding.etValue.text?.toString()?.let {
                    onConnect(this, it)
                }
            }
        }

        return mBinding.root
    }

}