package id.dionix.kiro.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import id.dionix.kiro.databinding.ItemScheduleBinding
import id.dionix.kiro.model.Prayer
import id.dionix.kiro.model.QiroGroup
import id.dionix.kiro.utility.ContentResolver
import id.dionix.kiro.utility.scaleOnClick

class ScheduleAdapter(
    onItemSelected: (prayerName: Prayer.Name, qiroGroup: QiroGroup) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    private val mOnItemSelected = onItemSelected

    private var mItems: List<QiroGroup> = listOf()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return field.size
                }

                override fun getNewListSize(): Int {
                    return value.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition] == value[newItemPosition]
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    return field[oldItemPosition] == value[newItemPosition]
                }
            })

            field = value
            diffResult.dispatchUpdatesTo(this)
        }

    var currentPrayerName: Prayer.Name = Prayer.Name.Fajr
        set(value) {
            field = value
            notifyItemRangeChanged(0, mItems.size, value)
        }

    fun setQiroGroups(qiroGroups: List<QiroGroup>) {
        mItems = qiroGroups.map { it.copy() }.sortedBy { it.dayOfWeek }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemScheduleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.qiroGroup = mItems[position]
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class ViewHolder(
        private val mBinding: ItemScheduleBinding,
    ) : RecyclerView.ViewHolder(mBinding.root) {

        private val mAdapter = ScheduleSurahAdapter()

        init {
            mBinding.root.scaleOnClick {
                mOnItemSelected(currentPrayerName, qiroGroup.copy())
            }

            mBinding.recyclerView.apply {
                setHasFixedSize(false)
                layoutManager = FlexboxLayoutManager(context)
                itemAnimator = null
                adapter = mAdapter
            }
        }

        var qiroGroup: QiroGroup = QiroGroup()
            set(value) {
                field = value

                mBinding.tvTitle.apply {
                    text = ContentResolver.getDayName(context, value.dayOfWeek)
                }

                mBinding.recyclerView.apply {
                    mAdapter.setQiro(context, value.getQiro(currentPrayerName))
                }
            }

    }
}