package dk.jens.backup;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BatchActivity extends BaseActivity
implements OnClickListener
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
    ProgressDialog progress;
    PackageManager pm;
    PowerManager powerManager;
    SharedPreferences prefs;

    BatchAdapter adapter;
    ArrayList<AppInfo> list;

    RadioButton rbData, rbApk, rbBoth;

    HandleMessages handleMessages;
    ShellCommands shellCommands;
    Sorter sorter;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backuprestorelayout);

        pm = getPackageManager();
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        handleMessages = new HandleMessages(this);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backupDirPath = prefs.getString("pathBackupFolder", FileCreationHelper.getDefaultBackupDirPath());
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
        shellCommands = new ShellCommands(prefs, users);

        Button bt = (Button) findViewById(R.id.backupRestoreButton);
        bt.setOnClickListener(this);
        rbApk = (RadioButton) findViewById(R.id.radioApk);
        rbData = (RadioButton) findViewById(R.id.radioData);
        rbBoth = (RadioButton) findViewById(R.id.radioBoth);
        rbBoth.setChecked(true);

        if(backupBoolean)
        {
            list = new ArrayList<AppInfo>();
            Iterator iter = appInfoList.iterator();
            while(iter.hasNext())
            {
                AppInfo appInfo = (AppInfo) iter.next();
                if(appInfo.isInstalled())
                {
                    list.add(appInfo);
                }
            }

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
        showConfirmDialog(BatchActivity.this, selectedList);
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
    public void showConfirmDialog(Context context, final ArrayList<AppInfo> selectedList)
    {
        String title = backupBoolean ? getString(R.string.backupConfirmation) : getString(R.string.restoreConfirmation);
        String message = "";
        for(AppInfo appInfo : selectedList)
        {
            message = message + appInfo.getLabel() + "\n";
        }
        new AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message.trim())
        .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        doAction(selectedList);
                    }
                }).start();
            }
        })
        .setNegativeButton(R.string.dialogNo, null)
        .show();
    }
    public void doAction(ArrayList<AppInfo> selectedList)
    {
        if(backupDir != null)
        {
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
                if(appInfo.isChecked())
                {
                    String message = "(" + Integer.toString(i) + "/" + Integer.toString(total) + ")";
                    File backupSubDir = new File(backupDir, appInfo.getPackageName());
                    String title = backupBoolean ? getString(R.string.backupProgress) : getString(R.string.restoreProgress);
                    title = title + " (" + i + "/" + total + ")";
                    NotificationHelper.showNotification(BatchActivity.this, BatchActivity.class, id, title, appInfo.getLabel(), false);
                    handleMessages.setMessage(appInfo.getLabel(), message);
                    if(backupBoolean)
                    {
                        if(backup(backupSubDir, appInfo) != 0)
                            errorFlag = true;
                    }
                    else
                    {
                        if(restore(backupSubDir, appInfo) != 0)
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
                Utils.showErrors(BatchActivity.this, shellCommands);
            }
        }
    }
    public int backup(File backupSubDir, AppInfo appInfo)
    {
        if(!backupSubDir.exists())
        {
            backupSubDir.mkdirs();
        }
        else
        {
            shellCommands.deleteOldApk(backupSubDir, appInfo.getSourceDir());
        }
        int backupMode = AppInfo.MODE_BOTH;
        if(rbApk.isChecked())
        {
            backupMode = AppInfo.MODE_APK;
        }
        else if(rbData.isChecked())
        {
            backupMode = AppInfo.MODE_DATA;
        }
        int backupRet = 0;
        if(appInfo.isSpecial())
        {
            backupMode = AppInfo.MODE_DATA;
            backupRet = shellCommands.backupSpecial(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getFilesList());
        }
        else
        {
            backupRet = shellCommands.doBackup(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getSourceDir(), backupMode, this.getApplicationInfo().dataDir);
        }
        shellCommands.logReturnMessage(this, backupRet);

        appInfo.setBackupMode(backupMode);
        LogFile.writeLogFile(backupSubDir, appInfo);

        return backupRet;
    }
    public int restore(File backupSubDir, AppInfo appInfo)
    {
        String apk = new LogFile(backupSubDir, appInfo.getPackageName()).getApk();
        String dataDir = appInfo.getDataDir();

        if(rbApk.isChecked() && apk != null && !appInfo.isSpecial())
        {
            int apkRet = shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk, appInfo.isSystem(), this.getApplicationInfo().dataDir);
            return apkRet;
        }
        else if(rbData.isChecked())
        {
            if(appInfo.isInstalled())
            {
                int restoreRet = 0;
                int permRet = 0;
                if(appInfo.isSpecial())
                {
                    restoreRet = shellCommands.restoreSpecial(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getFilesList());
                    permRet = shellCommands.setPermissionsSpecial(appInfo.getDataDir(), appInfo.getFilesList());
                }
                else
                {
                    restoreRet = shellCommands.doRestore(this, backupSubDir, appInfo.getLabel(), appInfo.getPackageName(), appInfo.getLogInfo().getDataDir());
                    shellCommands.logReturnMessage(this, restoreRet);
                    permRet = shellCommands.setPermissions(dataDir);
                }
                return restoreRet + permRet;
            }
            else
            {
                Log.e(TAG, getString(R.string.restoreDataWithoutApkError) + appInfo.getPackageName());
            }
        }
        else if(rbBoth.isChecked() && apk != null && !appInfo.isSpecial())
        {
            int apkRet = shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk, appInfo.isSystem(), this.getApplicationInfo().dataDir);
            if(apkRet == 0)
            {
                int restoreRet = shellCommands.doRestore(this, backupSubDir, appInfo.getLabel(), appInfo.getPackageName(), appInfo.getLogInfo().getDataDir());
                shellCommands.logReturnMessage(this, restoreRet);
                int permRet = shellCommands.setPermissions(dataDir);
                if(restoreRet != 0 || permRet != 0)
                {
                    return 1;
                }
            }
            return apkRet;
        }
        return 1;
    }
}
