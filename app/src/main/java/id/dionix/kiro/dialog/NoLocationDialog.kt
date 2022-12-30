package id.dionix.kiro.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import id.dionix.kiro.databinding.DialogNoLocationBinding
import id.dionix.kiro.utility.dip

class NoLocationDialog(
    private var onPermit: (() -> Unit) = {},
    private var onExit: (() -> Unit) = {}
) : AppCompatDialogFragment() {

    private var isPermitted = false

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
        if (!isPermitted) {
            onExit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogNoLocationBinding.inflate(inflater, container, false)

        binding.cvPermit.setOnClickListener {
            isPermitted = true
            onPermit()
            dismiss()
        }

        binding.cvExit.setOnClickListener {
            dismiss()
        }

        return binding.root
    }
}
