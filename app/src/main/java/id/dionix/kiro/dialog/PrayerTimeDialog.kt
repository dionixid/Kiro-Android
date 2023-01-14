package id.dionix.kiro.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.adapter.PrayerTimeOffsetAdapter
import id.dionix.kiro.databinding.DialogPrayerTimeBinding
import id.dionix.kiro.model.PrayerTimeOffset
import id.dionix.kiro.utility.dip
import id.dionix.kiro.utility.scaleOnClick

class PrayerTimeDialog(
    prayerTimeOffset: PrayerTimeOffset,
    onSave: (prayerTimeOffset: PrayerTimeOffset) -> Unit = {},
    onDismiss: () -> Unit = {}
) : AppCompatDialogFragment() {

    private var mPrayerTimeOffset = prayerTimeOffset.copy()

    private val mOnSave = onSave
    private val mOnDismiss = onDismiss

    private lateinit var mBinding: DialogPrayerTimeBinding

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
        mBinding = DialogPrayerTimeBinding.inflate(inflater, container, false)

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

        mBinding.recyclerView.apply {
            overScrollMode = View.OVER_SCROLL_NEVER
            setHasFixedSize(true)
            adapter = PrayerTimeOffsetAdapter(mPrayerTimeOffset) {
                mPrayerTimeOffset = it
            }

            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun supportsPredictiveItemAnimations(): Boolean {
                    return false
                }
            }

            addItemDecoration(object : RecyclerView.ItemDecoration() {
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

        mBinding.cvSave.scaleOnClick {
            mOnSave(mPrayerTimeOffset)
            dismiss()
        }

        mBinding.cvBack.scaleOnClick {
            dismiss()
        }

        return mBinding.root
    }

}