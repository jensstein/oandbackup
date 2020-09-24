package com.machiav3lli.backup.handler;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.machiav3lli.backup.Constants;

import java.io.FileNotFoundException;
import java.util.ArrayList;

@RequiresApi(26)
public class StorageFile {
    static final String TAG = Constants.classTag(".StorageFile");

    @Nullable
    private final StorageFile parent;
    private final Context context;
    private Uri uri;
    private String name;

    protected StorageFile(@Nullable StorageFile parent, Context context, Uri uri) {
        this.parent = parent;
        this.context = context;
        this.uri = uri;
    }

    public static StorageFile fromUri(@NonNull Context context, @NonNull Uri uri) {
        // Todo: Figure out what's wrong with the Uris coming from the intend and why they need to be processed with DocumentsContract.buildDocumentUriUsingTree(value, DocumentsContract.getTreeDocumentId(value)) first
        return new StorageFile(null, context, uri);
    }

    @Nullable
    public StorageFile createDirectory(@NonNull String displayName) {
        final Uri result = StorageFile.createFile(
                this.context, this.uri, DocumentsContract.Document.MIME_TYPE_DIR, displayName);
        return (result != null) ? new StorageFile(this, context, result) : null;
    }

    public StorageFile createFile(@NonNull String mimeType, @NonNull String displayName) {
        final Uri result = StorageFile.createFile(this.context, this.uri, mimeType, displayName);
        return (result != null) ? new StorageFile(this, this.context, result) : null;
    }

    private static Uri createFile(Context context, Uri self, String mimeType, String displayName) {
        try {
            return DocumentsContract.createDocument(context.getContentResolver(), self, mimeType, displayName);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public boolean delete() {
        try {
            return DocumentsContract.deleteDocument(this.context.getContentResolver(), this.uri);
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    @Nullable
    public StorageFile findFile(@NonNull String displayName) {
        try {
            for (StorageFile doc : this.listFiles()) {
                if (displayName.equals(doc.getName())) {
                    return doc;
                }
            }
        } catch (FileNotFoundException e) {
            return null;
        }
        return null;
    }

    public StorageFile[] listFiles() throws FileNotFoundException {
        if(!this.exists()){
            throw new FileNotFoundException("File " + this.uri + " does not exist");
        }
        final ContentResolver resolver = this.context.getContentResolver();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(this.uri,
                DocumentsContract.getDocumentId(this.uri));
        final ArrayList<Uri> results = new ArrayList<>();
        Cursor cursor = null;
        //noinspection OverlyBroadCatchBlock
        try {
            cursor = resolver.query(childrenUri, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID}, null, null, null);
            while (cursor.moveToNext()) {
                final String documentId = cursor.getString(0);
                final Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(this.uri,
                        documentId);
                results.add(documentUri);
            }
        } catch (Exception e) {
            Log.w(StorageFile.TAG, "Failed query: " + e);
        } finally {
            StorageFile.closeQuietly(cursor);
        }
        final Uri[] result = results.toArray(new Uri[0]);
        final StorageFile[] resultFiles = new StorageFile[result.length];
        for (int i = 0; i < result.length; i++) {
            resultFiles[i] = new StorageFile(this, this.context, result[i]);
        }
        return resultFiles;
    }

    public boolean renameTo(String displayName) {
        //noinspection OverlyBroadCatchBlock
        try {
            final Uri result = DocumentsContract.renameDocument(
                    this.context.getContentResolver(), this.uri, displayName);
            if (result != null) {
                this.uri = result;
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Uri getUri() {
        return this.uri;
    }

    public String getName() {
        if(this.name == null)
            this.name = DocumentContractApi.getName(this.context, this.uri);
        return this.name;
    }

    public StorageFile getParentFile() {
        return this.parent;
    }

    public boolean isFile() {
        return DocumentContractApi.isFile(this.context, this.uri);
    }

    public boolean isDirectory() {
        return DocumentContractApi.isDirectory(this.context, this.uri);
    }

    public boolean exists() {
        return DocumentContractApi.exists(this.context, this.uri);
    }

    private static void closeQuietly(@Nullable AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                //noinspection ProhibitedExceptionThrown
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public String toString() {
        return DocumentsContract.getDocumentId(this.uri);
    }
}
