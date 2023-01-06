package id.dionix.kiro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.ItemScheduleBinding
import id.dionix.kiro.model.Prayer
import id.dionix.kiro.model.QiroGroup
import id.dionix.kiro.utility.ContentResolver
import id.dionix.kiro.utility.runMain
import id.dionix.kiro.utility.scaleOnClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
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
                    text = ContentResolver.getDayName(context, value.dayOfWeek)
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
                    if (qiro.surahList.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val name = ContentResolver.getSurahProperties(qiro.surahList[0]).name
                            runMain {
                                text = name
                            }
                        }
                    } else {
                        text = context.getString(R.string.inactive)
                    }
                }
            }

    }
}