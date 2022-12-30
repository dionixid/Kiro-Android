package id.dionix.kiro.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.DialogPermissionBinding
import id.dionix.kiro.utility.dip

class PermissionDialog(
    private var onContinue: (() -> Unit) = {},
    private var onReject: (() -> Unit) = {}
) : AppCompatDialogFragment() {

    private var isContinue = false

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
        if (!isContinue) {
            onReject()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogPermissionBinding.inflate(inflater, container, false)

        binding.tvContinue.setOnClickListener {
            isContinue = true
            onContinue()
            dismiss()
        }

        binding.tvNotNow.setOnClickListener {
            dismiss()
        }

        binding.rvDrawable.apply {
            setHasFixedSize(true)
            adapter = IconAdapter(
                listOf(
                    R.drawable.ic_baseline_location_on,
                    R.drawable.ic_round_wifi
                )
            )
        }

        return binding.root
    }

    class IconAdapter(@DrawableRes private var drawables: List<Int>) :
        RecyclerView.Adapter<IconAdapter.ViewHolder>() {

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivIcon: AppCompatImageView = itemView.findViewById(R.id.iv_icon)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_permission, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.ivIcon.setImageResource(drawables[position])
        }

        override fun getItemCount(): Int {
            return drawables.size
        }
    }
}