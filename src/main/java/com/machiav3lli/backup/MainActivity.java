package com.machiav3lli.backup;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.annimon.stream.IntStream;
import com.annimon.stream.Optional;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.machiav3lli.backup.adapters.MainAdapter;
import com.machiav3lli.backup.schedules.SchedulerActivity;
import com.machiav3lli.backup.tasks.BackupTask;
import com.machiav3lli.backup.tasks.RestoreTask;
import com.machiav3lli.backup.ui.HandleMessages;
import com.machiav3lli.backup.ui.Help;
import com.machiav3lli.backup.ui.LanguageHelper;
import com.machiav3lli.backup.ui.NotificationHelper;
import com.machiav3lli.backup.ui.dialogs.BackupRestoreDialogFragment;
import com.machiav3lli.backup.ui.dialogs.ShareDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.machiav3lli.backup.Constants.classAddress;

public class MainActivity extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener, ActionListener {
    public static final String TAG = Constants.TAG;
    static final int BATCH_REQUEST = 1;
    static final int TOOLS_REQUEST = 2;
    private static final int REQUEST_PERMISSIONS_CODE = 3;

    File backupDir;
    MenuItem mSearchItem;

    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.btn_batch_backup)
    Button btnBackup;
    @BindView(R.id.btn_batch_restore)
    Button btnRestore;
    @BindView(R.id.btn_schedule)
    Button btnSchedule;
    @BindView(R.id.btn_settings)
    Button btnSettings;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.sort_filter_fab)
    ExtendedFloatingActionButton fabSortFilter;
    @BindView(R.id.toolBar)
    androidx.appcompat.widget.Toolbar toolBar;
    @BindView(R.id.search_view)
    androidx.appcompat.widget.SearchView searchView;

    MainAdapter adapter;
    SortFilterSheet sheetSortFilter;
    /*
     * appInfoList is too large to transfer as an intent extra.
     * making it accessible as a public field instead is recommended
     * by the android documentation:
     * http://developer.android.com/guide/faq/framework.html#3
     */
    public static ArrayList<AppInfo> appInfoList;

    boolean languageChanged; // flag for work-around for fixing language change on older android versions
    int notificationId = (int) System.currentTimeMillis();
    long threadId = -1;

    @RestrictTo(RestrictTo.Scope.TESTS)
    Optional<Thread> uiThread = Optional.empty();

    ShellCommands shellCommands;
    HandleMessages handleMessages;
    MainSorter mainSorter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.logDeviceInfo(this, TAG);
        setContentView(R.layout.activity_main);
        handleMessages = new HandleMessages(this);

        ButterKnife.bind(this);

        checkForPermissions(savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            refresh();
            swipeRefreshLayout.setRefreshing(false);
        });

        setSupportActionBar(toolBar);

        searchView.setQueryHint(getString(R.string.searchHint));
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (MainActivity.this.adapter != null)
                    MainActivity.this.adapter.getFilter().filter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                if (MainActivity.this.adapter != null)
                    MainActivity.this.adapter.getFilter().filter(query);
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
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (IntStream.of(grantResults).allMatch(result -> result ==
                    PackageManager.PERMISSION_GRANTED)) {
                threadId = startUiThread(false, 0, new ArrayList<>());
                if (!canAccessExternalStorage()) {
                    /*
                     * Check if the external storage is accessible after the
                     * permissions have been granted. If this is not the case
                     * then the device might be afflicted by an android bug
                     * where the READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
                     * are only fully in affect after the application process
                     * have been restarted. This should only happen on very few
                     * devices hopefully.
                     * This issue has been discussed in different SO questions:
                     * https://stackoverflow.com/q/32699129
                     * https://stackoverflow.com/q/32471888
                     */
                    restart();
                }
            } else {
                Log.w(TAG, String.format("Permissions were not granted: %s -> %s",
                        Arrays.toString(permissions), Arrays.toString(grantResults)));
                Toast.makeText(this, getString(
                        R.string.permission_not_granted), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, String.format("Unknown permissions request code: %s",
                    requestCode));
        }
    }

    private boolean canAccessExternalStorage() {
        final File externalStorage = Environment.getExternalStorageDirectory();
        return externalStorage != null && externalStorage.canRead() &&
                externalStorage.canWrite();
    }

    private void checkForPermissions(Bundle savedInstanceState) {
        // if onCreate is called due to a configuration change su and busybox shouldn't be checked again
        boolean checked = false;
        int firstVisiblePosition = 0;
        ArrayList<String> users = null;
        if (savedInstanceState != null) {
            threadId = savedInstanceState.getLong(Constants.BUNDLE_THREADID);
            Utils.reShowMessage(handleMessages, threadId);
            checked = savedInstanceState.getBoolean(Constants.BUNDLE_STATECHECKED);
            firstVisiblePosition = savedInstanceState.getInt(
                    Constants.BUNDLE_FIRSTVISIBLEPOSITION);
            users = savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS);
        } else {
            // only copy assets if onCreate is not called due to a configuration change
            final AssetHandlerTask assetHandlerTask =
                    new AssetHandlerTask();
            assetHandlerTask.execute(this);
        }
        final String[] perms = {
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, perms,
                    REQUEST_PERMISSIONS_CODE);
        } else {
            /*
             * only set threadId here if this is not after a configuration change.
             * otherwise it could overwrite the value of a running backup / restore thread.
             * a better fix would probably be to check for the thread name or have the threads
             * in an array.
             */
            threadId = startUiThread(checked, firstVisiblePosition, users);
        }
    }

    private void restart() {
        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, getIntent(), PendingIntent.FLAG_CANCEL_CURRENT);
        new AlertDialog.Builder(this)
                .setTitle(R.string.restart_dialog)
                .setPositiveButton(R.string.dialogOK, (dialog, which) -> {
                    final AlarmManager alarmManager = (AlarmManager) getSystemService(
                            Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        // Schedule an immediate restart
                        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() +
                                500L, pendingIntent);
                        System.exit(0);
                    } else {
                        Log.w(TAG, "Restart could not be scheduled");
                    }
                })
                .setNegativeButton(R.string.dialogNo, (dialog, which) -> {
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // reload handlemessages in case its context has been garbage collected
        handleMessages = new HandleMessages(this);
    }

    // The users parameter should ideally be List instead of ArrayList
    // but the following methods expect ArrayList.
    private long startUiThread(boolean checked, int firstVisiblePosition,
                               ArrayList<String> users) {
        final Thread initThread = new Thread(new InitRunnable(checked,
                firstVisiblePosition, users));
        uiThread = Optional.of(initThread);
        initThread.start();
        return initThread.getId();
    }

    @Override
    public void onDestroy() {
        if (handleMessages != null) {
            handleMessages.endMessage();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Constants.BUNDLE_STATECHECKED, true);
        outState.putLong(Constants.BUNDLE_THREADID, threadId);
        int firstVisiblePosition = 0;
        if (listView != null)
            firstVisiblePosition = listView.getFirstVisiblePosition();
        outState.putInt(Constants.BUNDLE_FIRSTVISIBLEPOSITION, firstVisiblePosition);
        if (shellCommands != null)
            outState.putStringArrayList(Constants.BUNDLE_USERS,
                    shellCommands.getUsers());
    }

    public void callBackup(final AppInfo appInfo, final int backupMode) {
        final BackupTask backupTask = new BackupTask(appInfo,
                handleMessages, this, backupDir, shellCommands, backupMode);
        backupTask.execute();
    }

    public void callRestore(final AppInfo appInfo, final int mode) {
        final RestoreTask restoreTask = new RestoreTask(appInfo,
                handleMessages, this, backupDir, shellCommands, mode);
        restoreTask.execute();
    }

    public void displayDialog(AppInfo appInfo) {
        if (!appInfo.isInstalled() && appInfo.getBackupMode() == AppInfo.MODE_DATA) {
            Toast.makeText(this, getString(R.string.notInstalledModeDataWarning), Toast.LENGTH_LONG).show();
        } else {
            Bundle arguments = new Bundle();
            arguments.putParcelable("appinfo", appInfo);
            BackupRestoreDialogFragment dialog = new BackupRestoreDialogFragment();
            dialog.setListener((ActionListener) this);
            dialog.setArguments(arguments);
            dialog.show(getFragmentManager(), "DialogFragment");
        }
    }

    @Override
    public void onActionCalled(AppInfo appInfo, BackupRestoreHelper.ActionType actionType, int mode) {
        if (actionType == BackupRestoreHelper.ActionType.BACKUP) {
            callBackup(appInfo, mode);
        } else if (actionType == BackupRestoreHelper.ActionType.RESTORE) {
            callRestore(appInfo, mode);
        } else {
            Log.e(Constants.TAG, "unknown actionType: " + actionType);
        }
    }


    public Intent batchIntent(Class batchClass, boolean backup) {
        Intent batchIntent = new Intent(getApplicationContext(), batchClass);
        batchIntent.putExtra(classAddress(".backupBoolean"), backup);
        batchIntent.putStringArrayListExtra(classAddress(".users"), shellCommands.getUsers());
        batchIntent.putExtra(classAddress(".filteringMethodId"), mainSorter.getFilteringMethod().getId());
        batchIntent.putExtra(classAddress(".sortingMethodId"), mainSorter.getSortingMethod().getId());
        return batchIntent;
    }

    public void bindNavigationButtons() {
        btnBackup.setOnClickListener(v -> startActivityForResult(batchIntent(BatchActivity.class, true), BATCH_REQUEST));
        btnRestore.setOnClickListener(v -> startActivityForResult(batchIntent(BatchActivity.class, false), BATCH_REQUEST));
        btnSchedule.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SchedulerActivity.class)));
        // btnSettings.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Preferences.class)));
    }

    public Thread refresh() {
        Thread refreshThread = new Thread(new Runnable() {
            public void run() {
                appInfoList = AppInfoHelper.getPackageInfo(MainActivity.this,
                        backupDir, true, PreferenceManager.getDefaultSharedPreferences(
                                MainActivity.this).getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS,
                                true));
                runOnUiThread(new Runnable() {
                    public void run() {
                        // temporary work-around until the race condition between refresh and oncreate when returning from batchactivity with changesmade have been fixed
                        if (adapter != null && mainSorter != null) {
                            // adapter.setNewOriginalValues(appInfoList);
                            mainSorter.sort(mainSorter.getFilteringMethod().getId());
                            mainSorter.sort(mainSorter.getSortingMethod().getId());
                            adapter.restoreFilter();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
        if (sheetSortFilter != null) sheetSortFilter.setCheckedDefault();

        refreshThread.start();
        threadId = refreshThread.getId();
        // This method return a thread here to facilitate testing
        return refreshThread;
    }

    public void refreshSingle(AppInfo appInfo) {
        if (backupDir != null) {
            LogFile logInfo = new LogFile(new File(backupDir, appInfo.getPackageName()), appInfo.getPackageName());
            int pos = appInfoList.indexOf(appInfo);
            appInfo.setLogInfo(logInfo);
            appInfoList.set(pos, appInfo);
            adapter.notifyDataSetChanged();
        }
    }

    public void setAppInfoAdapter(MainAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LanguageHelper.initLanguage(this, prefs.getString(
                Constants.PREFS_LANGUAGES, Constants.PREFS_LANGUAGES_DEFAULT));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BATCH_REQUEST || requestCode == TOOLS_REQUEST) {
            if (appInfoList != null) {
                for (AppInfo appInfo : appInfoList) {
                    appInfo.setChecked(false);
                }
            }
            if (data != null) {
                boolean changesMade = data.getBooleanExtra("changesMade", false);
                if (changesMade) {
                    refresh();
                }
                if (mainSorter != null) {
                    mainSorter.sort(data.getIntExtra("filteringMethodId", 0));
                    mainSorter.sort(data.getIntExtra("sortingMethodId", 0));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // clear menu so menus from other activities aren't shown also
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tools:
                if (backupDir != null) {
                    Intent toolsIntent = new Intent(this, Tools.class);
                    toolsIntent.putExtra(classAddress(".backupDir"), backupDir);
                    toolsIntent.putStringArrayListExtra(classAddress(".users"), shellCommands.getUsers());
                    startActivityForResult(toolsIntent, TOOLS_REQUEST);
                }
                break;
            case R.id.help:
                startActivity(new Intent(this, Help.class));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu, menu);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        AppInfo appInfo = adapter.items.get(info.position);
        if (appInfo.getLogInfo() == null) {
            menu.removeItem(R.id.deleteBackup);
            menu.removeItem(R.id.share);
        }
        menu.setHeaderTitle(appInfo.getLabel());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.uninstall:
                new AlertDialog.Builder(this)
                        .setTitle(adapter.items.get(info.position).getLabel())
                        .setMessage(R.string.uninstallDialogMessage)
                        .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Thread uninstallThread = new Thread(new Runnable() {
                                    public void run() {
                                        AppInfo appInfo = adapter.items.get(info.position);
                                        Log.i(TAG, "uninstalling " + appInfo.getLabel());
                                        handleMessages.showMessage(appInfo.getLabel(), getString(R.string.uninstallProgress));
                                        int ret = shellCommands.uninstall(appInfo.getPackageName(), appInfo.getSourceDir(), appInfo.getDataDir(), appInfo.isSystem());
                                        refresh();
                                        handleMessages.endMessage();
                                        if (ret == 0) {
                                            NotificationHelper.showNotification(MainActivity.this, MainActivity.class, notificationId++, getString(R.string.uninstallSuccess), appInfo.getLabel(), true);
                                        } else {
                                            NotificationHelper.showNotification(MainActivity.this, MainActivity.class, notificationId++, getString(R.string.uninstallFailure), appInfo.getLabel(), true);
                                            Utils.showErrors(MainActivity.this);
                                        }
                                    }
                                });
                                uninstallThread.start();
                                threadId = uninstallThread.getId();
                            }
                        })
                        .setNegativeButton(R.string.dialogNo, null)
                        .show();
                return true;
            case R.id.deleteBackup:
                new AlertDialog.Builder(this)
                        .setTitle(adapter.items.get(info.position).getLabel())
                        .setMessage(R.string.deleteBackupDialogMessage)
                        .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Thread deleteBackupThread = new Thread(new Runnable() {
                                    public void run() {
                                        handleMessages.showMessage(adapter.items.get(info.position).getLabel(), getString(R.string.deleteBackup));
                                        if (backupDir != null) {
                                            File backupSubDir = new File(backupDir, adapter.items.get(info.position).getPackageName());
                                            shellCommands.deleteBackup(backupSubDir);
                                            refresh(); // behøver ikke refresh af alle pakkerne, men refresh(packageName) kalder readLogFile(), som ikke kan håndtere, hvis logfilen ikke findes
                                        }
                                        handleMessages.endMessage();
                                    }
                                });
                                deleteBackupThread.start();
                                threadId = deleteBackupThread.getId();
                            }
                        })
                        .setNegativeButton(R.string.dialogNo, null)
                        .show();
                return true;
            case R.id.enablePackage:
                displayDialogEnableDisable(adapter.items.get(info.position).getPackageName(), true);
                return true;
            case R.id.disablePackage:
                displayDialogEnableDisable(adapter.items.get(info.position).getPackageName(), false);
                return true;
            case R.id.share:
                AppInfo appInfo = adapter.items.get(info.position);
                File apk = new File(backupDir, appInfo.getPackageName() + "/" + appInfo.getLogInfo().getApk());
                String dataPath = appInfo.getLogInfo().getDataDir();
                dataPath = dataPath.substring(dataPath.lastIndexOf("/") + 1);
                File data = new File(backupDir, appInfo.getPackageName() + "/" + dataPath + ".zip");
                Bundle arguments = new Bundle();
                arguments.putString("label", appInfo.getLabel());
                if (apk.exists()) {
                    arguments.putSerializable("apk", apk);
                }
                if (data.exists()) {
                    arguments.putSerializable("data", data);
                }
                ShareDialogFragment shareDialog = new ShareDialogFragment();
                shareDialog.setArguments(arguments);
                shareDialog.show(getFragmentManager(), "DialogFragment");
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(Constants.PREFS_PATH_BACKUP_DIRECTORY)) {
            String backupDirPath = preferences.getString(key, FileCreationHelper.getDefaultBackupDirPath());
            backupDir = Utils.createBackupDir(MainActivity.this, backupDirPath);
            refresh();
        } else if (key.equals(Constants.PREFS_PATH_BUSYBOX)) {
            shellCommands = new ShellCommands(preferences, getFilesDir());
            checkBusybox();
        } else if (key.equals(Constants.PREFS_TIMESTAMP)) {
            adapter.setLocalTimestampFormat(preferences.getBoolean(key, true));
            adapter.notifyDataSetChanged();
        } else if (key.equals(Constants.PREFS_OLDBACKUPS)) {
            mainSorter = new MainSorter(adapter, preferences);
        } else if (key.equals(Constants.PREFS_LANGUAGES)) {
            languageChanged = true;
        } else if (key.equals(Constants.PREFS_ENABLESPECIALBACKUPS))
            refresh();
        else if (key.equals(Constants.PREFS_ENABLECRYPTO) &&
                preferences.getBoolean(key, false))
            startCrypto();
    }

    public boolean onSearchRequested() {
        if (mSearchItem != null) {
            mSearchItem.expandActionView();
        }
        return true;
    }

    public void displayDialogEnableDisable(final String packageName, final boolean enable) {
        String title = enable ? getString(R.string.enablePackageTitle) : getString(R.string.disablePackageTitle);
        final ArrayList<String> selectedUsers = new ArrayList<String>();
        final ArrayList<String> userList = shellCommands.getUsers();
        CharSequence[] users = userList.toArray(new CharSequence[userList.size()]);
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMultiChoiceItems(users, null, (dialog, chosen, checked) -> {
                    if (checked) {
                        selectedUsers.add(userList.get(chosen));
                    } else if (selectedUsers.contains(userList.get(chosen))) {
                        selectedUsers.remove(userList.get(chosen));
                    }
                })
                .setPositiveButton(R.string.dialogOK, (dialog, which) ->
                        shellCommands.enableDisablePackage(packageName,
                                selectedUsers, enable)
                )
                .setNegativeButton(R.string.dialogCancel, (dialog, which) -> {
                })
                .show();
    }

    public void checkBusybox() {
        if (!shellCommands.checkBusybox())
            Utils.showWarning(this, "", getString(R.string.busyboxProblem));
    }

    private void checkOabUtils() {
        if (!shellCommands.checkOabUtils()) {
            Utils.showWarning(this, "", getString(R.string.oabUtilsProblem));
        }
    }

    private class InitRunnable implements Runnable {
        boolean checked;
        int firstVisiblePosition;
        ArrayList<String> users;

        public InitRunnable(boolean checked, int firstVisiblePosition, ArrayList<String> users) {
            this.checked = checked;
            this.firstVisiblePosition = firstVisiblePosition;
            this.users = users;
        }

        public void run() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            prefs.registerOnSharedPreferenceChangeListener(MainActivity.this);
            shellCommands = new ShellCommands(prefs, users, getFilesDir());
            String langCode = prefs.getString(Constants.PREFS_LANGUAGES,
                    Constants.PREFS_LANGUAGES_DEFAULT);
            LanguageHelper.initLanguage(MainActivity.this, langCode);
            String backupDirPath = prefs.getString(
                    Constants.PREFS_PATH_BACKUP_DIRECTORY,
                    FileCreationHelper.getDefaultBackupDirPath());
            backupDir = Utils.createBackupDir(MainActivity.this, backupDirPath);
            // temporary method to move logfile from bottom of external storage to bottom of backupdir
            new FileCreationHelper().moveLogfile(prefs.getString("pathLogfile", FileCreationHelper.getDefaultLogFilePath()));
            if (!checked) {
                handleMessages.showMessage("", getString(R.string.suCheck));
                boolean haveSu = ShellCommands.checkSuperUser();
                if (!haveSu) {
                    Utils.showWarning(MainActivity.this, "", getString(R.string.noSu));
                }
                checkBusybox();
                handleMessages.changeMessage("", getString(
                        R.string.oabUtilsCheck));
                checkOabUtils();
                handleMessages.endMessage();
            }

            if (appInfoList == null) {
                handleMessages.changeMessage("", getString(R.string.collectingData));
                appInfoList = AppInfoHelper.getPackageInfo(MainActivity.this,
                        backupDir, true, prefs.getBoolean(Constants.
                                PREFS_ENABLESPECIALBACKUPS, true));
                handleMessages.endMessage();
            }

            registerForContextMenu(listView);

            adapter = new MainAdapter(MainActivity.this, R.layout.item_main_list, appInfoList);
            adapter.setLocalTimestampFormat(prefs.getBoolean(
                    Constants.PREFS_TIMESTAMP, true));
            mainSorter = new MainSorter(adapter, prefs);
            if (prefs.getBoolean("rememberFiltering", false)) {
                mainSorter.sort(MainSorter.convertFilteringId(prefs.getInt("filteringId", 0)));
                mainSorter.sort(MainSorter.convertSortingId(prefs.getInt("sortingId", 1)));
            }
            runOnUiThread(() -> {
                listView.setAdapter(adapter);
                listView.setSelection(firstVisiblePosition);
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    AppInfo appInfo = adapter.items.get(position);
                    displayDialog(appInfo);
                });
            });
            bindNavigationButtons();
        }
    }

    private static class AssetHandlerTask extends AsyncTask<Context, Void, Context> {
        private Throwable throwable;

        @Override
        public Context doInBackground(Context... contexts) {
            try {
                AssetsHandler.copyOabutils(contexts[0]);
            } catch (AssetsHandler.AssetsHandlerException e) {
                throwable = e;
            }
            return contexts[0];
        }

        @Override
        public void onPostExecute(Context context) {
            if (throwable != null) {
                Log.e(TAG, String.format(
                        "error during AssetHandlerTask.onPostExecute: %s",
                        throwable.toString()));
                Toast.makeText(context, throwable.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
