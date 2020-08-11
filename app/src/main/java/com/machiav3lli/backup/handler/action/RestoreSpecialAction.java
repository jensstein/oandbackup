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
package com.machiav3lli.backup.handler.action;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.machiav3lli.backup.Constants;
import com.machiav3lli.backup.handler.Crypto;
import com.machiav3lli.backup.handler.ShellHandler;
import com.machiav3lli.backup.handler.StorageFile;
import com.machiav3lli.backup.handler.TarUtils;
import com.machiav3lli.backup.items.AppInfoV2;
import com.machiav3lli.backup.items.BackupProperties;
import com.machiav3lli.backup.items.SpecialAppMetaInfo;
import com.machiav3lli.backup.utils.PrefUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RestoreSpecialAction extends RestoreAppAction {
    private static final String TAG = Constants.classTag(".RestoreSpecialAction");

    public RestoreSpecialAction(Context context, ShellHandler shell) {
        super(context, shell);
    }

    private static boolean areBasefilesSubsetOf(File[] set, File[] subsetList) {
        Collection<String> baseCollection = Arrays.stream(set).map(File::getName).collect(Collectors.toCollection(HashSet::new));
        Collection<String> subsetCollection = Arrays.stream(subsetList).map(File::getName).collect(Collectors.toCollection(HashSet::new));
        return baseCollection.containsAll(subsetCollection);
    }

    @Override
    protected void restoreAllData(AppInfoV2 app, BackupProperties backupProperties, Uri backupLocation) throws Crypto.CryptoSetupException, RestoreFailedException {
        this.restoreData(app, backupProperties, StorageFile.fromUri(this.getContext(), backupLocation));
    }

    @Override
    public void restoreData(AppInfoV2 app, BackupProperties backupProperties, StorageFile backupLocation) throws RestoreFailedException, Crypto.CryptoSetupException {
        Log.i(RestoreSpecialAction.TAG, String.format("%s: Restore special data", app));
        SpecialAppMetaInfo metaInfo = (SpecialAppMetaInfo) app.getAppInfo();
        File tempPath = new File(this.getContext().getCacheDir(), backupProperties.getPackageName());

        boolean isEncrypted = PrefUtils.isEncryptionEnabled(this.getContext());
        String backupArchiveFilename = this.getBackupArchiveFilename(BaseAppAction.BACKUP_DIR_DATA, isEncrypted);
        StorageFile backupArchiveFile = backupLocation.findFile(backupArchiveFilename);
        if (backupArchiveFile == null) {
            throw new RestoreFailedException("Backup archive at " + backupArchiveFilename + " is missing");
        }

        try (TarArchiveInputStream archive = this.openArchiveFile(backupArchiveFile.getUri(), isEncrypted)) {
            tempPath.mkdir();
            // Extract the contents to a temporary directory
            TarUtils.suUncompressTo(archive, tempPath.getAbsolutePath());

            // check if all expected files are there
            File[] filesInBackup = tempPath.listFiles();
            File[] expectedFiles = Arrays.stream(metaInfo.getFileList()).map(File::new).toArray(File[]::new);
            if (filesInBackup != null && (filesInBackup.length != expectedFiles.length || !RestoreSpecialAction.areBasefilesSubsetOf(expectedFiles, filesInBackup))) {
                String errorMessage = String.format(
                        "%s: Backup is missing files. Found %s; needed: %s",
                        app, Arrays.toString(filesInBackup), Arrays.toString(expectedFiles));
                Log.e(RestoreSpecialAction.TAG, errorMessage);
                throw new RestoreFailedException(errorMessage, null);
            }

            List<String> commands = new ArrayList<>(expectedFiles.length);
            for (File restoreFile : expectedFiles) {
                commands.add(this.prependUtilbox(String.format(
                        "mv -f \"%s\" \"%s\"",
                        new File(tempPath, restoreFile.getName()),
                        restoreFile)));
            }
            String command = String.join(" && ", commands);
            ShellHandler.runAsRoot(command);

        } catch (ShellHandler.ShellCommandFailedException e) {
            String error = BaseAppAction.extractErrorMessage(e.getShellResult());
            Log.e(RestoreSpecialAction.TAG, String.format(
                    "%s: Restore %s failed. System might be inconsistent: %s", app, BaseAppAction.BACKUP_DIR_DATA, error));
            throw new RestoreFailedException(error, e);
        } catch (FileNotFoundException e) {
            throw new RestoreFailedException("Could not find backup archive", e);
        } catch (IOException e) {
            Log.e(RestoreSpecialAction.TAG, String.format(
                    "%s: Restore %s failed with IOException. System might be inconsistent: %s", app, BaseAppAction.BACKUP_DIR_DATA, e));
            throw new RestoreFailedException("IOException", e);
        } finally {
            boolean backupDeleted = FileUtils.deleteQuietly(tempPath);
            Log.d(RestoreSpecialAction.TAG, String.format(
                    "%s: Uncompressed %s was deleted: %s", app, BaseAppAction.BACKUP_DIR_DATA, backupDeleted));
        }
    }

    @Override
    public void restorePackage(Uri backupLocation, BackupProperties backupProperties) {
        // stub
    }

    @Override
    public void restoreDeviceProtectedData(AppInfoV2 app, BackupProperties backupProperties, StorageFile backupLocation) {
        // stub
    }

    @Override
    public void restoreExternalData(AppInfoV2 app, BackupProperties backupProperties, StorageFile backupLocation) {
        // stub
    }

    @Override
    public void restoreObbData(AppInfoV2 app, BackupProperties backupProperties, StorageFile backupLocation) {
        // stub
    }
}
