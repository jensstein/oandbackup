package dk.jens.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OAndBackup extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    static final String TAG = OAndBackup.class.getSimpleName().toLowerCase();
    static final int BATCH_REQUEST = 1;
    static final int TOOLS_REQUEST = 2;

    PackageManager pm;
    File backupDir;
    List<PackageInfo> pinfoList;
    MenuItem mSearchItem;
    SharedPreferences prefs;

    AppInfoAdapter adapter;
    ArrayList<AppInfo> appInfoList;

    boolean localTimestampFormat;
    boolean languageChanged; // flag for work-around for fixing language change on older android versions
    int notificationNumber = 0;
    int notificationId = (int) System.currentTimeMillis();

    ShellCommands shellCommands;
    HandleMessages handleMessages;
    FileCreationHelper fileCreator;
    Sorter sorter;

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
        
        new Thread(new Runnable(){
            public void run()
            {
                prefs = PreferenceManager.getDefaultSharedPreferences(OAndBackup.this);
                prefs.registerOnSharedPreferenceChangeListener(OAndBackup.this);
                String langCode = prefs.getString("languages", "system");
                LanguageHelper languageHelper = new LanguageHelper();
                languageHelper.initLanguage(OAndBackup.this, langCode);
                handleMessages.showMessage("", getString(R.string.suCheck));
                boolean haveSu = shellCommands.checkSuperUser();
                languageHelper.legacyKeepLanguage(OAndBackup.this, langCode);
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
                    Utils.showWarning(OAndBackup.this, "", getString(R.string.busyboxProblem));
                }
                handleMessages.changeMessage("", getString(R.string.collectingData));
                pm = getPackageManager();
                String backupDirPath = prefs.getString("pathBackupFolder", fileCreator.getDefaultBackupDirPath());
                backupDir = Utils.createBackupDir(OAndBackup.this, backupDirPath, fileCreator);
                localTimestampFormat = prefs.getBoolean("timestamp", true);
                
                appInfoList = new ArrayList<AppInfo>();
                getPackageInfo();
                languageHelper.legacyKeepLanguage(OAndBackup.this, langCode);
                handleMessages.endMessage();
                listView = (ListView) findViewById(R.id.listview);
                registerForContextMenu(listView);
                

                adapter = new AppInfoAdapter(OAndBackup.this, R.layout.listlayout, appInfoList);
                sorter = new Sorter(adapter, prefs);
                if(prefs.getBoolean("rememberFiltering", false))
                {
                    sorter.sort(Sorter.convertFilteringId(prefs.getInt("filteringId", 0)));
                    sorter.sort(Sorter.convertSortingId(prefs.getInt("sortingId", 1)));
                }
                runOnUiThread(new Runnable(){
                    public void run()
                    {
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                        {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
                            {
                                AppInfo appInfo = adapter.getItem(pos);
                                displayDialog(appInfo);
                            }
                        });
                    }
                });
            }
        }).start();
    }
    @Override
    public void onResume()
    {
        super.onResume();
        // work-around for changing language on older android versions since TaskStackBuilder doesn't seem to recreate the activities below the top in the stack properly
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB && languageChanged)
        {
            Utils.reloadWithParentStack(OAndBackup.this);
            languageChanged = false;
        }
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
        if(!appInfo.isInstalled && appInfo.getBackupMode() == AppInfo.MODE_DATA)
        {
            Toast.makeText(this, getString(R.string.notInstalledModeDataWarning), Toast.LENGTH_LONG).show();
        }
        else
        {
            BackupRestoreDialogFragment dialog = new BackupRestoreDialogFragment(this, appInfo);
    //        dialog.show(getFragmentManager(), "DialogFragment");
            dialog.show(getSupportFragmentManager(), "DialogFragment");
        }
    }
    public void callBackup(final AppInfo appInfo, final int backupMode)
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
                    backupRet = shellCommands.doBackup(backupSubDir, appInfo.getLabel(), appInfo.getDataDir(), appInfo.getSourceDir(), backupMode);

                    shellCommands.logReturnMessage(backupRet);

                    new LogFile(OAndBackup.this).writeLogFile(backupSubDir, appInfo.getPackageName(), appInfo.getLabel(), appInfo.getVersionName(), appInfo.getVersionCode(), appInfo.getSourceDir(), appInfo.getDataDir(), null, appInfo.isSystem, appInfo.setNewBackupMode(backupMode));
                
                    // køre på uitråd for at undgå WindowLeaked
                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            refreshSingle(appInfo);
                        }
                    });
                }
                handleMessages.endMessage();
                if(backupRet == 0)
                {
                    NotificationHelper.showNotification(OAndBackup.this, OAndBackup.class, notificationId++, getString(R.string.backupSuccess), appInfo.getLabel(), true);
                }
                else
                {
                    NotificationHelper.showNotification(OAndBackup.this, OAndBackup.class, notificationId++, getString(R.string.backupFailure), appInfo.getLabel(), true);
                    Utils.showErrors(OAndBackup.this, shellCommands);
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
                                shellCommands.logReturnMessage(restoreRet);

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
                            shellCommands.logReturnMessage(restoreRet);
                            permRet = shellCommands.setPermissions(dataDir);
                            break;
                    }
                    refresh();
                }
                handleMessages.endMessage();
                if(apkRet == 0 && restoreRet == 0 && permRet == 0)
                {
                    NotificationHelper.showNotification(OAndBackup.this, OAndBackup.class, notificationId++, getString(R.string.restoreSuccess), appInfo.getLabel(), true);
                }
                else
                {
                    NotificationHelper.showNotification(OAndBackup.this, OAndBackup.class, notificationId++, getString(R.string.restoreFailure), appInfo.getLabel(), true);
                    Utils.showErrors(OAndBackup.this, shellCommands);
                }
            }
        }).start();
    }
    public void getPackageInfo()
    {
        pinfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        Collections.sort(pinfoList, pInfoPackageNameComparator);
        // list seemingly starts scrambled on 4.3

        for(PackageInfo pinfo : pinfoList)
        {
            String lastBackup = getString(R.string.noBackupYet);
            boolean isSystem = false;
            if((pinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            {
                isSystem = true;
            }
            if(backupDir != null)
            {
                Drawable icon = pm.getApplicationIcon(pinfo.applicationInfo);
                File subdir = new File(backupDir, pinfo.packageName);
                if(subdir.exists())
                {
                    LogFile logInfo = new LogFile(subdir, pinfo.packageName, localTimestampFormat);
                    AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), pinfo.versionName, pinfo.versionCode, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, isSystem, true, logInfo);
                    appInfo.icon = icon;
                    appInfoList.add(appInfo);
                }
                else
                {
                    AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), pinfo.versionName, pinfo.versionCode, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, isSystem, true);
                    appInfo.icon = icon;
                    appInfoList.add(appInfo);
                }
            }
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
                        AppInfo appInfo = new AppInfo(logInfo.getPackageName(), logInfo.getLabel(), logInfo.getVersionName(), logInfo.getVersionCode(), logInfo.getSourceDir(), logInfo.getDataDir(), logInfo.isSystem(), false, logInfo);
                        appInfoList.add(appInfo);
                    }
                }
            }
        }
        BatchActivity.appInfoList = appInfoList;
        CustomPackageList.appInfoList = appInfoList;
        Tools.appInfoList = appInfoList;
    }
    public void refresh()
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                handleMessages.showMessage("", getString(R.string.collectingData));
                appInfoList.clear();
                getPackageInfo();
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        adapter.setNewOriginalValues(appInfoList);
                        sorter.sort(sorter.getFilteringMethod().getId());
                        sorter.sort(sorter.getSortingMethod().getId());
                        adapter.restoreFilter();
                        adapter.notifyDataSetChanged();
                    }
                });
                handleMessages.endMessage();
            }
        }).start();
    }
    public void refreshSingle(AppInfo appInfo)
    {
        if(backupDir != null)
        {
            LogFile logInfo = new LogFile(new File(backupDir, appInfo.getPackageName()), appInfo.getPackageName(), localTimestampFormat);
            int pos = appInfoList.indexOf(appInfo);
            appInfo.setLogInfo(logInfo);
            appInfoList.set(pos, appInfo);
            adapter.notifyDataSetChanged();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        new LanguageHelper().initLanguage(this, prefs.getString("languages", "system"));
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == BATCH_REQUEST || requestCode == TOOLS_REQUEST)
        {
            if(data != null)
            {
                boolean changesMade = data.getBooleanExtra("changesMade", false);
                if(changesMade)
                {
                    refresh();
                }
                else
                {
                    for(AppInfo appInfo : appInfoList)
                    {
                        if(appInfo.isChecked)
                        {
                            appInfo.toggle();
                        }
                    }
                }
                sorter.sort(data.getIntExtra("filteringMethodId", 0));
                sorter.sort(data.getIntExtra("sortingMethodId", 0));
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
            search.setQueryHint(getString(R.string.searchHint));
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
                    sorter.filterShowAll();
                    return true;
                }
            });
        }
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
    public Intent batchIntent(Class batchClass, boolean backup)
    {
        Intent batchIntent = new Intent(this, batchClass);
        batchIntent.putExtra("dk.jens.backup.backupBoolean", backup);
        batchIntent.putStringArrayListExtra("dk.jens.backup.users", shellCommands.getUsers());
        batchIntent.putExtra("dk.jens.backup.filteringMethodId", sorter.getFilteringMethod().getId());
        batchIntent.putExtra("dk.jens.backup.sortingMethodId", sorter.getSortingMethod().getId());
        return batchIntent;
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
                startActivityForResult(batchIntent(BatchActivity.class, true), BATCH_REQUEST);
                break;
            case R.id.batchrestore:
                startActivityForResult(batchIntent(BatchActivity.class, false), BATCH_REQUEST);
                break;
            case R.id.preferences:
                startActivity(new Intent(this, Preferences.class));
                break;
            case R.id.schedules:
                startActivity(new Intent(this, Scheduler.class));
                break;
            case R.id.search:
                setupLegacySearch();
                break;
            case R.id.tools:
                if(backupDir != null)
                {
                    Intent toolsIntent = new Intent(this, Tools.class);
                    toolsIntent.putExtra("dk.jens.backup.backupDir", backupDir);
                    toolsIntent.putStringArrayListExtra("dk.jens.backup.users", shellCommands.getUsers());
                    startActivityForResult(toolsIntent, TOOLS_REQUEST);
                }
                break;
            case R.id.help:
                startActivity(new Intent(this, Help.class));
                break;
            default:
                item.setChecked(!item.isChecked());
                sorter.sort(item.getItemId());
                break;
        }
        return true;
    }    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contextmenu, menu);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        AppInfo appInfo = adapter.getItem(info.position);
        if(appInfo.getLogInfo() == null)
        {
            menu.removeItem(R.id.deleteBackup);
        }
        menu.setHeaderTitle(appInfo.getLabel());
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
                                handleMessages.showMessage(appInfo.getLabel(), getString(R.string.uninstallProgress));
                                shellCommands.uninstall(appInfo.getPackageName(), appInfo.getSourceDir(), appInfo.getDataDir(), appInfo.isSystem);
                                refresh();
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
                                    refresh(); // behøver ikke refresh af alle pakkerne, men refresh(packageName) kalder readLogFile(), som ikke kan håndtere, hvis logfilen ikke findes
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
            backupDir = Utils.createBackupDir(OAndBackup.this, backupDirPath, fileCreator);
            refresh();
        }
        if(key.equals("pathBusybox"))
        {
            shellCommands = new ShellCommands(this);
        }
        if(key.equals("timestamp"))
        {
            localTimestampFormat = prefs.getBoolean("timestamp", true);
//            refresh(); 
                // conflicts with the other call to refresh() if both this and pathBackupFolder is changed
        }
        if(key.equals("oldBackups"))
        {
            sorter = new Sorter(adapter, prefs);
        }
        if(key.equals("languages"))
        {
            languageChanged = true;
        }
    }
    public boolean onSearchRequested()
    {
        setupLegacySearch();
        if(mSearchItem != null)
        {
            mSearchItem.expandActionView();
        }
        return true;
    }
    public void setupLegacySearch()
    {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
        {
            android.widget.LinearLayout linearLayout = (android.widget.LinearLayout) findViewById(R.id.linearLayout);
            View child = linearLayout.getChildAt(0);
            if(child.getClass() != android.widget.EditText.class)
            {
                final android.widget.EditText et = new android.widget.EditText(this);
                et.addTextChangedListener(new android.text.TextWatcher()
                {
                    public void afterTextChanged(android.text.Editable s){}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                    public void onTextChanged(CharSequence s, int start, int before, int count)
                    {
                        OAndBackup.this.adapter.getFilter().filter(s.toString());
                    }
                });
                android.view.ViewGroup.LayoutParams lp = new android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.addView(et, 0, lp);
                et.postDelayed(new Runnable(){
                    public void run()
                    {
                        et.requestFocus();
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(et, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    }
                }, 200);
                // workaround for showing the keyboard automatically when search is selected from menu - probably not the best solution: turbomanage.wordpress.com/2012/05/02/show-soft-keyboard-automatically-when-edittext-receives-focus/
            }
            else
            {
                child.requestFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(child, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }
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