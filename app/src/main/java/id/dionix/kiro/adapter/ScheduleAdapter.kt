package id.dionix.kiro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.ItemScheduleBinding
import id.dionix.kiro.model.Prayer
import id.dionix.kiro.model.QiroGroup
import id.dionix.kiro.utility.scaleOnClick

class ScheduleAdapter(
    onItemSelected: (prayerName: Prayer.Name, qiroGroup: QiroGroup) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    private val mOnItemSelected = onItemSelected

    private var mItems: MutableList<QiroGroup> = mutableListOf()

    var currentPrayerName: Prayer.Name = Prayer.Name.Fajr
        set(value) {
            field = value
            notifyItemRangeChanged(0, mItems.size, value)
        }

    fun setQiroGroups(qiroGroups: List<QiroGroup>) {
        mItems = qiroGroups.map { it.copy() }.sortedBy { it.dayOfWeek }.toMutableList()
        notifyItemRangeChanged(0, mItems.size, qiroGroups)
    }

    fun setQiroGroup(qiroGroup: QiroGroup) {
        val position = mItems.indexOfFirst { it.dayOfWeek == qiroGroup.dayOfWeek }
        if (position != -1) {
            mItems[position] = qiroGroup.copy()
            notifyItemChanged(position, qiroGroup)
        }
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

        init {
            mBinding.root.scaleOnClick {
                mOnItemSelected(currentPrayerName, qiroGroup.copy())
            }
        }

        var qiroGroup: QiroGroup = QiroGroup()
            set(value) {
                field = value
                val qiro = value.getQiro(currentPrayerName)

                mBinding.tvTitle.apply {
                    text = context.getString(
                        when (value.dayOfWeek) {
                            0 -> R.string.sunday
                            1 -> R.string.monday
                            2 -> R.string.tuesday
                            3 -> R.string.wednesday
                            4 -> R.string.thursday
                            5 -> R.string.friday
                            else -> R.string.saturday
                        }
                    )
                }

                mBinding.cvQiroDuration.apply {
                    setCardBackgroundColor(
                        context.getColor(
                            if (qiro.durationMinutes > 0) {
                                R.color.green
                            } else {
                                R.color.red
                            }
                        )
                    )
                }

                mBinding.tvQiroDuration.apply {
                    text = if (qiro.durationMinutes > 0) {
                        context.getString(
                            if (qiro.durationMinutes > 1) {
                                R.string.minutes_format
                            } else {
                                R.string.minute_format
                            },
                            qiro.durationMinutes
                        )
                    } else {
                        context.getString(R.string.inactive)
                    }
                }

                mBinding.cvSurah.apply {
                    visibility = if (qiro.durationMinutes > 0) View.VISIBLE else View.GONE
                }

                mBinding.tvSurah.apply {
                    text = if (qiro.durationMinutes > 0) {
                        "Al baqarah" // TODO Get surah name based on its id
                    } else {
                        context.getString(R.string.inactive)
                    }
                }
            }

    }
}