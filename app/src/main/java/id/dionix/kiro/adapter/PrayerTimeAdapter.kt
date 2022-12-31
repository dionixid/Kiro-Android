package id.dionix.kiro.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.ItemPrayerTimeActiveBinding
import id.dionix.kiro.databinding.ItemPrayerTimeBinding
import id.dionix.kiro.model.Prayer
import id.dionix.kiro.model.PrayerGroup
import id.dionix.kiro.model.Qiro
import id.dionix.kiro.model.QiroGroup
import id.dionix.kiro.utility.scaleOnClick

class PrayerTimeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = listOf(
        PrayerQiro(Prayer.Name.Fajr),
        PrayerQiro(Prayer.Name.Fajr),
        PrayerQiro(Prayer.Name.Dhuhr),
        PrayerQiro(Prayer.Name.Asr),
        PrayerQiro(Prayer.Name.Maghrib),
        PrayerQiro(Prayer.Name.Isha)
    )

    var isRunning: Boolean = false
        set(value) {
            field = value
            notifyItemChanged(0, value)
        }

    fun setPrayerGroup(prayerGroup: PrayerGroup) {
        items[1].prayer = prayerGroup.fajr
        items[2].prayer = prayerGroup.dhuhr
        items[3].prayer = prayerGroup.asr
        items[4].prayer = prayerGroup.maghrib
        items[5].prayer = prayerGroup.isha
        notifyItemRangeChanged(1, items.size, prayerGroup)
    }

    fun setQiroGroup(qiroGroup: QiroGroup) {
        items[1].qiro = qiroGroup.fajr
        items[2].qiro = qiroGroup.dhuhr
        items[3].qiro = qiroGroup.asr
        items[4].qiro = qiroGroup.maghrib
        items[5].qiro = qiroGroup.isha
        notifyItemRangeChanged(1, items.size, qiroGroup)
    }

    fun setNextPrayer(prayer: Prayer) {
        items[0].prayer = prayer
        notifyItemChanged(0, prayer)
    }

    fun setNextQiro(qiro: Qiro) {
        items[1].qiro = qiro
        notifyItemChanged(0, qiro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PRAYER_ACTIVE -> PrayerActiveViewHolder(
                ItemPrayerTimeActiveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> PrayerViewHolder(
                ItemPrayerTimeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PrayerActiveViewHolder -> {
                holder.prayerQiro = items[position]
            }
            is PrayerViewHolder -> {
                holder.prayerQiro = items[position]
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_PRAYER_ACTIVE else TYPE_PRAYER
    }

    private inner class PrayerActiveViewHolder(
        private val mBinding: ItemPrayerTimeActiveBinding,
    ) : RecyclerView.ViewHolder(mBinding.root) {

        private val context: Context get() = mBinding.root.context

        var prayerQiro: PrayerQiro = PrayerQiro()
            set(value) {
                field = value

                mBinding.tvTitle.text = context.getString(
                    if (isRunning) R.string.ongoing
                    else R.string.next
                )

                mBinding.tvQiroTime.text = if (value.qiro.durationMinutes > 0) {
                    value.qiro.getFormattedTime(value.prayer)
                } else {
                    context.getString(R.string.invalid_time)
                }

                mBinding.cvQiroTime.setCardBackgroundColor(
                    context.getColor(
                        when (value.prayer.name) {
                            Prayer.Name.Asr,
                            Prayer.Name.Maghrib,
                            -> R.color.red

                            else -> R.color.yellow
                        }
                    )
                )

                mBinding.tvSurah.text = if (value.qiro.durationMinutes > 0) {
                    "Al baqarah" // TODO Get surah name based on its id
                } else {
                    context.getString(R.string.inactive)
                }

                mBinding.tvPrayerTime.text = when (value.prayer.name) {
                    Prayer.Name.Fajr -> context.getString(R.string.fajr_format,
                        value.prayer.getFormattedTime())
                    Prayer.Name.Dhuhr -> context.getString(R.string.dhuhr_format,
                        value.prayer.getFormattedTime())
                    Prayer.Name.Asr -> context.getString(R.string.asr_format,
                        value.prayer.getFormattedTime())
                    Prayer.Name.Maghrib -> context.getString(R.string.maghrib_format,
                        value.prayer.getFormattedTime())
                    Prayer.Name.Isha -> context.getString(R.string.isha_format,
                        value.prayer.getFormattedTime())
                }

                mBinding.clContainer.setBackgroundResource(
                    when (value.prayer.name) {
                        Prayer.Name.Fajr -> R.drawable.bg_fajr
                        Prayer.Name.Dhuhr -> R.drawable.bg_dhuhr
                        Prayer.Name.Asr -> R.drawable.bg_asr
                        Prayer.Name.Maghrib -> R.drawable.bg_maghrib
                        Prayer.Name.Isha -> R.drawable.bg_isha
                    }
                )

                mBinding.ivIcon.setImageResource(
                    when (value.prayer.name) {
                        Prayer.Name.Fajr -> R.drawable.ic_fajr
                        Prayer.Name.Dhuhr -> R.drawable.ic_dhuhr
                        Prayer.Name.Asr -> R.drawable.ic_asr
                        Prayer.Name.Maghrib -> R.drawable.ic_maghrib
                        Prayer.Name.Isha -> R.drawable.ic_isha
                    }
                )
            }
    }

    private inner class PrayerViewHolder(
        private val mBinding: ItemPrayerTimeBinding,
    ) : RecyclerView.ViewHolder(mBinding.root) {

        private val context: Context get() = mBinding.root.context

        init {
            mBinding.root.scaleOnClick {
                // TODO open configuration dialog
            }
        }

        var prayerQiro: PrayerQiro = PrayerQiro()
            set(value) {
                field = value

                mBinding.tvTitle.text = context.getString(
                    when (value.prayer.name) {
                        Prayer.Name.Fajr -> R.string.fajr
                        Prayer.Name.Dhuhr -> R.string.dhuhr
                        Prayer.Name.Asr -> R.string.asr
                        Prayer.Name.Maghrib -> R.string.maghrib
                        Prayer.Name.Isha -> R.string.isha
                    }
                )

                mBinding.cvOffset.apply {
                    visibility = if (value.prayer.offset == 0) View.GONE else View.VISIBLE
                    setCardBackgroundColor(
                        context.getColor(
                            if (value.prayer.offset < 0) R.color.red
                            else R.color.green
                        )
                    )
                }

                mBinding.tvOffset.text = value.prayer.getFormattedOffset()
                mBinding.tvTime.text = value.prayer.getFormattedTime()

                mBinding.cvQiroDuration.visibility = if (value.qiro.durationMinutes == 0) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                mBinding.tvQiroDuration.text = context.getString(
                    if (value.qiro.durationMinutes > 1) {
                        R.string.minutes_format
                    } else {
                        R.string.minute_format
                    },
                    value.qiro.durationMinutes
                )

                mBinding.tvSurah.apply {
                    visibility = if (value.qiro.durationMinutes == 0) View.GONE else View.VISIBLE
                    text = if (value.qiro.durationMinutes > 0) {
                        "Al baqarah" // TODO Get surah name based on its id
                    } else {
                        context.getString(R.string.inactive)
                    }
                }

                mBinding.ivCloud.visibility = if (value.qiro.durationMinutes == 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }

    }

    companion object {
        private const val TYPE_PRAYER_ACTIVE = 0
        private const val TYPE_PRAYER = 1
    }

    private data class PrayerQiro(
        var prayer: Prayer = Prayer(),
        var qiro: Qiro = Qiro(),
    ) {

        constructor(name: Prayer.Name) : this() {
            prayer.name = name
            qiro.name = name
        }

    }

}