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
import com.machiav3lli.backup.R
import com.machiav3lli.backup.databinding.ActivityPrefsBinding
import com.machiav3lli.backup.fragments.HelpSheet
import com.machiav3lli.backup.handler.getApplicationList
import com.machiav3lli.backup.items.AppInfo
import com.machiav3lli.backup.utils.FileUtils
import com.machiav3lli.backup.utils.StorageLocationNotConfiguredException
import com.machiav3lli.backup.utils.setCustomTheme

class PrefsActivity : BaseActivity() {
    private lateinit var binding: ActivityPrefsBinding
    private var sheetHelp: HelpSheet? = null
    var appInfoList: List<AppInfo> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        setCustomTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityPrefsBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)
        appInfoList = oabx.cache.get("appInfoList") ?: mutableListOf()
        if (appInfoList.isNullOrEmpty()) refreshAppsList()
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
            if (sheetHelp == null) sheetHelp = HelpSheet()
            sheetHelp!!.showNow(supportFragmentManager, "HELPSHEET")
        }
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == binding.bottomNavigation.selectedItemId) return@setOnNavigationItemSelectedListener false
            navController.navigate(item.itemId)
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
    }

    fun refreshAppsList() {
        appInfoList = listOf()
        Thread {
            try {
                appInfoList = getApplicationList(listOf())
            } catch (e: FileUtils.BackupLocationIsAccessibleException) {
                e.printStackTrace()
            } catch (e: StorageLocationNotConfiguredException) {
                e.printStackTrace()
            }
        }.start()
    }
}