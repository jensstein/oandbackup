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
package com.machiav3lli.backup.fragments

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.machiav3lli.backup.PREFS_IGNORE_BATTERY_OPTIMIZATION
import com.machiav3lli.backup.R
import com.machiav3lli.backup.activities.IntroActivityX
import com.machiav3lli.backup.databinding.FragmentPermissionsBinding
import com.machiav3lli.backup.utils.*
import timber.log.Timber

class PermissionsFragment : Fragment() {
    private lateinit var binding: FragmentPermissionsBinding
    private lateinit var powerManager: PowerManager
    private lateinit var prefs: SharedPreferences

    private val askForDirectory =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.data != null && result.resultCode == Activity.RESULT_OK) {
                result.data?.let {
                    val uri = it.data ?: return@registerForActivityResult
                    val flags = it.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    requireContext().contentResolver.takePersistableUriPermission(uri, flags)
                    requireContext().setBackupDir(uri)
                }
            }
        }

    private val usageStatsPermission: Unit
        get() {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.grant_usage_access_title)
                .setMessage(R.string.grant_usage_access_message)
                .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int ->
                    startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }
                .setNeutralButton(getString(R.string.dialog_refuse)) { _: DialogInterface?, _: Int -> }
                .setCancelable(false)
                .show()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupOnClicks()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = requireContext().getPrivateSharedPrefs()
        powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    override fun onResume() {
        super.onResume()
        updateState()
    }

    private fun setupViews() {
        binding.cardStoragePermission.visibility =
            if (requireContext().hasStoragePermissions) View.GONE
            else View.VISIBLE
        binding.cardStorageLocation.visibility =
            if (requireContext().isStorageDirSetAndOk) View.GONE
            else View.VISIBLE
        binding.cardUsageAccess.visibility =
            if (requireContext().checkUsageStatsPermission) View.GONE
            else View.VISIBLE
        binding.cardBatteryOptimization.visibility =
            if (requireContext().checkBatteryOptimization(prefs, powerManager)) View.GONE
            else View.VISIBLE
    }

    private fun setupOnClicks() {
        binding.cardStoragePermission.setOnClickListener { requireActivity().getStoragePermission() }
        binding.cardStorageLocation.setOnClickListener {
            requireActivity().requireStorageLocation(askForDirectory)
        }
        binding.cardUsageAccess.setOnClickListener { usageStatsPermission }
        binding.cardBatteryOptimization.setOnClickListener {
            showBatteryOptimizationDialog(powerManager)
        }
    }

    private fun updateState() {
        if (requireContext().hasStoragePermissions &&
            requireContext().isStorageDirSetAndOk &&
            requireContext().checkUsageStatsPermission &&
            (prefs.getBoolean(PREFS_IGNORE_BATTERY_OPTIMIZATION, false)
                    || powerManager.isIgnoringBatteryOptimizations(requireContext().packageName))
        ) {
            (requireActivity() as IntroActivityX).moveTo(3)
        } else {
            setupViews()
        }
    }

    private fun showBatteryOptimizationDialog(powerManager: PowerManager?) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.ignore_battery_optimization_title)
            .setMessage(R.string.ignore_battery_optimization_message)
            .setPositiveButton(R.string.dialog_approve) { _: DialogInterface?, _: Int ->
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:" + requireContext().packageName)
                try {
                    startActivity(intent)
                    prefs.edit().putBoolean(
                        PREFS_IGNORE_BATTERY_OPTIMIZATION,
                        powerManager?.isIgnoringBatteryOptimizations(requireContext().packageName) == true
                    ).apply()
                } catch (e: ActivityNotFoundException) {
                    Timber.w(e, "Ignore battery optimizations not supported")
                    Toast.makeText(
                        requireContext(),
                        R.string.ignore_battery_optimization_not_supported,
                        Toast.LENGTH_LONG
                    ).show()
                    prefs.edit().putBoolean(PREFS_IGNORE_BATTERY_OPTIMIZATION, true).apply()
                }
            }
            .setNeutralButton(R.string.dialog_refuse) { _: DialogInterface?, _: Int ->
                prefs.edit()
                    ?.putBoolean(PREFS_IGNORE_BATTERY_OPTIMIZATION, true)?.apply()
            }
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == WRITE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Timber.w("Permissions were granted: $permissions -> $grantResults")
                if (!requireContext().canAccessExternalStorage) {
                    Toast.makeText(
                        requireContext(),
                        "Permissions were granted but because of an android bug you have to restart your phone",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Timber.w("Permissions were not granted: $permissions -> $grantResults")
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_not_granted),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Timber.w("Unknown permissions request code: $requestCode")
        }
    }
}