package id.dionix.kiro.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.WindowInsetsCompat
import com.codedillo.rttp.model.Value
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.maps.android.ktx.addMarker
import id.dionix.kiro.R
import id.dionix.kiro.databinding.DialogLocationBinding
import id.dionix.kiro.model.Setting
import id.dionix.kiro.utility.*

class LocationDialog(
    latitude: Setting,
    longitude: Setting,
    elevation: Setting,
    private val onDismiss: () -> Unit = {}
) : AppCompatDialogFragment(), OnMapReadyCallback {

    private lateinit var mBinding: DialogLocationBinding
    private lateinit var mMap: GoogleMap
    private lateinit var mMarker: Marker

    private val mLatitude = latitude.copy()
    private val mLongitude = longitude.copy()
    private val mElevation = elevation.copy()

    private lateinit var mLocationProvider: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object: AppCompatDialog(requireContext(), R.style.Theme_Kiro_Fullscreen) {
            init {
                onBackPressedDispatcher.addCallback {
                    dialog?.window?.decorView?.let {
                        it.animate()
                            .setDuration(200)
                            .translationX(it.measuredWidth.toFloat())
                            .withEndAction {
                                dismiss()
                            }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss()
    }

    override fun dismiss() {
        dialog?.window?.decorView?.let {
            it.animate()
                .setDuration(200)
                .translationX(it.measuredWidth.toFloat())
                .withEndAction {
                    super.dismiss()
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogLocationBinding.inflate(inflater, container, false)

        dialog?.window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
            mBinding.mapView.apply {
                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    topMargin = -WindowInsetsCompat.toWindowInsetsCompat(insets)
                        .getInsets(WindowInsetsCompat.Type.statusBars()).top
                }
            }

            // This is a workaround for SoftInput covering edit text. For some reason,
            // the adjust pan mode does not move the editText up, although it seems that
            // the map itself was panned vertically to top.
            mBinding.cvSave.apply {
                val imeHeight = WindowInsetsCompat.toWindowInsetsCompat(insets)
                    .getInsets(WindowInsetsCompat.Type.ime()).bottom

                layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    bottomMargin = 16.dip + (if (imeHeight == 0) 0 else (imeHeight - 146.dip))
                }

                if (imeHeight == 0) {
                    clearEditTextFocus()
                }
            }

            return@setOnApplyWindowInsetsListener insets
        }

        dialog?.window?.decorView?.let {
            it.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    it.translationX = it.measuredWidth.toFloat()
                    it.post {
                        it.animate()
                            .setDuration(200)
                            .translationX(0f)
                    }
                }
            })
        }

        mBinding.mapView.onCreate(savedInstanceState)
        mBinding.mapView.getMapAsync(this)

        mBinding.etElevation.setText(mElevation.value.toInt().toString())
        updateLatLngText()

        mBinding.etElevation.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                clearEditTextFocus()
            }
            return@setOnEditorActionListener false
        }

        mBinding.tvLatitude.setOnClickListener {
            // Do Nothing. Prevent clicking the map behind.
        }

        mBinding.tvLongitude.setOnClickListener {
            // Do Nothing. Prevent clicking the map behind.
        }

        mBinding.cvMyLocation.scaleOnClick {
            clearEditTextFocus()
            getDeviceLocation()
        }

        mBinding.cvSave.scaleOnClick {
            // TODO Send Data to Server
            dismiss()
        }

        return mBinding.root
    }

    private fun updateLatLngText() {
        mBinding.tvLatitude.text = mLatitude.value.toDouble().latitudeDMS(requireContext())
        mBinding.tvLongitude.text = mLongitude.value.toDouble().longitudeDMS(requireContext())
    }

    private fun updateMarker() {
        val latLng = LatLng(mLatitude.value.toDouble(), mLongitude.value.toDouble())
        mMarker.remove()
        mMarker = mMap.addMarker {
            position(latLng)
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setPadding(0, 0, 0, 16.dip)

        if (isDarkMode(requireContext())) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.night_map))
        }

        try {
            mMap.isMyLocationEnabled = true
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isCompassEnabled = true
        mMap.setOnMapClickListener {
            mLatitude.value = Value(it.latitude)
            mLongitude.value = Value(it.longitude)
            updateLatLngText()
            updateMarker()
            clearEditTextFocus()
        }

        val pos = LatLng(mLatitude.value.toDouble(), mLongitude.value.toDouble())
        mMarker = mMap.addMarker {
            position(pos)
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))

        mLocationProvider = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun clearEditTextFocus() {
        mBinding.etElevation.clearFocus()
    }

    private fun getDeviceLocation() {
        try {
            mLocationProvider
                .getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    object : CancellationToken() {
                        override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                            return CancellationTokenSource().token
                        }

                        override fun isCancellationRequested(): Boolean {
                            return false
                        }
                    }
                )
                .addOnFailureListener {
                    it.printStackTrace()
                }
                .addOnSuccessListener {
                    mLatitude.value = Value(it.latitude)
                    mLongitude.value = Value(it.longitude)
                    updateLatLngText()
                    updateMarker()
                }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        mBinding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mBinding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mBinding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mBinding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mBinding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding.mapView.onLowMemory()
    }

}