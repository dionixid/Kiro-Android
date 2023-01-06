package id.dionix.kiro.dialog

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.viewModels
import id.dionix.kiro.R
import id.dionix.kiro.database.DataViewModel
import id.dionix.kiro.databinding.DialogAudioPreviewBinding
import id.dionix.kiro.model.SurahAudio
import id.dionix.kiro.utility.dip
import id.dionix.kiro.utility.scaleOnClick
import id.dionix.kiro.utility.tryRun

class AudioPreviewDialog(
    title: String,
    buttonLabel: String,
    surahAudio: SurahAudio,
    onApply: (surahAudio: SurahAudio) -> Unit,
    onDismiss: () -> Unit = {}
) : AppCompatDialogFragment() {

    private val mTitle = title
    private val mButtonLabel = buttonLabel
    private val mAudio = surahAudio
    private val mOnApply = onApply
    private val mOnDismiss = onDismiss

    private lateinit var mBinding: DialogAudioPreviewBinding

    private val mDataViewModel by viewModels<DataViewModel>()

    private var mIsPaused = false
        set(value) {
            field = value
            requireContext().apply {
                if (value) {
                    mBinding.ivPause.imageTintList =
                        ColorStateList.valueOf(getColor(R.color.disabled))
                    mBinding.ivPlay.imageTintList =
                        ColorStateList.valueOf(getColor(R.color.green))
                } else {
                    mBinding.ivPause.imageTintList = ColorStateList.valueOf(
                        getColor(
                            if (mIsPlaying) R.color.yellow
                            else R.color.disabled
                        )
                    )

                    if (mIsPlaying) {
                        mBinding.ivPlay.imageTintList =
                            ColorStateList.valueOf(getColor(R.color.disabled))
                    }
                }
            }
        }

    private var mIsPlaying = false
        set(value) {
            field = value
            requireContext().apply {
                if (value) {
                    mBinding.ivPlay.imageTintList =
                        ColorStateList.valueOf(getColor(R.color.disabled))
                    mBinding.ivStop.imageTintList =
                        ColorStateList.valueOf(getColor(R.color.red))
                    mBinding.ivPause.imageTintList =
                        ColorStateList.valueOf(getColor(R.color.yellow))
                } else {
                    mBinding.ivPlay.imageTintList =
                        ColorStateList.valueOf(getColor(R.color.green))
                    mBinding.ivStop.imageTintList =
                        ColorStateList.valueOf(getColor(R.color.disabled))
                    mBinding.ivPause.imageTintList =
                        ColorStateList.valueOf(getColor(R.color.disabled))
                }
            }
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
        mOnDismiss()
        if (mIsPlaying) {
            mDataViewModel.sendSurahPreview(mAudio.copy(isPaused = false, isPlaying = false))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogAudioPreviewBinding.inflate(inflater, container, false)

        mBinding.tvTitle.text = mTitle
        mBinding.tvButton.text = mButtonLabel

        mBinding.slider.apply {
            addOnChangeListener { _, value, _ ->
                mAudio.volume = value.toInt()
                mBinding.tvVolume.text = value.toInt().toString()
            }
            value = mAudio.volume.toFloat()
        }

        mDataViewModel.surahPreview.observe(viewLifecycleOwner) {
            it?.let { audio ->
                if (audio.id != mAudio.id) {
                    return@observe
                }

                mAudio.volume = audio.volume
                mAudio.isPlaying = audio.isPlaying
                mAudio.isPaused = audio.isPaused

                tryRun {
                    mBinding.slider.value = audio.volume.toFloat()
                }

                mIsPlaying = audio.isPlaying
                mIsPaused = audio.isPaused
            } ?: kotlin.run {
                mIsPlaying = false
                mIsPaused = false
            }
        }

        mBinding.cvPlay.scaleOnClick {
            if (!mIsPlaying) {
                mDataViewModel.sendSurahPreview(mAudio.copy(isPlaying = true))
            } else {
                if (mIsPaused) {
                    mDataViewModel.sendSurahPreview(mAudio.copy(isPaused = false))
                }
            }
        }

        mBinding.cvPause.scaleOnClick {
            if (mIsPlaying && !mIsPaused) {
                mDataViewModel.sendSurahPreview(mAudio.copy(isPaused = true))
            }
        }

        mBinding.cvStop.scaleOnClick {
            if (mIsPlaying) {
                mDataViewModel.sendSurahPreview(mAudio.copy(isPaused = false, isPlaying = false))
            }
        }

        mBinding.cvButton.scaleOnClick {
            mOnApply(mAudio)
            dismiss()
        }

        return mBinding.root
    }

}