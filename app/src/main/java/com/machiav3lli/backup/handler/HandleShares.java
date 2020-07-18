package com.machiav3lli.backup.handler;

import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HandleShares {
    private static Map<String, String> mimeTypes;

    public static Intent constructIntentSingle(String title, File file, Uri uri) {
        String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase(Locale.ENGLISH);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType(getMimeType(ext));
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(intent, title);
    }

    public static Intent constructIntentMultiple(String title, Uri... files) {
        ArrayList<Uri> uris = new ArrayList<>();
        Collections.addAll(uris, files);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        // assume an apk and a zip of data is being sent
        intent.setType("application/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return Intent.createChooser(intent, title);
    }

    public static String getMimeType(String extension) {
        if (mimeTypes == null) {
            mimeTypes = new HashMap<>();
            mimeTypes.put("apk", "application/vnd.android.package-archive");
            mimeTypes.put("zip", "application/zip");
        }
        if (mimeTypes.containsKey(extension)) {
            return mimeTypes.get(extension);
        } else {
            return "*/*"; // catch-all mimetype
        }
    }
}