package com.machiav3lli.backup.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.R;
import com.machiav3lli.backup.handler.AppInfoHelper;
import com.machiav3lli.backup.handler.AssetsHandler;
import com.machiav3lli.backup.handler.HandleMessages;
import com.machiav3lli.backup.handler.ShellCommands;
import com.machiav3lli.backup.handler.Utils;
import com.machiav3lli.backup.items.AppInfo;
import com.scottyab.rootbeer.RootBeer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;
import static com.machiav3lli.backup.handler.FileCreationHelper.getDefaultBackupDirPath;

public class IntroActivity extends BaseActivity {
    static final String TAG = Constants.TAG;
    public static ArrayList<AppInfo> originalList;

    SharedPreferences prefs;
    ArrayList<String> users;
    ShellCommands shellCommands;
    HandleMessages handleMessages;
    public static File backupDir;

    @BindView(R.id.action)
    MaterialButton btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setDayNightTheme(Utils.getPrefsString(this, Constants.PREFS_THEME));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        ButterKnife.bind(this);

        Utils.logDeviceInfo(this, Constants.TAG);
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
        else new AssetHandlerTask().execute(this);
    }

    private void getPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1337);
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1337) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        handleMessages.changeMessage(TAG, getString(R.string.oabUtilsCheck));
        if (!checkOabUtils()) Utils.showWarning(this, TAG, getString(R.string.oabUtilsProblem));
        handleMessages.endMessage();
    }

    private boolean checkBusybox() {
        return (shellCommands.checkBusybox());
    }

    private boolean checkOabUtils() {
        return (shellCommands.checkOabUtils());
    }

    private void launchMainActivity() {
        btn.setVisibility(View.GONE);

        String backupDirPath = getDefaultBackupDirPath(this);
        backupDir = Utils.createBackupDir(this, backupDirPath);
        originalList = AppInfoHelper.getPackageInfo(this, backupDir, true,
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(Constants.PREFS_ENABLESPECIALBACKUPS, true));
        startActivity(new Intent(this, MainActivityX.class));
    }

    private static class AssetHandlerTask extends AsyncTask<Context, Void, Context> {
        private Throwable throwable;

        @Override
        public Context doInBackground(Context... contexts) {
            try {
                AssetsHandler.copyOabutils(contexts[0]);
            } catch (AssetsHandler.AssetsHandlerException e) {
                throwable = e;
            }
            return contexts[0];
        }

        @Override
        public void onPostExecute(Context context) {
            if (throwable != null) {
                Log.e(TAG, String.format(
                        "error during AssetHandlerTask.onPostExecute: %s",
                        throwable.toString()));
                Toast.makeText(context, throwable.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
