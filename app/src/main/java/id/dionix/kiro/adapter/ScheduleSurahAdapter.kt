package id.dionix.kiro.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.ItemScheduleSurahBinding
import id.dionix.kiro.model.Qiro
import id.dionix.kiro.utility.ContentResolver
import id.dionix.kiro.utility.runMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduleSurahAdapter : RecyclerView.Adapter<ScheduleSurahAdapter.ViewHolder>() {

    private var mItems = listOf<String>()

    fun setQiro(context: Context, qiro: Qiro) {
        CoroutineScope(Dispatchers.IO).launch {
            val newItems = buildList {
                if (qiro.surahList.isEmpty()) {
                    add(context.getString(R.string.inactive))
                    return@buildList
                }

                add(
                    context.getString(
                        if (qiro.durationMinutes > 1) R.string.minutes_format else R.string.minute_format,
                        qiro.durationMinutes
                    )
                )

                qiro.surahList.forEach {
                    add(ContentResolver.getSurahProperties(it).name)
                }
            }

            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return mItems.size
                }

                override fun getNewListSize(): Int {
                    return newItems.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return mItems[oldItemPosition] == newItems[newItemPosition]
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return mItems[oldItemPosition] == newItems[newItemPosition]
                }
            })

            runMain {
                mItems = newItems
                suppressLayout(false)
                diffResult.dispatchUpdatesTo(this@ScheduleSurahAdapter)
                suppressLayout(true)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemScheduleSurahBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setValue(mItems[position])
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class ViewHolder(
        private val mBinding: ItemScheduleSurahBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        fun setValue(value: String) {
            mBinding.tvValue.apply {
                text = value

                setTextColor(
                    context.getColor(
                        if (adapterPosition == 0) R.color.white
                        else R.color.text_primary
                    )
                )

                typeface = ResourcesCompat.getFont(
                    context,
                    if (adapterPosition == 0) R.font.dm_sans_bold
                    else R.font.dm_sans_medium
                )
            }

            mBinding.cvContainer.apply {
                setCardBackgroundColor(
                    context.getColor(
                        when (adapterPosition) {
                            0 -> {
                                if (mItems.size > 1) R.color.green
                                else R.color.red
                            }
                            else -> {
                                R.color.background_tertiary
                            }
                        }
                    )
                )
            }

        }

    }

    private var mSuppressLayoutImpl: (isSuppressed: Boolean) -> Unit = {}

    private fun suppressLayout(isSuppressed: Boolean) {
        mSuppressLayoutImpl(isSuppressed)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mSuppressLayoutImpl = {
            recyclerView.suppressLayout(it)
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mSuppressLayoutImpl = {}
    }

}