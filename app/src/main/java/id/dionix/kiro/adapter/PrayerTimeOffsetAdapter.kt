package id.dionix.kiro.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.codedillo.numberpicker.NumberPicker
import id.dionix.kiro.R
import id.dionix.kiro.databinding.ItemPrayerTimeOffsetBinding
import id.dionix.kiro.model.PrayerTimeOffset
import id.dionix.kiro.utility.*

class PrayerTimeOffsetAdapter(
    prayerTimeOffset: PrayerTimeOffset,
    onChanged: (prayerTimeOffset: PrayerTimeOffset) -> Unit = {}
): RecyclerView.Adapter<PrayerTimeOffsetAdapter.ViewHolder>() {

    private val mPrayerTimeOffset = prayerTimeOffset.copy()
    private val mOnChanged = onChanged

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPrayerTimeOffsetBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.update()
    }

    override fun getItemCount(): Int {
        return 5
    }

    inner class ViewHolder(
        private val mBinding: ItemPrayerTimeOffsetBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        private val numberSet: NumberPicker.NumberSet = NumberPicker.NumberSet().apply {
            value.appearance.size = 14.dp
            width = 100.dip
            divider.width = 80.dip
            separator.appearance.size = 16.dp
            isEndlessModeEnabled = true
            separator.text = ":"
            value.min = -30f
            value.count = 61
        }

        init {
            val context = mBinding.root.context
            numberSet.apply {
                divider.color = context.getColor(R.color.disabled)
                separator.appearance.color = context.getColor(R.color.text_default)
                separator.appearance.typeface =
                    ResourcesCompat.getFont(context, R.font.dm_sans) ?: Typeface.DEFAULT
                value.appearance.color = context.getColor(R.color.text_default)
                value.appearance.colorFocus = context.getColor(R.color.secondary)
                value.appearance.typeface =
                    ResourcesCompat.getFont(context, R.font.dm_sans) ?: Typeface.DEFAULT

                value.setFormatter {
                    when {
                        it == 0f || it == -1f -> {
                            context.getString(R.string.minute_format, it.toInt())
                        }
                        it == 1f -> {
                            context.getString(R.string.positive_minute_format, it.toInt())
                        }
                        it > 1f -> {
                            context.getString(R.string.positive_minutes_format, it.toInt())
                        }
                        else -> {
                            context.getString(R.string.minutes_format, it.toInt())
                        }
                    }
                }

                value.setOnValueChangedListener { value, _ ->
                    when (adapterPosition) {
                        0 -> mPrayerTimeOffset.fajr = value.toInt()
                        1 -> mPrayerTimeOffset.dhuhr = value.toInt()
                        2 -> mPrayerTimeOffset.asr = value.toInt()
                        3 -> mPrayerTimeOffset.maghrib = value.toInt()
                        4 -> mPrayerTimeOffset.isha = value.toInt()
                    }
                    mOnChanged(mPrayerTimeOffset.copy())
                }

                value.currentValue = when (adapterPosition) {
                    0 -> mPrayerTimeOffset.fajr.toFloat()
                    1 -> mPrayerTimeOffset.dhuhr.toFloat()
                    2 -> mPrayerTimeOffset.asr.toFloat()
                    3 -> mPrayerTimeOffset.maghrib.toFloat()
                    4 -> mPrayerTimeOffset.isha.toFloat()
                    else -> 0f
                }
            }

            mBinding.numberPicker.apply {
                addNumberSet(numberSet)
            }
        }

        fun update() {
            mBinding.tvTitle.apply {
                text = when (adapterPosition) {
                    0 -> context.getString(R.string.fajr)
                    1 -> context.getString(R.string.dhuhr)
                    2 -> context.getString(R.string.asr)
                    3 -> context.getString(R.string.maghrib)
                    4 -> context.getString(R.string.isha)
                    else -> ""
                }
            }

            mBinding.numberPicker.post {
                numberSet.value.setValue(
                    when (adapterPosition) {
                        0 -> mPrayerTimeOffset.fajr.toFloat()
                        1 -> mPrayerTimeOffset.dhuhr.toFloat()
                        2 -> mPrayerTimeOffset.asr.toFloat()
                        3 -> mPrayerTimeOffset.maghrib.toFloat()
                        4 -> mPrayerTimeOffset.isha.toFloat()
                        else -> 0f
                    }
                )
            }

        }

    }

}