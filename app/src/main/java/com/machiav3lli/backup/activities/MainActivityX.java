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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.badge.BadgeDrawable;
import com.machiav3lli.backup.BuildConfig;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.SearchViewController;
import com.machiav3lli.backup.databinding.ActivityMainXBinding;
import com.machiav3lli.backup.dialogs.BatchConfirmDialog;
import com.machiav3lli.backup.fragments.AppSheet;
import com.machiav3lli.backup.fragments.SortFilterSheet;
import com.machiav3lli.backup.handler.BackendController;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.SortFilterManager;
import com.machiav3lli.backup.handler.action.BaseAppAction;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.AppMetaInfo;
import com.machiav3lli.backup.items.BackupItem;
import com.machiav3lli.backup.items.BatchItemX;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.items.SortFilterModel;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.PrefUtils;
import com.machiav3lli.backup.utils.UIUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainActivityX extends BaseActivity implements BatchConfirmDialog.ConfirmListener {
    private static final String TAG = Constants.classTag(".MainActivityNeo");
    private static ShellHandler shellHandler;

    static {
        /* Shell.Config methods shall be called before any shell is created
         * This is the why in this example we call it in a static block
         * The followings are some examples, check Javadoc for more details */
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setTimeout(20));
    }

    private static List<AppInfoV2> appsList;
    public final ArrayList<String> checkedList = new ArrayList<>();


    private BadgeDrawable updatedBadge;
    private int badgeCounter;
    private HandleMessages handleMessages;
    private PowerManager powerManager;
    private long threadId = -1;
    private ActivityMainXBinding binding;
    private final ItemAdapter<MainItemX> mainItemAdapter = new ItemAdapter<>();
    private FastAdapter<MainItemX> mainFastAdapter;
    private final ItemAdapter<BatchItemX> batchItemAdapter = new ItemAdapter<>();
    private FastAdapter<BatchItemX> batchFastAdapter;
    private boolean mainBoolean;
    private boolean backupBoolean;
    private SharedPreferences prefs;
    private NavController navController;
    private SortFilterSheet sheetSortFilter;
    private AppSheet sheetApp;
    private SearchViewController searchViewController;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainXBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        handleMessages = new HandleMessages(this);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        prefs = PrefUtils.getPrivateSharedPrefs(this);
        runOnUiThread(this::showEncryptionDialog);
        if (savedInstanceState != null) {
            threadId = savedInstanceState.getLong(Constants.BUNDLE_THREADID);
            UIUtils.reShowMessage(handleMessages, threadId);
        }
        checkUtilBox();
    }

    @Override
    protected void onStart() {
        super.onStart();
        runOnUiThread(() -> {
            setupViews();
            setupNavigation();
            setupOnClicks();
        });
        if (getIntent().getExtras() != null) {
            int fragmentNumber = getIntent().getExtras().getInt(Constants.classAddress(".fragmentNumber"));
            moveTo(fragmentNumber);
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public void onResume() {
        super.onResume();
        handleMessages = new HandleMessages(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState = mainFastAdapter.saveInstanceState(outState);
        outState = batchFastAdapter.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (handleMessages != null) handleMessages.endMessage();
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setupNavigation();
    }

    public static ShellHandler getShellHandlerInstance() {
        return MainActivityX.shellHandler;
    }

    public static List<AppInfoV2> getAppsList() {
        return appsList;
    }

    public ItemAdapter<MainItemX> getMainItemAdapter() {
        return mainItemAdapter;
    }

    public ItemAdapter<BatchItemX> getBatchItemAdapter() {
        return batchItemAdapter;
    }

    public void setSearchViewController(SearchViewController searchViewController) {
        this.searchViewController = searchViewController;
    }

    private void setupViews() {
        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.app_accent, getTheme()));
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.app_primary_base, getTheme()));
        binding.cbAll.setChecked(false);
        updatedBadge = binding.bottomNavigation.getOrCreateBadge(R.id.mainFragment);
        updatedBadge.setBackgroundColor(getResources().getColor(R.color.app_accent, getTheme()));
        updatedBadge.setVisible(badgeCounter != 0);
        mainFastAdapter = FastAdapter.with(mainItemAdapter);
        mainFastAdapter.setHasStableIds(true);
        batchFastAdapter = FastAdapter.with(batchItemAdapter);
        batchFastAdapter.setHasStableIds(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.refreshLayout.setOnRefreshListener(this::refresh);
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == binding.bottomNavigation.getSelectedItemId())
                return false;
            NavigationUI.onNavDestinationSelected(item, navController);
            return true;
        });
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.mainFragment) {
                navigateMain();
            } else if (destination.getId() == R.id.backupFragment) {
                backupBoolean = true;
                navigateBatch();
            } else if (destination.getId() == R.id.restoreFragment) {
                backupBoolean = false;
                navigateBatch();
            }
        });
    }

    private void navigateMain() {
        mainBoolean = true;
        binding.batchBar.setVisibility(View.GONE);
        binding.modeBar.setVisibility(View.GONE);
        binding.recyclerView.setAdapter(mainFastAdapter);
    }

    private void navigateBatch() {
        mainBoolean = false;
        binding.radioBoth.setChecked(true);
        binding.batchBar.setVisibility(View.VISIBLE);
        binding.modeBar.setVisibility(View.VISIBLE);
        binding.buttonAction.setText(backupBoolean ? R.string.backup : R.string.restore);
        binding.recyclerView.setAdapter(batchFastAdapter);
        binding.buttonAction.setOnClickListener(v -> actionOnClick(backupBoolean));
        checkedList.clear();
    }

    private void setupOnClicks() {
        binding.buttonSettings.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), PrefsActivity.class)));
        binding.buttonScheduler.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SchedulerActivityX.class)));
        binding.cbAll.setOnClickListener(this::onCheckAllChanged);
        binding.buttonSortFilter.setOnClickListener(v -> {
            if (sheetSortFilter == null)
                sheetSortFilter = new SortFilterSheet(new SortFilterModel(SortFilterManager.getFilterPreferences(this).toString()));
            sheetSortFilter.show(getSupportFragmentManager(), "SORTFILTERSHEET");
        });
        mainFastAdapter.setOnClickListener((view, itemIAdapter, item, position) -> {
            if (sheetApp != null) sheetApp.dismissAllowingStateLoss();
            sheetApp = new AppSheet(item, position);
            sheetApp.showNow(getSupportFragmentManager(), "APPSHEET");
            return false;
        });
        batchFastAdapter.setOnClickListener((view, itemIAdapter, item, integer) -> {
            item.setChecked(!item.isChecked());
            if (item.isChecked()) {
                if (!checkedList.contains(item.getApp().getPackageName())) {
                    checkedList.add(item.getApp().getPackageName());
                }
            } else {
                checkedList.remove(item.getApp().getPackageName());
            }
            batchFastAdapter.notifyAdapterDataSetChanged();
            updateCheckAll();
            return false;
        });
    }

    private void onCheckAllChanged(View v) {
        boolean startIsChecked = ((AppCompatCheckBox) v).isChecked();
        binding.cbAll.setChecked(startIsChecked);
        for (BatchItemX item : batchItemAdapter.getAdapterItems()) {
            item.setChecked(startIsChecked);
            if (startIsChecked) {
                if (!checkedList.contains(item.getApp().getPackageName())) {
                    checkedList.add(item.getApp().getPackageName());
                }
            } else {
                checkedList.remove(item.getApp().getPackageName());
            }
        }
        batchFastAdapter.notifyAdapterDataSetChanged();
    }

    private void updateCheckAll() {
        binding.cbAll.setChecked(checkedList.size() == batchItemAdapter.getItemList().size());
    }

    private void actionOnClick(boolean backupBoolean) {
        ArrayList<AppMetaInfo> selectedList = this.batchItemAdapter.getAdapterItems().stream()
                .filter(BatchItemX::isChecked)
                .map(item -> item.getApp().getAppInfo()).collect(Collectors.toCollection(ArrayList::new));
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList("selectedList", selectedList);
        arguments.putBoolean("backupBoolean", backupBoolean);
        BatchConfirmDialog dialog = new BatchConfirmDialog();
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "DialogFragment");
    }

    private int checkSelectedMode() {
        if (binding.radioApk.isChecked()) {
            return BaseAppAction.MODE_APK;
        } else if (binding.radioData.isChecked()) {
            return BaseAppAction.MODE_DATA;
        } else return BaseAppAction.MODE_BOTH;
    }

    public void moveTo(int position) {
        if (position == 1) {
            navController.navigate(R.id.mainFragment);
        } else if (position == 2) {
            navController.navigate(R.id.backupFragment);
        } else if (position == 3) {
            navController.navigate(R.id.restoreFragment);
        }
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

    @Override
    public void onConfirmed(List<AppMetaInfo> selectedList) {
        Thread thread = new Thread(() -> doAction(selectedList));
        thread.start();
        threadId = thread.getId();
    }

    // TODO 1. implement the new logic with app/data checkboxes, 2. optimize/reduce complexity
    public void doAction(List<AppMetaInfo> selectedList) {
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = this.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MainActivityX.TAG);
        if (this.prefs.getBoolean("acquireWakelock", true)) {
            wl.acquire(15 * 60 * 1000L /*15 minutes*/);
            Log.i(MainActivityX.TAG, "wakelock acquired");
        }
        // get the AppInfoV2 objects again
        List<AppInfoV2> selectedApps = new ArrayList<>(selectedList.size());
        for (AppMetaInfo appInfo : selectedList) {
            Optional<BatchItemX> foundItem = this.batchItemAdapter.getAdapterItems().stream()
                    .filter(item -> item.getApp().getAppInfo().equals(appInfo))
                    .findFirst();
            if (foundItem.isPresent()) {
                selectedApps.add(foundItem.get().getApp());
            } else {
                throw new RuntimeException("Selected item for processing went lost from the item adapter.");
            }
        }

        int notificationId = (int) System.currentTimeMillis();
        int total = selectedList.size();
        int i = 1;
        List<ActionResult> results = new ArrayList<>(total);
        for (AppInfoV2 app : selectedApps) {
            String message = "(" + i + '/' + total + ')';
            String title = (this.backupBoolean ? this.getString(R.string.backupProgress) : this.getString(R.string.restoreProgress))
                    + " (" + i + '/' + total + ')';
            NotificationHelper.showNotification(this, MainActivityX.class, notificationId, title, app.getAppInfo().getPackageLabel(), false);
            this.handleMessages.setMessage(app.getAppInfo().getPackageLabel(), message);
            int mode = checkSelectedMode();
            final BackupRestoreHelper backupRestoreHelper = new BackupRestoreHelper();
            ActionResult result;
            if (this.backupBoolean) {
                result = backupRestoreHelper.backup(this, MainActivityX.getShellHandlerInstance(), app, mode);
            } else {
                // Latest backup for now
                BackupItem selectedBackup = app.getLatestBackup();
                result = backupRestoreHelper.restore(
                        this, app, selectedBackup.getBackupProperties(),
                        selectedBackup.getBackupLocation(),
                        MainActivityX.getShellHandlerInstance(), mode);
            }
            results.add(result);
            i++;
        }
        if (this.handleMessages.isShowing()) {
            this.handleMessages.endMessage();
        }
        if (wl.isHeld()) {
            wl.release();
            Log.i(MainActivityX.TAG, "wakelock released");
        }
        // Calculate the overall result
        String errors = results.stream()
                .map(ActionResult::getMessage)
                .filter(msg -> !msg.isEmpty())
                .collect(Collectors.joining("\n"));
        ActionResult overAllResult = new ActionResult(null, null, errors, results.parallelStream().anyMatch(ar -> ar.succeeded));

        // Update the notification
        String msg = this.backupBoolean ? this.getString(R.string.batchbackup) : this.getString(R.string.batchrestore);
        String notificationTitle = overAllResult.succeeded ? this.getString(R.string.batchSuccess) : this.getString(R.string.batchFailure);
        NotificationHelper.showNotification(this, MainActivityX.class, notificationId, notificationTitle, msg, true);

        // show results to the user. Add a save button, if logs should be saved to the application log (in case it's too much)
        UIUtils.showActionResult(this, overAllResult, overAllResult.succeeded ? null : (dialog, which) -> {
            try (FileWriter fw = new FileWriter(FileUtils.getDefaultLogFilePath(this.getApplicationContext()), true)) {
                fw.write(errors);
                Toast.makeText(
                        MainActivityX.this,
                        String.format(this.getString(R.string.logfileSavedAt), FileUtils.getDefaultLogFilePath(this.getApplicationContext())),
                        Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                new AlertDialog.Builder(MainActivityX.this)
                        .setTitle(R.string.errorDialogTitle)
                        .setMessage(e.getLocalizedMessage())
                        .setPositiveButton(R.string.dialogOK, null)
                        .show();
            }
        });
        this.refresh();
    }

    public static boolean initShellHandler() {
        try {
            MainActivityX.shellHandler = new ShellHandler();
        } catch (ShellHandler.UtilboxNotAvailableException e) {
            Log.e(MainActivityX.TAG, "Could initialize ShellHandler: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void checkUtilBox() {
        this.handleMessages.showMessage(MainActivityX.TAG, getString(R.string.utilboxCheck));
        // Initialize the ShellHandler for further root checks
        if (!MainActivityX.initShellHandler()) {
            UIUtils.showWarning(this, MainActivityX.TAG, this.getString(R.string.busyboxProblem), (dialog, id) -> this.finishAffinity());
        }
        this.handleMessages.endMessage();
    }

    public void refresh() {
        if (mainBoolean) {
            refresh(true, false, new ArrayList<>());
        } else {
            refresh(false, backupBoolean, new ArrayList<>());
        }
    }

    public void refreshWithAppSheet() {
        refresh(true, true, new ArrayList<>());
    }

    public void resumeRefresh(List<String> checkedList) {
        if (mainBoolean) {
            refresh(true, true, checkedList);
        } else {
            refresh(false, backupBoolean, checkedList);
        }
    }

    public void refresh(boolean mainBoolean, boolean backupOrAppSheetBoolean, List<String> checkedList) {
        Log.d(MainActivityX.TAG, "refreshing");
        runOnUiThread(() -> {
            binding.refreshLayout.setRefreshing(true);  //TODO: hg42 refresh can run in parallel, needs counting or multiple (overlaying?) Spinners
            searchViewController.clean();
        });
        badgeCounter = 0;
        if (mainBoolean || checkedList.isEmpty()) this.checkedList.clear();
        sheetSortFilter = new SortFilterSheet(SortFilterManager.getFilterPreferences(this));
        new Thread(() -> {
            try {
                appsList = BackendController.getApplicationList(this.getApplicationContext());
                PrefUtils.getPrivateSharedPrefs(this).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true);
                List<AppInfoV2> filteredList = SortFilterManager.applyFilter(appsList, SortFilterManager.getFilterPreferences(this).toString(), this);
                if (mainBoolean) refreshMain(filteredList, backupOrAppSheetBoolean);
                else refreshBatch(filteredList, backupOrAppSheetBoolean, checkedList);

            } catch (FileUtils.BackupLocationInAccessibleException | PrefUtils.StorageLocationNotConfiguredException e) {
                Log.e(TAG, "Could not update application list: " + e);
            }
        }).start();
    }

    private void refreshMain(List<AppInfoV2> filteredList, boolean appSheetBoolean) {
        ArrayList<MainItemX> mainList = createMainAppsList(filteredList);
        runOnUiThread(() -> {
            if (filteredList.isEmpty()) {
                Toast.makeText(getBaseContext(), getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show();
                mainItemAdapter.clear();
            }
            FastAdapterDiffUtil.INSTANCE.set(mainItemAdapter, mainList);
            searchViewController.setup();
            if (updatedBadge != null) {
                updatedBadge.setNumber(badgeCounter);
                updatedBadge.setVisible(badgeCounter != 0);
            }
            mainFastAdapter.notifyAdapterDataSetChanged();
            binding.refreshLayout.setRefreshing(false);
            if (appSheetBoolean && sheetApp != null) {
                refreshAppSheet();
            }
        });
    }

    private ArrayList<MainItemX> createMainAppsList(List<AppInfoV2> filteredList) {
        ArrayList<MainItemX> list = new ArrayList<>();
        if (filteredList.isEmpty()) {
            for (AppInfoV2 app : SortFilterManager.applyFilter(appsList, "0000", this)) {
                list.add(new MainItemX(app));
                if (app.isUpdated()) badgeCounter += 1;
            }
            SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
        } else {
            for (AppInfoV2 app : filteredList) {
                list.add(new MainItemX(app));
                if (app.isUpdated()) badgeCounter += 1;
            }
        }
        return list;
    }

    private void refreshAppSheet() {
        int position = sheetApp.getPosition();
        if (mainItemAdapter.getItemList().size() > position) {
            if (sheetApp.getPackageName().equals(mainFastAdapter.getItem(position).getApp().getPackageName())) {
                sheetApp.updateApp(mainFastAdapter.getItem(position));
            } else {
                sheetApp.dismissAllowingStateLoss();
            }
        } else {
            sheetApp.dismissAllowingStateLoss();
        }
    }

    private void refreshBatch(List<AppInfoV2> filteredList, boolean backupBoolean, List<String> checkedList) {
        ArrayList<BatchItemX> batchList = createBatchAppsList(filteredList, backupBoolean, checkedList);
        runOnUiThread(() -> {
            if (filteredList.isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show();
                batchItemAdapter.clear();
            }
            FastAdapterDiffUtil.INSTANCE.set(batchItemAdapter, batchList);
            searchViewController.setup();
            batchFastAdapter.notifyAdapterDataSetChanged();
            updateCheckAll();
            binding.refreshLayout.setRefreshing(false);
        });
    }

    private ArrayList<BatchItemX> createBatchAppsList(List<AppInfoV2> filteredList, boolean backupBoolean, List<String> checkedList) {
        ArrayList<BatchItemX> list = new ArrayList<>();
        if (filteredList.isEmpty()) {
            for (AppInfoV2 app : SortFilterManager.applyFilter(appsList, "0000", this)) {
                if (toAddToBatch(backupBoolean, app)) list.add(new BatchItemX(app));
            }
            SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
        } else {
            for (AppInfoV2 app : filteredList) {
                if (!checkedList.isEmpty() && checkedList.contains(app.getPackageName())) {
                    this.checkedList.add(app.getPackageName());
                }
                if (toAddToBatch(backupBoolean, app)) {
                    BatchItemX item = new BatchItemX(app);
                    item.setChecked(true);
                    list.add(item);
                }
            }
        }
        return list;
    }

    private boolean toAddToBatch(boolean backupBoolean, AppInfoV2 app) {
        return backupBoolean ? app.isInstalled() : app.getBackupMode() != BaseAppAction.MODE_UNSET;
    }
}
