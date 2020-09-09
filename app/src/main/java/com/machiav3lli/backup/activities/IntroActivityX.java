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
package com.machiav3lli.backup.activities;

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
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.ActivityIntroXBinding;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.utils.PrefUtils;
import com.machiav3lli.backup.utils.UIUtils;
import com.scottyab.rootbeer.RootBeer;

import java.util.Arrays;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class IntroActivityX extends BaseActivity {
    private static final String TAG = Constants.classTag(".IntroActivityX");
    private ActivityIntroXBinding binding;
    SharedPreferences prefs;
    NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroXBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefs = PrefUtils.getPrivateSharedPrefs(this);
        setupNavigation();
        if (getIntent().getExtras() != null) {
            int fragmentNumber = getIntent().getExtras().getInt(Constants.classAddress(".fragmentNumber"));
            moveTo(fragmentNumber);
        }
    }

    private void setupNavigation() {
        navController = Navigation.findNavController(this, R.id.introContainer);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            switch (destination.getId()) {
                case R.id.welcomeFragment: {
                    binding.positiveButton.setText(R.string.dialog_start);
                    binding.positiveButton.setOnClickListener(v -> {
                        if (this.checkRootAccess()) {
                            prefs.edit().putBoolean(Constants.PREFS_FIRST_LAUNCH, false).apply();
                            moveTo(2);
                        }
                    });
                    break;
                }
                case R.id.permissionsFragment: {
                    binding.positiveButton.setText(R.string.ask_for_permissions);
                    binding.positiveButton.setOnClickListener(view -> PrefUtils.getStoragePermission(this));
                    if (PrefUtils.checkStoragePermissions(this)) moveTo(3);
                    break;
                }
                case R.id.optimizationsFragment: {
                    if (!PrefUtils.checkUsageStatsPermission(this)) {
                        binding.positiveButton.setText(R.string.grant_usage_access_title);
                        binding.positiveButton.setVisibility(View.VISIBLE);
                        binding.positiveButton.setOnClickListener(v -> getUsageStatsPermission());
                    } else {
                        binding.positiveButton.setVisibility(View.GONE);
                        moveTo(4);
                    }
                    PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    if (!prefs.getBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, false) && !powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                        binding.negativeButton.setText(R.string.ignore_battery_optimization_title);
                        binding.negativeButton.setVisibility(View.VISIBLE);
                        binding.negativeButton.setOnClickListener(v -> showBatteryOptimizationDialog(powerManager));
                    } else {
                        binding.negativeButton.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void moveTo(int posistion) {
        switch (posistion) {
            case 1:
                navController.navigate(R.id.welcomeFragment);
                break;
            case 2:
                navController.navigate(R.id.permissionsFragment);
                break;
            case 3:
                navController.navigate(R.id.optimizationsFragment);
                break;
            case 4:
                binding.positiveButton.setOnClickListener(view -> launchMainActivity());
                launchMainActivity();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    private boolean checkRootAccess() {
        RootBeer rootBeer = new RootBeer(this);
        if (!rootBeer.isRooted()) {
            this.showFatalUiWarning(this.getString(R.string.noSu));
            return false;
        }
        try {
            ShellHandler.runAsRoot("id");
        } catch (ShellHandler.ShellCommandFailedException e) {
            this.showFatalUiWarning(this.getString(R.string.noSu));
            return false;
        }
        return true;
    }

    private void getUsageStatsPermission() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.grant_usage_access_title)
                .setMessage(R.string.grant_usage_access_message)
                .setPositiveButton(R.string.dialog_approve,
                        (dialog, which) -> startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), PrefUtils.STATS_PERMISSION))
                .setNeutralButton(getString(R.string.dialog_refuse), (dialog, which) -> finishAffinity())
                .setCancelable(false)
                .show();
    }

    private void showBatteryOptimizationDialog(PowerManager powerManager) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.ignore_battery_optimization_title)
                .setMessage(R.string.ignore_battery_optimization_message)
                .setPositiveButton(R.string.dialog_approve, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    try {
                        startActivity(intent);
                        prefs.edit().putBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, powerManager.isIgnoringBatteryOptimizations(getPackageName())).apply();
                    } catch (ActivityNotFoundException e) {
                        Log.w(IntroActivityX.TAG, "Ignore battery optimizations not supported", e);
                        Toast.makeText(this, R.string.ignore_battery_optimization_not_supported, Toast.LENGTH_LONG).show();
                        prefs.edit().putBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, true).apply();
                    }
                    if (prefs.getBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, false)) {
                        binding.negativeButton.setVisibility(View.GONE);
                    }
                })
                .setNeutralButton(R.string.dialog_refuse, (dialog, which) -> {
                    prefs.edit().putBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, true).apply();
                    binding.negativeButton.setVisibility(View.GONE);
                })
                .setCancelable(false)
                .show();
    }

    private void showFatalUiWarning(String message) {
        UIUtils.showWarning(this, IntroActivityX.TAG, message, (dialog, id) -> this.finishAffinity());
    }

    public void launchMainActivity() {
        if (PrefUtils.isBiometricLockAvailable(this) && PrefUtils.isLockEnabled(this)) {
            launchBiometricPrompt();
        } else {
            startActivity(new Intent(this, MainActivityX.class));
        }
    }

    private void launchBiometricPrompt() {
        BiometricPrompt biometricPrompt = createBeometricPrompt(this);
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.prefs_biometriclock))
                .setConfirmationRequired(true)
                .setDeviceCredentialAllowed(true)
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    private BiometricPrompt createBeometricPrompt(Activity activity) {
        return new BiometricPrompt(this, ContextCompat.getMainExecutor(this), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                startActivity(new Intent(activity, MainActivityX.class));
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricConstants.ERROR_USER_CANCELED) {
                    binding.positiveButton.setText(R.string.dialog_unlock);
                    binding.positiveButton.setVisibility(View.VISIBLE);
                } else {
                    binding.positiveButton.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PrefUtils.WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                if (!PrefUtils.canAccessExternalStorage(this)) {
                    Log.w(IntroActivityX.TAG, String.format("Permissions were granted: %s -> %s",
                            Arrays.toString(permissions), Arrays.toString(grantResults)));
                    Toast.makeText(this, "Permissions were granted but because of an android bug you have to restart your phone",
                            Toast.LENGTH_LONG).show();
                } else {
                    moveTo(3);
                }
            } else {
                Log.w(IntroActivityX.TAG, String.format("Permissions were not granted: %s -> %s",
                        Arrays.toString(permissions), Arrays.toString(grantResults)));
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(IntroActivityX.TAG, String.format("Unknown permissions request code: %s", requestCode));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PrefUtils.STATS_PERMISSION) {
            if (PrefUtils.checkUsageStatsPermission(this)) {
                if (PrefUtils.checkStoragePermissions(this)) {
                    moveTo(4);
                }
            } else {
                finishAffinity();
            }
        }
    }
}
