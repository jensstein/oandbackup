package dk.jens.openbackup;

import android.support.v4.app.FragmentActivity;
import android.app.Activity;
import android.app.AlertDialog;
//import android.app.FragmentManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
//import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

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

public class OBackup extends FragmentActivity // FragmentActivity i stedet for Activity for at kunne bruge ting fra support-bibliotekerne
{
    static final String TAG = "obackup";
    final static int SHOW_DIALOG = 0;
    final static int CHANGE_DIALOG = 1;
    final static int DISMISS_DIALOG = 2;

    PackageManager pm;
    File backupDir;
    List<PackageInfo> pinfoList;
    ProgressDialog progress;
    MenuItem mSearchItem;

    TextAdapter adapter;
    ArrayList<AppInfo> appInfoList;

    boolean showAll = true;
    boolean showOnlyUser = false;
    int notificationNumber = 0;
    int notificationId = (int) Calendar.getInstance().getTimeInMillis();
    // taget herfra: https://github.com/sanathe06/AndroidGuide/blob/master/ExampleCompatNotificationBuilder/src/com/android/guide/compatnotificationbuilder/MainActivity.java#L41

    DoBackupRestore doBackupRestore = new DoBackupRestore();

    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        pm = getPackageManager();
        
        backupDir = new File(Environment.getExternalStorageDirectory() + "/obackups");
        if(!backupDir.exists())
        {
            backupDir.mkdirs();
        }

        appInfoList = new ArrayList<AppInfo>();
        getPackageInfo();
        listView = (ListView) findViewById(R.id.listview);
        registerForContextMenu(listView);

        adapter = new TextAdapter(this, R.layout.listlayout, appInfoList);

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
    public void displayDialog(AppInfo appInfo)
    {
        BackupRestoreDialogFragment dialog = new BackupRestoreDialogFragment(this, appInfo);
//        dialog.show(getFragmentManager(), "DialogFragment");
        dialog.show(getSupportFragmentManager(), "DialogFragment");
    }
    public void callBackup(final AppInfo appInfo)
    {
        final String log = appInfo.getLabel() + "\n" + appInfo.getVersion() + "\n" + appInfo.getPackageName() + "\n" + appInfo.getSourceDir() + "\n" + appInfo.getDataDir();
        new Thread(new Runnable()
        {
            public void run()
            {
                String[] string = {appInfo.getLabel(), "backup"};
                Message startMessage = Message.obtain();
                startMessage.what = SHOW_DIALOG;
                startMessage.obj = string;
                handler.sendMessage(startMessage);
                File backupSubDir = new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName());
                if(!backupSubDir.exists())
                {
                    backupSubDir.mkdirs();
                }
                else
                {
                    doBackupRestore.deleteBackup(backupSubDir);
                    backupSubDir.mkdirs();
                }
                doBackupRestore.doBackup(backupSubDir, appInfo.getDataDir(), appInfo.getSourceDir());
                doBackupRestore.writeLogFile(backupSubDir.getAbsolutePath() + "/" + appInfo.getPackageName() + ".log", log);
                
                // køre på uitråd for at undgå WindowLeaked
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        refresh(appInfo);
                    }
                });
                Message endMessage = Message.obtain();
                endMessage.what = DISMISS_DIALOG;
                handler.sendMessage(endMessage);
                showNotification(notificationId++, "backup complete", appInfo.getLabel());
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
                String[] string = {appInfo.getLabel(), "restore"};
                Message startMessage = Message.obtain();
                startMessage.what = SHOW_DIALOG;
                startMessage.obj = string;
                handler.sendMessage(startMessage);

                ArrayList<String> log = doBackupRestore.readLogFile(backupSubDir, appInfo.getPackageName());
                String dataDir = log.get(4);
                String apk = log.get(3);
                String[] apkArray = apk.split("/");
                apk = apkArray[apkArray.length - 1];
                switch(options)
                {
                    case 1:
                        doBackupRestore.restoreApk(backupSubDir, apk);
                        break;
                    case 2:
                        if(appInfo.isInstalled)
                        {
                            doBackupRestore.doRestore(backupSubDir, appInfo.getPackageName());
                            doBackupRestore.setPermissions(dataDir);
                        }
                        else
                        {
                            Log.i(TAG, "kan ikke doRestore uden restoreApk: " + appInfo.getPackageName() + " er ikke installeret");
                        }
                        break;
                    case 3:
                        doBackupRestore.restoreApk(backupSubDir, apk);
                        doBackupRestore.doRestore(backupSubDir, appInfo.getPackageName());
                        doBackupRestore.setPermissions(dataDir);
                        break;
                }
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        refresh();
                    }
                });
                Message endMessage = Message.obtain();
                endMessage.what = DISMISS_DIALOG;
                handler.sendMessage(endMessage);
                showNotification(notificationId++, "restore complete", appInfo.getLabel());
            }
        }).start();
    }
    // http://www.helloandroid.com/tutorials/using-threads-and-progressdialog
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            String[] array = (String[]) message.obj; // måske ikke den bedste måde at sende en samling af data på
            switch(message.what)
            {
                case SHOW_DIALOG:
//                    Log.i(TAG, "show");
                    progress = ProgressDialog.show(OBackup.this, array[0].toString(), array[1].toString(), true, false); // den sidste boolean er cancelable -> sættes til true, når der er skrevet en måde at afbryde handlingen (threaden) på
                    break;
                case CHANGE_DIALOG:
                    if(progress != null)
                    {
                        progress.setTitle(array[0].toString());
                        progress.setMessage("(" + array[1].toString() + "/" + array[2].toString() + ")");
                    }
                    break;
                case DISMISS_DIALOG:
//                    Log.i(TAG, "dismiss");
                    if(progress != null)
                    {
                        progress.dismiss();
                    }
                    break;
            }
        }
    };
    public void showNotification(int id, String title, String text)
    {
        // bør nok være det eksterne android.support.v4.app og NotificationCompat.Builder: http://developer.android.com/guide/topics/ui/notifiers/notifications.html
        // http://developer.android.com/training/basics/fragments/support-lib.html
//        Notification.Builder mBuilder = new Notification.Builder(this)
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.backup_small)
            .setContentTitle(title)
            .setContentText(text)
            .setNumber(++notificationNumber);
        Intent resultIntent = new Intent(this, OBackup.class);        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(OBackup.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }
    public void getPackageInfo()
    {
        pinfoList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);

        // overføre store datamængder fx ved hjælp af static fields:
        // http://developer.android.com/guide/faq/framework.html
        // http://stackoverflow.com/a/12848199
        // http://stackoverflow.com/questions/1441871/passing-data-of-a-non-primitive-type-between-activities-in-android

        BatchActivity.pinfoList = pinfoList;

        for(PackageInfo pinfo : pinfoList)
        {
            String lastBackup;
            ArrayList<String> loglines = doBackupRestore.readLogFile(new File(backupDir.getAbsolutePath() + "/" + pinfo.packageName), pinfo.packageName);
            try
            {
                lastBackup = loglines.get(5);
            }
            catch(IndexOutOfBoundsException e)
            {
                lastBackup = this.getString(R.string.noBackupYet);
            }

            boolean isSystem = false;
            if((pinfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
            {
                isSystem = true;
            }
            AppInfo appInfo = new AppInfo(pinfo.packageName, pinfo.applicationInfo.loadLabel(pm).toString(), pinfo.versionName, pinfo.applicationInfo.sourceDir, pinfo.applicationInfo.dataDir, lastBackup, isSystem, true);
            appInfoList.add(appInfo);

        }
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
                    ArrayList<String> loginfo = doBackupRestore.readLogFile(new File(backupDir.getAbsolutePath() + "/" + folder), folder);
                    AppInfo appInfo = new AppInfo(loginfo.get(2), loginfo.get(0),loginfo.get(1), loginfo.get(3), loginfo.get(4), loginfo.get(5), false, false);
                    // kan ikke tjekke om afinstallerede programmer var system : måske gemme i log
                    appInfoList.add(appInfo);
                }
                catch(IndexOutOfBoundsException e)
                {
                    // mappen er enten ikke en backupmappe eller noget er galt
                }
            }
        }
//        BatchActivity.appInfoList = appInfoList;
        // måske bruges hvis BatchActivity skal have listview
    }
    public void refresh()
    {
        showAll = true;
        appInfoList.clear();
        getPackageInfo();
        adapter.notifyDataSetChanged();
    }
    public void refresh(AppInfo appInfo)
    {
        ArrayList<String> loginfo = doBackupRestore.readLogFile(new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName()), appInfo.getPackageName());
        int pos = appInfoList.indexOf(appInfo);
        appInfo.label = loginfo.get(0);
        appInfo.lastBackup = loginfo.get(5);
        appInfoList.set(pos, appInfo);
        adapter.notifyDataSetChanged();
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
            // taget fra mms: http://androidxref.com/source/xref/packages/apps/Mms/src/com/android/mms/ui/ConversationList.java

            // http://developer.android.com/guide/topics/ui/actionbar.html#ActionView
            SearchView search = (SearchView) mSearchItem.getActionView();
            search.setIconifiedByDefault(true);
            search.setOnQueryTextListener(new OnQueryTextListener()
            {
                @Override
                public boolean onQueryTextChange(String newText)
                {
                    OBackup.this.adapter.getFilter().filter(newText);
                    return true;
                }
                @Override
                public boolean onQueryTextSubmit(String query)
                {
                    OBackup.this.adapter.getFilter().filter(query);
                    return true;
                }
            });
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
                // sende extra med intent: http://stackoverflow.com/a/4828453
                Intent backupIntent = new Intent(this, BatchActivity.class);
                backupIntent.putExtra("dk.jens.openbackup.backupBoolean", true);
                startActivity(backupIntent);
                break;
            case R.id.batchrestore:
                Intent restoreIntent = new Intent(this, BatchActivity.class);
                restoreIntent.putExtra("dk.jens.openbackup.backupBoolean", false);
                startActivity(restoreIntent);
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
            case R.id.sortByLabel:
                adapter.sortByLabel();
                // på grund af mObjects og mOriginalValues i ArrayAdapter bliver man nødt til at foretage en filtrering hver gang man vil ændre sin adapters dataset efter den første filtrering:
                // http://code.google.com/p/android/issues/detail?id=9666
                // http://stackoverflow.com/a/3418939/2320781
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
                .setPositiveButton(R.string.uninstallDialogYes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new Thread(new Runnable()
                        {
                            public void run()
                            {
                                Log.i(TAG, "uninstalling " + appInfoList.get(info.position).getLabel());
                                String[] string = {appInfoList.get(info.position).getLabel(), "uninstall"};
                                Message startMessage = Message.obtain();
                                startMessage.what = SHOW_DIALOG;
                                startMessage.obj = string;
                                handler.sendMessage(startMessage);

                                doBackupRestore.uninstall(appInfoList.get(info.position).getPackageName());
                                runOnUiThread(new Runnable()
                                {
                                    public void run()
                                    {
                                        refresh();
                                    }
                                });

                                Message endMessage = Message.obtain();
                                endMessage.what = DISMISS_DIALOG;
                                handler.sendMessage(endMessage);
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.uninstallDialogNo, null)
                .show();
                return true;
            case R.id.deleteBackup:
                new AlertDialog.Builder(this)
                .setTitle(appInfoList.get(info.position).getLabel())
                .setMessage(R.string.deleteBackupDialogMessage)
                .setPositiveButton(R.string.deleteBackupDialogYes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        new Thread(new Runnable()
                        {
                            public void run()
                            {
                                String[] string = {appInfoList.get(info.position).getLabel(), "delete backup files"};
                                Message startMessage = Message.obtain();
                                startMessage.what = SHOW_DIALOG;
                                startMessage.obj = string;
                                handler.sendMessage(startMessage);

                                File backupSubDir = new File(backupDir.getAbsolutePath() + "/" + appInfoList.get(info.position).getPackageName());
                                doBackupRestore.deleteBackup(backupSubDir);
                                runOnUiThread(new Runnable()
                                {
                                    public void run()
                                    {
                                        refresh(); // behøver ikke refresh af alle pakkerne, men refresh(packageName) kalder readLogFile(), som ikke kan håndtere, hvis logfilen ikke findes
                                    }
                                });

                                Message endMessage = Message.obtain();
                                endMessage.what = DISMISS_DIALOG;
                                handler.sendMessage(endMessage);
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.deleteBackupDialogNo, null)
                .show();
                return true;
            default:
                return super.onContextItemSelected(item);
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
}
