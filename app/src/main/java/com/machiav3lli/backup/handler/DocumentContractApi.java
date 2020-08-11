package com.machiav3lli.backup.handler;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.machiav3lli.backup.Constants;

@RequiresApi(26)
public class DocumentContractApi {
    static final String TAG = Constants.classTag(".DocumentContractApi");

    @Nullable
    public static String getName(Context context, Uri self) {
        return DocumentContractApi.queryForString(context, self, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null);
    }

    @Nullable
    private static String getRawType(Context context, Uri self) {
        return DocumentContractApi.queryForString(context, self, DocumentsContract.Document.COLUMN_MIME_TYPE, null);
    }

    @Nullable
    public static String getType(Context context, Uri self) {
        final String rawType = DocumentContractApi.getRawType(context, self);
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(rawType)) {
            return null;
        }
        return rawType;
    }

    public static long getFlags(Context context, Uri self) {
        return DocumentContractApi.queryForLong(context, self, DocumentsContract.Document.COLUMN_FLAGS, 0);
    }

    public static boolean isDirectory(Context context, Uri self) {
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(DocumentContractApi.getRawType(context, self));
    }

    public static boolean isFile(Context context, Uri self) {
        final String type = DocumentContractApi.getRawType(context, self);
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type) || TextUtils.isEmpty(type)) {
            return false;
        }
        return true;
    }

    public static long lastModified(Context context, Uri self) {
        return DocumentContractApi.queryForLong(context, self, DocumentsContract.Document.COLUMN_LAST_MODIFIED, 0);
    }

    public static long length(Context context, Uri self) {
        return DocumentContractApi.queryForLong(context, self, DocumentsContract.Document.COLUMN_SIZE, 0);
    }

    public static boolean canRead(Context context, Uri self) {
        // Ignore if grant doesn't allow read
        if (context.checkCallingOrSelfUriPermission(self, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        // Ignore documents without MIME
        return !TextUtils.isEmpty(DocumentContractApi.getRawType(context, self));
    }

    public static boolean canWrite(Context context, Uri self) {
        // Ignore if grant doesn't allow write
        if (context.checkCallingOrSelfUriPermission(self, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        final String type = DocumentContractApi.getRawType(context, self);
        final int flags = DocumentContractApi.queryForInt(context, self, DocumentsContract.Document.COLUMN_FLAGS, 0);
        // Ignore documents without MIME
        if (TextUtils.isEmpty(type)) {
            return false;
        }
        // Deletable documents considered writable
        if ((flags & DocumentsContract.Document.FLAG_SUPPORTS_DELETE) != 0) {
            return true;
        }
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type)
                && (flags & DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE) != 0) {
            // Directories that allow create considered writable
            return true;
        }
        // Writable normal files considered writable
        return !TextUtils.isEmpty(type)
                && (flags & DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0;
    }

    public static boolean exists(Context context, Uri self) {
        final ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(self, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID}, null, null, null);
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.w(DocumentContractApi.TAG, "Failed query: " + e);
            return false;
        } finally {
            DocumentContractApi.closeQuietly(cursor);
        }
    }

    @Nullable
    private static String queryForString(Context context, Uri self, String column,
                                         @Nullable String defaultValue) {
        final ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            //noinspection resource
            cursor = resolver.query(self, new String[]{column}, null, null, null);
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getString(0);
            }
            return defaultValue;
        } catch (Exception e) {
            Log.w(DocumentContractApi.TAG, "Failed query: " + e);
            return defaultValue;
        } finally {
            DocumentContractApi.closeQuietly(cursor);
        }
    }

    private static int queryForInt(Context context, Uri self, String column,
                                   int defaultValue) {
        return (int) DocumentContractApi.queryForLong(context, self, column, defaultValue);
    }

    private static long queryForLong(Context context, Uri self, String column,
                                     long defaultValue) {
        final ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(self, new String[]{column}, null, null, null);
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getLong(0);
            }
            return defaultValue;
        } catch (Exception e) {
            Log.w(DocumentContractApi.TAG, "Failed query: " + e);
            return defaultValue;
        } finally {
            DocumentContractApi.closeQuietly(cursor);
        }
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

    private DocumentContractApi() {
    }
}
