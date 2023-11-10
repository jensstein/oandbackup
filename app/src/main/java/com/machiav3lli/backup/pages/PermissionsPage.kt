/*
 * Neo Backup: open-source apps backup and restore app.
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
package com.machiav3lli.backup.pages

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.preferences.persist_ignoreBatteryOptimization
import com.machiav3lli.backup.ui.compose.blockBorder
import com.machiav3lli.backup.ui.compose.item.PermissionItem
import com.machiav3lli.backup.ui.compose.item.TopBar
import com.machiav3lli.backup.ui.item.Permission
import com.machiav3lli.backup.ui.navigation.NavItem
import com.machiav3lli.backup.utils.checkBatteryOptimization
import com.machiav3lli.backup.utils.checkCallLogsPermission
import com.machiav3lli.backup.utils.checkContactsPermission
import com.machiav3lli.backup.utils.checkSMSMMSPermission
import com.machiav3lli.backup.utils.checkUsageStatsPermission
import com.machiav3lli.backup.utils.getStoragePermission
import com.machiav3lli.backup.utils.hasStoragePermissions
import com.machiav3lli.backup.utils.isStorageDirSetAndOk
import com.machiav3lli.backup.utils.requireCallLogsPermission
import com.machiav3lli.backup.utils.requireContactsPermission
import com.machiav3lli.backup.utils.requireSMSMMSPermission
import com.machiav3lli.backup.utils.requireStorageLocation
import com.machiav3lli.backup.utils.setBackupDir
import timber.log.Timber

// TODO use rememberPermissionState to manage more permissions
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsPage() {
    val context = LocalContext.current
    val mainActivity = context as MainActivityX
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    val permissionsList = remember {
        mutableStateMapOf<Permission, () -> Unit>()
    }

    val permissionStatePostNotifications = if (OABX.minSDK(Build.VERSION_CODES.TIRAMISU)) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    val askForDirectory =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.data != null && result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val uri = it.data ?: return@rememberLauncherForActivityResult
                    val flags = it.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                    setBackupDir(uri)
                }
            }
        }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsList.apply {
                    if (!context.hasStoragePermissions && none { it.key == Permission.StorageAccess })
                        set(Permission.StorageAccess) { mainActivity.getStoragePermission() }

                    if (!context.isStorageDirSetAndOk && none { it.key == Permission.StorageLocation })
                        set(Permission.StorageLocation) {
                            mainActivity.requireStorageLocation(askForDirectory)
                        }

                    if (!context.checkBatteryOptimization(powerManager) && none { it.key == Permission.BatteryOptimization })
                        set(Permission.BatteryOptimization) {
                            mainActivity.showBatteryOptimizationDialog(powerManager)
                        }

                    if (!context.checkUsageStatsPermission && none { it.key == Permission.UsageStats })
                        set(Permission.UsageStats) { mainActivity.usageStatsPermission }

                    if (!context.checkSMSMMSPermission && none { it.key == Permission.SMSMMS })
                        set(Permission.SMSMMS) { mainActivity.smsmmsPermission }

                    if (!context.checkCallLogsPermission && none { it.key == Permission.CallLogs })
                        set(Permission.CallLogs) { mainActivity.callLogsPermission }

                    if (!context.checkContactsPermission && none { it.key == Permission.Contacts })
                        set(Permission.Contacts) { mainActivity.contactsPermission }

                    if (permissionStatePostNotifications?.status?.isGranted == false && none { it.key == Permission.PostNotifications })
                        set(Permission.PostNotifications) {
                            permissionStatePostNotifications.launchPermissionRequest()
                        }
                    if (context.hasStoragePermissions)
                        remove(Permission.StorageAccess)
                    if (context.isStorageDirSetAndOk)
                        remove(Permission.StorageLocation)
                    if (context.checkBatteryOptimization(powerManager))
                        remove(Permission.BatteryOptimization)
                    if (context.checkUsageStatsPermission)
                        remove(Permission.UsageStats)
                    if (context.checkSMSMMSPermission)
                        remove(Permission.SMSMMS)
                    if (context.checkCallLogsPermission)
                        remove(Permission.CallLogs)
                    if (context.checkContactsPermission)
                        remove(Permission.Contacts)
                    if (permissionStatePostNotifications?.status?.isGranted == true)
                        remove(Permission.PostNotifications)
                }
                if (permissionsList.isEmpty()) mainActivity.moveTo(NavItem.Main.destination)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })

    Scaffold(
        topBar = {
            TopBar(title = stringResource(id = R.string.app_name)) {}
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .blockBorder()
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(permissionsList.toList(), key = { it.first.nameId }) { (permission, onClick) ->
                PermissionItem(permission, onClick)
            }
        }
    }
}

val AppCompatActivity.usageStatsPermission: Unit
    get() {
        AlertDialog.Builder(this)
            .setTitle(R.string.grant_usage_access_title)
            .setMessage(R.string.grant_usage_access_message)
            .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            .setNeutralButton(getString(R.string.dialog_refuse)) { _: DialogInterface?, _: Int -> }
            .setCancelable(false)
            .show()
    }

val AppCompatActivity.smsmmsPermission: Unit
    get() {
        AlertDialog.Builder(this)
            .setTitle(R.string.smsmms_permission_title)
            .setMessage(R.string.grant_smsmms_message)
            .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int ->
                requireSMSMMSPermission()
            }
            .setNeutralButton(getString(R.string.dialog_refuse)) { _: DialogInterface?, _: Int -> }
            .setCancelable(false)
            .show()
    }

val AppCompatActivity.callLogsPermission: Unit
    get() {
        AlertDialog.Builder(this)
            .setTitle(R.string.calllogs_permission_title)
            .setMessage(R.string.grant_calllogs_message)
            .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int ->
                this.requireCallLogsPermission()
            }
            .setNeutralButton(getString(R.string.dialog_refuse)) { _: DialogInterface?, _: Int -> }
            .setCancelable(false)
            .show()
    }

val AppCompatActivity.contactsPermission: Unit
    get() {
        AlertDialog.Builder(this)
            .setTitle(R.string.contacts_permission_title)
            .setMessage(R.string.grant_contacts_message)
            .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int ->
                this.requireContactsPermission()
            }
            .setNeutralButton(getString(R.string.dialog_refuse)) { _: DialogInterface?, _: Int -> }
            .setCancelable(false)
            .show()
    }

fun AppCompatActivity.showBatteryOptimizationDialog(powerManager: PowerManager?) {
    AlertDialog.Builder(this)
        .setTitle(R.string.ignore_battery_optimization_title)
        .setMessage(R.string.ignore_battery_optimization_message)
        .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int ->
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            try {
                startActivity(intent)
                persist_ignoreBatteryOptimization.value =
                    powerManager?.isIgnoringBatteryOptimizations(packageName) == true
            } catch (e: ActivityNotFoundException) {
                Timber.w(e, "Ignore battery optimizations not supported")
                Toast.makeText(
                    this,
                    R.string.ignore_battery_optimization_not_supported,
                    Toast.LENGTH_LONG
                ).show()
                persist_ignoreBatteryOptimization.value = true
            }
        }
        .setNeutralButton(R.string.dialog_refuse) { _: DialogInterface?, _: Int ->
            persist_ignoreBatteryOptimization.value = true
        }
        .setCancelable(false)
        .show()
}
