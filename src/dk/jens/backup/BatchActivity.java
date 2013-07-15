package dk.jens.backup;

import android.support.v4.app.TaskStackBuilder;

import android.app.Activity;
//import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class BatchActivity extends Activity implements OnClickListener
{
    public static List<PackageInfo> pinfoList;
    public static ArrayList<AppInfo> appInfoList;
    final static String TAG = OBackup.TAG; 
    boolean backupBoolean;
    final static int SHOW_DIALOG = 0;
    final static int CHANGE_DIALOG = 1;
    final static int DISMISS_DIALOG = 2;

    final static int RESULT_OK = 0;

    boolean checkboxSelectAllBoolean = true;

    ShellCommands shellCommands = new ShellCommands();
    File backupDir;
    ProgressDialog progress;
    PackageManager pm;

    ListView listView;
    BatchAdapter adapter;
    ArrayList<AppInfo> list;

    LinearLayout linearLayout;
    RadioButton rb, rbData, rbApk, rbBoth;
    ArrayList<CheckBox> checkboxList = new ArrayList<CheckBox>();
    HashMap<String, PackageInfo> pinfoMap = new HashMap<String, PackageInfo>();
    HandleMessages handleMessages = new HandleMessages(this);

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backuprestorelayout);

        pm = getPackageManager();

        backupDir = new File(Environment.getExternalStorageDirectory() + "/obackups");
        if(!backupDir.exists())
        {
            backupDir.mkdirs();
        }

        Bundle extra = getIntent().getExtras();
        if(extra != null)
        {
            backupBoolean = extra.getBoolean("dk.jens.backup.backupBoolean");
        }
        linearLayout = (LinearLayout) findViewById(R.id.backupLinear);

        Button bt = (Button) findViewById(R.id.backupRestoreButton);
        bt.setOnClickListener(this);

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

            bt.setText(R.string.backupButton);
        }
        else
        {
            list = new ArrayList<AppInfo>(appInfoList);

            bt.setText(R.string.restoreButton);
            RadioGroup rg = new RadioGroup(this);
            rg.setOrientation(LinearLayout.HORIZONTAL);
            rbData = new RadioButton(this);
            rbData.setText(R.string.radioData);
            rg.addView(rbData);
            rbData.setChecked(true); // hvis ikke setChecked() kaldes før  knappen er tilføjet til sin parent bliver den permanent trykket 
            rbApk = new RadioButton(this);
            rbApk.setText(R.string.radioApk);
            rg.addView(rbApk);
            rbBoth = new RadioButton(this);
            rbBoth.setText(R.string.radioBoth);
            rg.addView(rbBoth);
            linearLayout.addView(rg);
        }

        listView = (ListView) findViewById(R.id.listview);
//        adapter = new BatchAdapter(this, R.layout.batchlistlayout, appInfoList);
        adapter = new BatchAdapter(this, R.layout.batchlistlayout, list);
        listView.setAdapter(adapter);
        // onItemClickListener gør at hele viewet kan klikkes - med onCheckedListener er det kun checkboxen der kan klikkes
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
            {
                AppInfo appInfo = list.get(pos);
                appInfo.toggle();
                adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void onClick(View v)
    {
        new Thread(new Runnable()
        {
            public void run()
            {
                int i = 0;
                for(AppInfo appInfo : list)
                {
                    if(appInfo.isChecked)
                    {
                        Log.i(TAG, appInfo.toString());
                        i++;
                    }
                }
                int id = (int) Calendar.getInstance().getTimeInMillis();
                int total = i;
                i = 0;
                for(final AppInfo appInfo: list)
                {
                    if(appInfo.isChecked)
                    {
                        i++;
                        String log;
                        String message = "(" + Integer.toString(i) + "/" + Integer.toString(total) + ")";
                        File backupSubDir = new File(backupDir.getAbsolutePath() + "/" + appInfo.getPackageName());
                        String title = backupBoolean ? "backing up" : "restoring";
                        showNotification(id, title, appInfo.getLabel());
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
                            log = appInfo.getLabel() + "\n" + appInfo.getVersion() + "\n" + appInfo.getPackageName() + "\n" + appInfo.getSourceDir() + "\n" + appInfo.getDataDir();                            
                            if(!backupSubDir.exists())
                            {
                                backupSubDir.mkdirs();
                            }
                            else
                            {
                                shellCommands.deleteBackup(backupSubDir);
                                backupSubDir.mkdirs();
                            }
                            shellCommands.doBackup(backupSubDir, appInfo.getDataDir(), appInfo.getSourceDir());
                            shellCommands.writeLogFile(backupSubDir.getAbsolutePath() + "/" + appInfo.getPackageName() + ".log", log);
                            Log.i(TAG, "backup: " + appInfo.getLabel());
                        }
                        else
                        {
                            Log.i(TAG, "restore: " + appInfo.getPackageName());
                            ArrayList<String> readlog = shellCommands.readLogFile(backupSubDir, appInfo.getPackageName());
                            String dataDir = readlog.get(4); // når alle logfilerne er genskrevet
                            String apk = readlog.get(3);
                            String[] apkArray = apk.split("/");
                            apk = apkArray[apkArray.length - 1];

                            if(rbApk.isChecked())
                            {
                                shellCommands.restoreApk(backupSubDir, apk);
                            }
                            else if(rbData.isChecked())
                            {
                                if(appInfo.isInstalled)
                                {
                                    shellCommands.doRestore(backupSubDir, appInfo.getPackageName());
                                    shellCommands.setPermissions(dataDir);
                                }
                                else
                                {
                            Log.i(TAG, getString(R.string.restoreDataWithoutApkError) + appInfo.getPackageName());
                                }
                            }
                            else if(rbBoth.isChecked())
                            {
                                shellCommands.restoreApk(backupSubDir, apk);
                                shellCommands.doRestore(backupSubDir, appInfo.getPackageName());                                
                                shellCommands.setPermissions(dataDir);
                            }
                        }
                        if(i == total)
                        {
                            String msg = backupBoolean ? "backup" : "restore";
                            showNotification(id, "operation complete", "batch " + msg);
                            handleMessages.endMessage();
                        }
                    }
                }
            }
        }).start();
    }
    public void setCheckBoxes(String string, int color)
    {
        CheckBox cb = new CheckBox(this);
        cb.setText(string);
        cb.setTextColor(color);
        checkboxList.add(cb);
        linearLayout.addView(cb);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
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
        }
        return true;
    }
    public void showNotification(int id, String title, String text)
    {
//        Notification.Builder mBuilder = new Notification.Builder(this)
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.backup_small)
            .setContentTitle(title)
            .setContentText(text);
        // resultIntent og taskstackbuilder er for bagudkompabilitet
        Intent resultIntent = new Intent(this, BatchActivity.class);        
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(BatchActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());
    }
}
