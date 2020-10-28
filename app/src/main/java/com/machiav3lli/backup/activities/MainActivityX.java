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
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.machiav3lli.backup.BuildConfig;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.SearchViewController;
import com.machiav3lli.backup.databinding.ActivityMainXBinding;
import com.machiav3lli.backup.dialogs.BatchConfirmDialog;
import com.machiav3lli.backup.fragments.AppSheet;
import com.machiav3lli.backup.fragments.HelpSheet;
import com.machiav3lli.backup.fragments.SortFilterSheet;
import com.machiav3lli.backup.handler.BackendController;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.SortFilterManager;
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfoX;
import com.machiav3lli.backup.items.AppMetaInfo;
import com.machiav3lli.backup.items.BackupItem;
import com.machiav3lli.backup.items.BatchItemX;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.items.SortFilterModel;
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.LogUtils;
import com.machiav3lli.backup.utils.PrefUtils;
import com.machiav3lli.backup.utils.UIUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.topjohnwu.superuser.Shell;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainActivityX extends BaseActivity implements BatchConfirmDialog.ConfirmListener {
    private static final String TAG = Constants.classTag(".MainActivityX");
    private static ShellHandler shellHandler;

    static {
        /* Shell.Config methods shall be called before any shell is created
         * This is the why in this example we call it in a static block
         * The followings are some examples, check Javadoc for more details */
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        Shell.setDefaultBuilder(Shell.Builder.create()
                .setTimeout(20));
    }

    private List<AppInfoX> appsList;
    // TODO optimize usage (maybe a map instead?)
    public final List<String> apkCheckedList = new ArrayList<>();
    public final List<String> dataCheckedList = new ArrayList<>();


    private BadgeDrawable updatedBadge;
    private int badgeCounter;
    private PowerManager powerManager;
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
    public HelpSheet sheetHelp;
    private SearchViewController searchViewController;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainXBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        prefs = PrefUtils.getPrivateSharedPrefs(this);
        checkUtilBox();
        setupViews(savedInstanceState);
        setupNavigation();
        setupOnClicks();
    }

    @Override
    protected void onStart() {
        super.onStart();
        runOnUiThread(this::showEncryptionDialog);
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState = mainFastAdapter.saveInstanceState(outState);
        outState = batchFastAdapter.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setupViews(savedInstanceState);
        setupNavigation();
    }

    public static ShellHandler getShellHandlerInstance() {
        return MainActivityX.shellHandler;
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

    private void setupViews(Bundle savedInstanceState) {
        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.app_accent, getTheme()));
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.app_primary_base, getTheme()));
        binding.cbAll.setChecked(false);
        updatedBadge = binding.bottomNavigation.getOrCreateBadge(R.id.mainFragment);
        updatedBadge.setBackgroundColor(getResources().getColor(R.color.app_accent, getTheme()));
        updatedBadge.setVisible(badgeCounter != 0);
        mainFastAdapter = FastAdapter.with(mainItemAdapter);
        batchFastAdapter = FastAdapter.with(batchItemAdapter);
        mainFastAdapter.setHasStableIds(true);
        batchFastAdapter.setHasStableIds(true);
        if (savedInstanceState != null) {
            if (mainBoolean) {
                mainFastAdapter = mainFastAdapter.withSavedInstanceState(savedInstanceState);
            } else {
                batchFastAdapter = batchFastAdapter.withSavedInstanceState(savedInstanceState);
            }
        }
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
        binding.batchBar.setVisibility(View.VISIBLE);
        binding.modeBar.setVisibility(View.VISIBLE);
        binding.buttonAction.setText(backupBoolean ? R.string.backup : R.string.restore);
        binding.recyclerView.setAdapter(batchFastAdapter);
        binding.buttonAction.setOnClickListener(v -> actionOnClick(backupBoolean));
        apkCheckedList.clear();
        dataCheckedList.clear();
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
            boolean oldChecked = item.isChecked();
            item.setApkChecked(!oldChecked);
            item.setDataChecked(!oldChecked);
            if (item.isChecked()) {
                if (!apkCheckedList.contains(item.getApp().getPackageName())) {
                    apkCheckedList.add(item.getApp().getPackageName());
                }
                if (!dataCheckedList.contains(item.getApp().getPackageName())) {
                    dataCheckedList.add(item.getApp().getPackageName());
                }
            } else {
                apkCheckedList.remove(item.getApp().getPackageName());
                dataCheckedList.remove(item.getApp().getPackageName());
            }
            batchFastAdapter.notifyAdapterDataSetChanged();
            updateCheckAll();
            return false;
        });
        binding.apkBatch.setOnClickListener(v -> {
            boolean checkBoolean = apkCheckedList.size() != batchItemAdapter.getItemList().size();
            batchItemAdapter.getAdapterItems().forEach(batchItemX -> {
                        String packageName = batchItemX.getApp().getPackageName();
                        batchItemX.setApkChecked(checkBoolean);
                        if (checkBoolean) apkCheckedList.add(packageName);
                        else apkCheckedList.remove(packageName);
                    }
            );
            batchFastAdapter.notifyAdapterDataSetChanged();
            this.updateCheckAll();
        });
        binding.dataBatch.setOnClickListener(v -> {
            boolean checkBoolean = dataCheckedList.size() != batchItemAdapter.getItemList().size();
            batchItemAdapter.getAdapterItems().forEach(batchItemX -> {
                        String packageName = batchItemX.getApp().getPackageName();
                        batchItemX.setDataChecked(checkBoolean);
                        if (checkBoolean) dataCheckedList.add(packageName);
                        else dataCheckedList.remove(packageName);
                    }
            );
            batchFastAdapter.notifyAdapterDataSetChanged();
            this.updateCheckAll();
        });
        batchFastAdapter.addEventHook(new OnApkCheckBoxClickHook());
        batchFastAdapter.addEventHook(new OnDataCheckBoxClickHook());
    }

    private void onCheckAllChanged(View v) {
        boolean startIsChecked = ((AppCompatCheckBox) v).isChecked();
        binding.cbAll.setChecked(startIsChecked);
        for (BatchItemX item : batchItemAdapter.getAdapterItems()) {
            item.setApkChecked(startIsChecked);
            item.setDataChecked(startIsChecked);
            if (startIsChecked) {
                if (!apkCheckedList.contains(item.getApp().getPackageName())) {
                    apkCheckedList.add(item.getApp().getPackageName());
                }
                if (!dataCheckedList.contains(item.getApp().getPackageName())) {
                    dataCheckedList.add(item.getApp().getPackageName());
                }
            } else {
                apkCheckedList.remove(item.getApp().getPackageName());
                dataCheckedList.remove(item.getApp().getPackageName());
            }
        }
        batchFastAdapter.notifyAdapterDataSetChanged();
    }

    private void updateCheckAll() {
        binding.cbAll.setChecked(apkCheckedList.size() == batchItemAdapter.getItemList().size() && dataCheckedList.size() == batchItemAdapter.getItemList().size());
    }

    public class OnApkCheckBoxClickHook extends ClickEventHook<BatchItemX> {

        @Nullable
        @Override
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder.itemView.findViewById(R.id.apkCheckBox);
        }

        @Override
        public void onClick(@NotNull View view, int i, @NotNull FastAdapter<BatchItemX> fastAdapter, @NotNull BatchItemX item) {
            item.setApkChecked(!item.isApkChecked());
            if (item.isApkChecked() && !apkCheckedList.contains(item.getApp().getPackageName())) {
                apkCheckedList.add(item.getApp().getPackageName());
            } else {
                apkCheckedList.remove(item.getApp().getPackageName());
            }
            batchFastAdapter.notifyAdapterDataSetChanged();
            updateCheckAll();
        }
    }

    public class OnDataCheckBoxClickHook extends ClickEventHook<BatchItemX> {

        @Nullable
        @Override
        public View onBind(@NotNull RecyclerView.ViewHolder viewHolder) {
            return viewHolder.itemView.findViewById(R.id.dataCheckbox);
        }

        @Override
        public void onClick(@NotNull View view, int i, @NotNull FastAdapter<BatchItemX> fastAdapter, @NotNull BatchItemX item) {
            item.setDataChecked(!item.isDataChecked());
            if (item.isDataChecked() && !dataCheckedList.contains(item.getApp().getPackageName())) {
                dataCheckedList.add(item.getApp().getPackageName());
            } else {
                dataCheckedList.remove(item.getApp().getPackageName());
            }
            batchFastAdapter.notifyAdapterDataSetChanged();
            updateCheckAll();
        }
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
                    .setPositiveButton(R.string.dialog_approve, (dialog, which) -> startActivity(new Intent(getApplicationContext(), PrefsActivity.class).putExtra(".toEncryption", true)))
                    .show();
        }
    }

    private void actionOnClick(boolean backupBoolean) {
        ArrayList<AppMetaInfo> selectedList = this.batchItemAdapter.getAdapterItems().stream()
                .filter(BatchItemX::isChecked)
                .map(item -> item.getApp().getAppInfo())
                .collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Integer> selectedListModes = this.batchItemAdapter.getAdapterItems().stream()
                .filter(BatchItemX::isChecked)
                .map(BatchItemX::getActionMode)
                .collect(Collectors.toCollection(ArrayList::new));
        Bundle arguments = new Bundle();
        arguments.putIntegerArrayList("selectedListModes", selectedListModes);
        arguments.putParcelableArrayList("selectedList", selectedList);
        arguments.putBoolean("backupBoolean", backupBoolean);
        BatchConfirmDialog dialog = new BatchConfirmDialog();
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "DialogFragment");
    }

    @Override
    public void onConfirmed(List<Pair<AppMetaInfo, Integer>> selectedItems) {
        new Thread(() -> runBatchTask(selectedItems)).start();
    }

    // TODO 1. optimize/reduce complexity
    public void runBatchTask(List<Pair<AppMetaInfo, Integer>> selectedItems) {
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = this.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MainActivityX.TAG);
        if (this.prefs.getBoolean("acquireWakelock", true)) {
            wl.acquire(60 * 60 * 1000L /*60 minutes to cope with slower devices*/);
            Log.i(MainActivityX.TAG, "wakelock acquired");
        }
        // get the AppInfoX objects again
        List<Pair<AppInfoX, Integer>> selectedApps = new ArrayList<>(selectedItems.size());
        for (Pair<AppMetaInfo, Integer> itemInfo : selectedItems) {
            Optional<BatchItemX> foundItem = this.batchItemAdapter.getAdapterItems().stream()
                    .filter(item -> item.getApp().getPackageName().equals(itemInfo.first.getPackageName()))
                    .findFirst();
            if (foundItem.isPresent()) {
                selectedApps.add(new Pair<>(foundItem.get().getApp(), itemInfo.second));
            } else {
                throw new RuntimeException("Selected item for processing went lost from the item adapter.");
            }
        }

        int notificationId = (int) System.currentTimeMillis();
        int totalOfActions = selectedItems.size();
        int i = 1;
        List<ActionResult> results = new ArrayList<>(totalOfActions);
        for (Pair<AppInfoX, Integer> itemInfo : selectedApps) {
            final String message = String.format("%s (%d/%d)", this.backupBoolean ? this.getString(R.string.backupProgress) : this.getString(R.string.restoreProgress), i, totalOfActions);
            NotificationHelper.showNotification(this, MainActivityX.class, notificationId, message, itemInfo.first.getPackageLabel(), false);
            List<Integer> mileStones = IntStream.range(0, 5).map(step -> (step * totalOfActions / 5) + 1).boxed().collect(Collectors.toList());
            if (mileStones.contains(i))
                runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
            int mode = itemInfo.second;
            final BackupRestoreHelper backupRestoreHelper = new BackupRestoreHelper();
            ActionResult result;
            if (this.backupBoolean) {
                result = backupRestoreHelper.backup(this, MainActivityX.getShellHandlerInstance(), itemInfo.first, mode);
            } else {
                // Latest backup for now
                BackupItem selectedBackup = itemInfo.first.getLatestBackup();
                result = backupRestoreHelper.restore(
                        this, itemInfo.first, selectedBackup.getBackupProperties(),
                        selectedBackup.getBackupLocation(),
                        MainActivityX.getShellHandlerInstance(), mode);
            }
            results.add(result);
            i++;
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
        String notificationTitle = overAllResult.succeeded ? this.getString(R.string.batchSuccess) : this.getString(R.string.batchFailure);
        String notificationMessage = this.backupBoolean ? this.getString(R.string.batchbackup) : this.getString(R.string.batchrestore);
        NotificationHelper.showNotification(this, MainActivityX.class, notificationId, notificationTitle, notificationMessage, true);
        runOnUiThread(() -> Toast.makeText(this, String.format("%s: %s)", notificationMessage, notificationTitle), Toast.LENGTH_LONG).show());

        // show results to the user. Add a save button, if logs should be saved to the application log (in case it's too much)
        UIUtils.showActionResult(this, overAllResult, overAllResult.succeeded ? null : (dialog, which) -> {
            try {
                LogUtils logUtils = new LogUtils(this);
                Uri logFileUri = logUtils.getLogFile();
                logUtils.writeToLogFile(errors);
                runOnUiThread(() -> Toast.makeText(MainActivityX.this, String.format(this.getString(R.string.logfileSavedAt), logFileUri), Toast.LENGTH_LONG).show());
            } catch (IOException | PrefUtils.StorageLocationNotConfiguredException | FileUtils.BackupLocationIsAccessibleException e) {
                e.printStackTrace();
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
        // Initialize the ShellHandler for further root checks
        if (!MainActivityX.initShellHandler()) {
            UIUtils.showWarning(this, MainActivityX.TAG, this.getString(R.string.busyboxProblem), (dialog, id) -> this.finishAffinity());
        }
    }

    public void refresh() {
        if (mainBoolean) {
            refresh(true, false, new ArrayList<>(), new ArrayList<>());
        } else {
            refresh(false, backupBoolean, new ArrayList<>(), new ArrayList<>());
        }
    }

    public void refreshWithAppSheet() {
        refresh(true, true, new ArrayList<>(), new ArrayList<>());
    }

    public void resumeRefresh(List<String> apkCheckedList, List<String> dataCheckedList) {
        if (mainBoolean) {
            refresh(true, true, apkCheckedList, dataCheckedList);
        } else {
            refresh(false, backupBoolean, apkCheckedList, dataCheckedList);
        }
    }

    public void refresh(boolean mainBoolean, boolean backupOrAppSheetBoolean, List<String> apkCheckedList, List<String> dataCheckedList) {
        Log.d(MainActivityX.TAG, "refreshing");
        runOnUiThread(() -> {
            binding.refreshLayout.setRefreshing(true);
            searchViewController.clean();
        });
        badgeCounter = 0;
        if (mainBoolean || apkCheckedList.isEmpty())
            this.apkCheckedList.clear();
        if (mainBoolean || dataCheckedList.isEmpty())
            this.dataCheckedList.clear();
        sheetSortFilter = new SortFilterSheet(SortFilterManager.getFilterPreferences(this));
        new Thread(() -> {
            try {
                appsList = BackendController.getApplicationList(this.getApplicationContext());
                PrefUtils.getPrivateSharedPrefs(this).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, false);
                List<AppInfoX> filteredList = SortFilterManager.applyFilter(appsList, SortFilterManager.getFilterPreferences(this).toString(), this);
                if (mainBoolean)
                    refreshMain(filteredList, backupOrAppSheetBoolean);
                else
                    refreshBatch(filteredList, backupOrAppSheetBoolean, apkCheckedList, dataCheckedList);
            } catch (FileUtils.BackupLocationIsAccessibleException | PrefUtils.StorageLocationNotConfiguredException e) {
                Log.e(TAG, "Could not update application list: " + e);
            }
        }).start();
    }

    private void refreshMain(List<AppInfoX> filteredList, boolean appSheetBoolean) {
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
            if (appSheetBoolean && sheetApp != null) refreshAppSheet();
            UIUtils.slideUp(binding.bottomBar);
        });
    }

    private ArrayList<MainItemX> createMainAppsList(List<AppInfoX> filteredList) {
        ArrayList<MainItemX> list = new ArrayList<>();
        if (filteredList.isEmpty()) {
            for (AppInfoX app : SortFilterManager.applyFilter(appsList, "0000", this)) {
                list.add(new MainItemX(app));
                if (app.isUpdated()) badgeCounter += 1;
            }
            SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
        } else {
            for (AppInfoX app : filteredList) {
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

    private void refreshBatch(List<AppInfoX> filteredList, boolean backupBoolean, List<String> apkCheckedList, List<String> dataCheckedList) {
        ArrayList<BatchItemX> batchList = createBatchAppsList(filteredList, backupBoolean, apkCheckedList, dataCheckedList);
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
            UIUtils.slideUp(binding.bottomBar);
        });
    }

    private ArrayList<BatchItemX> createBatchAppsList(List<AppInfoX> filteredList, boolean backupBoolean, List<String> apkCheckedList, List<String> dataCheckedList) {
        ArrayList<BatchItemX> list = new ArrayList<>();
        if (filteredList.isEmpty()) {
            for (AppInfoX app : SortFilterManager.applyFilter(appsList, "0000", this)) {
                if (toAddToBatch(backupBoolean, app)) list.add(new BatchItemX(app));
            }
            SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
        } else {
            for (AppInfoX app : filteredList) {
                if (!apkCheckedList.isEmpty() && apkCheckedList.contains(app.getPackageName()))
                    this.apkCheckedList.add(app.getPackageName());
                if (!dataCheckedList.isEmpty() && dataCheckedList.contains(app.getPackageName()))
                    this.dataCheckedList.add(app.getPackageName());
                if (toAddToBatch(backupBoolean, app)) {
                    BatchItemX item = new BatchItemX(app);
                    if (this.apkCheckedList.contains(app.getPackageName()))
                        item.setApkChecked(true);
                    if (this.dataCheckedList.contains(app.getPackageName()))
                        item.setDataChecked(true);
                    list.add(item);
                }
            }
        }
        return list;
    }

    private boolean toAddToBatch(boolean backupBoolean, AppInfoX app) {
        return backupBoolean ? app.isInstalled() : app.hasBackups();
    }
}
