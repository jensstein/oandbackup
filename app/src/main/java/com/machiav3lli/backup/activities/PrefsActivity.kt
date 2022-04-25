/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ActivityPrefsBinding
import com.machiav3lli.backup.fragments.HelpSheet
import com.machiav3lli.backup.items.Package
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.itemIdToOrder
import com.machiav3lli.backup.utils.navigateLeft
import com.machiav3lli.backup.utils.navigateRight
import com.machiav3lli.backup.utils.setCustomTheme

class PrefsActivity : BaseActivity() {
    lateinit var binding: ActivityPrefsBinding
    private var sheetHelp: HelpSheet? = null
    //var packageList: List<Package> = mutableListOf()
    val packageList: MutableList<Package>
            get() = OABX.activity?.viewModel?.packageList?.value ?: mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        setCustomTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityPrefsBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)
        //packageList = OABX.app.cache.get("appInfoList") ?: mutableListOf()
        //if (packageList.isNullOrEmpty()) refreshPackageList()
    }

    override fun onStart() {
        super.onStart()
        setupOnClicks()
        setupNavigation()
    }

    private fun setupOnClicks() {
        binding.backButton.setOnClickListener {
            if (supportFragmentManager.backStackEntryCount == 0) super.onBackPressed()
            else supportFragmentManager.popBackStack()
        }
        binding.helpButton.setOnClickListener {
            if (sheetHelp != null && sheetHelp!!.isVisible) sheetHelp?.dismissAllowingStateLoss()
            sheetHelp = HelpSheet()
            sheetHelp!!.showNow(supportFragmentManager, "HELPSHEET")
        }
    }

    private fun setupNavigation() {
        try {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
            val navController = navHostFragment.navController

            binding.bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
                if (item.itemId == binding.bottomNavigation.selectedItemId) return@setOnItemSelectedListener false
                if (binding.bottomNavigation.selectedItemId.itemIdToOrder() < item.itemId.itemIdToOrder())
                    navController.navigateRight(item.itemId)
                else
                    navController.navigateLeft(item.itemId)
                true
            }
            navController.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
                binding.pageHeadline.text = when (destination.id) {
                    R.id.userFragment -> getString(R.string.prefs_user)
                    R.id.serviceFragment -> getString(R.string.prefs_service)
                    R.id.advancedFragment -> getString(R.string.prefs_advanced)
                    R.id.toolsFragment -> getString(R.string.prefs_tools)
                    else -> getString(R.string.prefs_title)
                }
            }
            if (intent.extras != null && intent.extras!!.getBoolean(".toEncryption", false)) {
                navController.navigate(R.id.serviceFragment)
            }
        } catch (e: ClassCastException) {
            finish()
            startActivity(intent)
        }
    }

    // TODO use database-based structure
    fun refreshPackageList() {
        //packageList = listOf()
        Thread {
            try {
                //packageList = getPackageList()
            } catch (e: FileUtils.BackupLocationInAccessibleException) {
                e.printStackTrace()
            } catch (e: StorageLocationNotConfiguredException) {
                e.printStackTrace()
            }
        }.start()
    }
}