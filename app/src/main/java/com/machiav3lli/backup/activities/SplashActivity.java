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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.databinding.ActivitySplashBinding;
import com.machiav3lli.backup.utils.PrefUtils;
import com.machiav3lli.backup.utils.UIUtils;

public class SplashActivity extends BaseActivity {
    private static final String TAG = Constants.classTag(".SplashActivity");
    private ActivitySplashBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        UIUtils.setDayNightTheme(PrefUtils.getPrivateSharedPrefs(this).getString(Constants.PREFS_THEME, "system"));
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SharedPreferences prefs = PrefUtils.getPrivateSharedPrefs(this);
        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        Intent introIntent = new Intent(getApplicationContext(), IntroActivityX.class);
        if (prefs.getBoolean(Constants.PREFS_FIRST_LAUNCH, true)) {
            startActivity(introIntent);
        } else if (PrefUtils.checkStoragePermissions(this) &&
                PrefUtils.isStorageDirSetAndOk(this) &&
                PrefUtils.checkUsageStatsPermission(this) &&
                (prefs.getBoolean(Constants.PREFS_IGNORE_BATTERY_OPTIMIZATION, false)
                        || powerManager.isIgnoringBatteryOptimizations(getPackageName()))) {
            introIntent.putExtra(Constants.classAddress(".fragmentNumber"), 3);
            startActivity(introIntent);
        } else {
            introIntent.putExtra(Constants.classAddress(".fragmentNumber"), 2);
            startActivity(introIntent);
        }
        this.overridePendingTransition(0, 0);
    }
}
