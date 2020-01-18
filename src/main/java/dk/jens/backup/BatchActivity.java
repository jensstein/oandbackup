package dk.jens.backup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import dk.jens.backup.adapters.BatchAdapter;
import dk.jens.backup.ui.HandleMessages;
import dk.jens.backup.ui.NotificationHelper;
import dk.jens.backup.ui.dialogs.BatchConfirmDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BatchActivity extends BaseActivity
implements OnClickListener, BatchConfirmDialog.ConfirmListener
{
    ArrayList<AppInfo> appInfoList = OAndBackup.appInfoList;
    final static String TAG = OAndBackup.TAG;
    boolean backupBoolean;
    final static int SHOW_DIALOG = 0;
    final static int CHANGE_DIALOG = 1;
    final static int DISMISS_DIALOG = 2;

    final static int RESULT_OK = 0;

    boolean checkboxSelectAllBoolean = false;
    boolean changesMade;

    File backupDir;
    PowerManager powerManager;
    SharedPreferences prefs;

    BatchAdapter adapter;
    ArrayList<AppInfo> list;

    RadioButton rbData, rbApk, rbBoth;

    HandleMessages handleMessages;
    ShellCommands shellCommands;
    Sorter sorter;

    long threadId = -1;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backuprestorelayout);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        handleMessages = new HandleMessages(this);

        if(savedInstanceState != null)
        {
            threadId = savedInstanceState.getLong(Constants.BUNDLE_THREADID);
            Utils.reShowMessage(handleMessages, threadId);
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backupDirPath = prefs.getString(
            Constants.PREFS_PATH_BACKUP_DIRECTORY,
            FileCreationHelper.getDefaultBackupDirPath());
        backupDir = Utils.createBackupDir(BatchActivity.this, backupDirPath);

        int filteringMethodId = 0;
        int sortingMethodId = 0;
        Bundle extra = getIntent().getExtras();
        if(extra != null)
        {
            backupBoolean = extra.getBoolean("dk.jens.backup.backupBoolean");
            filteringMethodId = extra.getInt("dk.jens.backup.filteringMethodId");
            sortingMethodId = extra.getInt("dk.jens.backup.sortingMethodId");
        }
        ArrayList<String> users = getIntent().getStringArrayListExtra("dk.jens.backup.users");
        shellCommands = new ShellCommands(prefs, users, getFilesDir());

        Button bt = (Button) findViewById(R.id.backupRestoreButton);
        bt.setOnClickListener(this);
        rbApk = (RadioButton) findViewById(R.id.radioApk);
        rbData = (RadioButton) findViewById(R.id.radioData);
        rbBoth = (RadioButton) findViewById(R.id.radioBoth);
        rbBoth.setChecked(true);

        if(appInfoList == null)
            appInfoList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                prefs.getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
        if(backupBoolean)
        {
            list = new ArrayList<AppInfo>();
            for(AppInfo appInfo : appInfoList)
                if(appInfo.isInstalled())
                    list.add(appInfo);

            bt.setText(R.string.backup);
        }
        else
        {
            list = new ArrayList<AppInfo>(appInfoList);
            bt.setText(R.string.restore);
        }

        ListView listView = (ListView) findViewById(R.id.listview);
        adapter = new BatchAdapter(this, R.layout.batchlistlayout, list);
        sorter = new Sorter(adapter, prefs);
        sorter.sort(filteringMethodId);
        sorter.sort(sortingMethodId);
        listView.setAdapter(adapter);
        // onItemClickListener gør at hele viewet kan klikkes - med onCheckedListener er det kun checkboxen der kan klikkes
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
            {
                AppInfo appInfo = adapter.getItem(pos);
                appInfo.setChecked(!appInfo.isChecked());
                adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void finish()
    {
        setResult(RESULT_OK, constructResultIntent());
        super.finish();
    }
    @Override
    public void onDestroy()
    {
        if(handleMessages != null)
        {
            handleMessages.endMessage();
        }
        super.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.BUNDLE_THREADID, threadId);
    }
    @Override
    public void onClick(View v)
    {
        ArrayList<AppInfo> selectedList = new ArrayList<AppInfo>();
        for(AppInfo appInfo : list)
        {
            if(appInfo.isChecked())
            {
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.batchmenu, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        int filteringId = Sorter.convertFilteringId(prefs.getInt("filteringId", 0));
        MenuItem filterItem = menu.findItem(filteringId);
        if(filterItem != null)
        {
            filterItem.setChecked(true);
        }
        int sortingId = Sorter.convertSortingId(prefs.getInt("sortingId", 1));
        MenuItem sortItem = menu.findItem(sortingId);
        if(sortItem != null)
        {
            sortItem.setChecked(true);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                setResult(RESULT_OK, constructResultIntent());
                /**
                    * since finish() is not called when navigating up via
                    * the actionbar it needs to be set here.
                    * break instead of return true to let it continue to
                    * the call to baseactivity where navigation is handled.
                */
                break;
            case R.id.de_selectAll:
                if(checkboxSelectAllBoolean)
                {
                    for(AppInfo appInfo : appInfoList)
                    {
                        appInfo.setChecked(false);
                    }
                }
                else
                {
                    // only check the shown items
                    for(int i = 0; i < adapter.getCount(); i++)
                    {
                        adapter.getItem(i).setChecked(true);
                    }
                }
                checkboxSelectAllBoolean = checkboxSelectAllBoolean ? false : true;
                adapter.notifyDataSetChanged();
                return true;
            default:
                item.setChecked(!item.isChecked());
                sorter.sort(item.getItemId());
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public Intent constructResultIntent()
    {
        Intent result = new Intent();
        result.putExtra("changesMade", changesMade);
        result.putExtra("filteringMethodId", sorter.getFilteringMethod().getId());
        result.putExtra("sortingMethodId", sorter.getSortingMethod().getId());
        return result;
    }
    @Override
    public void onConfirmed(ArrayList<AppInfo> selectedList)
    {
        final ArrayList<AppInfo> list = new ArrayList<AppInfo>(selectedList);
        Thread thread = new Thread(new Runnable()
        {
            public void run()
            {
                doAction(list);
            }
        });
        thread.start();
        threadId = thread.getId();
    }
    public void doAction(ArrayList<AppInfo> selectedList)
    {
        if(backupDir != null)
        {
            Crypto crypto = null;
            if(backupBoolean && prefs.getBoolean(
                    Constants.PREFS_ENABLECRYPTO, false) &&
                    Crypto.isAvailable(this))
                crypto = getCrypto();
            PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            if(prefs.getBoolean("acquireWakelock", true))
            {
                wl.acquire();
                Log.i(TAG, "wakelock acquired");
            }
            changesMade = true;
            int id = (int) System.currentTimeMillis();
            int total = selectedList.size();
            int i = 1;
            boolean errorFlag = false;
            for(AppInfo appInfo: selectedList)
            {
                // crypto may be needed for restoring even if the preference is set to false
                if(!backupBoolean && appInfo.getLogInfo() != null && appInfo.getLogInfo().isEncrypted() && Crypto.isAvailable(this))
                    crypto = getCrypto();
                if(appInfo.isChecked())
                {
                    String message = "(" + Integer.toString(i) + "/" + Integer.toString(total) + ")";
                    String title = backupBoolean ? getString(R.string.backupProgress) : getString(R.string.restoreProgress);
                    title = title + " (" + i + "/" + total + ")";
                    NotificationHelper.showNotification(BatchActivity.this, BatchActivity.class, id, title, appInfo.getLabel(), false);
                    handleMessages.setMessage(appInfo.getLabel(), message);
                    int mode = AppInfo.MODE_BOTH;
                    if(rbApk.isChecked())
                        mode = AppInfo.MODE_APK;
                    else if(rbData.isChecked())
                        mode = AppInfo.MODE_DATA;
                    final BackupRestoreHelper backupRestoreHelper = new BackupRestoreHelper();
                    if(backupBoolean)
                    {
                        if(backupRestoreHelper.backup(this, backupDir, appInfo, shellCommands, mode) != 0)
                            errorFlag = true;
                        else if(crypto != null)
                        {
                            crypto.encryptFromAppInfo(this, backupDir, appInfo, mode, prefs);
                            if(crypto.isErrorSet())
                            {
                                Crypto.cleanUpEncryptedFiles(new File(backupDir, appInfo.getPackageName()), appInfo.getSourceDir(), appInfo.getDataDir(), mode, prefs.getBoolean("backupExternalFiles", false));
                                errorFlag = true;
                            }
                        }
                    }
                    else
                    {
                        if(backupRestoreHelper.restore(this, backupDir, appInfo, shellCommands, mode, crypto) != 0)
                            errorFlag = true;
                    }
                    if(i == total)
                    {
                        String msg = backupBoolean ? getString(R.string.batchbackup) : getString(R.string.batchrestore);
                        String notificationTitle = errorFlag ? getString(R.string.batchFailure) : getString(R.string.batchSuccess);
                        NotificationHelper.showNotification(BatchActivity.this, BatchActivity.class, id, notificationTitle, msg, true);
                        handleMessages.endMessage();
                    }
                    i++;
                }
            }
            if(wl.isHeld())
            {
                wl.release();
                Log.i(TAG, "wakelock released");
            }
            if(errorFlag)
            {
                Utils.showErrors(BatchActivity.this);
            }
        }
    }
}
