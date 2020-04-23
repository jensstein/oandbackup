package com.machiav3lli.backup.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.fragments.AppSheet;
import com.machiav3lli.backup.fragments.SortFilterSheet;
import com.machiav3lli.backup.handler.AppInfoHelper;
import com.machiav3lli.backup.handler.FileCreationHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.SortFilterManager;
import com.machiav3lli.backup.handler.Utils;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.MainItemX;
import com.machiav3lli.backup.items.SortFilterModel;
import com.machiav3lli.backup.schedules.SchedulerActivity;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.machiav3lli.backup.Constants.classAddress;


public class MainActivityX extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    static final int BATCH_REQUEST = 1;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.sort_filter_fab)
    FloatingActionButton fabSortFilter;
    @BindView(R.id.search_view)
    SearchView searchView;
    @BindView(R.id.bottom_bar)
    BottomAppBar bottomBar;

    SharedPreferences prefs;
    File backupDir = IntroActivity.backupDir;
    public static ArrayList<AppInfo> originalList = IntroActivity.originalList;
    ArrayList<MainItemX> list;
    ItemAdapter<MainItemX> itemAdapter;
    FastAdapter<MainItemX> fastAdapter;
    SortFilterSheet sheetSortFilter;
    AppSheet sheetApp;

    long threadId = -1;

    HandleMessages handleMessages;
    ShellCommands shellCommands;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_x);
        handleMessages = new HandleMessages(this);
        prefs = this.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE);
        SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
        shellCommands = new ShellCommands(prefs, getFilesDir());

        if (savedInstanceState != null) {
            threadId = savedInstanceState.getLong(Constants.BUNDLE_THREADID);
            Utils.reShowMessage(handleMessages, threadId);
        }

        ButterKnife.bind(this);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refresh();
            swipeRefreshLayout.setRefreshing(false);
        });

        originalList = AppInfoHelper.getPackageInfo(this,
                backupDir, true, PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
        list = new ArrayList<>();
        for (AppInfo appInfo : originalList) list.add(new MainItemX(appInfo));

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
        itemAdapter.add(list);

        bottomBar.replaceMenu(R.menu.main_bottom_bar);
        bottomBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.preferences)
                startActivity(new Intent(getApplicationContext(), PrefsActivity.class));
            return true;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                itemAdapter.filter(newText);
                itemAdapter.getItemFilter().setFilterPredicate((mainItemX, charSequence) -> mainItemX.getApp().getLabel().toLowerCase().contains(String.valueOf(charSequence).toLowerCase()));
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                itemAdapter.filter(query);
                itemAdapter.getItemFilter().setFilterPredicate((mainItemX, charSequence) -> mainItemX.getApp().getLabel().toLowerCase().contains(String.valueOf(charSequence).toLowerCase()));
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

    @OnClick(R.id.btn_batch_backup)
    public void btnBatchBackup() {
        startActivityForResult(batchIntent(BatchActivityX.class, true), BATCH_REQUEST);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @OnClick(R.id.btn_batch_restore)
    public void btnBatchRestore() {
        startActivityForResult(batchIntent(BatchActivityX.class, false), BATCH_REQUEST);
    }

    @OnClick(R.id.btn_scheduler)
    public void btnScheduler() {
        startActivity(new Intent(getApplicationContext(), SchedulerActivity.class));
    }

    public Intent batchIntent(Class batchClass, boolean backup) {
        Intent batchIntent = new Intent(getApplicationContext(), batchClass);
        batchIntent.putExtra(classAddress(".backupBoolean"), backup);
        return batchIntent;
    }

    public void refresh() {
        Thread refreshThread = new Thread(() -> {
            sheetSortFilter = new SortFilterSheet(SortFilterManager.getFilterPreferences(this));
            originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                    this.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
            ArrayList<AppInfo> filteredList = SortFilterManager.applyFilter(originalList, SortFilterManager.getFilterPreferences(this).toString(), this);
            list = new ArrayList<>();
            for (AppInfo app : filteredList) list.add(new MainItemX(app));
            runOnUiThread(() -> {
                if (itemAdapter != null) FastAdapterDiffUtil.INSTANCE.set(itemAdapter, list);
            });
        });
        refreshThread.start();
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
        SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {
            case Constants.PREFS_PATH_BACKUP_DIRECTORY:
                String backupDirPath = prefs.getString(key, FileCreationHelper.getDefaultBackupDirPath());
                assert backupDirPath != null;
                backupDir = Utils.createBackupDir(this, backupDirPath);
            case Constants.PREFS_PATH_BUSYBOX:
                shellCommands = new ShellCommands(prefs, getFilesDir());
                if (!shellCommands.checkBusybox())
                    Utils.showWarning(this, "", getString(R.string.busyboxProblem));
            default:
                refresh();
        }
    }
}
