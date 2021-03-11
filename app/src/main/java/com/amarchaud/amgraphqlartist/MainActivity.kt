package com.amarchaud.amgraphqlartist

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.amarchaud.amgraphqlartist.databinding.ActivityMainBinding
import com.amarchaud.amgraphqlartist.view.ArtistsFragment
import com.amarchaud.amgraphqlartist.view.BookmarksFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // nav host
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.my_first_host_fragment) as NavHostFragment
        navController = navHostFragment.navController


        // do not display back arrow on...
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.splashFragment,
                R.id.artistsFragment,
                R.id.bookmarksFragment
            )
        )

        with(binding) {

            setSupportActionBar(toolbar)

            // if you want to control actionBar/bottomNav visibility
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.splashFragment -> {
                        supportActionBar?.hide()
                        bottomNav.visibility = View.GONE
                    }
                    else -> {
                        supportActionBar?.show()
                        bottomNav.visibility = View.VISIBLE
                    }
                }


                toolbar.setupWithNavController(navController, appBarConfiguration)
            }

            // action bar
            //setupActionBarWithNavController(navController, appBarConfiguration)

            // bottom nav
            bottomNav.setupWithNavController(navController)
        }
    }

    private fun getForegroundFragment(): Fragment? {
        val navHostFragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.my_first_host_fragment)
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    override fun onBackPressed() {

        val currentFragment = getForegroundFragment()
        currentFragment?.let {

            if (it is ArtistsFragment || it is BookmarksFragment) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.exitAppTitle)
                    .setMessage(R.string.exitAppBody)
                    .setPositiveButton(android.R.string.ok) { dialog, which ->
                        finish()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, which ->
                        dialog.dismiss()
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            } else {
                super.onBackPressed()
            }
        } ?: super.onBackPressed()
    }
}