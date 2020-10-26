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
package com.machiav3lli.backup.utils;

import android.content.Context;
import android.net.Uri;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.StorageFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class LogUtils {
    private static final String TAG = Constants.classTag(".LogUtils");
    Uri logFile;
    Context context;

    public LogUtils(Context context) throws FileUtils.BackupLocationIsAccessibleException, PrefUtils.StorageLocationNotConfiguredException {
        StorageFile backupRootFolder = StorageFile.fromUri(context, FileUtils.getBackupDir(context));
        StorageFile logDocumentFile = backupRootFolder.findFile(FileUtils.LOG_FILE_NAME);
        if (logDocumentFile == null || !logDocumentFile.exists()) {
            logDocumentFile = backupRootFolder.createFile("application/octet-stream", FileUtils.LOG_FILE_NAME);
            assert logDocumentFile != null;
        }
        this.logFile = logDocumentFile.getUri();
        this.context = context;
    }

    public Uri getLogFile() {
        return this.logFile;
    }

    public void writeToLogFile(String log) throws IOException {
        try (BufferedWriter logWriter = FileUtils.openFileForWriting(this.context, this.logFile, "wa")) {
            logWriter.write(log);
        }
    }

    public String readFromLogFile() throws IOException {
        StringBuilder stringBuilder;
        try (BufferedReader logReader = FileUtils.openFileForReading(this.context, this.logFile)) {
            String line;
            stringBuilder = new StringBuilder();
            while ((line = logReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}
