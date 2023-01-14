package id.dionix.kiro.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.ItemPrayerTimeActiveBinding
import id.dionix.kiro.databinding.ItemPrayerTimeBinding
import id.dionix.kiro.model.*
import id.dionix.kiro.utility.*

class PrayerTimeAdapter(
    onForceStop: (surah: SurahProperties) -> Unit = {},
    onItemSelected: (prayerTimeOffset: PrayerTimeOffset) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mOnForceStop = onForceStop
    private val mOnItemSelected = onItemSelected
    private val mGroup = PrayerQiroGroup()

    var surahAudio: SurahAudio = SurahAudio()
        set(value) {
            field = value
            notifyItemChanged(0, value)
        }

    fun setPrayerGroup(prayerGroup: PrayerGroup) {
        mGroup.setPrayerGroup(prayerGroup)
        notifyItemRangeChanged(1, 5, prayerGroup)
    }

    fun setQiroGroup(qiroGroup: QiroGroup) {
        mGroup.setQiroGroup(qiroGroup)
        notifyItemRangeChanged(1, 5, qiroGroup)
    }

    fun setOngoingPrayer(prayer: Prayer) {
        mGroup.setOngoingPrayer(prayer)
        notifyItemChanged(0, prayer)
    }

    fun setOngoingQiro(qiro: Qiro) {
        mGroup.setOngoingQiro(qiro)
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
                holder.prayerQiro = mGroup.ongoing
            }
            is PrayerViewHolder -> {
                holder.prayerQiro = when (position) {
                    1 -> mGroup.fajr
                    2 -> mGroup.dhuhr
                    3 -> mGroup.asr
                    4 -> mGroup.maghrib
                    5 -> mGroup.isha
                    else -> PrayerQiro()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return 6
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_PRAYER_ACTIVE else TYPE_PRAYER
    }

    private inner class PrayerActiveViewHolder(
        private val mBinding: ItemPrayerTimeActiveBinding,
    ) : RecyclerView.ViewHolder(mBinding.root) {

        private val context: Context get() = mBinding.root.context

        init {
            mBinding.cvStop.scaleOnClick {
                if (surahAudio.isPlaying) {
                    mOnForceStop(ContentResolver.getSurahProperties(surahAudio.toSurah()))
                }
            }
        }

        var prayerQiro: PrayerQiro = PrayerQiro()
            set(value) {
                field = value

                mBinding.tvTitle.text = context.getString(
                    if (surahAudio.isPlaying) R.string.ongoing
                    else R.string.next
                )

                mBinding.cvStop.visibility = if (surahAudio.isPlaying) View.VISIBLE else View.GONE

                mBinding.tvQiroTime.text = if (value.qiro.durationMinutes > 0) {
                    value.qiro.getFormattedTime(value.prayer)
                } else {
                    context.getString(R.string.invalid_time)
                }

                if (value.qiro.surahList.isNotEmpty()) {
                    val name = if (surahAudio.isPlaying) {
                        ContentResolver.getSurahProperties(surahAudio.toSurah()).name
                    } else {
                        val firstSurahName =
                            ContentResolver.getSurahProperties(value.qiro.surahList[0]).name
                        when (val surahSize = value.qiro.surahList.size) {
                            1 -> firstSurahName
                            2 -> {
                                "$firstSurahName & ${ContentResolver.getSurahProperties(value.qiro.surahList[1]).name}"
                            }
                            else -> {
                                "$firstSurahName ${
                                    context.getString(
                                        R.string.and_more_format,
                                        surahSize - 1
                                    )
                                }"
                            }
                        }
                    }

                    mBinding.tvSurah.text = name
                } else {
                    mBinding.tvSurah.text = context.getString(R.string.inactive)
                }

                mBinding.tvPrayerTime.text = when (value.prayer.name) {
                    Prayer.Name.Fajr -> context.getString(
                        R.string.fajr_format,
                        value.prayer.getFormattedTime()
                    )
                    Prayer.Name.Dhuhr -> context.getString(
                        R.string.dhuhr_format,
                        value.prayer.getFormattedTime()
                    )
                    Prayer.Name.Asr -> context.getString(
                        R.string.asr_format,
                        value.prayer.getFormattedTime()
                    )
                    Prayer.Name.Maghrib -> context.getString(
                        R.string.maghrib_format,
                        value.prayer.getFormattedTime()
                    )
                    Prayer.Name.Isha -> context.getString(
                        R.string.isha_format,
                        value.prayer.getFormattedTime()
                    )
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
            mBinding.root.setOnClickListener {
                mOnItemSelected(mGroup.toPrayerTimeOffset())
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
                    visibility = if (value.qiro.surahList.isEmpty()) View.GONE else View.VISIBLE

                    if (value.qiro.surahList.isNotEmpty()) {
                        val firstSurahName =
                            ContentResolver.getSurahProperties(value.qiro.surahList[0]).name

                        val name = when (val surahSize = value.qiro.surahList.size) {
                            1 -> firstSurahName
                            2 -> {
                                "$firstSurahName & ${
                                    ContentResolver.getSurahProperties(
                                        value.qiro.surahList[1]
                                    ).name
                                }"
                            }
                            else -> {
                                "$firstSurahName ${
                                    context.getString(
                                        R.string.and_more_format,
                                        surahSize - 1
                                    )
                                }"
                            }
                        }

                        text = name
                    } else {
                        text = context.getString(R.string.inactive)
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

    private data class PrayerQiroGroup(
        var ongoing: PrayerQiro = PrayerQiro(Prayer.Name.Fajr),
        var fajr: PrayerQiro = PrayerQiro(Prayer.Name.Fajr),
        var dhuhr: PrayerQiro = PrayerQiro(Prayer.Name.Dhuhr),
        var asr: PrayerQiro = PrayerQiro(Prayer.Name.Asr),
        var maghrib: PrayerQiro = PrayerQiro(Prayer.Name.Maghrib),
        var isha: PrayerQiro = PrayerQiro(Prayer.Name.Isha)
    ) {

        fun setPrayerGroup(prayerGroup: PrayerGroup) {
            fajr.prayer = prayerGroup.fajr.copy()
            dhuhr.prayer = prayerGroup.dhuhr.copy()
            asr.prayer = prayerGroup.asr.copy()
            maghrib.prayer = prayerGroup.maghrib.copy()
            isha.prayer = prayerGroup.isha.copy()
        }

        fun setQiroGroup(qiroGroup: QiroGroup) {
            fajr.qiro = qiroGroup.fajr.deepCopy()
            dhuhr.qiro = qiroGroup.dhuhr.deepCopy()
            asr.qiro = qiroGroup.asr.deepCopy()
            maghrib.qiro = qiroGroup.maghrib.deepCopy()
            isha.qiro = qiroGroup.isha.deepCopy()
        }

        fun setOngoingPrayer(prayer: Prayer) {
            ongoing.prayer = prayer.copy()
        }

        fun setOngoingQiro(qiro: Qiro) {
            ongoing.qiro = qiro.deepCopy()
        }

        fun toPrayerTimeOffset(): PrayerTimeOffset {
            return PrayerTimeOffset(
                fajr.prayer.offset,
                dhuhr.prayer.offset,
                asr.prayer.offset,
                maghrib.prayer.offset,
                isha.prayer.offset
            )
        }
    }

}