package id.dionix.kiro.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.databinding.ItemDeviceBinding
import id.dionix.kiro.model.DeviceConnection

class DeviceAdapter(
    onItemSelected: (device: DeviceConnection) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {

    private val mOnItemSelected = onItemSelected

    private var mItems: List<DeviceConnection> = listOf()
        set(value) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return field.size
                }

                override fun getNewListSize(): Int {
                    return value.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return field[oldItemPosition].name == value[newItemPosition].name
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

    fun setDevices(devices: List<DeviceConnection>) {
        mItems = devices.map { it.copy() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemDeviceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.device = mItems[position]
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class ViewHolder(
        private val mBinding: ItemDeviceBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.root.setOnClickListener {
                mOnItemSelected(device.copy())
            }
        }

        var device: DeviceConnection = DeviceConnection()
            set(value) {
                field = value
                mBinding.tvName.text = value.name
                mBinding.tvInfo.text = when {
                    value.isWifi -> value.mac
                    value.isLan -> value.ip
                    value.isInternet -> value.id
                    else -> value.version
                }

                mBinding.ivWifi.visibility = if (value.isWifi) View.VISIBLE else View.GONE
                mBinding.ivLan.visibility = if (value.isLan) View.VISIBLE else View.GONE
                mBinding.ivInternet.visibility = if (value.isInternet) View.VISIBLE else View.GONE
            }

    }

}