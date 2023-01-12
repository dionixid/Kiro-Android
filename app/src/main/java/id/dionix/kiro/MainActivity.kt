package id.dionix.kiro

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.codedillo.tinydb.TinyDB
import id.dionix.kiro.adapter.PagerAdapter
import id.dionix.kiro.database.AppDatabase
import id.dionix.kiro.database.DataViewModel
import id.dionix.kiro.databinding.ActivityMainBinding
import id.dionix.kiro.dialog.NoLocationDialog
import id.dionix.kiro.dialog.PermissionDialog
import id.dionix.kiro.model.Notification
import id.dionix.kiro.model.SurahAudio
import id.dionix.kiro.model.SurahCollection
import id.dionix.kiro.utility.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mLocationPermissionLauncher: ActivityResultLauncher<String>

    private val mDataViewModel by viewModels<DataViewModel>()

    private var mAudioPreview: SurahAudio? = null
        set(value) {
            field = value
            if (value != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val name = ContentResolver.getSurahProperties(value.toSurah()).name
                    runMain {
                        mBinding.tvSurahPreviewName.text = name
                    }
                }

                mBinding.ivPlay.imageTintList = ColorStateList.valueOf(
                    getColor(
                        if (!value.isPlaying || value.isPaused) R.color.green
                        else R.color.disabled
                    )
                )

                mBinding.ivPause.imageTintList = ColorStateList.valueOf(
                    getColor(
                        if (value.isPlaying && !value.isPaused) R.color.yellow
                        else R.color.disabled
                    )
                )

                mBinding.ivStop.imageTintList = ColorStateList.valueOf(
                    getColor(
                        if (value.isPlaying) R.color.red
                        else R.color.disabled
                    )
                )
            } else {
                ColorStateList.valueOf(getColor(R.color.disabled)).let {
                    mBinding.ivPlay.imageTintList = it
                    mBinding.ivPause.imageTintList = it
                    mBinding.ivStop.imageTintList = it
                }
            }
        }

    private var surahUpdateAnimator = ValueAnimator()

    private var surahCollection: SurahCollection = SurahCollection()
        set(value) {
            surahUpdateAnimator.cancel()
            surahUpdateAnimator = ValueAnimator.ofInt(field.percentInt, value.percentInt).apply {
                addUpdateListener {
                    mBinding.tvSurahUpdateProgressValue.text =
                        String.format("%d%%", it.animatedValue as Int)
                }
                duration = 300
                start()
            }
            field = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        WiFi.initialize(applicationContext)
        UDP.initialize(applicationContext)
        TinyDB.initialize(applicationContext)
        AppDatabase.initialize(applicationContext)
        SingleToast.initialize(applicationContext)

        WiFi.enableLog()
        Log.enable(Log.Priority.ASSERT)

        mDataViewModel.initialize()
        mDataViewModel.attachToLifecycle(lifecycle)

        Config.loadDevice { device ->
            Config.loadApp(applicationContext) { app ->
                mDataViewModel.setClientID(app.name, app.id)
                mDataViewModel.setServer(device.ip, 80)
                mDataViewModel.join(device.key)
            }
        }

        Config.loadCollection {
            mDataViewModel.setSurahCollection(it)
        }

        mDataViewModel.device.observe(this) { device ->
            if (device != null) {
                mDataViewModel.fetchSurahList()
            }
        }

        val pagerAdapter = PagerAdapter(this@MainActivity)

        mBinding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false
            offscreenPageLimit = 2
        }

        mBinding.bottomNavigation.setOnItemSelectedListener {
            mBinding.viewPager.setCurrentItem(it, false)
            mBackPressedCallback.isEnabled = it != 0
        }

        mDataViewModel.notification.observe(this) { notification ->
            if (notification == null) {
                hideNotification()
            } else {
                showNotification(notification)
            }
        }

        mDataViewModel.surahPreview.observe(this) { audio ->
            mAudioPreview = audio
            if (audio.isPlaying) {
                showSurahPreview()
            } else {
                hideSurahPreview()
            }
        }

        mDataViewModel.isUpdatingSurahCollection.observe(this) { isFetching ->
            if (isFetching) {
                showSurahUpdate()
            } else {
                hideSurahUpdate()
            }
        }

        mDataViewModel.surahCollection.observe(this) {
            surahCollection = it
        }

        mBinding.cvNotificationContainer.apply {
            visibility = View.GONE
            isClickable = true
            mBinding.root.doOnLayout {
                translationX = -mBinding.root.measuredWidth.toFloat()
            }
        }

        mBinding.clSurahUpdate.apply {
            visibility = View.GONE
            isClickable = true
            layoutParams = (layoutParams as MarginLayoutParams).apply {
                bottomMargin = (-48).dip
            }
        }

        mBinding.clSurahPreview.apply {
            visibility = View.GONE
            isClickable = true
            layoutParams = (layoutParams as MarginLayoutParams).apply {
                bottomMargin = (-48).dip
            }
        }

        mBinding.cvCloseNotification.scaleOnClick {
            hideNotification()
        }

        mBinding.cvPlay.scaleOnClick {
            mAudioPreview?.apply {
                if (!isPlaying || isPaused) {
                    mDataViewModel.sendSurahPreview(copy(isPlaying = true, isPaused = false))
                }
            }
        }

        mBinding.cvPause.scaleOnClick {
            mAudioPreview?.apply {
                if (isPlaying && !isPaused) {
                    mDataViewModel.sendSurahPreview(copy(isPaused = true))
                }
            }
        }

        mBinding.cvStop.scaleOnClick {
            mAudioPreview?.apply {
                if (isPlaying) {
                    mDataViewModel.sendSurahPreview(copy(isPlaying = false, isPaused = false))
                }
            }
        }

        fun openNoLocationDialog() {
            NoLocationDialog(
                onPermit = {
                    mLocationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                },
                onExit = {
                    finish()
                }
            ).show(supportFragmentManager, "dialog_no_permission")
        }

        mLocationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    openNoLocationDialog()
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Do Nothing
            }

            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                PermissionDialog(
                    onContinue = {
                        mLocationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                    },
                    onReject = {
                        openNoLocationDialog()
                    }
                ).show(supportFragmentManager, "dialog_permission")
            }

            else -> {
                PermissionDialog(
                    onContinue = {
                        mLocationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                    },
                    onReject = {
                        openNoLocationDialog()
                    }
                ).show(supportFragmentManager, "dialog_permission")
            }
        }

        onBackPressedDispatcher.addCallback(mBackPressedCallback)
    }

    private val notificationTimer = makeTimer(5000) {
        mDataViewModel.setNotification(null)
    }

    private fun showNotification(notification: Notification) {
        notificationTimer.cancel()
        mBinding.tvNotification.text = notification.message
        mBinding.ivNotification.setImageResource(
            if (notification.isError) R.drawable.ic_round_nearby_error
            else R.drawable.ic_round_check
        )
        mBinding.cvNotification.setCardBackgroundColor(
            getColor(
                if (notification.isError) R.color.red
                else R.color.green
            )
        )

        fun show() {
            mBinding.cvNotificationContainer.apply {
                visibility = View.VISIBLE
                post {
                    animate()
                        .setDuration(300)
                        .translationX(0f)
                        .withEndAction {
                            notificationTimer.start()
                        }
                }
            }
        }

        if (mBinding.cvNotificationContainer.translationX == 0f) {
            hideNotification(::show)
        } else {
            show()
        }
    }

    private fun hideNotification(onFinish: (() -> Unit)? = null) {
        notificationTimer.cancel()
        mBinding.root.doOnLayout {
            mBinding.cvNotificationContainer
                .animate()
                .setDuration(300)
                .translationX(-mBinding.root.measuredWidth.toFloat())
                .withEndAction {
                    if (onFinish != null) {
                        onFinish()
                    } else {
                        mBinding.cvNotificationContainer.visibility = View.GONE
                    }
                }
        }
    }

    private var updateAnimator = ValueAnimator()

    private fun showSurahUpdate() {
        updateAnimator.cancel()
        mBinding.clSurahUpdate.apply {
            visibility = View.VISIBLE
            post {
                val params = (layoutParams as MarginLayoutParams)
                updateAnimator = ValueAnimator.ofInt(params.bottomMargin, 0).apply {
                    addUpdateListener {
                        params.bottomMargin = it.animatedValue as Int
                        layoutParams = params
                    }
                    duration = 200
                    start()
                }
            }
        }
    }

    private fun hideSurahUpdate() {
        updateAnimator.cancel()
        mBinding.clSurahUpdate.apply {
            visibility = View.VISIBLE
            post {
                val params = (layoutParams as MarginLayoutParams)
                updateAnimator = ValueAnimator.ofInt(params.bottomMargin, (-48).dip).apply {
                    addUpdateListener {
                        params.bottomMargin = it.animatedValue as Int
                        layoutParams = params
                    }

                    addListener(object : AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}

                        override fun onAnimationEnd(animation: Animator) {
                            visibility = View.GONE
                        }
                    })

                    duration = 200
                    start()
                }
            }
        }
    }

    private var previewAnimator = ValueAnimator()

    private fun showSurahPreview() {
        previewAnimator.cancel()
        mBinding.clSurahPreview.apply {
            visibility = View.VISIBLE
            post {
                val params = (layoutParams as MarginLayoutParams)
                previewAnimator = ValueAnimator.ofInt(params.bottomMargin, 0).apply {
                    addUpdateListener {
                        params.bottomMargin = it.animatedValue as Int
                        layoutParams = params
                    }
                    duration = 200
                    start()
                }
            }
        }
    }

    private fun hideSurahPreview() {
        previewAnimator.cancel()
        mBinding.clSurahPreview.apply {
            visibility = View.VISIBLE
            post {
                val params = (layoutParams as MarginLayoutParams)
                previewAnimator = ValueAnimator.ofInt(params.bottomMargin, (-48).dip).apply {
                    addUpdateListener {
                        params.bottomMargin = it.animatedValue as Int
                        layoutParams = params
                    }

                    addListener(object : AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}
                        override fun onAnimationCancel(animation: Animator) {}
                        override fun onAnimationRepeat(animation: Animator) {}

                        override fun onAnimationEnd(animation: Animator) {
                            visibility = View.GONE
                        }
                    })

                    duration = 200
                    start()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        UDP.release()
    }

    private val mBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            mBinding.bottomNavigation.currentItem = 0
        }
    }

}