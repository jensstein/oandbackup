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
import android.os.PowerManager
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.machiav3lli.backup.BuildConfig
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.classAddress
import com.machiav3lli.backup.preferences.extendedInfo
import com.machiav3lli.backup.preferences.persist_beenWelcomed
import com.machiav3lli.backup.preferences.persist_ignoreBatteryOptimization
import com.machiav3lli.backup.preferences.textLogShare
import com.machiav3lli.backup.ui.compose.icons.Phosphor
import com.machiav3lli.backup.ui.compose.icons.phosphor.ArrowRight
import com.machiav3lli.backup.ui.compose.icons.phosphor.ShareNetwork
import com.machiav3lli.backup.ui.compose.icons.phosphor.Warning
import com.machiav3lli.backup.ui.compose.item.ElevatedActionButton
import com.machiav3lli.backup.ui.compose.navigation.NavItem
import com.machiav3lli.backup.ui.compose.theme.AppTheme
import com.machiav3lli.backup.utils.SystemUtils.applicationIssuer
import com.machiav3lli.backup.utils.checkCallLogsPermission
import com.machiav3lli.backup.utils.checkContactsPermission
import com.machiav3lli.backup.utils.checkRootAccess
import com.machiav3lli.backup.utils.checkSMSMMSPermission
import com.machiav3lli.backup.utils.checkUsageStatsPermission
import com.machiav3lli.backup.utils.hasStoragePermissions
import com.machiav3lli.backup.utils.isBiometricLockAvailable
import com.machiav3lli.backup.utils.isBiometricLockEnabled
import com.machiav3lli.backup.utils.isDeviceLockAvailable
import com.machiav3lli.backup.utils.isDeviceLockEnabled
import com.machiav3lli.backup.utils.isStorageDirSetAndOk
import com.topjohnwu.superuser.Shell
import kotlin.system.exitProcess

@Preview
@Composable
fun NoRootPreview() {
    OABX.fakeContext = LocalContext.current.applicationContext
    RootMissing()
    OABX.fakeContext = null
}

@Preview
@Composable
fun SplashPreview() {
    OABX.fakeContext = LocalContext.current.applicationContext
    SplashPage()
    OABX.fakeContext = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootMissing(activity: Activity? = null) {
    AppTheme {
        Scaffold { paddingValues ->
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(50.dp)
            ) {
                Text(
                    text = stringResource(R.string.root_missing),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.root_is_mandatory),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.see_faq),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(80.dp))
                ElevatedActionButton(
                    text = "try to share a support log",
                    icon = Phosphor.ShareNetwork,
                    fullWidth = true,
                    modifier = Modifier
                ) {
                    textLogShare(extendedInfo(), temporary = true)
                }
                Spacer(modifier = Modifier.height(80.dp))
                ElevatedActionButton(
                    text = stringResource(id = R.string.dialogOK),
                    icon = Phosphor.Warning,
                    fullWidth = true,
                    modifier = Modifier
                ) {
                    activity?.finishAffinity()
                    exitProcess(0)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashPage() {
    AppTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.6f))
                Image(
                    modifier = Modifier
                        .fillMaxSize(0.5f),
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = stringResource(id = R.string.app_name)
                )
                Spacer(modifier = Modifier.weight(0.4f))
                Text(
                    text = listOf(
                        BuildConfig.APPLICATION_ID,
                        BuildConfig.VERSION_NAME,
                        applicationIssuer?.let { "signed by $it" } ?: "",
                    ).joinToString("\n"),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Shell.getShell()

        setContent {
            SplashPage()
        }

        val powerManager = this.getSystemService(POWER_SERVICE) as PowerManager

        if (!checkRootAccess()) {
            setContent {
                RootMissing(this)
            }
            return
        }

        val mainIntent = Intent(applicationContext, MainActivityX::class.java)
        when {
            !persist_beenWelcomed.value -> {
                launchMainActivity(mainIntent)
            }
            hasStoragePermissions && isStorageDirSetAndOk &&
                    checkSMSMMSPermission &&
                    checkCallLogsPermission &&
                    checkContactsPermission &&
                    checkUsageStatsPermission &&
                    (persist_ignoreBatteryOptimization.value
                            || powerManager.isIgnoringBatteryOptimizations(packageName)
                            )           -> {

                mainIntent.putExtra(
                    classAddress(".fragmentNumber"),
                    NavItem.Main.destination,
                )
                launchMainActivity(mainIntent)
            }
            else                        -> {
                mainIntent.putExtra(
                    classAddress(".fragmentNumber"),
                    NavItem.Permissions.destination,
                )
                launchMainActivity(mainIntent)
            }
        }
    }

    private fun launchMainActivity(mainIntent: Intent) {
        when {
            isBiometricLockAvailable() && isBiometricLockEnabled() && isDeviceLockEnabled() ->
                launchBiometricPrompt(mainIntent, true)
            isDeviceLockAvailable() && isDeviceLockEnabled()                                ->
                launchBiometricPrompt(mainIntent, false)
            else                                                                            ->
                startActivity(mainIntent)
        }
    }

    private fun launchBiometricPrompt(mainIntent: Intent, withBiometric: Boolean) {
        setContent {
            LockPage(mainIntent)
        }
        try {
            val biometricPrompt = createBiometricPrompt(mainIntent)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.prefs_biometriclock))
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL or (if (withBiometric) BiometricManager.Authenticators.BIOMETRIC_WEAK else 0))
                .build()
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Throwable) {
            startActivity(mainIntent)
        }
    }

    private fun createBiometricPrompt(mainIntent: Intent): BiometricPrompt {
        return BiometricPrompt(this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(mainIntent)
                }
            })
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LockPage(mainIntent: Intent) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            bottomBar = {
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ElevatedActionButton(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(id = R.string.dialog_start),
                        icon = Phosphor.ArrowRight,
                    ) {
                        launchMainActivity(mainIntent)
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {

            }
        }
    }
}