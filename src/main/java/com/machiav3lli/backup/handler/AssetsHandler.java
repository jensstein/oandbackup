package com.machiav3lli.backup.handler;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.machiav3lli.backup.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AssetsHandler {
    final static String TAG = Constants.classTag(".AssetHandler");

    /**
     * copy bytes from assets file to an application private file
     *
     * @param context        application context
     * @param assetPath      path of asset to copy
     * @param outputFilename filename of private file to write
     * @throws AssetsHandlerException on error copying the asset
     */
    public static void copyAsset(Context context, String assetPath,
                                 String outputFilename) throws AssetsHandlerException {
        try (final FileOutputStream outputStream = context.openFileOutput(
                outputFilename, Context.MODE_PRIVATE);
             final InputStream is = context.getAssets().open(assetPath)) {
            Log.d(TAG, String.format("copying asset %s",
                    assetPath));

            byte[] buffer = new byte[16384];
            int read;
            while ((read = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            Log.d(TAG, String.format("copied asset %s",
                    assetPath));
        } catch (IOException e) {
            final String msg = String.format("error copying asset %s",
                    assetPath);
            throw new AssetsHandlerException(msg, e);
        }
    }

    public static String getAbi() {
        return Build.SUPPORTED_ABIS[0];
    }

    public static class AssetsHandlerException extends Exception {
        private AssetsHandlerException(String msg) {
            super(msg);
        }

        private AssetsHandlerException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
