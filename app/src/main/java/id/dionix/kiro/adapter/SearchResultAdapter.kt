package id.dionix.kiro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.databinding.ItemSearchResultBinding
import id.dionix.kiro.model.SurahProperties
import id.dionix.kiro.utility.scaleOnClick

class SearchResultAdapter(
    onItemSelected: (surahProps: SurahProperties) -> Unit = {}
) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

    private val mOnItemSelected = onItemSelected

    private var mItems: List<SurahProperties> = listOf()

    fun setSurahList(surahProperties: List<SurahProperties>) {
        mItems = surahProperties.map { it.copy() }
        notifyItemRangeChanged(0, mItems.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSearchResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.surah = mItems[position]
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class ViewHolder(
        private val mBinding: ItemSearchResultBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.root.scaleOnClick {
                mOnItemSelected(surah.copy())
            }
        }

        var surah: SurahProperties = SurahProperties()
            set(value) {
                field = value
                mBinding.tvLabel.text = surah.name
            }

    }

}