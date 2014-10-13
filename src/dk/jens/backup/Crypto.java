package dk.jens.backup;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openintents.openpgp.IOpenPgpService;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

public class Crypto
{
    final static String TAG = OAndBackup.TAG;
    private OpenPgpServiceConnection service;
    private boolean successFlag, errorFlag, testFlag;
    private File[] files;
    private long[] keyIds;
    private String[] userIds;
    private String provider;
    public Crypto(SharedPreferences prefs)
    {
        userIds = prefs.getString("cryptoUserIds", "").split(",");
        // openkeychain doesn't like it if the string is empty
        if(userIds.length == 1 && userIds[0].length() == 0)
            userIds[0] = "dummy";
        else
            for(int i = 0; i < userIds.length; i++)
                userIds[i] = userIds[i].trim();
        provider = prefs.getString("openpgpProviderList", "org.sufficientlysecure.keychain");
    }
    public void testResponse(Activity activity, Intent intent, long[] keyIds)
    {
        /*
         * this method is only used to cause the user interaction screen
         * to be displayed if necessary before looping over the files to
         * to be de/encrypted since that would otherwise trigger it multiple
         * times.
         */
        java.io.InputStream is = new java.io.ByteArrayInputStream(new byte[]{0});
        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        intent.setAction(OpenPgpApi.ACTION_ENCRYPT);
        // this way the key ids are remembered if the user ids are unknown
        // and a user interaction screen is shown
        if(keyIds != null)
            this.keyIds = keyIds;
        intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, userIds);
        OpenPgpApi api = new OpenPgpApi(activity, service.getService());
        Intent result = api.executeApi(intent, is, os);
        handleResult(activity, result, BaseActivity.OPENPGP_REQUEST_TESTRESPONSE);
    }
    public void bind(Context context)
    {
        service = new OpenPgpServiceConnection(context, provider, new OpenPgpServiceConnection.OnBound()
            {
                @Override
                public void onBound(IOpenPgpService service)
                {
                    Log.i(TAG, "openpgp-api service bound");
                }
                @Override
                public void onError(Exception e)
                {
                    Log.e(TAG, "couldn't bind openpgp service: " + e.toString());
                }
            }
        );
        service.bindToService();
    }
    public void unbind()
    {
        if(service != null)
            service.unbindFromService();
    }
    public void decryptFiles(Activity activity, File... filesList)
    {
        Intent intent = new Intent(OpenPgpApi.ACTION_DECRYPT_VERIFY);
        handleFiles(activity, intent, BaseActivity.OPENPGP_REQUEST_DECRYPT, filesList);
    }
    public void decryptFiles(Activity activity, File file)
    {
        decryptFiles(activity, new File[]{file});
    }
    public void encryptFiles(Activity activity, File... filesList)
    {
        Intent intent = new Intent(OpenPgpApi.ACTION_ENCRYPT);
        intent.putExtra(OpenPgpApi.EXTRA_USER_IDS, userIds);
        handleFiles(activity, intent, BaseActivity.OPENPGP_REQUEST_ENCRYPT, filesList);
    }
    public void encryptFiles(Activity activity, File file)
    {
        encryptFiles(activity, new File[] {file});
    }
    public void handleFiles(Activity activity, Intent intent, int requestCode, File... filesList)
    {
        waitForServiceBound();
        /*
         * a more elegant solution would be to set the filenames as an extra
         * in the intent, but that doesn't work since any extras which
         * OpenPgpApi doesn't know about seem to be removed if it goes
         * through the user interaction phase.
         */
        files = filesList;
        doAction(activity, intent, requestCode);
    }
    public void doAction(Activity activity, Intent intent, int requestCode)
    {
        if(!testFlag)
        {
            testResponse(activity, new Intent(), null);
            waitForResult();
        }
        try
        {
            if(files != null)
            {
                if(requestCode == BaseActivity.OPENPGP_REQUEST_ENCRYPT && keyIds != null)
                    intent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, keyIds);
                for(File file : files)
                {
                    String outputFilename;
                    if(requestCode == BaseActivity.OPENPGP_REQUEST_DECRYPT)
                        outputFilename = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".gpg"));
                    else
                        outputFilename = file.getAbsolutePath() + ".gpg";
                    Log.i(TAG, "crypto input: " + file.getAbsolutePath() + " output: " + outputFilename);
                    FileInputStream is = new FileInputStream(file);
                    FileOutputStream os = new FileOutputStream(outputFilename);
                    OpenPgpApi api = new OpenPgpApi(activity, service.getService());
                    Intent result = api.executeApi(intent, is, os);
                    handleResult(activity, result, requestCode);
                    waitForResult();
                    os.close();
                }
            }
            else
            {
                Log.e(TAG, "Crypto: no files to de/encrypt");
            }
        }
        catch(IOException e)
        {
            Log.e(TAG, "Crypto error: " + e.toString());
        }
    }
    public void cancel()
    {
        errorFlag = true;
        Log.i(TAG, "Crypto action was cancelled");
    }
    public void setError()
    {
        // to be used if the openpgp provider crashes so there isn't any usable callback
        errorFlag = true;
        Log.e(TAG, "Crypto error set. Did the openpgp provider crash?");
    }
    private boolean waitForServiceBound()
    {
        int i = 0;
        while(service.getService() == null)
        {
            try
            {
                if(i % 20 == 0)
                    Log.i(TAG, "waiting for openpgp-api service to be bound");
                Thread.sleep(100);
                if(i > 1000)
                    break;
                i++;
            }
            catch(InterruptedException e)
            {
                Log.e(TAG, "Crypto.waitForServiceBound interrupted");
            }
        }
        return service.getService() != null;
    }
    private void waitForResult()
    {
        try
        {
            int i = 0;
            while(successFlag == false && errorFlag == false)
            {
                if(i % 200 == 0)
                    Log.i(TAG, "waiting for openpgp-api user interaction");
                Thread.sleep(100);
                if(i > 1000)
                    break;
                i++;
            }
        }
        catch(InterruptedException e)
        {
            Log.e(TAG, "Crypto.waitForResult interrupted");
        }
    }
    private void handleResult(Activity activity, Intent result, int requestCode)
    {
        successFlag = false;
        errorFlag = false;
        switch(result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR))
        {
        case OpenPgpApi.RESULT_CODE_SUCCESS:
            testFlag = true;
            successFlag = true;
            break;
        case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED:
            PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
            try
            {
                activity.startIntentSenderFromChild(activity, pi.getIntentSender(), requestCode, null, 0, 0, 0);
            }
            catch(IntentSender.SendIntentException e)
            {
                errorFlag = true;
                Log.e(TAG, "Crypto.handleResult error: " + e.toString());
            }
            break;
        case OpenPgpApi.RESULT_CODE_ERROR:
            OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
            Log.e(TAG, "Crypto.handleResult error id: " + error.getErrorId());
            Log.e(TAG, "Crypto.handleResult error message: " + error.getMessage());
            errorFlag = true;
            break;
        }
    }
}
