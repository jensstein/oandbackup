package com.machiav3lli.backup.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
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
import com.machiav3lli.backup.items.ActionResult;
import com.machiav3lli.backup.items.AppInfo;
import com.machiav3lli.backup.items.BatchItemX;
import com.machiav3lli.backup.items.SortFilterModel;
import com.machiav3lli.backup.utils.LogUtils;
import com.machiav3lli.backup.utils.UIUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.machiav3lli.backup.Constants.classAddress;
import static com.machiav3lli.backup.utils.FileUtils.createBackupDir;
import static com.machiav3lli.backup.utils.FileUtils.getDefaultBackupDirPath;

public class BatchActivityX extends BaseActivity
        implements BatchConfirmDialog.ConfirmListener, SharedPreferences.OnSharedPreferenceChangeListener {
    static final String TAG = Constants.classTag(".BatchActivityX");
    final static int RESULT_OK = 0;
    long threadId = -1;
    boolean backupBoolean;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.radioApk)
    Chip rbApk;
    @BindView(R.id.radioData)
    Chip rbData;
    @BindView(R.id.radioBoth)
    Chip rbBoth;
    @BindView(R.id.backupRestoreButton)
    AppCompatButton actionButton;
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
    File backupDir;
    SortFilterSheet sheetSortFilter;
    ItemAdapter<BatchItemX> itemAdapter;
    FastAdapter<BatchItemX> fastAdapter;
    ArrayList<AppInfo> originalList = MainActivityX.originalList;

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
            UIUtils.reShowMessage(handleMessages, threadId);
        }

        String backupDirPath = getDefaultBackupDirPath(this);
        backupDir = createBackupDir(this, backupDirPath);
        if (originalList == null) originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                prefs.getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));

        ButterKnife.bind(this);
        rbBoth.setChecked(true);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.app_accent, getTheme()));
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.app_primary_base, getTheme()));
        swipeRefreshLayout.setOnRefreshListener(this::refresh);

        if (getIntent().getExtras() != null)
            backupBoolean = getIntent().getExtras().getBoolean(classAddress(".backupBoolean"));
        if (backupBoolean) actionButton.setText(R.string.backup);
        else actionButton.setText(R.string.restore);

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

        back.setOnClickListener(v -> finish());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) fab.hide();
                else if (dy < 0) fab.show();
            }
        });
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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
        for (BatchItemX item : itemAdapter.getAdapterItems()) {
            if (item.getApp().isChecked()) {
                selectedList.add(item);
            }
        }
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
        if (this.backupDir != null) {
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = this.powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BatchActivityX.TAG);
            if (this.prefs.getBoolean("acquireWakelock", true)) {
                wl.acquire(10 * 60 * 1000L /*10 minutes*/);
                Log.i(BatchActivityX.TAG, "wakelock acquired");
            }
            this.changesMade = true;
            int notificationId = (int) System.currentTimeMillis();
            int total = selectedList.size();
            int i = 1;
            List<ActionResult> results = new ArrayList<>(total);
            for (BatchItemX item : selectedList) {
                String message = "(" + i + '/' + total + ')';
                String title = (this.backupBoolean ? this.getString(R.string.backupProgress) : this.getString(R.string.restoreProgress))
                        + " (" + i + '/' + total + ')';
                NotificationHelper.showNotification(this, BatchActivityX.class, notificationId, title, item.getApp().getLabel(), false);
                this.handleMessages.setMessage(item.getApp().getLabel(), message);
                int mode = AppInfo.MODE_BOTH;
                if (this.rbApk.isChecked()) {
                    mode = AppInfo.MODE_APK;
                } else if (this.rbData.isChecked()) {
                    mode = AppInfo.MODE_DATA;
                }
                final BackupRestoreHelper backupRestoreHelper = new BackupRestoreHelper();
                ActionResult result;
                if (this.backupBoolean) {
                    result = backupRestoreHelper.backup(this, IntroActivity.getShellHandlerInstance(), item.getApp(), mode);
                } else {
                    result = backupRestoreHelper.restore(this, item.getApp(), IntroActivity.getShellHandlerInstance(), mode);
                }
                results.add(result);
                i++;
                this.handleMessages.endMessage();
            }
            if (wl.isHeld()) {
                wl.release();
                Log.i(BatchActivityX.TAG, "wakelock released");
            }
            // Calculate the overall result
            String errors = results.parallelStream()
                    .map(ar -> ar.message)
                    .filter(msg -> !msg.isEmpty())
                    .collect(Collectors.joining("\n"));
            ActionResult overAllResult = new ActionResult(null, errors, results.parallelStream().anyMatch(ar -> ar.succeeded));

            // Update the notification
            String msg = this.backupBoolean ? this.getString(R.string.batchbackup) : this.getString(R.string.batchrestore);
            String notificationTitle = overAllResult.succeeded ? this.getString(R.string.batchSuccess) : this.getString(R.string.batchFailure);
            NotificationHelper.showNotification(this, BatchActivityX.class, notificationId, notificationTitle, msg, true);

            // show results to the user. Add a save button, if logs should be saved to the application log (in case it's too much)
            UIUtils.showActionResult(this, overAllResult, overAllResult.succeeded ? null : (dialog, which) -> {
                try (FileWriter fw = new FileWriter(LogUtils.getDefaultLogFilePath(this.getApplicationContext()), true)) {
                    fw.write(errors);
                    Toast.makeText(
                            BatchActivityX.this,
                            String.format(this.getString(R.string.logfileSavedAt),
                                    LogUtils.getDefaultLogFilePath(this.getApplicationContext())),
                            Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    new AlertDialog.Builder(BatchActivityX.this)
                            .setTitle(R.string.errorDialogTitle)
                            .setMessage(e.getLocalizedMessage())
                            .setPositiveButton(R.string.dialogOK, null)
                            .show();
                }
            });
            this.refresh();
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
                backupDir = createBackupDir(this, backupDirPath);
            case Constants.PREFS_PATH_TOYBOX:
                shellCommands = new ShellCommands(this, sharedPreferences, getFilesDir());
                if (!shellCommands.checkUtilBoxPath())
                    UIUtils.showWarning(this, TAG, getString(R.string.busyboxProblem));
            default:
                refresh();
        }
    }

    public void refresh() {
        sheetSortFilter = new SortFilterSheet(SortFilterManager.getFilterPreferences(this));
        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(true));
        new Thread(() -> {
            cbAll.setChecked(false);
            originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                    this.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
            ArrayList<AppInfo> filteredList = SortFilterManager.applyFilter(originalList, SortFilterManager.getFilterPreferences(this).toString(), this);
            ArrayList<BatchItemX> list = new ArrayList<>();
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
                swipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }
}
