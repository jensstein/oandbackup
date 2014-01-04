package dk.jens.backup;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

public class Tools extends ListActivity
{
    public static ArrayList<AppInfo> appInfoList;
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
        
        handleMessages = new HandleMessages(this);

        Bundle extra = getIntent().getExtras();
        if(extra != null)
        {
            backupDir = (File) extra.get("dk.jens.backup.backupDir");
        }
        // get users to prevent an unnecessary call to su
        ArrayList<String> users = getIntent().getStringArrayListExtra("dk.jens.backup.users");
        shellCommands = new ShellCommands(this, users);
                
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
    public void onListItemClick(ListView l, View v, int pos, long id)
    {
        switch(pos)
        {
            case 0:
                quickReboot();
                break;
            case 1:
                new AlertDialog.Builder(this)
                .setTitle(R.string.tools_batchDeleteTitle)
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
                                deleteBackups();
                            }
                        }).start();
                    }
                })
                .setNegativeButton(R.string.dialogNo, null)
                .show();
                break;
        }
    }
    public void changesMade()
    {
        Intent result = new Intent();
        result.putExtra("changesMade", true);
        setResult(RESULT_OK, result);
    }    
    public void deleteBackups()
    {
        handleMessages.showMessage(getString(R.string.tools_batchDeleteMessage), "");
        for(AppInfo appInfo : appInfoList)
        {
            if(backupDir != null && !appInfo.isInstalled)
            {
                handleMessages.changeMessage(getString(R.string.tools_batchDeleteMessage), appInfo.getLabel());
                Log.i(TAG, "deleting backup of " + appInfo.getLabel());
                File backupSubDir = new File(backupDir, appInfo.getPackageName());
                shellCommands.deleteBackup(backupSubDir);
            }
        }
        handleMessages.endMessage();
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