package id.dionix.kiro

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import id.dionix.kiro.adapter.PagerAdapter
import id.dionix.kiro.databinding.ActivityMainBinding
import id.dionix.kiro.dialog.NoLocationDialog
import id.dionix.kiro.dialog.PermissionDialog

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mLocationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val pagerAdapter = PagerAdapter(this@MainActivity)

        mBinding.viewPager.apply {
            adapter = pagerAdapter
            isUserInputEnabled = false
            offscreenPageLimit = 2
        }

        mBinding.bottomNavigation.setOnItemSelectedListener {
            mBinding.viewPager.setCurrentItem(it, false)
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
                if (!isGranted)  {
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

    }

}