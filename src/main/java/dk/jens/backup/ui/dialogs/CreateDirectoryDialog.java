package dk.jens.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import dk.jens.backup.Constants;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;

public class CreateDirectoryDialog extends DialogFragment
{
    final static String TAG = OAndBackup.TAG;

    EditText editText;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        final String root = arguments.getString(Constants.BUNDLE_FILEBROWSER_ROOT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.filebrowser_createDirectory);
        builder.setMessage(R.string.filebrowser_createDirectoryDlgMsg);

        editText = new EditText(getActivity());
        if(savedInstanceState != null)
            editText.setText(savedInstanceState.getString(
                Constants.BUNDLE_CREATEDIRECTORYDIALOG_EDITTEXT));
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(lp);
        builder.setView(editText);

        builder.setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                String dirname = editText.getText().toString();
                try
                {
                    PathListener activity = (PathListener) getActivity();
                    activity.onPathSet(root, dirname);
                }
                catch(ClassCastException e)
                {
                    Log.e(TAG, "CreateDirectoryDialog: " + e.toString());
                }
            }
        });
        builder.setNegativeButton(R.string.dialogCancel, null);
        return builder.create();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.BUNDLE_CREATEDIRECTORYDIALOG_EDITTEXT,
            editText.getText().toString());
    }
    public interface PathListener
    {
        void onPathSet(String root, String dirname);
    }
}
