package id.dionix.kiro.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatDialogFragment
import id.dionix.kiro.databinding.DialogConfirmationBinding
import id.dionix.kiro.utility.dip
import id.dionix.kiro.utility.scaleOnClick

class ConfirmationDialog(
    title: String,
    description: String,
    buttonLabel: String,
    @ColorInt buttonColor: Int,
    onConfirm: (() -> Unit) = {},
    onDismiss: (() -> Unit) = {}
) : AppCompatDialogFragment() {

    private val mTitle = title
    private val mDescription = description
    private val mButtonLabel = buttonLabel
    private val mButtonColor = buttonColor
    private val mOnConfirm = onConfirm
    private val mOnDismiss = onDismiss

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
        mOnDismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogConfirmationBinding.inflate(inflater, container, false)

        binding.tvTitle.text = mTitle
        binding.tvDescription.text = mDescription
        binding.tvButton.text = mButtonLabel
        binding.cvButton.setCardBackgroundColor(mButtonColor)

        binding.cvButton.scaleOnClick {
            mOnConfirm()
            dismiss()
        }

        return binding.root
    }
}
