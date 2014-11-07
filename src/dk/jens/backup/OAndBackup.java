package dk.jens.backup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class OAndBackup extends BaseActivity
implements SharedPreferences.OnSharedPreferenceChangeListener
{
    static final String TAG = OAndBackup.class.getSimpleName().toLowerCase();
    static final int BATCH_REQUEST = 1;
    static final int TOOLS_REQUEST = 2;

    File backupDir;
    MenuItem mSearchItem;

    AppInfoAdapter adapter;
    ListView listView;
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

    ShellCommands shellCommands;
    HandleMessages handleMessages;
    Sorter sorter;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        handleMessages = new HandleMessages(this);

        // if onCreate is called due to a configuration change su and busybox shouldn't be checked again
        final boolean checked = savedInstanceState != null ? savedInstanceState.getBoolean("stateChecked") : false;
        final int firstVisiblePosition = savedInstanceState != null ? savedInstanceState.getInt("firstVisiblePosition") : 0;
        if(savedInstanceState != null)
        {
            threadId = savedInstanceState.getLong("threadId");
            Utils.reShowMessage(handleMessages, threadId);
        }

        Thread initThread = new Thread(new Runnable()
        {
            public void run()
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(OAndBackup.this);
                prefs.registerOnSharedPreferenceChangeListener(OAndBackup.this);
                shellCommands = new ShellCommands(prefs);
                String langCode = prefs.getString("languages", "system");
                LanguageHelper.initLanguage(OAndBackup.this, langCode);
                String backupDirPath = prefs.getString("pathBackupFolder", FileCreationHelper.getDefaultBackupDirPath());
                backupDir = Utils.createBackupDir(OAndBackup.this, backupDirPath);
                // temporary method to move logfile from bottom of external storage to bottom of backupdir
                new FileCreationHelper().moveLogfile(prefs.getString("pathLogfile", FileCreationHelper.getDefaultLogFilePath()));
                if(!checked)
                {
                    handleMessages.showMessage("", getString(R.string.suCheck));
                    boolean haveSu = ShellCommands.checkSuperUser();
                    LanguageHelper.legacyKeepLanguage(OAndBackup.this, langCode);
                    if(!haveSu)
                    {
                        Utils.showWarning(OAndBackup.this, "", getString(R.string.noSu));
                    }
                    checkBusybox();
                    handleMessages.endMessage();
                }

                if(appInfoList == null)
                {
                    handleMessages.changeMessage("", getString(R.string.collectingData));
                    appInfoList = AppInfoHelper.getPackageInfo(OAndBackup.this, backupDir, true);
                    LanguageHelper.legacyKeepLanguage(OAndBackup.this, langCode);
                    handleMessages.endMessage();
                }

                listView = (ListView) findViewById(R.id.listview);
                registerForContextMenu(listView);

                adapter = new AppInfoAdapter(OAndBackup.this, R.layout.listlayout, appInfoList);
                adapter.setLocalTimestampFormat(prefs.getBoolean("timestamp", true));
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
                        listView.setSelection(firstVisiblePosition);
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
        });
        initThread.start();
        /*
         * only set threadId here if this is not after a configuration change.
         * otherwise it could overwrite the value of a running backup / restore thread.
         * a better fix would probably be to check for the thread name or have the threads
         * in an array.
         */
        if(!checked)
            threadId = initThread.getId();
    }
    @Override
    public void onResume()
    {
        super.onResume();
        // reload handlemessages in case its context has been garbage collected
        handleMessages = new HandleMessages(this);
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
            handleMessages.endMessage();
        }
        super.onDestroy();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean("stateChecked", true);
        outState.putLong("threadId", threadId);
        int firstVisiblePosition = 0;
        if(listView != null)
            firstVisiblePosition = listView.getFirstVisiblePosition();
        outState.putInt("firstVisiblePosition", firstVisiblePosition);
    }
    public void displayDialog(AppInfo appInfo)
    {
        if(!appInfo.isInstalled() && appInfo.getBackupMode() == AppInfo.MODE_DATA)
        {
            Toast.makeText(this, getString(R.string.notInstalledModeDataWarning), Toast.LENGTH_LONG).show();
        }
        else
        {
            Bundle arguments = new Bundle();
            arguments.putParcelable("appinfo", appInfo);
            BackupRestoreDialogFragment dialog = new BackupRestoreDialogFragment();
            dialog.setArguments(arguments);
    //        dialog.show(getFragmentManager(), "DialogFragment");
            dialog.show(getSupportFragmentManager(), "DialogFragment");
        }
    }
    public void callBackup(final AppInfo appInfo, final int backupMode)
    {
        Thread backupThread = new Thread(new Runnable()
        {
            int backupRet = 0;
            public void run()
            {
                handleMessages.showMessage(appInfo.getLabel(), getString(R.string.backup));
                if(backupDir != null)
                {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(OAndBackup.this);
                    Crypto crypto = null;
                    if(prefs.getBoolean("enableCrypto", false) && Crypto.isAvailable(OAndBackup.this))
                        crypto = getCrypto();
                    backupRet = BackupRestoreHelper.backup(OAndBackup.this, backupDir, appInfo, shellCommands, backupMode);
                    if(backupRet == 0 && crypto != null)
                        crypto.encryptFromAppInfo(OAndBackup.this, backupDir, appInfo, backupMode, prefs);
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
                    Utils.showErrors(OAndBackup.this);
                }
            }
        });
        backupThread.start();
        threadId = backupThread.getId();
    }
    public void callRestore(final AppInfo appInfo, final int mode)
    {
        Thread restoreThread = new Thread(new Runnable()
        {
            public void run()
            {
                int ret = 0;
                if(backupDir != null)
                {
                    handleMessages.showMessage(appInfo.getLabel(), getString(R.string.restore));
                    Crypto crypto = null;
                    if(Crypto.isAvailable(OAndBackup.this) && Crypto.needToDecrypt(backupDir, appInfo, mode))
                        crypto = getCrypto();
                    ret = BackupRestoreHelper.restore(OAndBackup.this, backupDir, appInfo, shellCommands, mode, crypto);
                    refresh();
                }
                handleMessages.endMessage();
                if(ret == 0)
                {
                    NotificationHelper.showNotification(OAndBackup.this, OAndBackup.class, notificationId++, getString(R.string.restoreSuccess), appInfo.getLabel(), true);
                }
                else
                {
                    NotificationHelper.showNotification(OAndBackup.this, OAndBackup.class, notificationId++, getString(R.string.restoreFailure), appInfo.getLabel(), true);
                    Utils.showErrors(OAndBackup.this);
                }
            }
        });
        restoreThread.start();
        threadId = restoreThread.getId();
    }
    public void refresh()
    {
        Thread refreshThread = new Thread(new Runnable()
        {
            public void run()
            {
                handleMessages.showMessage("", getString(R.string.collectingData));
                appInfoList = AppInfoHelper.getPackageInfo(OAndBackup.this, backupDir, true);
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        // temporary work-around until the race condition between refresh and oncreate when returning from batchactivity with changesmade have been fixed
                        if(adapter != null && sorter != null)
                        {
                            adapter.setNewOriginalValues(appInfoList);
                            sorter.sort(sorter.getFilteringMethod().getId());
                            sorter.sort(sorter.getSortingMethod().getId());
                            adapter.restoreFilter();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                handleMessages.endMessage();
            }
        });
        refreshThread.start();
        threadId = refreshThread.getId();
    }
    public void refreshSingle(AppInfo appInfo)
    {
        if(backupDir != null)
        {
            LogFile logInfo = new LogFile(new File(backupDir, appInfo.getPackageName()), appInfo.getPackageName());
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LanguageHelper.initLanguage(this, prefs.getString("languages", "system"));
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BATCH_REQUEST || requestCode == TOOLS_REQUEST)
        {
            if(appInfoList != null)
            {
                for(AppInfo appInfo : appInfoList)
                {
                    appInfo.setChecked(false);
                }
            }
            if(data != null)
            {
                boolean changesMade = data.getBooleanExtra("changesMade", false);
                if(changesMade)
                {
                    refresh();
                }
                if(sorter != null)
                {
                    sorter.sort(data.getIntExtra("filteringMethodId", 0));
                    sorter.sort(data.getIntExtra("sortingMethodId", 0));
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
            search.setQueryHint(getString(R.string.searchHint));
            search.setOnQueryTextListener(new OnQueryTextListener()
            {
                @Override
                public boolean onQueryTextChange(String newText)
                {
                    if(OAndBackup.this.adapter != null)
                        OAndBackup.this.adapter.getFilter().filter(newText);
                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query)
                {
                    if(OAndBackup.this.adapter != null)
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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
            menu.removeItem(R.id.share);
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
                .setTitle(adapter.getItem(info.position).getLabel())
                .setMessage(R.string.uninstallDialogMessage)
                .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Thread uninstallThread = new Thread(new Runnable()
                        {
                            public void run()
                            {
                                AppInfo appInfo = adapter.getItem(info.position);
                                Log.i(TAG, "uninstalling " + appInfo.getLabel());
                                handleMessages.showMessage(appInfo.getLabel(), getString(R.string.uninstallProgress));
                                int ret = shellCommands.uninstall(appInfo.getPackageName(), appInfo.getSourceDir(), appInfo.getDataDir(), appInfo.isSystem());
                                refresh();
                                handleMessages.endMessage();
                                if(ret == 0)
                                {
                                    NotificationHelper.showNotification(OAndBackup.this, OAndBackup.class, notificationId++, getString(R.string.uninstallSuccess), appInfo.getLabel(), true);
                                }
                                else
                                {
                                    NotificationHelper.showNotification(OAndBackup.this, OAndBackup.class, notificationId++, getString(R.string.uninstallFailure), appInfo.getLabel(), true);
                                    Utils.showErrors(OAndBackup.this);
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
                .setTitle(adapter.getItem(info.position).getLabel())
                .setMessage(R.string.deleteBackupDialogMessage)
                .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Thread deleteBackupThread = new Thread(new Runnable()
                        {
                            public void run()
                            {
                                handleMessages.showMessage(adapter.getItem(info.position).getLabel(), getString(R.string.deleteBackup));
                                if(backupDir != null)
                                {
                                    File backupSubDir = new File(backupDir, adapter.getItem(info.position).getPackageName());
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
                displayDialogEnableDisable(adapter.getItem(info.position).getPackageName(), true);
                return true;
            case R.id.disablePackage:
                displayDialogEnableDisable(adapter.getItem(info.position).getPackageName(), false);
                return true;
            case R.id.share:
                AppInfo appInfo = adapter.getItem(info.position);
                File apk = new File(backupDir, appInfo.getPackageName() + "/" + appInfo.getLogInfo().getApk());
                String dataPath = appInfo.getLogInfo().getDataDir();
                dataPath = dataPath.substring(dataPath.lastIndexOf("/") + 1);
                File data = new File(backupDir, appInfo.getPackageName() + "/" + dataPath + ".zip");
                Bundle arguments = new Bundle();
                arguments.putString("label", appInfo.getLabel());
                if(apk.exists())
                {
                    arguments.putSerializable("apk", apk);
                }
                if(data.exists())
                {
                    arguments.putSerializable("data", data);
                }
                ShareDialogFragment shareDialog = new ShareDialogFragment();
                shareDialog.setArguments(arguments);
                shareDialog.show(getSupportFragmentManager(), "DialogFragment");
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
            String backupDirPath = preferences.getString(key, FileCreationHelper.getDefaultBackupDirPath());
            backupDir = Utils.createBackupDir(OAndBackup.this, backupDirPath);
            refresh();
        }
        else if(key.equals("pathBusybox"))
        {
            shellCommands = new ShellCommands(preferences);
            checkBusybox();
        }
        else if(key.equals("timestamp"))
        {
            adapter.setLocalTimestampFormat(preferences.getBoolean(key, true));
            adapter.notifyDataSetChanged();
        }
        else if(key.equals("oldBackups"))
        {
            sorter = new Sorter(adapter, preferences);
        }
        else if(key.equals("languages"))
        {
            languageChanged = true;
        }
        else if(key.equals("enableSpecialBackups"))
            refresh();
        else if(key.equals("enableCrypto") && preferences.getBoolean(key, false))
            startCrypto();
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
        final ArrayList<String> selectedUsers = new ArrayList<String>();
        final ArrayList<String> userList = shellCommands.getUsers();
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
    public void checkBusybox()
    {
        if(!shellCommands.checkBusybox())
            Utils.showWarning(this, "", getString(R.string.busyboxProblem));
    }
}
