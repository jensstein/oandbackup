package com.machiav3lli.backup.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.ActivityMainXBinding;
import com.machiav3lli.backup.fragments.AppSheet;
import com.machiav3lli.backup.fragments.SortFilterSheet;
import com.machiav3lli.backup.handler.AppInfoHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellCommands;
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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivityX extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = Constants.classTag(".MainActivityX");
    private static final int BATCH_REQUEST = 1;
    private static List<AppInfo> originalList;

    static {
        /* Shell.Config methods shall be called before any shell is created
         * This is the why in this example we call it in a static block
         * The followings are some examples, check Javadoc for more details */
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(20);
    }

    long threadId = -1;
    File backupDir = IntroActivity.backupDir;
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
        prefs = this.getSharedPreferences(Constants.PREFS_SHARED_PRIVATE, Context.MODE_PRIVATE);
        showBatteryOptimizationDialog();
        showEncryptionDialog();
        users = new ArrayList<>();
        if (savedInstanceState != null)
            users = savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS);
        shellCommands = new ShellCommands(this, prefs, users);

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

        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) binding.fabSortFilter.hide();
                else if (dy < 0) binding.fabSortFilter.show();
            }
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

    public Intent batchIntent(Class batchClass, boolean backup) {
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
        if (Constants.PREFS_PATH_BACKUP_DIRECTORY.equals(key)) {
            backupDir = FileUtils.getDefaultBackupDir(this, this);
        } else if (Constants.PREFS_PATH_TOYBOX.equals(key)) {
            shellCommands = new ShellCommands(this, sharedPreferences);
            if (!shellCommands.checkUtilBoxPath())
                UIUtils.showWarning(this, TAG, getString(R.string.busyboxProblem));
        }
        refresh();
    }

    private void showBatteryOptimizationDialog() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean dontShowAgain = prefs.getBoolean(Constants.PREFS_Ignore_Battery_Optimization, false);
        if (dontShowAgain || powerManager.isIgnoringBatteryOptimizations(getPackageName())) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.ignore_battery_optimization_title)
                .setMessage(R.string.ignore_battery_optimization_message)
                .setPositiveButton(R.string.dialog_approve, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.w(TAG, "Ignore battery optimizations not supported", e);
                        Toast.makeText(this, R.string.ignore_battery_optimization_not_supported, Toast.LENGTH_LONG).show();
                        prefs.edit().putBoolean(Constants.PREFS_Ignore_Battery_Optimization, true).apply();
                    }
                })
                .setNeutralButton(R.string.dialog_refuse, (dialog, which) ->
                        prefs.edit().putBoolean(Constants.PREFS_Ignore_Battery_Optimization, true).apply())
                .show();
    }

    private void showEncryptionDialog() {
        SharedPreferences defPrefs = PrefUtils.getDefaultSharedPreferences(this);
        boolean dontShowAgain = defPrefs.getBoolean(Constants.PREFS_ENCRYPTION, false) && !defPrefs.getString(Constants.PREFS_PASSWORD, "").isEmpty();
        if (dontShowAgain) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.enable_encryption_title)
                .setMessage(R.string.enable_encryption_message)
                .setPositiveButton(R.string.dialog_approve, (dialog, which) -> startActivity(new Intent(getApplicationContext(), PrefsActivity.class)))
                .show();
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
