package dk.jens.backup;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import dk.jens.backup.adapters.ToolsAdapter;
import dk.jens.backup.ui.HandleMessages;
import dk.jens.backup.ui.LanguageHelper;
import dk.jens.backup.ui.NotificationHelper;

import java.io.File;
import java.util.ArrayList;

public class Tools extends ListActivity
{
    ArrayList<AppInfo> appInfoList = OAndBackup.appInfoList;
    final static String TAG = OAndBackup.TAG; 
    File backupDir;
    ShellCommands shellCommands;
    HandleMessages handleMessages;
    final static int RESULT_OK = 0;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolslayout);
        if(android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.JELLY_BEAN)
        {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String langCode = prefs.getString(Constants.PREFS_LANGUAGES,
            Constants.PREFS_LANGUAGES_DEFAULT);
        LanguageHelper.initLanguage(this, langCode);
        
        handleMessages = new HandleMessages(this);

        Bundle extra = getIntent().getExtras();
        if(extra != null)
        {
            backupDir = (File) extra.get("dk.jens.backup.backupDir");
        }
        // get users to prevent an unnecessary call to su
        ArrayList<String> users = getIntent().getStringArrayListExtra("dk.jens.backup.users");
        shellCommands = new ShellCommands(PreferenceManager.getDefaultSharedPreferences(this), users);
                
        String[] titles = getResources().getStringArray(R.array.tools_titles);
        String[] descriptions = getResources().getStringArray(R.array.tools_descriptions);
        ArrayList<Pair> items = new ArrayList<Pair>();
        for(int i = 0; i < titles.length; i++)
        {
            Pair pair = new Pair(titles[i], descriptions[i]);
            items.add(pair);
        }
        ToolsAdapter adapter = new ToolsAdapter(this, R.layout.toolslist, items);
        setListAdapter(adapter);
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
    public void onListItemClick(ListView l, View v, int pos, long id)
    {
        switch(pos)
        {
            case 0:
                quickReboot();
                break;
            case 1:
                final ArrayList<AppInfo> deleteList = new ArrayList<AppInfo>();
                String message = "";
                for(AppInfo appInfo : appInfoList)
                {
                    if(!appInfo.isInstalled())
                    {
                        deleteList.add(appInfo);
                        message += appInfo.getLabel() + "\n";
                    }
                }
                if(!deleteList.isEmpty())
                {
                    new AlertDialog.Builder(this)
                    .setTitle(R.string.tools_batchDeleteTitle)
                    .setMessage(message.trim())
                    .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            changesMade();
                            new Thread(new Runnable()
                            {
                                public void run()
                                {
                                    deleteBackups(deleteList);
                                }
                            }).start();
                        }
                    })
                    .setNegativeButton(R.string.dialogNo, null)
                    .show();
                }
                else
                {
                    Toast.makeText(this, getString(R.string.tools_nothingToDelete), Toast.LENGTH_LONG).show();
                }
                break;
            case 2:
                Intent intent = new Intent(this, LogViewer.class);
                startActivity(intent);
                break;
        }
    }
    public void changesMade()
    {
        Intent result = new Intent();
        result.putExtra("changesMade", true);
        setResult(RESULT_OK, result);
    }    
    public void deleteBackups(ArrayList<AppInfo> deleteList)
    {
        handleMessages.showMessage(getString(R.string.tools_batchDeleteMessage), "");
        for(AppInfo appInfo : deleteList)
        {
            if(backupDir != null)
            {
                handleMessages.changeMessage(getString(R.string.tools_batchDeleteMessage), appInfo.getLabel());
                Log.i(TAG, "deleting backup of " + appInfo.getLabel());
                File backupSubDir = new File(backupDir, appInfo.getPackageName());
                ShellCommands.deleteBackup(backupSubDir);
            }
            else
            {
                Log.e(TAG, "Tools.deleteBackups: backupDir null");
            }
        }
        handleMessages.endMessage();
        NotificationHelper.showNotification(this, Tools.class, (int) System.currentTimeMillis(), getString(R.string.tools_notificationTitle), getString(R.string.tools_backupsDeleted) + " " + deleteList.size(), false);
    }
    public void quickReboot()
    {
        new AlertDialog.Builder(this)
        .setTitle(R.string.quickReboot)
        .setMessage(R.string.quickRebootMessage)
        .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                shellCommands.quickReboot();
            }
        })
        .setNegativeButton(R.string.dialogNo, null)
        .show();
    }
    public static class Pair
    {
        public final String title, description;
        public Pair(String title, String description)
        {
            this.title = title;
            this.description = description;
        }
    }
}
