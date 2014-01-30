package dk.jens.backup;

import android.app.Activity;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BatchActivity extends Activity implements OnClickListener
{
    public static ArrayList<AppInfo> appInfoList;
    final static String TAG = OAndBackup.TAG;
    boolean backupBoolean;
    final static int SHOW_DIALOG = 0;
    final static int CHANGE_DIALOG = 1;
    final static int DISMISS_DIALOG = 2;

    final static int RESULT_OK = 0;

    boolean checkboxSelectAllBoolean = true;
    boolean changesMade;

    File backupDir;
    ProgressDialog progress;
    PackageManager pm;
    PowerManager powerManager;
    SharedPreferences prefs;

    ListView listView;
    BatchAdapter adapter;
    ArrayList<AppInfo> list;

    RadioButton rbData, rbApk, rbBoth;

    HandleMessages handleMessages;
    ShellCommands shellCommands;
    FileCreationHelper fileCreator;
    LogFile logFile;
    NotificationHelper notificationHelper;
    Sorter sorter;
    
    boolean localTimestampFormat;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backuprestorelayout);

        pm = getPackageManager();
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        handleMessages = new HandleMessages(this);
        fileCreator = new FileCreationHelper(this);
        notificationHelper = new NotificationHelper(this);
        logFile = new LogFile(this);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backupDirPath = prefs.getString("pathBackupFolder", fileCreator.getDefaultBackupDirPath());
        backupDir = Utils.createBackupDir(BatchActivity.this, backupDirPath, fileCreator);
        localTimestampFormat = prefs.getBoolean("timestamp", true);

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
        shellCommands = new ShellCommands(this, users);

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
                if(appInfo.isInstalled)
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

        listView = (ListView) findViewById(R.id.listview);
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
                appInfo.toggle();
                adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void finish()
    {
        Intent result = new Intent();
        result.putExtra("changesMade", changesMade);
        result.putExtra("filteringMethodId", sorter.getFilteringMethod().getId());
        result.putExtra("sortingMethodId", sorter.getSortingMethod().getId());
        setResult(RESULT_OK, result);
        super.finish();
    }
    @Override
    public void onDestroy()
    {
        if(handleMessages != null)
        {
            handleMessages.dismissMessage();
        }
        super.onDestroy();
    }    
    @Override
    public void onClick(View v)
    {
        ArrayList<AppInfo> selectedList = new ArrayList<AppInfo>();
        for(AppInfo appInfo : list)
        {
            if(appInfo.isChecked)
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
            case R.id.de_selectAll:
                for(AppInfo appInfo : appInfoList)
                {
                    if(appInfo.isChecked != checkboxSelectAllBoolean)
                    {
                        appInfo.toggle();
                    }
                }
                checkboxSelectAllBoolean = checkboxSelectAllBoolean ? false : true;
                adapter.notifyDataSetChanged();
                break;
            default:
                item.setChecked(!item.isChecked());
                sorter.sort(item.getItemId());
                break;                
        }
        return super.onOptionsItemSelected(item);
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
            int id = (int) Calendar.getInstance().getTimeInMillis();
            int total = selectedList.size();
            int i = 0;
            boolean errorFlag = false;
            for(AppInfo appInfo: selectedList)
            {
                if(appInfo.isChecked)
                {
                    i++;
                    String message = "(" + Integer.toString(i) + "/" + Integer.toString(total) + ")";
                    File backupSubDir = new File(backupDir, appInfo.getPackageName());
                    String title = backupBoolean ? getString(R.string.backupProgress) : getString(R.string.restoreProgress);
                    title = title + " (" + i + "/" + total + ")";
                    notificationHelper.showNotification(BatchActivity.class, id, title, appInfo.getLabel(), false);
                    if(i == 1)
                    {
                        handleMessages.showMessage(appInfo.getLabel(), message);
                    }
                    else
                    {
                        handleMessages.changeMessage(appInfo.getLabel(), message);
                    }
                    if(backupBoolean)
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
                        int backupRet = shellCommands.doBackup(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getSourceDir(), backupMode);
                        shellCommands.logReturnMessage(backupRet);

                        logFile.writeLogFile(backupSubDir, appInfo.getPackageName(), appInfo.getLabel(), appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getSourceDir(), appInfo.getDataDir(), null, appInfo.isSystem, appInfo.setNewBackupMode(backupMode));
                        if(backupRet != 0)
                        {
                            errorFlag = true;
                        }
                    }
                    else
                    {
                        String apk = new LogFile(backupSubDir, appInfo.getPackageName(), localTimestampFormat).getApk();
                        String dataDir = appInfo.getDataDir();

                        if(rbApk.isChecked() && apk != null)
                        {
                            int apkRet = shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk, appInfo.isSystem);
                            if(apkRet != 0)
                            {
                                errorFlag = true;
                            }
                        }
                        else if(rbData.isChecked())
                        {
                            if(appInfo.isInstalled)
                            {
                                int restoreRet = shellCommands.doRestore(backupSubDir, appInfo.getLabel(), appInfo.getPackageName());
                                shellCommands.logReturnMessage(restoreRet);
                                int permRet = shellCommands.setPermissions(dataDir);
                                if(restoreRet != 0 || permRet != 0)
                                {
                                    errorFlag = true;
                                }
                            }
                            else
                            {
                                Log.i(TAG, getString(R.string.restoreDataWithoutApkError) + appInfo.getPackageName());
                            }
                        }
                        else if(rbBoth.isChecked() && apk != null)
                        {
                            int apkRet = shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk, appInfo.isSystem);
                            int restoreRet = shellCommands.doRestore(backupSubDir, appInfo.getLabel(), appInfo.getPackageName());
                            shellCommands.logReturnMessage(restoreRet);
                            int permRet = shellCommands.setPermissions(dataDir);
                            if(apkRet != 0 || restoreRet != 0 || permRet != 0)
                            {
                                errorFlag = true;
                            }
                        }
                    }
                    if(i == total)
                    {
                        String msg = backupBoolean ? getString(R.string.batchbackup) : getString(R.string.batchrestore);
                        String notificationTitle = errorFlag ? getString(R.string.batchFailure) : getString(R.string.batchSuccess);
                        notificationHelper.showNotification(BatchActivity.class, id, notificationTitle, msg, true);
                        handleMessages.endMessage();
                    }
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
}
