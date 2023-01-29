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
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.classAddress
import com.machiav3lli.backup.preferences.persist_beenWelcomed
import com.machiav3lli.backup.ui.compose.navigation.IntroNavHost
import com.machiav3lli.backup.ui.compose.navigation.NavItem
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.isBiometricLockAvailable
import com.machiav3lli.backup.utils.isBiometricLockEnabled
import com.machiav3lli.backup.utils.isDeviceLockAvailable
import com.machiav3lli.backup.utils.isDeviceLockEnabled
import com.machiav3lli.backup.utils.setCustomTheme

class IntroActivityX : BaseActivity() {
    private lateinit var navController: NavHostController

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        OABX.activity = this
        setCustomTheme()
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                navController = rememberAnimatedNavController()

                Scaffold(
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) { paddingValues ->

                    SideEffect {
                        if (intent.extras != null) {
                            val fragmentNumber =
                                intent.extras!!.getInt(classAddress(".fragmentNumber"))
                            moveTo(fragmentNumber)
                        }
                    }

                    IntroNavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController,
                        persist_beenWelcomed.value
                    )
                }
            }

        }
    }

    fun moveTo(position: Int) {
        persist_beenWelcomed.value = position != 1
        when (position) {
            1 -> navController.navigate(NavItem.Welcome.destination)
            2 -> navController.navigate(NavItem.Permissions.destination)
            3 -> launchMainActivity()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    private fun launchMainActivity() {
        if (isBiometricLockAvailable() && isBiometricLockEnabled() && isDeviceLockEnabled()) {
            launchBiometricPrompt(true)
        } else if (isDeviceLockAvailable() && isDeviceLockEnabled()) {
            launchBiometricPrompt(false)
        } else {
            navController.navigate(NavItem.Main.destination)
        }
    }

    private fun launchBiometricPrompt(withBiometric: Boolean) {
        try {
            val biometricPrompt = createBiometricPrompt(this)
            val promptInfo = PromptInfo.Builder()
                .setTitle(getString(R.string.prefs_biometriclock))
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(DEVICE_CREDENTIAL or (if (withBiometric) BIOMETRIC_WEAK else 0))
                .build()
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Throwable) {
            startActivity(Intent(this, MainActivityX::class.java))
        }
    }

    private fun createBiometricPrompt(activity: Activity): BiometricPrompt {
        return BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(Intent(activity, MainActivityX::class.java))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        //binding.positiveButton.setText(R.string.dialog_unlock)
                        //binding.positiveButton.visibility = View.VISIBLE
                    } else {
                        //binding.positiveButton.visibility = View.GONE
                    }
                }
            })
    }
}