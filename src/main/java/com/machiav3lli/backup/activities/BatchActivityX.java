package com.machiav3lli.backup.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.dialogs.BatchConfirmDialog;
import com.machiav3lli.backup.fragments.SortFilterSheet;
import com.machiav3lli.backup.handler.AppInfoHelper;
import com.machiav3lli.backup.handler.BackupRestoreHelper;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.NotificationHelper;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.SortFilterManager;
import com.machiav3lli.backup.handler.Utils;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.BatchItemX;
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

public class BatchActivityX extends BaseActivity
        implements BatchConfirmDialog.ConfirmListener, SharedPreferences.OnSharedPreferenceChangeListener {
    static final String TAG = Constants.classTag(".BatchActivityX");
    long threadId = -1;
    final static int RESULT_OK = 0;
    ArrayList<AppInfo> originalList = MainActivityX.originalList;
    boolean backupBoolean;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.radioApk)
    AppCompatRadioButton rbApk;
    @BindView(R.id.radioData)
    AppCompatRadioButton rbData;
    @BindView(R.id.radioBoth)
    AppCompatRadioButton rbBoth;
    @BindView(R.id.backupRestoreButton)
    MaterialButton actionButton;
    @BindView(R.id.cbAll)
    AppCompatCheckBox cbAll;
    @BindView(R.id.search_view)
    SearchView searchView;
    @BindView(R.id.back)
    AppCompatImageView back;
    @BindView(R.id.sort_filter_fab)
    FloatingActionButton fab;

    boolean checkboxSelectAllBoolean = false;
    boolean changesMade;
    SortFilterSheet sheetSortFilter;
    ArrayList<BatchItemX> list;
    ItemAdapter<BatchItemX> itemAdapter;
    FastAdapter<BatchItemX> fastAdapter;
    File backupDir;

    HandleMessages handleMessages;
    SharedPreferences prefs;
    PowerManager powerManager;
    ArrayList<String> users;
    ShellCommands shellCommands;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_x);
        handleMessages = new HandleMessages(this);
        prefs = this.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        users = new ArrayList<>();
        if (savedInstanceState != null)
            users = savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS);
        shellCommands = new ShellCommands(this, prefs, users, getFilesDir());

        if (savedInstanceState != null) {
            threadId = savedInstanceState.getLong(Constants.BUNDLE_THREADID);
            Utils.reShowMessage(handleMessages, threadId);
        }
        if (getIntent().getExtras() != null)
            backupBoolean = getIntent().getExtras().getBoolean(classAddress(".backupBoolean"));

        String backupDirPath = getDefaultBackupDirPath(this);
        backupDir = Utils.createBackupDir(this, backupDirPath);
        if (originalList == null) originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                prefs.getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));

        ButterKnife.bind(this);
        rbBoth.setChecked(true);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refresh();
            swipeRefreshLayout.setRefreshing(false);
        });

        list = new ArrayList<>();
        if (backupBoolean) {
            if (SortFilterManager.getRememberFiltering(this)) {
                ArrayList<AppInfo> filteredList = SortFilterManager.applyFilter(originalList, SortFilterManager.getFilterPreferences(this).toString(), this);
                for (AppInfo app : filteredList)
                    if (app.isInstalled()) list.add(new BatchItemX(app));
            } else {
                SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
                for (AppInfo app : originalList)
                    if (app.isInstalled()) list.add(new BatchItemX(app));
            }
            actionButton.setText(R.string.backup);
        } else {
            if (SortFilterManager.getRememberFiltering(this)) {
                ArrayList<AppInfo> filteredList = SortFilterManager.applyFilter(originalList, SortFilterManager.getFilterPreferences(this).toString(), this);
                for (AppInfo app : filteredList)
                    if (app.getBackupMode() != AppInfo.MODE_UNSET) list.add(new BatchItemX(app));
            } else {
                SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
                for (AppInfo app : originalList)
                    if (app.getBackupMode() != AppInfo.MODE_UNSET) list.add(new BatchItemX(app));
            }
            actionButton.setText(R.string.restore);
        }

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        fastAdapter.setHasStableIds(true);
        recyclerView.setAdapter(fastAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fastAdapter.setOnClickListener((view, itemIAdapter, item, integer) -> {
            item.getApp().setChecked(!item.getApp().isChecked());
            fastAdapter.notifyAdapterDataSetChanged();
            return false;
        });
        itemAdapter.add(list);

        back.setOnClickListener(v -> finish());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0)
                    fab.hide();
                else if (dy < 0)
                    fab.show();
            }
        });
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                itemAdapter.filter(newText);
                itemAdapter.getItemFilter().setFilterPredicate((batchItemX, charSequence) -> batchItemX.getApp().getLabel().toLowerCase().contains(String.valueOf(charSequence).toLowerCase()));
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                itemAdapter.filter(query);
                itemAdapter.getItemFilter().setFilterPredicate((batchItemX, charSequence) -> batchItemX.getApp().getLabel().toLowerCase().contains(String.valueOf(charSequence).toLowerCase()));
                return true;
            }
        });
        cbAll.setOnClickListener(v -> {
            if (checkboxSelectAllBoolean) {
                for (BatchItemX item : itemAdapter.getAdapterItems())
                    item.getApp().setChecked(false);
            } else {
                for (BatchItemX item : itemAdapter.getAdapterItems())
                    item.getApp().setChecked(true);
            }
            fastAdapter.notifyAdapterDataSetChanged();
            checkboxSelectAllBoolean = !checkboxSelectAllBoolean;
        });
    }

    @OnClick(R.id.sort_filter_fab)
    public void showSortFilterDialog() {
        if (sheetSortFilter == null)
            sheetSortFilter = new SortFilterSheet(new SortFilterModel(SortFilterManager.getFilterPreferences(this).toString()));
        sheetSortFilter.show(getSupportFragmentManager(), "SORTFILTERSHEET");
    }

    @OnClick(R.id.backupRestoreButton)
    public void action() {
        ArrayList<BatchItemX> selectedList = new ArrayList<>();
        for (BatchItemX item : list) if (item.getApp().isChecked()) selectedList.add(item);
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList("selectedList", selectedList);
        arguments.putBoolean("backupBoolean", backupBoolean);
        BatchConfirmDialog dialog = new BatchConfirmDialog();
        dialog.setArguments(arguments);
        dialog.show(getSupportFragmentManager(), "DialogFragment");
    }

    @Override
    public void onConfirmed(ArrayList<BatchItemX> selectedList) {
        final ArrayList<BatchItemX> list = new ArrayList<>(selectedList);
        Thread thread = new Thread(() -> doAction(list));
        thread.start();
        threadId = thread.getId();
    }

    public void doAction(ArrayList<BatchItemX> selectedList) {
        if (backupDir != null) {
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            if (prefs.getBoolean("acquireWakelock", true)) {
                wl.acquire(10 * 60 * 1000L /*10 minutes*/);
                Log.i(TAG, "wakelock acquired");
            }
            changesMade = true;
            int id = (int) System.currentTimeMillis();
            int total = selectedList.size();
            int i = 1;
            boolean errorFlag = false;
            for (BatchItemX item : selectedList) {
                if (item.getApp().isChecked()) {
                    String message = "(" + i + "/" + total + ")";
                    String title = backupBoolean ? getString(R.string.backupProgress) : getString(R.string.restoreProgress);
                    title = title + " (" + i + "/" + total + ")";
                    NotificationHelper.showNotification(this, BatchActivityX.class, id, title, item.getApp().getLabel(), false);
                    handleMessages.setMessage(item.getApp().getLabel(), message);
                    int mode = AppInfo.MODE_BOTH;
                    if (rbApk.isChecked()) mode = AppInfo.MODE_APK;
                    else if (rbData.isChecked()) mode = AppInfo.MODE_DATA;
                    final BackupRestoreHelper backupRestoreHelper = new BackupRestoreHelper();
                    if (backupBoolean) {
                        if (backupRestoreHelper.backup(this, backupDir, item.getApp(), shellCommands, mode) != 0)
                            errorFlag = true;
                    } else {
                        if (backupRestoreHelper.restore(this, backupDir, item.getApp(), shellCommands, mode) != 0)
                            errorFlag = true;
                    }
                    if (i == total) {
                        String msg = backupBoolean ? getString(R.string.batchbackup) : getString(R.string.batchrestore);
                        String notificationTitle = errorFlag ? getString(R.string.batchFailure) : getString(R.string.batchSuccess);
                        NotificationHelper.showNotification(this, BatchActivityX.class, id, notificationTitle, msg, true);
                        handleMessages.endMessage();
                    }
                    i++;
                }
            }
            if (wl.isHeld()) {
                wl.release();
                Log.i(TAG, "wakelock released");
            }
            if (errorFlag) {
                Utils.showErrors(this);
            }
            refresh();
        }
    }

    @Override
    public void finish() {
        setResult(RESULT_OK, new Intent().putExtra("changesMade", changesMade));
        super.finish();
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
            case Constants.PREFS_PATH_BUSYBOX:
                shellCommands = new ShellCommands(this, sharedPreferences, getFilesDir());
                if (!shellCommands.checkBusybox())
                    Utils.showWarning(this, TAG, getString(R.string.busyboxProblem));
            default:
                refresh();
        }
    }

    public void refresh() {
        sheetSortFilter = new SortFilterSheet(SortFilterManager.getFilterPreferences(this));
        new Thread(() -> {
            cbAll.setChecked(false);
            originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                    this.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
            ArrayList<AppInfo> filteredList = SortFilterManager.applyFilter(originalList, SortFilterManager.getFilterPreferences(this).toString(), this);
            list = new ArrayList<>();
            if (!filteredList.isEmpty()) {
                if (backupBoolean) {
                    for (AppInfo app : filteredList)
                        if (app.isInstalled()) list.add(new BatchItemX(app));
                } else for (AppInfo app : filteredList)
                    if (app.getBackupMode() != AppInfo.MODE_UNSET) list.add(new BatchItemX(app));
            }
            boolean listIsEmpty = list.isEmpty();
            if (listIsEmpty) {
                if (backupBoolean) {
                    for (AppInfo app : originalList)
                        if (app.isInstalled()) list.add(new BatchItemX(app));
                } else for (AppInfo app : originalList)
                    if (app.getBackupMode() != AppInfo.MODE_UNSET) list.add(new BatchItemX(app));
                SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
            }
            runOnUiThread(() -> {
                if (listIsEmpty) {
                    itemAdapter.clear();
                    Toast.makeText(this, getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show();
                    itemAdapter.add(list);
                } else FastAdapterDiffUtil.INSTANCE.set(itemAdapter, list);
                searchView.setQuery("", false);
            });
        }).start();
    }
}
