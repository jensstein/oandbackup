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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.ActivityMainXBinding;
import com.machiav3lli.backup.fragments.AppSheet;
import com.machiav3lli.backup.fragments.SortFilterSheet;
import com.machiav3lli.backup.handler.AppInfoHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.SortFilterManager;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.items.SortFilterModel;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.PrefUtils;
import com.machiav3lli.backup.utils.UIUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;
import com.topjohnwu.superuser.BuildConfig;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivityX extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = Constants.classTag(".MainActivityX");
    private static final int BATCH_REQUEST = 1;
    private static List<AppInfo> originalList;
    private static ShellHandler shellHandler;

    static {
        /* Shell.Config methods shall be called before any shell is created
         * This is the why in this example we call it in a static block
         * The followings are some examples, check Javadoc for more details */
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(20);
    }

    long threadId = -1;
    File backupDir;
    ItemAdapter<MainItemX> itemAdapter;
    FastAdapter<MainItemX> fastAdapter;
    SortFilterSheet sheetSortFilter;
    AppSheet sheetApp;
    HandleMessages handleMessages;
    SharedPreferences prefs;
    ArrayList<String> users;
    ShellCommands shellCommands;
    private ActivityMainXBinding binding;

    public static List<AppInfo> getOriginalList() {
        return originalList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainXBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupOnClicks(this);

        handleMessages = new HandleMessages(this);
        prefs = PrefUtils.getPrivateSharedPrefs(this);
        showEncryptionDialog();
        users = new ArrayList<>();
        if (savedInstanceState != null)
            users = savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS);
        shellCommands = new ShellCommands(this, users);
        checkUtilBox();

        if (!SortFilterManager.getRememberFiltering(this))
            SortFilterManager.saveFilterPreferences(this, new SortFilterModel());

        if (savedInstanceState != null) {
            threadId = savedInstanceState.getLong(Constants.BUNDLE_THREADID);
            UIUtils.reShowMessage(handleMessages, threadId);
        }

        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.app_accent, getTheme()));
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.app_primary_base, getTheme()));
        binding.refreshLayout.setOnRefreshListener(this::refresh);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        fastAdapter.setHasStableIds(true);
        binding.recyclerView.setAdapter(fastAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fastAdapter.setOnClickListener((view, itemIAdapter, item, position) -> {
            if (sheetApp != null) sheetApp.dismissAllowingStateLoss();
            sheetApp = new AppSheet(item, position);
            sheetApp.showNow(getSupportFragmentManager(), "APPSHEET");
            return false;
        });
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                itemAdapter.filter(newText);
                itemAdapter.getItemFilter().setFilterPredicate((mainItemX, charSequence) ->
                        mainItemX.getApp().getLabel().toLowerCase().contains(String.valueOf(charSequence).toLowerCase())
                                || mainItemX.getApp().getPackageName().toLowerCase().contains(String.valueOf(charSequence).toLowerCase()));
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                itemAdapter.filter(query);
                itemAdapter.getItemFilter().setFilterPredicate((mainItemX, charSequence) ->
                        mainItemX.getApp().getLabel().toLowerCase().contains(String.valueOf(charSequence).toLowerCase())
                                || mainItemX.getApp().getPackageName().toLowerCase().contains(String.valueOf(charSequence).toLowerCase()));
                return true;
            }
        });
    }

    private void setupOnClicks(Context context) {
        binding.fabSortFilter.setOnClickListener(v -> {
            if (sheetSortFilter == null)
                sheetSortFilter = new SortFilterSheet(new SortFilterModel(SortFilterManager.getFilterPreferences(context).toString()));
            sheetSortFilter.show(getSupportFragmentManager(), "SORTFILTERSHEET");
        });
        binding.settingsButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), PrefsActivity.class)));
        binding.batchBackupButton.setOnClickListener(v -> startActivityForResult(batchIntent(BatchActivityX.class, true), BATCH_REQUEST));
        binding.batchRestoreButton.setOnClickListener(v -> startActivityForResult(batchIntent(BatchActivityX.class, false), BATCH_REQUEST));
        binding.schedulerButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SchedulerActivityX.class)));
    }

    public boolean initShellHandler(Context context) {
        try {
            MainActivityX.shellHandler = new ShellHandler();
        } catch (ShellHandler.UtilboxNotAvailableException e) {
            Log.e(MainActivityX.TAG, "Could initialize ShellHandler: " + e.getMessage());
            return false;
        }
        return true;
    }

    public static ShellHandler getShellHandlerInstance() {
        return MainActivityX.shellHandler;
    }

    private boolean checkUtilBox() {
        this.handleMessages.showMessage(MainActivityX.TAG, getString(R.string.utilboxCheck));
        boolean goodToGo = true;
        // Initialize the ShellHandler for further root checks
        if (!this.initShellHandler(this)) {
            UIUtils.showWarning(this, MainActivityX.TAG, this.getString(R.string.busyboxProblem), (dialog, id) -> this.finishAffinity());
            goodToGo = false;
        }
        this.handleMessages.endMessage();
        return goodToGo;
    }

    public Intent batchIntent(Class<BatchActivityX> batchClass, boolean backup) {
        Intent batchIntent = new Intent(getApplicationContext(), batchClass);
        batchIntent.putExtra(Constants.classAddress(".backupBoolean"), backup);
        return batchIntent;
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState = fastAdapter.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        handleMessages = new HandleMessages(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        refresh();
    }

    @Override
    public void onDestroy() {
        if (handleMessages != null) handleMessages.endMessage();
        prefs.registerOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Constants.PREFS_PATH_BACKUP_DIRECTORY.equals(key))
            backupDir = FileUtils.getDefaultBackupDir(this, this);
        refresh();
    }

    private void showEncryptionDialog() {
        SharedPreferences defPrefs = PrefUtils.getDefaultSharedPreferences(this);
        boolean dontShowAgain = defPrefs.getBoolean(Constants.PREFS_ENCRYPTION, false) && !defPrefs.getString(Constants.PREFS_PASSWORD, "").isEmpty();
        if (dontShowAgain) return;
        int dontShowCounter = prefs.getInt(Constants.PREFS_SKIPPEDENCRYPTION, 0);
        prefs.edit().putInt(Constants.PREFS_SKIPPEDENCRYPTION, dontShowCounter + 1).apply();
        if (dontShowCounter % 10 == 0) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.enable_encryption_title)
                    .setMessage(R.string.enable_encryption_message)
                    .setPositiveButton(R.string.dialog_approve, (dialog, which) -> startActivity(new Intent(getApplicationContext(), PrefsActivity.class)))
                    .show();
        }
    }

    public void refresh(boolean withAppSheet) {
        sheetSortFilter = new SortFilterSheet(SortFilterManager.getFilterPreferences(this));
        backupDir = FileUtils.getDefaultBackupDir(this, this);
        runOnUiThread(() -> binding.refreshLayout.setRefreshing(true));
        new Thread(() -> {
            originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                    this.getSharedPreferences(Constants.PREFS_SHARED_PRIVATE, Context.MODE_PRIVATE).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
            ArrayList<AppInfo> filteredList = SortFilterManager.applyFilter(originalList, SortFilterManager.getFilterPreferences(this).toString(), this);
            ArrayList<MainItemX> list = createItemsList(filteredList);
            runOnUiThread(() -> {
                if (filteredList.isEmpty()) {
                    Toast.makeText(this, getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show();
                    itemAdapter.clear();
                    itemAdapter.add(list);
                } else {
                    FastAdapterDiffUtil.INSTANCE.set(itemAdapter, list);
                }
                if (withAppSheet && sheetApp != null) {
                    refreshAppSheet();
                }
                binding.searchView.setQuery("", false);
                binding.refreshLayout.setRefreshing(false);
            });
        }).start();
    }

    private void refreshAppSheet() {
        int position = sheetApp.getPosition();
        if (itemAdapter.getItemList().size() > position) {
            if (sheetApp.getPackageName().equals(fastAdapter.getItem(position).getApp().getPackageName())) {
                sheetApp.updateApp(fastAdapter.getItem(position));
            } else {
                sheetApp.dismissAllowingStateLoss();
            }
        } else {
            sheetApp.dismissAllowingStateLoss();
        }
    }

    private ArrayList<MainItemX> createItemsList(ArrayList<AppInfo> filteredList) {
        ArrayList<MainItemX> list = new ArrayList<>();
        if (filteredList.isEmpty()) {
            for (AppInfo app : originalList) {
                list.add(new MainItemX(app));
            }
            SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
        } else {
            for (AppInfo app : filteredList) {
                list.add(new MainItemX(app));
            }
        }
        return list;
    }

    public void refresh() {
        refresh(false);
    }
}
