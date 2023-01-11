package id.dionix.kiro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.databinding.ItemSearchPlaylistBinding
import id.dionix.kiro.model.SurahProperties
import id.dionix.kiro.utility.format
import id.dionix.kiro.utility.secondsToTime

class AudioPickerAdapter(
    onItemSelected: (surahProps: SurahProperties) -> Unit = {}
) : RecyclerView.Adapter<AudioPickerAdapter.ViewHolder>() {

    private val mOnItemSelected = onItemSelected

    private var mItems: List<SurahProperties> = listOf()

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioPickerAdapter.ViewHolder {
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
            mBinding.root.setOnClickListener {
                mOnItemSelected(surah.copy())
            }
        }

        var surah: SurahProperties = SurahProperties()
            set(value) {
                field = value
                mBinding.tvId.text = String.format("%03d", value.id)
                mBinding.tvName.text = value.name
                mBinding.tvDuration.text = value.durationSeconds.secondsToTime().format("HH:mm:ss")
            }

    }

}