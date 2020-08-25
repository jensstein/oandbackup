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
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.ContextWraperX;
import com.machiav3lli.backup.utils.PrefUtils;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PrefUtils.getDefaultSharedPreferences(this);
        String langCode = prefs.getString(Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT);
        assert langCode != null;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String newLang = PrefUtils.getDefaultSharedPreferences(newBase).getString(Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT);
        super.attachBaseContext(ContextWraperX.wrap(newBase, newLang));
    }
}
