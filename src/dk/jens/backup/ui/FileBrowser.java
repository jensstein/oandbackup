package dk.jens.backup.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import dk.jens.backup.BaseActivity;
import dk.jens.backup.Constants;
import dk.jens.backup.ui.dialogs.CreateDirectoryDialog;
import dk.jens.backup.FileCreationHelper;
import dk.jens.backup.adapters.FileListAdapter;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FileBrowser extends BaseActivity
implements View.OnClickListener, CreateDirectoryDialog.PathListener
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
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filebrowser);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(savedInstanceState != null)
            root = savedInstanceState.getString(Constants.BUNDLE_FILEBROWSER_ROOT);
        else
            root = prefs.getString(Constants.PREFS_PATH_BACKUP_DIRECTORY,
                FileCreationHelper.getDefaultBackupDirPath());
        resultPath = null;

        filesList = getFilesList(root);

        currentPathTextView = (TextView) findViewById(R.id.fileBrowserCurrentPath);
        currentPathTextView.setText(root);
        setButton = (Button) findViewById(R.id.fileBrowserSetPath);
        setButton.setOnClickListener(this);
        scroll = (HorizontalScrollView) findViewById(R.id.fileBrowserHorizontalScrollView);

        adapter = new FileListAdapter(this, R.layout.fileslist, filesList);
        listView = (ListView) findViewById(R.id.fileBrowserListview);
        registerForContextMenu(listView);
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
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.BUNDLE_FILEBROWSER_ROOT, root);
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
        Collections.sort(list, pathComparator);
        File parent = dir.getParentFile();
        // set as ParentFile so that FileListAdapter will abbreviate
        // the representation of its path to '..'
        if(parent != null)
            list.add(0, new ParentFile(parent.getAbsolutePath()));
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
        refresh();
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
    public void refresh()
    {
        filesList = getFilesList(root);
        adapter.clear();
        adapter.addAll(filesList);
        adapter.notifyDataSetChanged();
    }
    public static String getPath()
    {
        return resultPath;
    }
    public static void invalidatePath()
    {
        resultPath = null;
    }
    public void setPath(String path)
    {
        resultPath = path;
        finish();
    }
    public boolean makedir(String root, String dirname)
    {
        try
        {
            File dir = new File(root, dirname);
            return dir.mkdir();
        }
        catch(SecurityException e)
        {
            Log.e(TAG, "makedir: " + e.toString());
        }
        return false;
    }
    @Override
    public void onPathSet(String root, String dirname)
    {
        if(!makedir(root, dirname))
            Toast.makeText(this, getString(R.string.filebrowser_createDirectoryError) + " " + root + "/" + dirname, Toast.LENGTH_LONG).show();
        refresh();
    }
    @Override
    public void onClick(View v)
    {
        setPath(root);
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filebrowsermenu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        case R.id.createDirectory:
            Bundle arguments = new Bundle();
            arguments.putString(Constants.BUNDLE_FILEBROWSER_ROOT, root);
            CreateDirectoryDialog dialog = new CreateDirectoryDialog();
            dialog.setArguments(arguments);
            dialog.show(getFragmentManager(), "DialogFragment");
            break;
        case R.id.refresh:
            refresh();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filebrowsercontextmenu, menu);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        File file = adapter.getItem(info.position);
        menu.setHeaderTitle(file.getName());
    }
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        case R.id.filebrowser_contextSetBackupDirectory:
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            File file = adapter.getItem(info.position);
            setPath(file.getAbsolutePath());
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }
    public Comparator<File> pathComparator = new Comparator<File>()
    {
        public int compare(File m1, File m2)
        {
            return m1.getName().compareToIgnoreCase(m2.getName());
        }
    };
    /*
    * this is just a placeholder class that allows for checking
    * whether a given file is the parent folder in the list.
    * the actual check is done in FileListAdapter with instanceof.
    */
    public class ParentFile extends File
    {
        public ParentFile(String path)
        {
            super(path);
        }
    }
}
