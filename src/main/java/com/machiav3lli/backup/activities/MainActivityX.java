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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.fragments.AppSheet;
import com.machiav3lli.backup.fragments.SortFilterSheet;
import com.machiav3lli.backup.handler.AppInfoHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.SortFilterManager;
import com.machiav3lli.backup.handler.Utils;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.items.SortFilterModel;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.machiav3lli.backup.Constants.classAddress;
import static com.machiav3lli.backup.handler.FileCreationHelper.getDefaultBackupDirPath;

public class MainActivityX extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final String TAG = Constants.classTag(".MainActivityX");
    static final int BATCH_REQUEST = 1;
    long threadId = -1;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.search_view)
    SearchView searchView;
    @BindView(R.id.sort_filter_fab)
    FloatingActionButton fab;

    public static ArrayList<AppInfo> originalList;
    File backupDir = IntroActivity.backupDir;
    ItemAdapter<MainItemX> itemAdapter;
    FastAdapter<MainItemX> fastAdapter;
    SortFilterSheet sheetSortFilter;
    AppSheet sheetApp;

    HandleMessages handleMessages;
    SharedPreferences prefs;
    ArrayList<String> users;
    ShellCommands shellCommands;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_x);
        handleMessages = new HandleMessages(this);
        prefs = this.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE);
        showBatteryOptimizationDialog();
        users = new ArrayList<>();
        if (savedInstanceState != null)
            users = savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS);
        shellCommands = new ShellCommands(this, prefs, users, getFilesDir());

        if (!SortFilterManager.getRememberFiltering(this))
            SortFilterManager.saveFilterPreferences(this, new SortFilterModel());

        ButterKnife.bind(this);
        if (savedInstanceState != null) {
            threadId = savedInstanceState.getLong(Constants.BUNDLE_THREADID);
            Utils.reShowMessage(handleMessages, threadId);
        }

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.app_accent, getTheme()));
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.app_primary_base, getTheme()));
        swipeRefreshLayout.setOnRefreshListener(this::refresh);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        fastAdapter.setHasStableIds(true);
        recyclerView.setAdapter(fastAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fastAdapter.setOnClickListener((view, itemIAdapter, item, integer) -> {
            if (sheetApp != null) sheetApp.dismissAllowingStateLoss();
            sheetApp = new AppSheet(item);
            sheetApp.show(getSupportFragmentManager(), "APPSHEET");
            return false;
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) fab.hide();
                else if (dy < 0) fab.show();
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    @OnClick(R.id.sort_filter_fab)
    public void showSortFilterDialog() {
        if (sheetSortFilter == null)
            sheetSortFilter = new SortFilterSheet(new SortFilterModel(SortFilterManager.getFilterPreferences(this).toString()));
        sheetSortFilter.show(getSupportFragmentManager(), "SORTFILTERSHEET");
    }

    @OnClick(R.id.btn_settings)
    public void btnSettings() {
        startActivity(new Intent(getApplicationContext(), PrefsActivity.class));
    }

    @OnClick(R.id.btn_batch_backup)
    public void btnBatchBackup() {
        startActivityForResult(batchIntent(BatchActivityX.class, true), BATCH_REQUEST);
    }

    @OnClick(R.id.btn_batch_restore)
    public void btnBatchRestore() {
        startActivityForResult(batchIntent(BatchActivityX.class, false), BATCH_REQUEST);
    }

    @OnClick(R.id.btn_scheduler)
    public void btnScheduler() {
        startActivity(new Intent(getApplicationContext(), SchedulerActivityX.class));
    }

    public Intent batchIntent(Class batchClass, boolean backup) {
        Intent batchIntent = new Intent(getApplicationContext(), batchClass);
        batchIntent.putExtra(classAddress(".backupBoolean"), backup);
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
        switch (key) {
            case Constants.PREFS_PATH_BACKUP_DIRECTORY:
                String backupDirPath = getDefaultBackupDirPath(this);
                backupDir = Utils.createBackupDir(this, backupDirPath);
            case Constants.PREFS_PATH_TOYBOX:
                shellCommands = new ShellCommands(this, sharedPreferences, getFilesDir());
                if (!shellCommands.checkToybox())
                    Utils.showWarning(this, TAG, getString(R.string.busyboxProblem));
            default:
                refresh();
        }
    }

    private void showBatteryOptimizationDialog() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean dontShowAgain = prefs.getBoolean(Constants.PREFS_Ignore_Battery_Optimization, false);
        if (dontShowAgain || powerManager.isIgnoringBatteryOptimizations(getPackageName())) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.ignore_battery_optimization_title)
                .setMessage(R.string.ignore_battery_optimization_message)
                .setPositiveButton(R.string.ignore_battery_optimization_approve, (dialog, which) -> {
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
                .setNeutralButton(R.string.ignore_battery_optimization_refuse, (dialog, which) ->
                        prefs.edit().putBoolean(Constants.PREFS_Ignore_Battery_Optimization, true).apply())
                .show();
    }

    public void refresh() {
        sheetSortFilter = new SortFilterSheet(SortFilterManager.getFilterPreferences(this));
        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));
        new Thread(() -> {
            originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                    this.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
            ArrayList<AppInfo> filteredList = SortFilterManager.applyFilter(originalList, SortFilterManager.getFilterPreferences(this).toString(), this);
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
            runOnUiThread(() -> {
                if (filteredList.isEmpty()) {
                    Toast.makeText(this, getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show();
                    itemAdapter.clear();
                    itemAdapter.add(list);
                } else {
                    FastAdapterDiffUtil.INSTANCE.set(itemAdapter, list);
                }
                searchView.setQuery("", false);
                swipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }
}
