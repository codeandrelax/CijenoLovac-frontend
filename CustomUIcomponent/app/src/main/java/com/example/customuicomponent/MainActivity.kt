package com.example.customuicomponent

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var frameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tabLayout = findViewById(R.id.tabLayout)
        frameLayout = findViewById(R.id.framelayout)

        // Set default fragment on app start
        setFragment(UserLoginFragment())

        // Set up a listener for tab selection
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedFragment: Fragment = when (tab?.position) {
                    0 -> ScannedReceipt() // First tab selected
                    1 -> getCheapest() // Second tab selected
                    else -> ScannedReceipt() // Default case
                }
                // Replace the FrameLayout with the selected fragment
                setFragment(selectedFragment)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Do something when the tab is unselected (optional)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Do something when the tab is reselected (optional)
            }
        })
    }

    // Helper function to replace fragments
    fun setFragment(fragment: Fragment) {
        // Replace the current fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.framelayout, fragment)
            .commit()

        // Show or hide TabLayout based on the fragment
        if (fragment is UserLoginFragment || fragment is UserSignUpFragment) {
            hideTabLayout()
        } else {
            showTabLayout()
        }
    }

    private fun hideTabLayout() {
        tabLayout.visibility = View.GONE
    }

    private fun showTabLayout() {
        tabLayout.visibility = View.VISIBLE
    }
}