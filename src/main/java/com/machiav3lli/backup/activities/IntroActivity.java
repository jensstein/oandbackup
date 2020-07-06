package com.machiav3lli.backup.activities;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.utils.UIUtils;
import com.scottyab.rootbeer.RootBeer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;
import static com.machiav3lli.backup.utils.FileUtils.createBackupDir;
import static com.machiav3lli.backup.utils.FileUtils.getDefaultBackupDirPath;
import static com.machiav3lli.backup.utils.LogUtils.logDeviceInfo;
import static com.machiav3lli.backup.utils.PrefUtils.getPrefsString;

public class IntroActivity extends BaseActivity {
    static final String TAG = Constants.classTag(".IntroActivity");
    static final int READ_PERMISSION = 2;
    static final int WRITE_PERMISSION = 3;
    static final int STATS_PERMISSION = 4;
    public static File backupDir;
    private static ShellHandler shellHandler;

    @BindView(R.id.action)
    MaterialButton btn;

    SharedPreferences prefs;
    ArrayList<String> users;
    ShellCommands shellCommands;
    HandleMessages handleMessages;

    public static boolean checkUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        assert appOps != null;
        final int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return (context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            return (mode == AppOpsManager.MODE_ALLOWED);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setDayNightTheme(getPrefsString(this, Constants.PREFS_THEME));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        logDeviceInfo(this, TAG);
        prefs = this.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE);
        users = new ArrayList<>();
        checkRun(savedInstanceState);
        shellCommands = new ShellCommands(this, prefs, users, getFilesDir());
        handleMessages = new HandleMessages(this);

        if (checkStoragePermissions() && checkUsageStatsPermission(this)) {
            btn.setVisibility(View.GONE);
            if (this.checkResources()) {
                this.launchMainActivity();
            }
        } else if (!checkUsageStatsPermission(this)) {
            btn.setOnClickListener(v -> getUsageStatsPermission());
        } else {
            btn.setOnClickListener(v -> getStoragePermission());
        }
    }

    private void setDayNightTheme(String theme) {
        switch (theme) {
            case "light":
                setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    private void checkRun(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            users = savedInstanceState.getStringArrayList(Constants.BUNDLE_USERS);
    }

    private void getStoragePermission() {
        requireWriteStoragePermission();
        requireReadStoragePermission();
    }

    private void getUsageStatsPermission() {
        if (!checkUsageStatsPermission(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.grant_usage_access_title)
                    .setMessage(R.string.grant_usage_access_message)
                    .setPositiveButton(R.string.permission_approve,
                            (dialog, which) -> startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), STATS_PERMISSION))
                    .setNeutralButton(getString(R.string.permission_refuse), (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    private boolean checkStoragePermissions() {
        return (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requireReadStoragePermission() {
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, READ_PERMISSION);
    }

    private void requireWriteStoragePermission() {
        if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                if (!canAccessExternalStorage()) {
                    Log.w(TAG, String.format("Permissions were granted: %s -> %s",
                            Arrays.toString(permissions), Arrays.toString(grantResults)));
                    Toast.makeText(this, "Permissions were granted but because of an android bug you have to restart your phone",
                            Toast.LENGTH_LONG).show();
                }
                if (this.checkResources()) {
                    this.launchMainActivity();
                } else {
                    this.finishAffinity();
                }
            } else {
                Log.w(TAG, String.format("Permissions were not granted: %s -> %s",
                        Arrays.toString(permissions), Arrays.toString(grantResults)));
                Toast.makeText(this, getString(
                        R.string.permission_not_granted), Toast.LENGTH_LONG).show();
            }
        } else {
            Log.w(TAG, String.format("Unknown permissions request code: %s",
                    requestCode));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STATS_PERMISSION) {
            if (checkUsageStatsPermission(this)) {
                if (checkStoragePermissions()) {
                    if (this.checkResources()) {
                        this.launchMainActivity();
                    } else {
                        // idea: Lead user to PrefsActivity to adjust the settings
                        // but it's rather unlikely that a user deviates from the standard and
                        // knows what to change
                        this.finishAffinity();
                    }
                } else {
                    getStoragePermission();
                }
            } else {
                finishAffinity();
            }
        }
    }

    private void showFatalUiWarning(String message){
        UIUtils.showWarning(this, IntroActivity.TAG, message, (dialog, id) -> {
            this.finishAffinity();
        });
    }

    private boolean canAccessExternalStorage() {
        final File externalStorage = Environment.getExternalStorageDirectory();
        return externalStorage != null && externalStorage.canRead() &&
                externalStorage.canWrite();
    }

    private boolean checkResources() {
        this.handleMessages.showMessage(IntroActivity.TAG, getString(R.string.suCheck));
        boolean goodToGo = true;

        // Initialize the ShellHandler for further root checks
        if (!this.initShellHandler()) {
            this.showFatalUiWarning(this.getString(R.string.busyboxProblem));
            goodToGo = false;
        }

        RootBeer rootBeer = new RootBeer(this);
        if (!rootBeer.isRooted()) {
            this.showFatalUiWarning(this.getString(R.string.noSu));
            goodToGo = false;
        }
        if(goodToGo){
            try {
                ShellHandler.runAsRoot("id");
            } catch (ShellHandler.ShellCommandFailedException e) {
                this.showFatalUiWarning(this.getString(R.string.noSu));
                goodToGo = false;
            }
        }
        this.handleMessages.endMessage();
        return goodToGo;
    }

    private boolean initShellHandler() {
        try {
            IntroActivity.shellHandler = new ShellHandler(this.getApplicationContext());
        } catch (ShellHandler.UtilboxNotAvailableException e) {
            Log.e(IntroActivity.TAG, "Could initialize ShellHandler: " + e.getMessage());
            return false;
        }
        return true;
    }

    private void launchMainActivity() {
        btn.setVisibility(View.GONE);
        String backupDirPath = getDefaultBackupDirPath(this);
        backupDir = createBackupDir(this, backupDirPath);
        startActivity(new Intent(this, MainActivityX.class));
    }

    public static ShellHandler getShellHandlerInstance() {
        return IntroActivity.shellHandler;
    }
}
