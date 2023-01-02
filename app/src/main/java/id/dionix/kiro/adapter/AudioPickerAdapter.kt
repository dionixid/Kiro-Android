package id.dionix.kiro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.databinding.ItemPlaylistBinding
import id.dionix.kiro.model.Surah
import id.dionix.kiro.utility.format
import id.dionix.kiro.utility.secondsToTime

class AudioPickerAdapter(
    onItemSelected: (surah: Surah) -> Unit = {}
) : RecyclerView.Adapter<AudioPickerAdapter.ViewHolder>() {

    private val mOnItemSelected = onItemSelected

    private var mItems: List<Surah> = listOf()

    fun setSurahList(surahList: List<Surah>) {
        mItems = surahList.map { it.copy() }
        notifyItemRangeChanged(0, mItems.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioPickerAdapter.ViewHolder {
        return ViewHolder(
            ItemPlaylistBinding.inflate(
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
        private val mBinding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.root.setOnClickListener {
                mOnItemSelected(surah.copy())
            }
            mBinding.cvDurationBackground.visibility = View.GONE
        }

        var surah: Surah = Surah()
            set(value) {
                field = value
                mBinding.tvName.text = value.name
                mBinding.tvDuration.text = value.durationSeconds.secondsToTime().format("HH:mm:ss")
            }

    }

}