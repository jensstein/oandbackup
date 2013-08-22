package dk.jens.backup;

import android.app.Activity;
import android.app.AlertDialog;
//import android.app.FragmentManager;
//import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class OAndBackup extends FragmentActivity // FragmentActivity i stedet for Activity for at kunne bruge ting fra support-bibliotekerne
implements SharedPreferences.OnSharedPreferenceChangeListener
{
    static final String TAG = OAndBackup.class.getSimpleName().toLowerCase();
    static final int BATCH_REQUEST = 1;

    PackageManager pm;
    File backupDir;
    List<PackageInfo> pinfoList;
    MenuItem mSearchItem;
    SharedPreferences prefs;

    AppInfoAdapter adapter;
    ArrayList<AppInfo> appInfoList;

    boolean showAll = true;
    boolean showOnlyUser = false;
    int notificationNumber = 0;
    int notificationId = (int) Calendar.getInstance().getTimeInMillis();

    ShellCommands shellCommands;
    HandleMessages handleMessages;
    FileCreationHelper fileCreator;
    LogFile logFile;
    NotificationHelper notificationHelper;

    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handleMessages = new HandleMessages(this);
        shellCommands = new ShellCommands(this);
        fileCreator = new FileCreationHelper(this);
        logFile = new LogFile(this);
        notificationHelper = new NotificationHelper(this);
        
        new Thread(new Runnable(){
            public void run()
            {
                handleMessages.showMessage("", getString(R.string.suCheck));
                boolean haveSu = shellCommands.checkSuperUser();
                if(!haveSu)
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(OAndBackup.this, getString(R.string.noSu), Toast.LENGTH_LONG).show();
                        }
                    });                    
                }
                boolean rsyncInstalled = shellCommands.checkRsync();
                boolean bboxInstalled = shellCommands.checkBusybox();
                if(!rsyncInstalled || !bboxInstalled)
                {
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(OAndBackup.this, getString(R.string.rsyncOrBusyboxProblem), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                handleMessages.changeMessage("", getString(R.string.collectingData));
                pm = getPackageManager();
                prefs = PreferenceManager.getDefaultSharedPreferences(OAndBackup.this);
                prefs.registerOnSharedPreferenceChangeListener(OAndBackup.this);
                String backupDirPath = prefs.getString("pathBackupFolder", fileCreator.getDefaultBackupDirPath());
                createBackupDir(backupDirPath);
                
                appInfoList = new ArrayList<AppInfo>();
                getPackageInfo();
                handleMessages.endMessage();
                listView = (ListView) findViewById(R.id.listview);
                registerForContextMenu(listView);

                adapter = new AppInfoAdapter(OAndBackup.this, R.layout.listlayout, appInfoList);
                runOnUiThread(new Runnable(){
                    public void run()
                    {
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                        {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
                            {
                                AppInfo appInfo = appInfoList.get(pos);
                                displayDialog(appInfo);
                            }
                        });
                    }
                });
            }
        }).start();
    }
    public void displayDialog(AppInfo appInfo)
    {
        BackupRestoreDialogFragment dialog = new BackupRestoreDialogFragment(this, appInfo);
//        dialog.show(getFragmentManager(), "DialogFragment");
        dialog.show(getSupportFragmentManager(), "DialogFragment");
    }
    public void callBackup(final AppInfo appInfo)
    {
        final String log = appInfo.getLabel() + "\n" + appInfo.getVersionName() + "\n" + appInfo.getPackageName() + "\n" + appInfo.getSourceDir() + "\n" + appInfo.getDataDir();
        new Thread(new Runnable()
        {
            public void run()
            {
                handleMessages.showMessage(appInfo.getLabel(), "backup");
                File backupSubDir = new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName());
                if(!backupSubDir.exists())
                {
                    backupSubDir.mkdirs();
                }
                else
                {
                    shellCommands.deleteOldApk(backupSubDir, appInfo.getSourceDir());
//                    shellCommands.deleteBackup(backupSubDir);
//                    backupSubDir.mkdirs();
                }
                shellCommands.doBackup(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getSourceDir());
//                shellCommands.writeLogFile(backupSubDir.getAbsolutePath() + "/" + appInfo.getPackageName() + ".log", log);
                logFile.writeLogFile(backupSubDir.getAbsolutePath() + "/" + appInfo.getPackageName() + ".log", log);
                
                // køre på uitråd for at undgå WindowLeaked
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        refresh(appInfo);
                    }
                });
                handleMessages.endMessage();
                notificationHelper.showNotification(OAndBackup.class, notificationId++, "backup complete", appInfo.getLabel(), true);
            }
        }).start();
    }
    public void callRestore(final AppInfo appInfo, final int options)
    {
        new Thread(new Runnable()
        {
            public void run()
            {          
                File backupSubDir = new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName());
                // error handling, hvis backupSubDir ikke findes
                handleMessages.showMessage(appInfo.getLabel(), "restore");

//                ArrayList<String> log = shellCommands.readLogFile(backupSubDir, appInfo.getPackageName());
//                ArrayList<String> log = logFile.readLogFile(backupSubDir, appInfo.getPackageName());
                LogFile logInfo = new LogFile(backupSubDir, appInfo.getPackageName());
                String dataDir = appInfo.getDataDir();
//                String apk = log.get(3);
                String apk = logInfo.getApk();
                String[] apkArray = apk.split("/");
                apk = apkArray[apkArray.length - 1];
                switch(options)
                {
                    case 1:
                        shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk);
                        break;
                    case 2:
                        if(appInfo.isInstalled)
                        {
                            shellCommands.doRestore(backupSubDir, appInfo.getLabel(), appInfo.getPackageName());
                            shellCommands.setPermissions(dataDir);
                        }
                        else
                        {
                            Log.i(TAG, getString(R.string.restoreDataWithoutApkError) + appInfo.getPackageName());
                        }
                        break;
                    case 3:
                        shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk);
                        shellCommands.doRestore(backupSubDir, appInfo.getLabel(), appInfo.getPackageName());
                        shellCommands.setPermissions(dataDir);
                        break;
                }
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        refresh();
                    }
                });
                handleMessages.endMessage();
                notificationHelper.showNotification(OAndBackup.class, notificationId++, "restore complete", appInfo.getLabel(), true);
            }
        }).start();
    }
    public void getPackageInfo()
    {
        pinfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        BatchActivity.pinfoList = pinfoList;

        for(PackageInfo pinfo : pinfoList)
        {
            String lastBackup = getString(R.string.noBackupYet);
            if(backupDir != null)
            {
//                ArrayList<String> loglines = shellCommands.readLogFile(new File(backupDir.getAbsolutePath() + "/" + pinfo.packageName), pinfo.packageName);
//                ArrayList<String> loglines = logFile.readLogFile(new File(backupDir.getAbsolutePath() + "/" + pinfo.packageName), pinfo.packageName);
                try
                {
//                    lastBackup = loglines.get(5);
                    lastBackup = new LogFile(new File(backupDir.getAbsolutePath() + "/" + pinfo.packageName), pinfo.packageName).getLastBackupTimestamp();
                }
                catch(IndexOutOfBoundsException e)
                {
                    lastBackup = this.getString(R.string.noBackupYet);
                }
            }

            boolean isSystem = false;
            if((pinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            {
                isSystem = true;
            }
            AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), pinfo.versionName, pinfo.versionCode, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, lastBackup, isSystem, true);
            appInfoList.add(appInfo);

        }
        if(backupDir != null && backupDir.exists())
        {
            for(String folder : backupDir.list())
            {
                boolean found = false;
                for(PackageInfo pinfo : pinfoList)
                {
                    if(pinfo.packageName.equals(folder))
                    {
                        found = true;
                    }
                }
                if(!found)
                {
                    try
                    {
//                        ArrayList<String> loginfo = shellCommands.readLogFile(new File(backupDir.getAbsolutePath() + "/" + folder), folder);
//                        ArrayList<String> loginfo = logFile.readLogFile(new File(backupDir.getAbsolutePath() + "/" + folder), folder);
//                        AppInfo appInfo = new AppInfo(loginfo.get(2), loginfo.get(0), loginfo.get(1), 0 /*versionCode*/, loginfo.get(3), loginfo.get(4), loginfo.get(5), false, false);
                        LogFile logInfo = new LogFile(new File(backupDir.getAbsolutePath() + "/" + folder), folder);
                        AppInfo appInfo = new AppInfo(logInfo.getPackageName(), logInfo.getLabel(), logInfo.getVersionName(), logInfo.getVersionCode(), logInfo.getSourceDir(), logInfo.getDataDir(), logInfo.getLastBackupTimestamp(), false, false);
                        // kan ikke tjekke om afinstallerede programmer var system : måske gemme i log
                        appInfoList.add(appInfo);
                    }
                    catch(IndexOutOfBoundsException e)
                    {
                        // mappen er enten ikke en backupmappe eller noget er galt
                    }
                }
            }
        }
        BatchActivity.appInfoList = appInfoList;
    }
    public void refresh()
    {
        new Thread(new Runnable(){
            public void run()
            {
                handleMessages.showMessage("", getString(R.string.collectingData));
                showAll = true;
                appInfoList.clear();
                getPackageInfo();
                runOnUiThread(new Runnable(){
                    public void run()
                    {
                        adapter.setNewOriginalValues(appInfoList);
                        adapter.notifyDataSetChanged();
                    }
                });
                handleMessages.endMessage();
            }
        }).start();
    }
    public void refresh(AppInfo appInfo)
    {
//        ArrayList<String> loginfo = shellCommands.readLogFile(new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName()), appInfo.getPackageName());
//        ArrayList<String> loginfo = logFile.readLogFile(new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName()), appInfo.getPackageName());
        LogFile logInfo = new LogFile(new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName()), appInfo.getPackageName());
        int pos = appInfoList.indexOf(appInfo);
        /*
        appInfo.label = loginfo.get(0);
        appInfo.lastBackup = loginfo.get(5);
        */
        appInfo.label = logInfo.getLabel();
        appInfo.lastBackup = logInfo.getLastBackupTimestamp();
        appInfoList.set(pos, appInfo);
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == BATCH_REQUEST)
        {
            if(data != null)
            {
                boolean changesMade = data.getBooleanExtra("changesMade", false);
                if(changesMade)
                {
                    refresh();
                }
            }
            for(AppInfo appInfo : appInfoList)
            {
                if(appInfo.isChecked)
                {
                    appInfo.toggle();
                }
            }
        }
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) // onPrepare i stedet for onCreate, så menuen kan være forskellig i de to aktiviteter - menu.clear() så menuen ikke duplikerer sig ved hvert tryk på menuknappen
    {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
        {
            mSearchItem = menu.findItem(R.id.search);
            SearchView search = (SearchView) mSearchItem.getActionView();
            search.setIconifiedByDefault(true);
            search.setOnQueryTextListener(new OnQueryTextListener()
            {
                @Override
                public boolean onQueryTextChange(String newText)
                {
                    OAndBackup.this.adapter.getFilter().filter(newText);
                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query)
                {
                    OAndBackup.this.adapter.getFilter().filter(query);
                    return true;
                }
            });
            // man kan ikke bruge onCloseListener efter 3.2: http://code.google.com/p/android/issues/detail?id=25758
            mSearchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener()
            {
                @Override
                public boolean onMenuItemActionExpand(MenuItem item)
                {
                    return true;
                }
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item)
                {
                    adapter.getFilter().filter("");
                    return true;
                }
            });
        }
        else
        {
            menu.removeItem(R.id.search);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.refresh:
                refresh();
                break;
            case R.id.batchbackup:
                Intent backupIntent = new Intent(this, BatchActivity.class);
                backupIntent.putExtra("dk.jens.backup.backupBoolean", true);
                startActivityForResult(backupIntent, BATCH_REQUEST);
                break;
            case R.id.batchrestore:
                Intent restoreIntent = new Intent(this, BatchActivity.class);
                restoreIntent.putExtra("dk.jens.backup.backupBoolean", false);
                startActivityForResult(restoreIntent, BATCH_REQUEST);
                break;
            case R.id.showAll:
                showAll = true;
                showOnlyUser = false;
                adapter.getFilter().filter("");
                break;
            case R.id.showOnlySystem:
                showOnlyUser = false;
                showAll = false;
                adapter.filterAppType(2);
                break;
            case R.id.showOnlyUser:
                showOnlyUser = true;
                showAll = false;
                adapter.filterAppType(1);
                break;
            case R.id.showNotBackedup:
                adapter.filterIsBackedup();
                break;
            case R.id.sortByLabel:
                adapter.sortByLabel();
                if(!showAll)
                {
                    if(showOnlyUser)
                    {
                        adapter.filterAppType(1);
                    }
                    else
                    {
                        adapter.filterAppType(2);
                    }
                }
                else
                {
                    adapter.getFilter().filter("");
                }
                break;
            case R.id.sortByPackageName:
                adapter.sortByPackageName();
                if(!showAll)
                {
                    if(showOnlyUser)
                    {
                        adapter.filterAppType(1);
                    }
                    else
                    {
                        adapter.filterAppType(2);
                    }
                }
                else
                {
                    adapter.getFilter().filter("");
                }
                break;
            case R.id.preferences:
                startActivity(new Intent(this, Preferences.class));
                break;
            case R.id.schedules:
                startActivity(new Intent(this, Scheduler.class));
                break;
        }
        return true;
    }    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId())
        {
            case R.id.uninstall:
                new AlertDialog.Builder(this)
                .setTitle(appInfoList.get(info.position).getLabel())
                .setMessage(R.string.uninstallDialogMessage)
                .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new Thread(new Runnable()
                        {
                            public void run()
                            {
                                Log.i(TAG, "uninstalling " + appInfoList.get(info.position).getLabel());
                                handleMessages.showMessage(appInfoList.get(info.position).getLabel(), "uninstall");
                                shellCommands.uninstall(appInfoList.get(info.position).getPackageName());
                                runOnUiThread(new Runnable()
                                {
                                    public void run()
                                    {
                                        refresh();
                                    }
                                });
                                handleMessages.endMessage();
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show();
                return true;
            case R.id.deleteBackup:
                new AlertDialog.Builder(this)
                .setTitle(appInfoList.get(info.position).getLabel())
                .setMessage(R.string.deleteBackupDialogMessage)
                .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new Thread(new Runnable()
                        {
                            public void run()
                            {
                                handleMessages.showMessage(appInfoList.get(info.position).getLabel(), "delete backup files");
                                File backupSubDir = new File(backupDir.getAbsolutePath() + "/" + appInfoList.get(info.position).getPackageName());
                                shellCommands.deleteBackup(backupSubDir);
                                runOnUiThread(new Runnable()
                                {
                                    public void run()
                                    {
                                        refresh(); // behøver ikke refresh af alle pakkerne, men refresh(packageName) kalder readLogFile(), som ikke kan håndtere, hvis logfilen ikke findes
                                    }
                                });
                                handleMessages.endMessage();
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key)
    {
        if(key.equals("pathBackupFolder"))
        {
            String backupDirPath = prefs.getString("pathBackupFolder", fileCreator.getDefaultBackupDirPath());
//            backupDir = new File(backupDirPath);
            createBackupDir(backupDirPath);
            refresh();
        }
        if(key.equals("pathRsync") || key.equals("pathBusybox"))
        {
            shellCommands = new ShellCommands(this);
        }
    }
    public boolean onSearchRequested()
    {
        if(mSearchItem != null)
        {
            mSearchItem.expandActionView();
        }
        return true;
    }
    public void createBackupDir(String path)
    {
        if(path.trim().length() > 0)
        {
            backupDir = fileCreator.createBackupFolder(path);
        }
        else
        {
            backupDir = fileCreator.createBackupFolder(fileCreator.getDefaultBackupDirPath());
        }
        if(backupDir == null)
        {
            runOnUiThread(new Runnable()
            {
                public void run()
                {
                    Toast.makeText(OAndBackup.this, getString(R.string.mkfileError) + " " + fileCreator.getDefaultBackupDirPath(), Toast.LENGTH_LONG).show();
                }
            });                    
        }
    }
}