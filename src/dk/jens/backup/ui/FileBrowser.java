package dk.jens.backup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FileBrowser extends BaseActivity
implements View.OnClickListener
{
    final static String TAG = OAndBackup.TAG;

    ArrayList<File> filesList;
    SharedPreferences prefs;
    Button setButton;
    HorizontalScrollView scroll;
    ListView listView;
    TextView currentPathTextView;
    FileListAdapter adapter;
    String root = "/";
    ArrayList<Integer> posList = new ArrayList<Integer>();
    private static String resultPath;

    @Override
    public void onCreate(Bundle savedInstance)
    {
        super.onCreate(savedInstance);
        setContentView(R.layout.filebrowser);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        root = prefs.getString("pathBackupFolder", FileCreationHelper.getDefaultBackupDirPath());
        resultPath = null;

        filesList = getFilesList(root);
        Collections.sort(filesList, pathComparator);

        currentPathTextView = (TextView) findViewById(R.id.fileBrowserCurrentPath);
        currentPathTextView.setText(root);
        setButton = (Button) findViewById(R.id.fileBrowserSetPath);
        setButton.setOnClickListener(this);
        scroll = (HorizontalScrollView) findViewById(R.id.fileBrowserHorizontalScrollView);

        adapter = new FileListAdapter(this, R.layout.fileslist, filesList);
        listView = (ListView) findViewById(R.id.fileBrowserListview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
            {
                navigateFiles(true, pos);
            }
        });
    }
    public ArrayList<File> getFilesList(String path)
    {
        ArrayList<File> list = new ArrayList<File>();
        File dir = new File(path);
        File[] dirList = dir.listFiles();
        if(dirList != null)
            for(File file : dirList)
                if(file.isDirectory())
                    list.add(file);
        return list;
    }
    public void navigateFiles(final boolean direction, int pos)
    {
        if(direction)
        {
            root = filesList.get(pos).getAbsolutePath();
            posList.add(pos);
        }
        else
        {
            File file = new File(root);
            if((root = file.getParent()) == null)
                root = "/";
        }
        adapter.clear();
        ArrayList<File> filesList = getFilesList(root);
        Collections.sort(filesList, pathComparator);
        adapter.addAll(filesList);
        adapter.notifyDataSetChanged();
        currentPathTextView.setText(root);
        scroll.post(new Runnable()
        {
            public void run()
            {
                scroll.fullScroll(View.FOCUS_RIGHT);
                if(!direction && posList.size() > 0)
                    listView.setSelection(posList.remove(posList.size() - 1));
                else
                    listView.setSelection(0);
            }
        });
    }
    public static String getPath()
    {
        return resultPath;
    }
    public static void invalidatePath()
    {
        resultPath = null;
    }
    @Override
    public void onClick(View v)
    {
        resultPath = root;
        finish();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(root.length() > 0 && !root.equals("/"))
                navigateFiles(false, 0);
            else
                onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public Comparator<File> pathComparator = new Comparator<File>()
    {
        public int compare(File m1, File m2)
        {
            return m1.getName().compareToIgnoreCase(m2.getName());
        }
    };
}
