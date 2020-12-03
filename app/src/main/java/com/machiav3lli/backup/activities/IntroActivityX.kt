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

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import com.machiav3lli.backup.PREFS_FIRST_LAUNCH
import com.machiav3lli.backup.R
import com.machiav3lli.backup.classAddress
import com.machiav3lli.backup.classTag
import com.machiav3lli.backup.databinding.ActivityIntroXBinding
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.handler.ShellHandler.ShellCommandFailedException
import com.machiav3lli.backup.utils.getPrivateSharedPrefs
import com.machiav3lli.backup.utils.isBiometricLockAvailable
import com.machiav3lli.backup.utils.isLockEnabled
import com.machiav3lli.backup.utils.showWarning
import com.scottyab.rootbeer.RootBeer

class IntroActivityX : BaseActivity() {
    private lateinit var binding: ActivityIntroXBinding
    private lateinit var prefs: SharedPreferences
    private var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroXBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = getPrivateSharedPrefs(this)
        setupNavigation()
        if (intent.extras != null) {
            val fragmentNumber = intent.extras!!.getInt(classAddress(".fragmentNumber"))
            moveTo(fragmentNumber)
        }
    }

    private fun setupNavigation() {
        navController = Navigation.findNavController(this, R.id.introContainer)
        navController!!.addOnDestinationChangedListener { _: NavController?, destination: NavDestination, _: Bundle? ->
            if (destination.id == R.id.welcomeFragment) {
                binding.positiveButton.setText(R.string.dialog_start)
                binding.positiveButton.setOnClickListener {
                    if (checkRootAccess()) {
                        prefs.edit().putBoolean(PREFS_FIRST_LAUNCH, false).apply()
                        moveTo(2)
                    }
                }
            }
        }
    }

    fun moveTo(position: Int) {
        when (position) {
            1 -> navController?.navigate(R.id.welcomeFragment)
            2 -> {
                navController?.navigate(R.id.permissionsFragment)
                binding.positiveButton.visibility = View.GONE
            }
            3 -> {
                binding.positiveButton.visibility = View.VISIBLE
                binding.positiveButton.setOnClickListener { launchMainActivity() }
                launchMainActivity()
            }
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun checkRootAccess(): Boolean {
        val rootBeer = RootBeer(this)
        if (!rootBeer.isRooted) {
            showFatalUiWarning(this.getString(R.string.noSu))
            return false
        }
        try {
            ShellHandler.runAsRoot("id")
        } catch (e: ShellCommandFailedException) {
            showFatalUiWarning(this.getString(R.string.noSu))
            return false
        }
        return true
    }

    private fun showFatalUiWarning(message: String) {
        showWarning(this, TAG, message) { _: DialogInterface?, _: Int -> finishAffinity() }
    }

    private fun launchMainActivity() {
        if (isBiometricLockAvailable(this) && isLockEnabled(this)) {
            launchBiometricPrompt()
        } else {
            startActivity(Intent(this, MainActivityX::class.java))
            overridePendingTransition(0, 0)
        }
    }

    private fun launchBiometricPrompt() {
        val biometricPrompt = createBiometricPrompt(this)
        val promptInfo = PromptInfo.Builder()
                .setTitle(getString(R.string.prefs_biometriclock))
                .setConfirmationRequired(true)
                .setDeviceCredentialAllowed(true)
                .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun createBiometricPrompt(activity: Activity): BiometricPrompt {
        return BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                startActivity(Intent(activity, MainActivityX::class.java))
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricConstants.ERROR_USER_CANCELED) {
                    binding.positiveButton.setText(R.string.dialog_unlock)
                    binding.positiveButton.visibility = View.VISIBLE
                } else {
                    binding.positiveButton.visibility = View.GONE
                }
            }
        })
    }

    companion object {
        private val TAG = classTag(".IntroActivityX")
    }
}