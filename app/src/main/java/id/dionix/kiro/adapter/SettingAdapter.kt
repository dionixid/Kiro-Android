package id.dionix.kiro.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.*
import id.dionix.kiro.model.Device
import id.dionix.kiro.model.Setting
import id.dionix.kiro.model.SettingGroup
import id.dionix.kiro.utility.*

class SettingAdapter(
    private val onItemSelected: (setting: Setting) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: MutableList<Any> = mutableListOf(
        Device(),
        Footer("© 2023 Dionix ID")
    )

    fun setDevice(device: Device) {
        if (items.getOrNull(0) is Device) {
            items[0] = device.copy()
            notifyItemChanged(0, device)
        }
    }

    fun setSettingGroups(settingGroups: List<SettingGroup>) {
        if (items.size > 1) {
            val count = items.count { it is Setting || it is String }
            items.removeIf { it is Setting || it is String }
            notifyItemRangeRemoved(1, count)
        }

        val settings = mutableListOf<Any>()
        settingGroups.forEach { group ->
            settings.add(group.name)
            group.settings.forEach { setting ->
                settings.add(setting.copy())
            }
        }

        items.addAll(1, settings)
        notifyItemRangeInserted(1, settings.size)
    }

    fun setSetting(setting: Setting) {
        items.forEachIndexed { index, any ->
            if (any is Setting && any.id == setting.id) {
                any.label = setting.label
                any.value = setting.value
                notifyItemChanged(index, setting)
                return
            }
        }
    }

    fun setActions(actions: List<Action>) {
        val start = items.indexOfFirst { it is Action }

        if (start != -1) {
            val count = items.count { it is Action }
            items.removeIf { it is Action }
            notifyItemRangeRemoved(start, count)
        }

        if (start == -1) {
            items.addAll(items.lastIndex, actions)
        } else {
            items.addAll(start, actions)
        }
        notifyItemRangeInserted(start, actions.size)
    }

    fun setAction(action: Action) {
        items.forEachIndexed { index, any ->
            if (any is Action && any.tag == action.tag) {
                any.label = action.label
                any.color = action.color
                any.run = action.run
                notifyItemChanged(index, action)
                return
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemSettingHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_CONTENT -> ContentViewHolder(
                ItemSettingContentBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_ACTION -> ActionViewHolder(
                ItemSettingActionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            TYPE_FOOTER -> FooterViewHolder(
                ItemSettingFooterBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> TitleViewHeader(
                ItemSettingTitleBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when {
            holder is HeaderViewHolder && item is Device -> holder.device = item
            holder is ContentViewHolder && item is Setting -> holder.setting = item
            holder is TitleViewHeader && item is String -> holder.title = item
            holder is ActionViewHolder && item is Action -> holder.action = item
            holder is FooterViewHolder && item is Footer -> holder.footer = item
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Device -> TYPE_HEADER
            is Setting -> TYPE_CONTENT
            is Action -> TYPE_ACTION
            is Footer -> TYPE_FOOTER
            else -> TYPE_TITLE
        }
    }

    private inner class HeaderViewHolder(
        private val mBinding: ItemSettingHeaderBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        var device: Device = Device()
            set(value) {
                field = value
                mBinding.tvId.text = value.id
                mBinding.tvName.text = value.name
            }

    }

    private inner class TitleViewHeader(
        private val mBinding: ItemSettingTitleBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        var title: CharSequence
            set(value) {
                mBinding.tvTitle.text =
                    ContentResolver.getString(mBinding.root.context, value.toString())
            }
            get() = mBinding.tvTitle.text

    }

    private inner class ContentViewHolder(
        private val mBinding: ItemSettingContentBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        private var isVisible = false

        init {
            mBinding.cvVisibility.setOnClickListener {
                isVisible = !isVisible
                updateValue()
                updateVisibility()
            }

            mBinding.root.setOnClickListener {
                onItemSelected(setting)
            }
        }

        private fun updateValue() {
            if (!isVisible) {
                mBinding.tvValue.text = "●●●●●●●●"
                return
            }

            mBinding.tvValue.apply {
                text = when (setting.type) {
                    Setting.Type.Time -> {
                        setting.value.toInt()
                            .secondsToTime()
                            .format("HH:mm")
                    }
                    Setting.Type.Date -> {
                        setting.value.toString()
                            .parseDate("dd-MM-yyyy")
                            ?.format("dd MMMM yyyy")
                            ?: ""
                    }
                    Setting.Type.Latitude -> {
                        setting.value.toDouble().latitudeDMS(context)
                    }
                    Setting.Type.Longitude -> {
                        setting.value.toDouble().longitudeDMS(context)
                    }
                    Setting.Type.Elevation -> {
                        setting.value.toInt().elevation(context)
                    }
                    else -> ContentResolver.getString(mBinding.root.context, setting.value.toString())
                }
            }
        }

        fun updateVisibility() {
            mBinding.cvVisibility.visibility =
                if (setting.isConfidential) View.VISIBLE else View.GONE

            mBinding.ivVisibility.setImageResource(
                if (isVisible) R.drawable.ic_round_visibility_off
                else R.drawable.ic_round_visibility
            )
        }

        var setting: Setting = Setting()
            set(value) {
                field = value
                mBinding.tvLabel.text =
                    ContentResolver.getString(mBinding.root.context, value.label)

                if (!value.isConfidential) {
                    isVisible = true
                }

                updateValue()
                updateVisibility()

                mBinding.clContainer.apply {
                    when {
                        items[adapterPosition - 1] !is Setting -> {
                            if (adapterPosition == items.lastIndex || items[adapterPosition + 1] !is Setting) {
                                setBackgroundResource(R.drawable.bg_setting_single)
                                setPadding(20.dip, 16.dip, 20.dip, 16.dip)
                            } else {
                                setBackgroundResource(R.drawable.bg_setting_top)
                                setPadding(20.dip, 16.dip, 20.dip, 8.dip)
                            }
                        }
                        adapterPosition == items.lastIndex || items[adapterPosition + 1] !is Setting -> {
                            setBackgroundResource(R.drawable.bg_setting_bottom)
                            setPadding(20.dip, 8.dip, 20.dip, 8.dip)
                            setPadding(20.dip, 8.dip, 20.dip, 16.dip)
                        }
                        else -> {
                            setBackgroundResource(R.drawable.bg_setting_middle)
                            setPadding(20.dip, 8.dip, 20.dip, 8.dip)
                        }
                    }
                }

                mBinding.ivIcon.setImageResource(
                    when (setting.type) {
                        Setting.Type.String -> {
                            if (value.isConfidential) {
                                R.drawable.ic_round_password
                            } else {
                                R.drawable.ic_round_text_fields
                            }
                        }
                        Setting.Type.Float,
                        Setting.Type.Integer -> R.drawable.ic_round_numbers
                        Setting.Type.Time -> R.drawable.ic_round_access_time
                        Setting.Type.Date -> R.drawable.ic_round_calendar_month
                        Setting.Type.WiFi -> {
                            if (value.isConfidential) {
                                R.drawable.ic_round_password
                            } else {
                                R.drawable.ic_round_wifi
                            }
                        }
                        Setting.Type.Latitude,
                        Setting.Type.Longitude -> R.drawable.ic_round_location_on
                        Setting.Type.Elevation -> R.drawable.ic_round_water
                        else -> R.drawable.ic_round_info
                    }
                )

                mBinding.ivArrow.visibility = when (setting.type) {
                    Setting.Type.Info -> View.GONE
                    else -> View.VISIBLE
                }
            }

    }

    private inner class ActionViewHolder(
        private val mBinding: ItemSettingActionBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.root.scaleOnClick {
                action.run()
            }
        }

        var action: Action = Action("", "", 0) {}
            set(value) {
                field = value
                mBinding.tvLabel.apply {
                    text = ContentResolver.getString(mBinding.root.context, value.label)
                    setTextColor(action.color)
                }

                mBinding.llContainer.apply {
                    background = (ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_setting_action
                    ) as? GradientDrawable)?.apply {
                        setStroke(1.2.dip, value.color)
                    }
                }

            }
    }

    private inner class FooterViewHolder(
        private val mBinding: ItemSettingFooterBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        var footer: Footer = Footer()
            set(value) {
                field = value
                mBinding.tvMessage.apply {
                    text = value.message
                }
            }

    }

    data class Action(
        var tag: String,
        var label: String,
        var color: Int,
        var run: () -> Unit
    )

    data class Footer(
        var message: String = ""
    )

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TITLE = 1
        private const val TYPE_CONTENT = 2
        private const val TYPE_ACTION = 3
        private const val TYPE_FOOTER = 4
    }

}