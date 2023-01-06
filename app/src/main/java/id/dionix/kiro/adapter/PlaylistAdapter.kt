package id.dionix.kiro.adapter

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ContextWrapper
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import id.dionix.kiro.R
import id.dionix.kiro.databinding.ItemPlaylistActionBinding
import id.dionix.kiro.databinding.ItemPlaylistBinding
import id.dionix.kiro.dialog.AudioPickerDialog
import id.dionix.kiro.dialog.AudioPreviewDialog
import id.dionix.kiro.model.Qiro
import id.dionix.kiro.model.Surah
import id.dionix.kiro.model.SurahAudio
import id.dionix.kiro.model.SurahProperties
import id.dionix.kiro.utility.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

class PlaylistAdapter(
    onChange: (surahList: List<Surah>) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mOnChange = onChange

    private var mItems: MutableList<Any> = mutableListOf()
    private var mTotalDuration: Int = 0
    private var mIsOpenDialog = false

    init {
        mItems.add(
            Action("Add") {
                if (!mIsOpenDialog) {
                    mIsOpenDialog = true
                    AudioPickerDialog(
                        onItemSelected = {
                            addSurah(it)
                        },
                        onDismiss = {
                            mIsOpenDialog = false
                        }
                    ).show(mSupportFragmentManager, "dialog_audio_picker")
                }
            }
        )
    }

    fun setQiro(qiro: Qiro) {
        mTotalDuration = qiro.durationMinutes * 60

        CoroutineScope(Dispatchers.IO).launch {
            val newItems = buildList {
                qiro.surahList.forEach {
                    add(ContentResolver.getSurahProperties(it))
                }

                add(
                    Action("Add") {
                        if (!mIsOpenDialog) {
                            mIsOpenDialog = true
                            AudioPickerDialog(
                                onItemSelected = {
                                    addSurah(it)
                                },
                                onDismiss = {
                                    mIsOpenDialog = false
                                }
                            ).show(mSupportFragmentManager, "dialog_audio_picker")
                        }
                    }
                )
            }

            runMain {
                mItems = newItems.toMutableList()
                notifyItemRangeChanged(0, mItems.size, mItems)
            }
        }
    }

    fun setTotalDuration(duration: Int) {
        mTotalDuration = duration * 60
        notifyItemRangeChanged(0, mItems.lastIndex, duration)
    }

    private fun addSurah(surahProps: SurahProperties) {
        val position = mItems.lastIndex
        mItems.add(position, surahProps)
        notifyItemInserted(position)
        mOnChange(mItems.filterIsInstance<SurahProperties>().map { it.toSurah() })
    }

    private fun removeSurah(position: Int) {
        mItems.removeAt(position)
        notifyItemRemoved(position)
        mOnChange(mItems.filterIsInstance<SurahProperties>().map { it.toSurah() })
    }

    private fun updateSurah(position: Int, surahProps: SurahProperties) {
        mItems[position] = surahProps
        notifyItemChanged(position, surahProps)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SURAH -> SurahViewHolder(
                ItemPlaylistBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> ActionViewHolder(
                ItemPlaylistActionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = mItems[position]
        when {
            holder is SurahViewHolder && item is SurahProperties -> holder.surah = item
            holder is ActionViewHolder && item is Action -> holder.action = item
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (mItems[position]) {
            is SurahProperties -> TYPE_SURAH
            else -> TYPE_ACTION
        }
    }

    private inner class SurahViewHolder(
        private val mBinding: ItemPlaylistBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.root.setOnLongClickListener {
                itemTouchHelper.startDrag(this)
                return@setOnLongClickListener true
            }

            mBinding.root.apply {
                setOnClickListener {
                    if (!mIsOpenDialog) {
                        mIsOpenDialog = true
                        AudioPreviewDialog(
                            surah.name,
                            context.getString(R.string.apply),
                            SurahAudio(surah.id, surah.volume, isPaused = false, isPlaying = false),
                            onApply =  { audio ->
                                updateSurah(adapterPosition, surah.copy(volume = audio.volume))
                            },
                            onDismiss = {
                                mIsOpenDialog = false
                            }
                        ).show(mSupportFragmentManager, "dialog_audio_preview")
                        AudioPickerDialog(
                            onItemSelected = {
                                updateSurah(adapterPosition, it)
                            },
                            onDismiss = {
                                mIsOpenDialog = false
                            }
                        ).show(mSupportFragmentManager, "dialog_audio_picker")
                    }
                }
            }
        }

        var surah: SurahProperties = SurahProperties()
            set(value) {
                field = value
                mBinding.tvName.text = value.name
                mBinding.tvVolume.text = value.volume.toString()
                mBinding.tvDuration.text = value.durationSeconds.secondsToTime().format("HH:mm:ss")

                durationAnimator?.cancel()
                mBinding.cvDurationBackground.doOnLayout {
                    durationAnimator = ValueAnimator.ofInt(
                        mBinding.cvDurationForeground.measuredWidth,
                        (calculateRelativePlaytime(adapterPosition) * mBinding.cvDurationBackground.measuredWidth).roundToInt()
                    ).apply {
                        addUpdateListener {
                            mBinding.cvDurationForeground.apply {
                                val params = layoutParams.apply {
                                    width = it.animatedValue as Int
                                }
                                layoutParams = params
                            }
                        }
                        duration = 200
                        start()
                    }
                }
            }

        fun onStartDragging() {
            backgroundAnimator.start()
        }

        fun onStopDragging() {
            backgroundAnimator.reverse()
        }

        private var durationAnimator: ValueAnimator? = null

        private val backgroundAnimator = ObjectAnimator.ofArgb(
            mBinding.clContainer,
            "backgroundColor",
            mBinding.root.context.getColor(R.color.transparent),
            mBinding.root.context.getColor(R.color.background_tertiary)
        ).apply {
            duration = 200
        }

    }

    private inner class ActionViewHolder(
        mBinding: ItemPlaylistActionBinding
    ) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.root.setOnClickListener {
                action.run()
            }
        }

        var action: Action = Action()

    }

    private fun calculateRelativePlaytime(position: Int): Float {
        var elapsed = 0
        val surah = mItems.getOrNull(position) ?: return 0f

        if (surah !is SurahProperties) {
            return 0f
        }

        for (i in 0 until position) {
            val item = mItems[i]
            if (item is SurahProperties) {
                elapsed += item.durationSeconds
            }
        }

        if (elapsed > mTotalDuration) {
            return 0f
        }

        val remaining = mTotalDuration - elapsed
        if (remaining > surah.durationSeconds) {
            return 1f
        }

        return 1f - (surah.durationSeconds - remaining) / surah.durationSeconds.toFloat()
    }

    private lateinit var mSupportFragmentManager: FragmentManager

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        var context = recyclerView.context
        while (context !is FragmentActivity && context is ContextWrapper) {
            context = context.baseContext
        }
        mSupportFragmentManager = (context as FragmentActivity).supportFragmentManager
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        itemTouchHelper.attachToRecyclerView(null)
    }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                if (viewHolder !is SurahViewHolder || target !is SurahViewHolder) {
                    return false
                }

                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                Collections.swap(mItems, from, to)
                notifyItemMoved(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (viewHolder is SurahViewHolder) {
                    removeSurah(viewHolder.adapterPosition)
                }
            }

            override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 15.dp / viewHolder.itemView.measuredHeight
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    (viewHolder as? SurahViewHolder)?.onStartDragging()
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ) {
                super.clearView(recyclerView, viewHolder)
                (viewHolder as? SurahViewHolder)?.onStopDragging()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean,
            ) {
                var deltaY = dY
                var deltaX = dX

                if (viewHolder.adapterPosition <= 0 && dY < 0) {
                    deltaY = 0f
                }

                if (viewHolder.adapterPosition >= itemCount - 2 && dY > 0) {
                    deltaY = 0f
                }

                if (viewHolder is ActionViewHolder) {
                    deltaX = 0f
                }

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    deltaX,
                    deltaY,
                    actionState,
                    isCurrentlyActive
                )
            }

        })
    }

    data class Action(
        var label: String = "",
        var run: () -> Unit = {}
    )

    companion object {
        private const val TYPE_SURAH = 0
        private const val TYPE_ACTION = 1
    }

}