package com.machiav3lli.backup.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.AssetsHandler;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.Utils;
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
import static com.machiav3lli.backup.handler.FileCreationHelper.getDefaultBackupDirPath;

public class IntroActivity extends BaseActivity {
    static final String TAG = Constants.classTag(".IntroActivity");
    static final int READ_PERMISSION = 2;
    static final int WRITE_PERMISSION = 3;

    @BindView(R.id.action)
    MaterialButton btn;

    public static File backupDir;
    SharedPreferences prefs;
    ArrayList<String> users;
    ShellCommands shellCommands;
    HandleMessages handleMessages;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setDayNightTheme(Utils.getPrefsString(this, Constants.PREFS_THEME));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        Utils.logDeviceInfo(this, TAG);
        prefs = this.getSharedPreferences(Constants.PREFS_SHARED, Context.MODE_PRIVATE);
        users = new ArrayList<>();
        checkRun(savedInstanceState);
        shellCommands = new ShellCommands(this, prefs, users, getFilesDir());
        handleMessages = new HandleMessages(this);

        if (!checkPermissions())
            btn.setOnClickListener(v -> getPermissions());
        else {
            btn.setVisibility(View.GONE);
            checkResources();
            launchMainActivity();
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

    private void getPermissions() {
        requireWriteStoragePermission();
        requireReadStoragePermission();
    }

    private boolean checkPermissions() {
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
                checkResources();
                launchMainActivity();
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

    private boolean canAccessExternalStorage() {
        final File externalStorage = Environment.getExternalStorageDirectory();
        return externalStorage != null && externalStorage.canRead() &&
                externalStorage.canWrite();
    }

    private void checkResources() {
        handleMessages.showMessage(TAG, getString(R.string.suCheck));
        RootBeer rootBeer = new RootBeer(this);
        if (!rootBeer.isRooted()) Utils.showWarning(this, TAG, getString(R.string.noSu));
        if (!checkBusybox()) Utils.showWarning(this, TAG, getString(R.string.busyboxProblem));
        handleMessages.endMessage();
    }

    private boolean checkBusybox() {
        return (shellCommands.checkToybox());
    }

    private void launchMainActivity() {
        btn.setVisibility(View.GONE);
        String backupDirPath = getDefaultBackupDirPath(this);
        backupDir = Utils.createBackupDir(this, backupDirPath);
        startActivity(new Intent(this, MainActivityX.class));
    }
}
