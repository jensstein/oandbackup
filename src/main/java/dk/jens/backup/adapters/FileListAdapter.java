package dk.jens.backup.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;
import dk.jens.backup.ui.FileBrowser;

import java.io.File;
import java.util.ArrayList;

public class FileListAdapter extends ArrayAdapter<File>
{
    final static String TAG = OAndBackup.TAG;

    Context context;
    ArrayList<File> items;
    int layout;
    public FileListAdapter(Context context, int layout, ArrayList<File> items)
    {
        super(context, layout, items);
        this.context = context;
        this.layout = layout;
        this.items = items;
    }
    public void addAll(ArrayList<File> list)
    {
        for(File file : list)
            items.add(file);
    }
    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.filename = (TextView) convertView.findViewById(R.id.filename);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        File file = getItem(pos);
        if(file != null)
        {
            if(file instanceof FileBrowser.ParentFile)
                viewHolder.filename.setText("..");
            else
                viewHolder.filename.setText(file.getAbsolutePath() + "/");
        }
        return convertView;
    }
    static class ViewHolder
    {
        TextView filename;
    }
}
