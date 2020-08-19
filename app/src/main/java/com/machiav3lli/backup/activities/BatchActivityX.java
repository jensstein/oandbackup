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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.databinding.ActivityBatchXBinding;
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
import com.machiav3lli.backup.utils.FileUtils;
import com.machiav3lli.backup.utils.LogUtils;
import com.machiav3lli.backup.utils.UIUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BatchActivityX extends BaseActivity
        implements BatchConfirmDialog.ConfirmListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = Constants.classTag(".BatchActivityX");
    private static final int RESULT_OK = 0;
    private File backupDir;
    private boolean checkboxSelectAllBoolean = false;
    private boolean changesMade;
    private SortFilterSheet sheetSortFilter;
    private ItemAdapter<BatchItemX> itemAdapter;
    private FastAdapter<BatchItemX> fastAdapter;
    private List<AppInfo> originalList = MainActivityX.getOriginalList();
    private ArrayList<String> checkedList = new ArrayList<>();
    private HandleMessages handleMessages;
    private SharedPreferences prefs;
    private PowerManager powerManager;
    private ShellCommands shellCommands;
    private long threadId = -1;
    private boolean backupBoolean;
    private ActivityBatchXBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBatchXBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupOnClicks(this);

        handleMessages = new HandleMessages(this);
        prefs = this.getSharedPreferences(Constants.PREFS_SHARED_PRIVATE, Context.MODE_PRIVATE);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        ArrayList<String> users = new ArrayList<>();
        if (savedInstanceState != null)
            users = savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS);
        shellCommands = new ShellCommands(this, prefs, users);

        if (savedInstanceState != null) {
            threadId = savedInstanceState.getLong(Constants.BUNDLE_THREADID);
            UIUtils.reShowMessage(handleMessages, threadId);
        }

        String backupDirPath = FileUtils.getBackupDirectoryPath(this);
        backupDir = FileUtils.createBackupDir(this, backupDirPath);
        if (originalList.isEmpty())
            originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                    prefs.getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));

        binding.radioBoth.setChecked(true);

        binding.refreshLayout.setColorSchemeColors(getResources().getColor(R.color.app_accent, getTheme()));
        binding.refreshLayout.setProgressBackgroundColorSchemeColor(getResources().getColor(R.color.app_primary_base, getTheme()));
        binding.refreshLayout.setOnRefreshListener(() -> refresh(false));

        if (getIntent().getExtras() != null)
            backupBoolean = getIntent().getExtras().getBoolean(Constants.classAddress(".backupBoolean"));
        if (backupBoolean) binding.actionButton.setText(R.string.backup);
        else binding.actionButton.setText(R.string.restore);

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        fastAdapter.setHasStableIds(true);
        binding.recyclerView.setAdapter(fastAdapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fastAdapter.setOnPreClickListener((view, batchItemXIAdapter, batchItemX, integer) -> false);
        fastAdapter.setOnClickListener((view, itemIAdapter, item, integer) -> {
            item.getApp().setChecked(!item.getApp().isChecked());
            fastAdapter.notifyAdapterDataSetChanged();
            return false;
        });
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) binding.fabSortFilter.hide();
                else if (dy < 0) binding.fabSortFilter.show();
            }
        });
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
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
        binding.backButton.setOnClickListener(v -> finish());
        binding.fabSortFilter.setOnClickListener(v -> {
            if (sheetSortFilter == null)
                sheetSortFilter = new SortFilterSheet(new SortFilterModel(SortFilterManager.getFilterPreferences(context).toString()));
            sheetSortFilter.show(getSupportFragmentManager(), "SORTFILTERSHEET");
        });
        binding.actionButton.setOnClickListener(v -> {
            ArrayList<BatchItemX> selectedList = new ArrayList<>();
            for (BatchItemX item : itemAdapter.getAdapterItems()) {
                if (item.getApp().isChecked()) selectedList.add(item);
            }
            Bundle arguments = new Bundle();
            arguments.putParcelableArrayList("selectedList", selectedList);
            arguments.putBoolean("backupBoolean", backupBoolean);
            BatchConfirmDialog dialog = new BatchConfirmDialog();
            dialog.setArguments(arguments);
            dialog.show(getSupportFragmentManager(), "DialogFragment");
        });
        binding.cbAll.setOnClickListener(v -> {
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

    @Override
    public void onConfirmed(List<BatchItemX> selectedList) {
        Thread thread = new Thread(() -> doAction(selectedList));
        thread.start();
        threadId = thread.getId();
    }

    public void doAction(List<BatchItemX> selectedList) {
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
                if (binding.radioApk.isChecked()) {
                    mode = AppInfo.MODE_APK;
                } else if (binding.radioData.isChecked()) {
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
            }
            if (this.handleMessages.isShowing()) {
                this.handleMessages.endMessage();
            }
            if (wl.isHeld()) {
                wl.release();
                Log.i(BatchActivityX.TAG, "wakelock released");
            }
            // Calculate the overall result
            String errors = results.parallelStream()
                    .map(ActionResult::getMessage)
                    .filter(msg -> !msg.isEmpty())
                    .collect(Collectors.joining("\n"));
            ActionResult overAllResult = new ActionResult(null, errors, results.parallelStream().anyMatch(ar -> ar.succeeded));

            // Update the notification
            String msg = this.backupBoolean ? this.getString(R.string.batchbackup) : this.getString(R.string.batchrestore);
            String notificationTitle = overAllResult.succeeded ? this.getString(R.string.batchSuccess) : this.getString(R.string.batchFailure);
            NotificationHelper.showNotification(this, BatchActivityX.class, notificationId, notificationTitle, msg, true);

            // show results to the user. Add a save button, if logs should be saved to the application log (in case it's too much)
            UIUtils.showActionResult(this, overAllResult, overAllResult.succeeded ? null : (dialog, which) -> {
                try (FileWriter fw = new FileWriter(FileUtils.getDefaultLogFilePath(this.getApplicationContext()), true)) {
                    fw.write(errors);
                    Toast.makeText(
                            BatchActivityX.this,
                            String.format(this.getString(R.string.logfileSavedAt), FileUtils.getDefaultLogFilePath(this.getApplicationContext())),
                            Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    new AlertDialog.Builder(BatchActivityX.this)
                            .setTitle(R.string.errorDialogTitle)
                            .setMessage(e.getLocalizedMessage())
                            .setPositiveButton(R.string.dialogOK, null)
                            .show();
                }
            });
            this.refresh(false);
        }
    }

    @Override
    public void finish() {
        setResult(RESULT_OK, new Intent().putExtra("changesMade", changesMade));
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkedList = new ArrayList<>();
        for (BatchItemX item : itemAdapter.getAdapterItems()) {
            if (item.getApp().isChecked()) checkedList.add(item.getApp().getPackageName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handleMessages = new HandleMessages(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        refresh(!checkedList.isEmpty());
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
                backupDir = FileUtils.getDefaultBackupDir(this, this);
                break;
            case Constants.PREFS_PATH_TOYBOX:
                shellCommands = new ShellCommands(this, sharedPreferences);
                if (!shellCommands.checkUtilBoxPath())
                    UIUtils.showWarning(this, TAG, getString(R.string.busyboxProblem));
                break;
            default:
                refresh(false);
        }
    }

    public void refresh(boolean resume) {
        sheetSortFilter = new SortFilterSheet(SortFilterManager.getFilterPreferences(this));
        runOnUiThread(() -> binding.refreshLayout.setRefreshing(true));
        new Thread(() -> {
            binding.cbAll.setChecked(false);
            originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                    this.getSharedPreferences(Constants.PREFS_SHARED_PRIVATE, Context.MODE_PRIVATE).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
            ArrayList<AppInfo> filteredList = SortFilterManager.applyFilter(originalList, SortFilterManager.getFilterPreferences(this).toString(), this);
            ArrayList<BatchItemX> list = createItemsList(filteredList, resume);
            boolean listIsEmpty = list.isEmpty();
            if (listIsEmpty) fillItemsList(list);
            runOnUiThread(() -> {
                if (listIsEmpty) {
                    itemAdapter.clear();
                    Toast.makeText(this, getString(R.string.empty_filtered_list), Toast.LENGTH_SHORT).show();
                    itemAdapter.add(list);


                } else FastAdapterDiffUtil.INSTANCE.set(itemAdapter, list);
                fastAdapter.notifyAdapterDataSetChanged();
                binding.searchView.setQuery("", false);
                binding.refreshLayout.setRefreshing(false);
            });
        }).start();
    }

    private ArrayList<BatchItemX> createItemsList(ArrayList<AppInfo> filteredList, boolean resume) {
        ArrayList<BatchItemX> list = new ArrayList<>();
        if (!filteredList.isEmpty()) {
            for (AppInfo app : filteredList) {
                if (resume && checkedList.contains(app.getPackageName())) app.setChecked(true);
                if ((backupBoolean && app.isInstalled())
                        || (!backupBoolean && app.getBackupMode() != AppInfo.MODE_UNSET))
                    list.add(new BatchItemX(app));
            }
        }
        return list;
    }

    private void fillItemsList(ArrayList<BatchItemX> list) {
        for (AppInfo app : originalList) {
            if ((backupBoolean && app.isInstalled())
                    || (!backupBoolean && app.getBackupMode() != AppInfo.MODE_UNSET))
                list.add(new BatchItemX(app));
        }
        SortFilterManager.saveFilterPreferences(this, new SortFilterModel());
    }
}