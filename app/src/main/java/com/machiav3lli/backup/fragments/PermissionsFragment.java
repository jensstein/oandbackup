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
package com.machiav3lli.backup.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.IntroActivityX;
import com.machiav3lli.backup.databinding.FragmentPermissionsBinding;
import com.machiav3lli.backup.utils.PrefUtils;

import java.util.Arrays;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionsFragment extends Fragment {
    private static final String TAG = Constants.classTag(".PermissionsFragment");
    private FragmentPermissionsBinding binding;
    PowerManager powerManager;
    SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentPermissionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupOnClicks();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PrefUtils.getPrivateSharedPrefs(requireContext());
        powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setupViews() {
        binding.cardStoragePermission.setVisibility(PrefUtils.checkStoragePermissions(requireContext()) ? View.GONE : View.VISIBLE);
        binding.cardStorageLocation.setVisibility(PrefUtils.isStorageDirSetAndOk(requireContext()) ? View.GONE : View.VISIBLE);
        binding.cardUsageAccess.setVisibility(PrefUtils.checkUsageStatsPermission(requireContext()) ? View.GONE : View.VISIBLE);
        binding.cardBatteryOptimization.setVisibility(PrefUtils.checkBatteryOptimization(requireContext(), prefs, powerManager) ? View.GONE : View.VISIBLE);
    }

    private void setupOnClicks() {
        binding.cardStoragePermission.setOnClickListener(view -> PrefUtils.getStoragePermission(requireActivity()));
        binding.cardStorageLocation.setOnClickListener(view -> PrefUtils.requireStorageLocation(this));
        binding.cardUsageAccess.setOnClickListener(view -> getUsageStatsPermission());
        binding.cardBatteryOptimization.setOnClickListener(view -> showBatteryOptimizationDialog(powerManager));
    }

    private void updateState() {
        if (PrefUtils.checkStoragePermissions(requireContext()) &&
                PrefUtils.isStorageDirSetAndOk(requireContext()) &&
                PrefUtils.checkUsageStatsPermission(requireContext()) &&
                (prefs.getBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, false)
                        || powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName()))) {
            moveTo(3);
        } else {
            setupViews();
        }
    }

    private void moveTo(int position) {
        ((IntroActivityX) requireActivity()).moveTo(position);
    }

    private void getUsageStatsPermission() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.grant_usage_access_title)
                .setMessage(R.string.grant_usage_access_message)
                .setPositiveButton(R.string.dialog_approve,
                        (dialog, which) -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)))
                .setNeutralButton(getString(R.string.dialog_refuse), (dialog, which) -> {
                })
                .setCancelable(false)
                .show();
    }

    private void showBatteryOptimizationDialog(PowerManager powerManager) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.ignore_battery_optimization_title)
                .setMessage(R.string.ignore_battery_optimization_message)
                .setPositiveButton(R.string.dialog_approve, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    try {
                        startActivity(intent);
                        prefs.edit().putBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName())).apply();
                    } catch (ActivityNotFoundException e) {
                        Log.w(PermissionsFragment.TAG, "Ignore battery optimizations not supported", e);
                        Toast.makeText(requireContext(), R.string.ignore_battery_optimization_not_supported, Toast.LENGTH_LONG).show();
                        prefs.edit().putBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, true).apply();
                    }
                })
                .setNeutralButton(R.string.dialog_refuse, (dialog, which) -> prefs.edit().putBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, true).apply())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PrefUtils.WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                Log.w(PermissionsFragment.TAG, String.format("Permissions were granted: %s -> %s",
                        Arrays.toString(permissions), Arrays.toString(grantResults)));
                if (!PrefUtils.canAccessExternalStorage(requireContext())) {
                    Toast.makeText(requireContext(), "Permissions were granted but because of an android bug you have to restart your phone",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Log.w(PermissionsFragment.TAG, String.format("Permissions were not granted: %s -> %s",
                        Arrays.toString(permissions), Arrays.toString(grantResults)));
                Toast.makeText(requireContext(), getString(R.string.permission_not_granted), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(PermissionsFragment.TAG, String.format("Unknown permissions request code: %s", requestCode));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PrefUtils.BACKUP_DIR:
                Uri uri = data.getData();
                if (resultCode == Activity.RESULT_OK) {
                    PrefUtils.setStorageRootDir(this.getContext(), uri);
                }
                break;
        }
    }
}
