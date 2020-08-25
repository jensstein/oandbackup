/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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