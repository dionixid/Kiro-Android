package id.dionix.kiro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.ItemScheduleBinding
import id.dionix.kiro.model.Prayer
import id.dionix.kiro.model.QiroGroup

class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    private var items = listOf(
        QiroGroup(),
        QiroGroup(),
        QiroGroup(),
        QiroGroup(),
        QiroGroup(),
        QiroGroup(),
        QiroGroup()
    )

    var currentPrayer: Prayer.Name = Prayer.Name.Fajr
        set(value) {
            field = value
            notifyItemRangeChanged(0, items.size, value)
        }

    fun setQiroGroupList(qiroGroups: List<QiroGroup>) {
        if (qiroGroups.size != items.size) {
            return
        }

        items = qiroGroups.map { it.copy() }
        notifyItemRangeChanged(0, items.size, qiroGroups)
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
        holder.qiroGroup = items[position]
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(
        private val mBinding: ItemScheduleBinding,
    ) : RecyclerView.ViewHolder(mBinding.root) {

        var qiroGroup: QiroGroup = QiroGroup()
            set(value) {
                field = value
                val qiro = value.getQiro(currentPrayer)

                mBinding.tvTitle.apply {
                    text = context.getString(
                        when (adapterPosition) {
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