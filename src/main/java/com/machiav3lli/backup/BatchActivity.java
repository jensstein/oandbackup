package com.machiav3lli.backup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.machiav3lli.backup.adapters.BatchAdapter;
import com.machiav3lli.backup.ui.HandleMessages;
import com.machiav3lli.backup.ui.NotificationHelper;
import com.machiav3lli.backup.ui.dialogs.BatchConfirmDialog;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BatchActivity extends BaseActivity
        implements OnClickListener, BatchConfirmDialog.ConfirmListener {
    ArrayList<AppInfo> appInfoList = MainActivity.appInfoList;
    final static String TAG = MainActivity.TAG;
    boolean backupBoolean;

    final static int RESULT_OK = 0;

    boolean checkboxSelectAllBoolean = false;
    boolean changesMade;

    File backupDir;
    PowerManager powerManager;
    SharedPreferences prefs;

    BatchAdapter adapter;
    SortFilterSheet sheetSortFilter;
    ArrayList<AppInfo> list;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.radioApk)
    AppCompatRadioButton rbApk;
    @BindView(R.id.radioData)
    AppCompatRadioButton rbData;
    @BindView(R.id.radioBoth)
    AppCompatRadioButton rbBoth;
    @BindView(R.id.backupRestoreButton)
    Button actionButton;
    @BindView(R.id.cbAll)
    AppCompatCheckBox cbAll;
    @BindView(R.id.toolBar)
    androidx.appcompat.widget.Toolbar toolBar;
    @BindView(R.id.search_view)
    androidx.appcompat.widget.SearchView searchView;

    HandleMessages handleMessages;
    ShellCommands shellCommands;
    BatchSorter batchSorter;

    long threadId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        handleMessages = new HandleMessages(this);

        if (savedInstanceState != null) {
            threadId = savedInstanceState.getLong(Constants.BUNDLE_THREADID);
            Utils.reShowMessage(handleMessages, threadId);
        }

        ButterKnife.bind(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backupDirPath = prefs.getString(
                Constants.PREFS_PATH_BACKUP_DIRECTORY,
                FileCreationHelper.getDefaultBackupDirPath());
        assert backupDirPath != null;
        backupDir = Utils.createBackupDir(BatchActivity.this, backupDirPath);

        int filteringMethodId = 0;
        int sortingMethodId = 0;
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            backupBoolean = extra.getBoolean("com.machiav3lli.backup.backupBoolean");
            filteringMethodId = extra.getInt("com.machiav3lli.backup.filteringMethodId");
            sortingMethodId = extra.getInt("com.machiav3lli.backup.sortingMethodId");
        }
        ArrayList<String> users = getIntent().getStringArrayListExtra("com.machiav3lli.backup.users");
        shellCommands = new ShellCommands(prefs, users, getFilesDir());


        actionButton.setOnClickListener(this);
        cbAll.setOnClickListener(v -> {
            if (checkboxSelectAllBoolean) {
                for (AppInfo appInfo : appInfoList) {
                    appInfo.setChecked(false);
                }
            } else {
                // only check the shown items
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    adapter.items.get(i).setChecked(true);
                }
            }
            checkboxSelectAllBoolean = !checkboxSelectAllBoolean;
            adapter.notifyDataSetChanged();
        });
        rbBoth.setChecked(true);

        if (appInfoList == null)
            appInfoList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                    prefs.getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
        if (backupBoolean) {
            list = new ArrayList<>();
            for (AppInfo appInfo : appInfoList)
                if (appInfo.isInstalled())
                    list.add(appInfo);

            actionButton.setText(R.string.backup);
        } else {
            list = new ArrayList<>(appInfoList);
            actionButton.setText(R.string.restore);
        }


        adapter = new BatchAdapter(this, R.layout.item_batch_list, list);
        batchSorter = new BatchSorter(adapter, prefs);
        batchSorter.sort(filteringMethodId);
        batchSorter.sort(sortingMethodId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        toolBar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        searchView.setQueryHint(getString(R.string.searchHint));
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null)
                    adapter.getFilter().filter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter != null)
                    adapter.getFilter().filter(query);
                return true;
            }
        });

    }

    @OnClick(R.id.sort_filter_fab)
    public void showFilterDialog() {
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        if (sheetSortFilter != null)
            sheetSortFilter = new SortFilterSheet(adapter, prefs, sheetSortFilter.getCheckedSort(), sheetSortFilter.getCheckedFilter());
        else sheetSortFilter = new SortFilterSheet(adapter, prefs);
        sheetSortFilter.show(getSupportFragmentManager(), "FILTER");
    }

    @Override
    public void finish() {
        setResult(RESULT_OK, constructResultIntent());
        super.finish();
    }

    @Override
    public void onDestroy() {
        if (handleMessages != null) {
            handleMessages.endMessage();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.BUNDLE_THREADID, threadId);
    }

    @Override
    public void onClick(View v) {
        ArrayList<AppInfo> selectedList = new ArrayList<>();
        for (AppInfo appInfo : list) {
            if (appInfo.isChecked()) {
                selectedList.add(appInfo);
            }
        }
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList("selectedList", selectedList);
        arguments.putBoolean("backupBoolean", backupBoolean);
        BatchConfirmDialog dialog = new BatchConfirmDialog();
        dialog.setArguments(arguments);
        dialog.show(getFragmentManager(), "DialogFragment");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        int filteringId = BatchSorter.convertFilteringId(prefs.getInt("filteringId", 0));
        MenuItem filterItem = menu.findItem(filteringId);
        if (filterItem != null) {
            filterItem.setChecked(true);
        }
        int sortingId = BatchSorter.convertSortingId(prefs.getInt("sortingId", 1));
        MenuItem sortItem = menu.findItem(sortingId);
        if (sortItem != null) {
            sortItem.setChecked(true);
        }
        return true;
    }

    // steps: implement the buttons and filters
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_OK, constructResultIntent());
            /*
             * since finish() is not called when navigating up via
             * the actionbar it needs to be set here.
             * break instead of return true to let it continue to
             * the call to baseactivity where navigation is handled.
             */
        } else {
            item.setChecked(!item.isChecked());
            batchSorter.sort(item.getItemId());
        }
        return super.onOptionsItemSelected(item);
    }

    public Intent constructResultIntent() {
        Intent result = new Intent();
        result.putExtra("changesMade", changesMade);
        result.putExtra("filteringMethodId", batchSorter.getFilteringMethod().getId());
        result.putExtra("sortingMethodId", batchSorter.getSortingMethod().getId());
        return result;
    }

    @Override
    public void onConfirmed(ArrayList<AppInfo> selectedList) {
        final ArrayList<AppInfo> list = new ArrayList<>(selectedList);
        Thread thread = new Thread(() -> doAction(list));
        thread.start();
        threadId = thread.getId();
    }

    public void doAction(ArrayList<AppInfo> selectedList) {
        if (backupDir != null) {
            Crypto crypto = null;
            if (backupBoolean && prefs.getBoolean(
                    Constants.PREFS_ENABLECRYPTO, false) &&
                    Crypto.isAvailable(this))
                crypto = getCrypto();
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
            for (AppInfo appInfo : selectedList) {
                // crypto may be needed for restoring even if the preference is set to false
                if (!backupBoolean && appInfo.getLogInfo() != null && appInfo.getLogInfo().isEncrypted() && Crypto.isAvailable(this))
                    crypto = getCrypto();
                if (appInfo.isChecked()) {
                    String message = "(" + i + "/" + total + ")";
                    String title = backupBoolean ? getString(R.string.backupProgress) : getString(R.string.restoreProgress);
                    title = title + " (" + i + "/" + total + ")";
                    NotificationHelper.showNotification(BatchActivity.this, BatchActivity.class, id, title, appInfo.getLabel(), false);
                    handleMessages.setMessage(appInfo.getLabel(), message);
                    int mode = AppInfo.MODE_BOTH;
                    if (rbApk.isChecked())
                        mode = AppInfo.MODE_APK;
                    else if (rbData.isChecked())
                        mode = AppInfo.MODE_DATA;
                    final BackupRestoreHelper backupRestoreHelper = new BackupRestoreHelper();
                    if (backupBoolean) {
                        if (backupRestoreHelper.backup(this, backupDir, appInfo, shellCommands, mode) != 0)
                            errorFlag = true;
                        else if (crypto != null) {
                            crypto.encryptFromAppInfo(this, backupDir, appInfo, mode, prefs);
                            if (crypto.isErrorSet()) {
                                Crypto.cleanUpEncryptedFiles(new File(backupDir, appInfo.getPackageName()), appInfo.getSourceDir(), appInfo.getDataDir(), mode, prefs.getBoolean("backupExternalFiles", false));
                                errorFlag = true;
                            }
                        }
                    } else {
                        if (backupRestoreHelper.restore(this, backupDir, appInfo, shellCommands, mode, crypto) != 0)
                            errorFlag = true;
                    }
                    if (i == total) {
                        String msg = backupBoolean ? getString(R.string.batchbackup) : getString(R.string.batchrestore);
                        String notificationTitle = errorFlag ? getString(R.string.batchFailure) : getString(R.string.batchSuccess);
                        NotificationHelper.showNotification(BatchActivity.this, BatchActivity.class, id, notificationTitle, msg, true);
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
                Utils.showErrors(BatchActivity.this);
            }
        }
    }
}
