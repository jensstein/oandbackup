package com.machiav3lli.backup.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.machiav3lli.backup.BlacklistListener;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.activities.MainActivityX;
import com.machiav3lli.backup.activities.SchedulerActivityX;
import com.machiav3lli.backup.items.AppInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BlacklistDialogFragment extends DialogFragment {
    private final ArrayList<BlacklistListener> blacklistListeners = new ArrayList<>();

    public void addBlacklistListener(BlacklistListener listener) {
        blacklistListeners.add(listener);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        final ArrayList<String> selections = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        Bundle args = getArguments();
        assert args != null;
        int blacklistId = args.getInt(Constants.BLACKLIST_ARGS_ID,
                SchedulerActivityX.GLOBALBLACKLISTID);
        ArrayList<String> blacklistedPackages = args.getStringArrayList(
                Constants.BLACKLIST_ARGS_PACKAGES);
        ArrayList<AppInfo> appInfoList = new ArrayList<>(MainActivityX.getOriginalList());
        boolean[] checkedPackages = new boolean[appInfoList.size()];
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        appInfoList.sort((appInfo1, appInfo2) -> {
            assert blacklistedPackages != null;
            boolean b1 = blacklistedPackages.contains(appInfo1.getPackageName());
            boolean b2 = blacklistedPackages.contains(appInfo2.getPackageName());
            return (b1 != b2) ? (b1 ? -1 : 1) : 0;
        });
        for (AppInfo appInfo : appInfoList) {
            labels.add(appInfo.getLabel());
            assert blacklistedPackages != null;
            if (blacklistedPackages.contains(appInfo.getPackageName())) {
                checkedPackages[i] = true;
                selections.add(appInfo.getPackageName());
            }
            i++;
        }
        builder.setTitle(R.string.sched_blacklist)
                .setMultiChoiceItems(labels.toArray(new CharSequence[0]),
                        checkedPackages, (dialogInterface, which, isChecked) -> {
                            String packageName = appInfoList.get(which).getPackageName();
                            if (isChecked)
                                selections.add(packageName);
                            else selections.remove(packageName);
                        })
                .setPositiveButton(R.string.dialogOK, (dialogInterface, id) -> {
                    for (BlacklistListener listener : blacklistListeners) {
                        listener.onBlacklistChanged(
                                selections.toArray(new CharSequence[0]),
                                blacklistId);
                    }
                });
        return builder.create();
    }
}
