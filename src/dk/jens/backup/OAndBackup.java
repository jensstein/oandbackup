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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class OAndBackup extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener
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
    boolean localTimestampFormat;
    int notificationNumber = 0;
    int notificationId = (int) Calendar.getInstance().getTimeInMillis();

    ShellCommands shellCommands;
    HandleMessages handleMessages;
    FileCreationHelper fileCreator;
    LogFile logFile;
    NotificationHelper notificationHelper;

    ListView listView;
    
    ArrayList<String> userList;
    ArrayList<String> selectedUsers;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handleMessages = new HandleMessages(this);
        shellCommands = new ShellCommands(this);
        fileCreator = new FileCreationHelper(this);
        notificationHelper = new NotificationHelper(this);
        logFile = new LogFile(this);
        
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
                boolean bboxInstalled = shellCommands.checkBusybox();
                if(!bboxInstalled)
                {
                    showWarning("", getString(R.string.busyboxProblem));
                }
                handleMessages.changeMessage("", getString(R.string.collectingData));
                pm = getPackageManager();
                prefs = PreferenceManager.getDefaultSharedPreferences(OAndBackup.this);
                prefs.registerOnSharedPreferenceChangeListener(OAndBackup.this);
                String backupDirPath = prefs.getString("pathBackupFolder", fileCreator.getDefaultBackupDirPath());
                createBackupDir(backupDirPath);
                localTimestampFormat = prefs.getBoolean("timestamp", true);
                
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
    @Override
    public void onDestroy()
    {
        if(handleMessages != null)
        {
            handleMessages.dismissMessage();
        }
        super.onDestroy();
    }
    public void displayDialog(AppInfo appInfo)
    {
        BackupRestoreDialogFragment dialog = new BackupRestoreDialogFragment(this, appInfo);
//        dialog.show(getFragmentManager(), "DialogFragment");
        dialog.show(getSupportFragmentManager(), "DialogFragment");
    }
    public void callBackup(final AppInfo appInfo)
    {
        new Thread(new Runnable()
        {
            int backupRet = 0;
            public void run()
            {
                handleMessages.showMessage(appInfo.getLabel(), getString(R.string.backup));
                if(backupDir != null)
                {
                    File backupSubDir = new File(backupDir, appInfo.getPackageName());
                    if(!backupSubDir.exists())
                    {
                        backupSubDir.mkdirs();
                    }
                    else
                    {
                        shellCommands.deleteOldApk(backupSubDir, appInfo.getSourceDir());
                    }
                    backupRet = shellCommands.doBackup(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getSourceDir());
                    logFile.writeLogFile(backupSubDir, appInfo.getPackageName(), appInfo.getLabel(), appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getSourceDir(), appInfo.getDataDir(), null, appInfo.isSystem);
                
                    // køre på uitråd for at undgå WindowLeaked
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            refresh(appInfo);
                        }
                    });
                }
                handleMessages.endMessage();
                if(backupRet == 0)
                {
                    notificationHelper.showNotification(OAndBackup.class, notificationId++, getString(R.string.backupSuccess), appInfo.getLabel(), true);
                }
                else
                {
                    notificationHelper.showNotification(OAndBackup.class, notificationId++, getString(R.string.backupFailure), appInfo.getLabel(), true);
                }
            }
        }).start();
    }
    public void callRestore(final AppInfo appInfo, final int options)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                int apkRet, restoreRet, permRet;
                apkRet = restoreRet = permRet = 0;
                if(backupDir != null)
                {
                    File backupSubDir = new File(backupDir, appInfo.getPackageName());
                    // error handling, hvis backupSubDir ikke findes
                    handleMessages.showMessage(appInfo.getLabel(), getString(R.string.restore));

                    LogFile logInfo = new LogFile(backupSubDir, appInfo.getPackageName(), localTimestampFormat);
                    String dataDir = appInfo.getDataDir();
                    String apk = logInfo.getApk();
                    switch(options)
                    {
                        case 1:
                            apkRet = shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk, appInfo.isSystem);
                            break;
                        case 2:
                            if(appInfo.isInstalled)
                            {
                                restoreRet = shellCommands.doRestore(backupSubDir, appInfo.getLabel(), appInfo.getPackageName());
                                permRet = shellCommands.setPermissions(dataDir);
                            }
                            else
                            {
                                Log.i(TAG, getString(R.string.restoreDataWithoutApkError) + appInfo.getPackageName());
                            }
                            break;
                        case 3:
                            apkRet = shellCommands.restoreApk(backupSubDir, appInfo.getLabel(), apk, appInfo.isSystem);
                            restoreRet = shellCommands.doRestore(backupSubDir, appInfo.getLabel(), appInfo.getPackageName());
                            permRet = shellCommands.setPermissions(dataDir);
                            break;
                    }
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            refresh();
                        }
                    });
                }
                handleMessages.endMessage();
                if(apkRet == 0 && restoreRet == 0 && permRet == 0)
                {
                    notificationHelper.showNotification(OAndBackup.class, notificationId++, getString(R.string.restoreSuccess), appInfo.getLabel(), true);
                }
                else
                {
                    notificationHelper.showNotification(OAndBackup.class, notificationId++, getString(R.string.restoreFailure), appInfo.getLabel(), true);
                }
            }
        }).start();
    }
    public void getPackageInfo()
    {
        pinfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        Collections.sort(pinfoList, pInfoPackageNameComparator);
        // list seemingly starts scrambled on 4.3
        BatchActivity.pinfoList = pinfoList;

        for(PackageInfo pinfo : pinfoList)
        {
            int loggedVersionCode;
            String loggedVersionName;
            String lastBackup = getString(R.string.noBackupYet);
            if(backupDir != null)
            {
                LogFile logInfo = new LogFile(new File(backupDir, pinfo.packageName), pinfo.packageName, localTimestampFormat);
                loggedVersionCode = logInfo.getVersionCode();
                loggedVersionName = logInfo.getVersionName();
                if(logInfo.getLastBackupTimestamp() != null)
                {
                    lastBackup = logInfo.getLastBackupTimestamp();
                }
            }
            else
            {
                loggedVersionCode = 0;
                loggedVersionName = "0";
            }

            boolean isSystem = false;
            if((pinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            {
                isSystem = true;
            }
            AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), loggedVersionName, pinfo.versionName, loggedVersionCode, pinfo.versionCode, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, lastBackup, isSystem, true);
            appInfoList.add(appInfo);

        }
        if(backupDir != null && backupDir.exists())
        {
            String[] files = backupDir.list();
            Arrays.sort(files);
            for(String folder : files)
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
                    LogFile logInfo = new LogFile(new File(backupDir.getAbsolutePath() + "/" + folder), folder, localTimestampFormat);
                    if(logInfo.getLastBackupTimestamp() != null)
                    {
                        AppInfo appInfo = new AppInfo(logInfo.getPackageName(), logInfo.getLabel(), "", logInfo.getVersionName(), 0, logInfo.getVersionCode(), logInfo.getSourceDir(), logInfo.getDataDir(), logInfo.getLastBackupTimestamp(), logInfo.isSystem(), false);
                        appInfoList.add(appInfo);
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
        if(backupDir != null)
        {
            LogFile logInfo = new LogFile(new File(backupDir, appInfo.getPackageName()), appInfo.getPackageName(), localTimestampFormat);
            int pos = appInfoList.indexOf(appInfo);
            appInfo.label = logInfo.getLabel();
            appInfo.lastBackup = logInfo.getLastBackupTimestamp();
            appInfoList.set(pos, appInfo);
            adapter.notifyDataSetChanged();
        }
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // clear menu so menus from other activities aren't shown also
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
                backupIntent.putStringArrayListExtra("dk.jens.backup.users", shellCommands.getUsers());
                filterShowAll();
                startActivityForResult(backupIntent, BATCH_REQUEST);
                break;
            case R.id.batchrestore:
                Intent restoreIntent = new Intent(this, BatchActivity.class);
                restoreIntent.putExtra("dk.jens.backup.backupBoolean", false);
                restoreIntent.putStringArrayListExtra("dk.jens.backup.users", shellCommands.getUsers());
                filterShowAll();
                startActivityForResult(restoreIntent, BATCH_REQUEST);
                break;
            case R.id.showAll:
                filterShowAll();
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
            case R.id.showNotInstalled:
                adapter.filterIsInstalled();
                break;
            case R.id.showNewAndUpdated:
                adapter.filterNewAndUpdated();
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
                                AppInfo appInfo = appInfoList.get(info.position);
                                Log.i(TAG, "uninstalling " + appInfo.getLabel());
                                handleMessages.showMessage(appInfo.getLabel(), getString(R.string.uninstallProgess));
                                shellCommands.uninstall(appInfo.getPackageName(), appInfo.getSourceDir(), appInfo.getDataDir(), appInfo.isSystem);
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
                                handleMessages.showMessage(appInfoList.get(info.position).getLabel(), getString(R.string.deleteBackup));
                                if(backupDir != null)
                                {
                                    File backupSubDir = new File(backupDir, appInfoList.get(info.position).getPackageName());
                                    shellCommands.deleteBackup(backupSubDir);
                                    runOnUiThread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            refresh(); // behøver ikke refresh af alle pakkerne, men refresh(packageName) kalder readLogFile(), som ikke kan håndtere, hvis logfilen ikke findes
                                        }
                                    });
                                }
                                handleMessages.endMessage();
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show();
                return true;
            case R.id.enablePackage:
                displayDialogEnableDisable(appInfoList.get(info.position).getPackageName(), true);
                return true;
            case R.id.disablePackage:
                displayDialogEnableDisable(appInfoList.get(info.position).getPackageName(), false);
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
            createBackupDir(backupDirPath);
            refresh();
        }
        if(key.equals("pathBusybox"))
        {
            shellCommands = new ShellCommands(this);
        }
        if(key.equals("timestamp"))
        {
            localTimestampFormat = prefs.getBoolean("timestamp", true);
            refresh();
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
    public void createBackupDir(final String path)
    {
        if(path.trim().length() > 0)
        {
            backupDir = fileCreator.createBackupFolder(path);
            if(fileCreator.fallbackFlag)
            {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(OAndBackup.this, getString(R.string.mkfileError) + " " + path + " - " + getString(R.string.fallbackToDefault) + ": " + fileCreator.getDefaultBackupDirPath(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        else
        {
            backupDir = fileCreator.createBackupFolder(fileCreator.getDefaultBackupDirPath());
        }
        if(backupDir == null)
        {
            showWarning(getString(R.string.mkfileError) + " " + fileCreator.getDefaultBackupDirPath(), getString(R.string.backupFolderError));
        }
    }
    public void showWarning(final String title, final String message)
    {
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                new AlertDialog.Builder(OAndBackup.this)
                    .setTitle(title)
                    .setMessage(message)
                    .setNeutralButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id){}})
                    .setCancelable(false)
                    .show();
            }
        });
    }
    public void filterShowAll()
    {
        showAll = true;
        showOnlyUser = false;
        adapter.getFilter().filter("");
    }
    public void displayDialogEnableDisable(final String packageName, final boolean enable)
    {
        String title = enable ? getString(R.string.enablePackageTitle) : getString(R.string.disablePackageTitle);
        selectedUsers = new ArrayList<String>();
        userList = shellCommands.getUsers();
        CharSequence[] users = userList.toArray(new CharSequence[userList.size()]);
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMultiChoiceItems(users, null, new DialogInterface.OnMultiChoiceClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int chosen, boolean checked)
                {
                    if(checked)
                    {
                        selectedUsers.add(userList.get(chosen));
                    }
                    else if(selectedUsers.contains(chosen))
                    {
                        selectedUsers.remove(Integer.valueOf(chosen));
                    }
                }
            })
            .setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    shellCommands.enableDisablePackage(packageName, selectedUsers, enable);
                }
            })
            .setNegativeButton(R.string.dialogCancel, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which){}
            })
            .show();
    }
    public Comparator<PackageInfo> pInfoPackageNameComparator = new Comparator<PackageInfo>()
    {
        public int compare(PackageInfo p1, PackageInfo p2)
        {
            return p1.packageName.compareToIgnoreCase(p2.packageName);
        }
    };
}