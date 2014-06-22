package dk.jens.backup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.io.File;

public class ShareDialogFragment extends DialogFragment
{
    public ShareDialogFragment()
    {
        super();
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Bundle arguments = getArguments();
        String label = arguments.getString("label");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(label);
        builder.setMessage(R.string.shareTitle);
        if(arguments.containsKey("apk"))
        {
            builder.setNegativeButton(R.string.radioApk, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), (File) arguments.get("apk")));
                }
            });
        }
        if(arguments.containsKey("data"))
        {
            builder.setNeutralButton(R.string.radioData, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    startActivity(HandleShares.constructIntentSingle(getString(R.string.shareTitle), (File) arguments.get("data")));
                }
            });
        }
        if(arguments.containsKey("apk") && arguments.containsKey("data"))
        {
            builder.setPositiveButton(R.string.radioBoth, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    startActivity(HandleShares.constructIntentMultiple(getString(R.string.shareTitle), (File) arguments.get("apk"), (File) arguments.get("data")));
                }
            });
        }
        return builder.create();
    }
}