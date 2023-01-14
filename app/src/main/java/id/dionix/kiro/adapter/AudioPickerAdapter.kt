package id.dionix.kiro.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.ItemSearchPlaylistBinding
import id.dionix.kiro.dialog.AudioPreviewDialog
import id.dionix.kiro.model.SurahAudio
import id.dionix.kiro.model.SurahProperties
import id.dionix.kiro.utility.format
import id.dionix.kiro.utility.scaleOnClick
import id.dionix.kiro.utility.secondsToTime

class AudioPickerAdapter(
    private val mSupportFragmentManager: FragmentManager,
    onSelectedItemChanged: (surahPropsList: List<SurahProperties>) -> Unit = {}
) : RecyclerView.Adapter<AudioPickerAdapter.ViewHolder>() {

    private val mOnItemSelected = onSelectedItemChanged

    private var mItems: List<SurahProperties> = listOf()
    val selectedItems: MutableList<SurahProperties> = mutableListOf()

    private var mIsOpenDialog = false

    fun setSurahList(surahList: List<SurahProperties>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return mItems.size
            }

            override fun getNewListSize(): Int {
                return surahList.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return mItems[oldItemPosition].id == surahList[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return mItems[oldItemPosition] == surahList[newItemPosition]
            }
        })

        mItems = surahList.map { it.copy() }
        diffResult.dispatchUpdatesTo(this)
    }

    fun select(surahProps: SurahProperties) {
        val idx = selectedItems.indexOfFirst { it.id == surahProps.id }
        if (idx == -1) {
            selectedItems.add(surahProps)
        } else {
            selectedItems[idx] = surahProps.copy()
        }

        val position = mItems.indexOfFirst { it.id == surahProps.id }
        if (position != -1) {
            notifyItemChanged(position, surahProps)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudioPickerAdapter.ViewHolder {
        return ViewHolder(
            ItemSearchPlaylistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AudioPickerAdapter.ViewHolder, position: Int) {
        holder.surah = mItems[position]
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class ViewHolder(
        private val mBinding: ItemSearchPlaylistBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.root.apply {
                setOnClickListener {
                    val idx = selectedItems.indexOfFirst { it.id == surah.id }
                    this@ViewHolder.isSelected = if (idx == -1) {
                        selectedItems.add(surah.copy())
                        true
                    } else {
                        selectedItems.removeAt(idx)
                        false
                    }
                    mOnItemSelected(selectedItems)
                }
            }

            mBinding.cvPlay.apply {
                scaleOnClick {
                    if (!mIsOpenDialog) {
                        mIsOpenDialog = true
                        AudioPreviewDialog(
                            surah.name,
                            context.getString(R.string.choose),
                            SurahAudio(
                                surah.id,
                                surah.volume,
                                isPaused = false,
                                isPlaying = false
                            ),
                            onApply = { audio ->
                                select(surah.copy(volume = audio.volume))
                            },
                            onDismiss = {
                                mIsOpenDialog = false
                            }
                        ).show(mSupportFragmentManager, "dialog_audio_preview")
                    }
                }
            }
        }

        var surah: SurahProperties = SurahProperties()
            set(value) {
                field = value
                mBinding.tvId.text = String.format("%03d", value.id)
                mBinding.tvName.text = value.name
                mBinding.tvDuration.text = value.durationSeconds.secondsToTime().format("HH:mm:ss")
                isSelected = selectedItems.indexOfFirst { it.id == value.id } != -1
            }

        private var isSelected = false
            set(value) {
                field = value

                mBinding.ivCheckbox.apply {
                    imageTintList = if (value) {
                        setImageResource(R.drawable.ic_round_check_box)
                        ColorStateList.valueOf(
                            context.getColor(R.color.secondary)
                        )
                    } else {
                        setImageResource(R.drawable.ic_round_check_box_outline_blank)
                        ColorStateList.valueOf(
                            context.getColor(R.color.text_default)
                        )
                    }
                }
            }

    }

}