package id.dionix.kiro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import id.dionix.kiro.adapter.PagerAdapter
import id.dionix.kiro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding

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
    }

}