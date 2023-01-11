package id.dionix.kiro.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import id.dionix.kiro.R
import id.dionix.kiro.adapter.AudioPickerAdapter
import id.dionix.kiro.adapter.SearchResultAdapter
import id.dionix.kiro.database.DataViewModel
import id.dionix.kiro.database.SurahViewModel
import id.dionix.kiro.databinding.DialogAudioPickerBinding
import id.dionix.kiro.model.SurahAudio
import id.dionix.kiro.model.SurahProperties
import id.dionix.kiro.utility.dip
import id.dionix.kiro.utility.dp
import id.dionix.kiro.utility.hideKeyboard
import id.dionix.kiro.utility.scaleOnClick

class AudioPickerDialog(
    onItemSelected: (surahProps: SurahProperties) -> Unit = {},
    onDismiss: () -> Unit = {}
) : AppCompatDialogFragment() {

    private val mOnItemSelected = onItemSelected
    private val mOnDismiss = onDismiss

    private lateinit var mBinding: DialogAudioPickerBinding

    private var mIsOpenDialog = false

    private val mSurahViewModel by activityViewModels<SurahViewModel>()
    private val mDataViewModel by activityViewModels<DataViewModel>()

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
        mSurahViewModel.filter("")
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
        mBinding = DialogAudioPickerBinding.inflate(inflater, container, false)

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

        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { _, insets ->
            if (insets.getInsets(WindowInsetsCompat.Type.ime()).bottom == 0) {
                mBinding.etSearch.clearFocus()
            }
            return@setOnApplyWindowInsetsListener insets
        }

        fun openAudioPreviewDialog(surahProps: SurahProperties) {
            if (!mIsOpenDialog) {
                mIsOpenDialog = true
                AudioPreviewDialog(
                    surahProps.name,
                    requireContext().getString(R.string.choose),
                    SurahAudio(
                        surahProps.id,
                        surahProps.volume,
                        isPaused = false,
                        isPlaying = false
                    ),
                    onApply = { audio ->
                        mOnItemSelected(surahProps.copy(volume = audio.volume))
                        mDataViewModel.addSurahSearchResult(surahProps)
                        dismiss()
                    },
                    onDismiss = {
                        mIsOpenDialog = false
                    }
                ).show(requireActivity().supportFragmentManager, "dialog_audio_preview")
            }
        }

        val searchAdapter = SearchResultAdapter(::openAudioPreviewDialog)

        mDataViewModel.lastSurahSearch.observe(viewLifecycleOwner) {
            searchAdapter.setSurahList(it)

            mBinding.tvLastSearchTitle.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            mBinding.rvLastResult.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE

            mBinding.rvLastResult.apply {
                post {
                    mBinding.marginSlider.maxMargin = if (it.isNotEmpty()) {
                        -(measuredHeight + mBinding.tvLastSearchTitle.measuredHeight + 8.dip)
                    } else {
                        8.dip
                    }
                }
            }
        }

        mBinding.rvLastResult.apply {
            adapter = searchAdapter
            layoutManager = FlexboxLayoutManager(requireContext())

            post {
                mBinding.marginSlider.maxMargin = if (measuredWidth > 0) {
                    -(measuredHeight + mBinding.tvLastSearchTitle.measuredHeight + 8.dip)
                } else {
                    8.dip
                }
            }
        }

        val audioAdapter = AudioPickerAdapter(::openAudioPreviewDialog)

        mSurahViewModel.searchResults.observe(viewLifecycleOwner) {
            audioAdapter.setSurahList(it)
        }

        mBinding.rvAudioPicker.apply {
            setHasFixedSize(true)
            adapter = audioAdapter
            itemAnimator = null

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
                        outRect.bottom = 20.dip
                    } else {
                        outRect.bottom = 0
                    }
                }
            })

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    when {
                        dy > 6.dip && mBinding.cvPlaylistTitle.cardElevation == 0f -> {
                            mBinding.cvPlaylistTitle.cardElevation = 4.dp
                        }
                        dy < 2.dip && mBinding.cvPlaylistTitle.cardElevation != 0f -> {
                            mBinding.cvPlaylistTitle.cardElevation = 0f
                        }
                    }
                }
            })
        }

        mBinding.etSearch.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    requireContext().hideKeyboard(windowToken)
                }
                return@setOnEditorActionListener false
            }

            addTextChangedListener {
                it?.toString()?.let { text ->
                    mBinding.cvClear.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
                    mSurahViewModel.filter(text)
                }
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    mBinding.marginSlider.open()
                }
            }
        }

        mBinding.cvClear.setOnClickListener {
            mBinding.etSearch.setText("")
        }

        mBinding.marginSlider.setOnInterceptEvent {
            !mBinding.rvAudioPicker.canScrollVertically(-1)
        }

        mBinding.cvBack.scaleOnClick {
            dismiss()
        }

        return mBinding.root
    }

}