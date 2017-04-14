package dk.jens.backup.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import dk.jens.backup.AppInfo;
import dk.jens.backup.BlacklistListener;
import dk.jens.backup.Constants;
import dk.jens.backup.OAndBackup;
import dk.jens.backup.R;
import dk.jens.backup.Sorter;
import dk.jens.backup.schedules.Scheduler;

import java.util.ArrayList;
import java.util.Collections;

public class BlacklistDialogFragment extends DialogFragment {
    private ArrayList<BlacklistListener> blacklistListeners = new ArrayList<>();

    public void addBlacklistListener(BlacklistListener listener) {
        blacklistListeners.add(listener);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        final ArrayList<String> selections = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        int blacklistId = args.getInt(Constants.BLACKLIST_ARGS_ID,
            Scheduler.GLOBALBLACKLISTID);
        ArrayList<String> blacklistedPackages = args.getStringArrayList(
            Constants.BLACKLIST_ARGS_PACKAGES);
        ArrayList<AppInfo> appInfoList = OAndBackup.appInfoList;
        boolean[] checkedPackages = new boolean[appInfoList.size()];
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        Collections.sort(appInfoList, Sorter.appInfoLabelComparator);
        // sort all checked items in the top
        // comparison taken from BooleanComparator of the springframework project
        // https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/util/comparator/BooleanComparator.html
        Collections.sort(appInfoList, (appInfo1, appInfo2) -> {
            boolean b1 = blacklistedPackages.contains(appInfo1.getPackageName());
            boolean b2 = blacklistedPackages.contains(appInfo2.getPackageName());
            return (b1 != b2) ? (!b1 ? 1 : -1) : 0;
        });
        for(AppInfo appInfo : appInfoList) {
            labels.add(appInfo.getLabel());
            if(blacklistedPackages.contains(appInfo.getPackageName())) {
                checkedPackages[i] = true;
                selections.add(appInfo.getPackageName());
            }
            i++;
        }
        builder.setTitle(R.string.blacklistDialogTitle)
            .setMultiChoiceItems(labels.toArray(new CharSequence[labels.size()]),
                    checkedPackages, (dialogInterface, which, isChecked) -> {
                String packageName = appInfoList.get(which).getPackageName();
                if (isChecked)
                    selections.add(packageName);
                else if(selections.contains(packageName))
                    selections.remove(packageName);
            })
            .setPositiveButton(R.string.dialogOK, (dialogInterface, id) -> {
                for(BlacklistListener listener : blacklistListeners) {
                    listener.onBlacklistChanged(
                        selections.toArray(new CharSequence[selections.size()]),
                        blacklistId);
                }
            });
        return builder.create();
    }
}
