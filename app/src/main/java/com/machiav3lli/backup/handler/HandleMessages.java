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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.machiav3lli.backup.Constants;

import java.lang.ref.WeakReference;

// TODO rebase messages handling on AlertDialog as Handler(s) all seem to be (being) deprecated
public class HandleMessages {
    private static final String TAG = Constants.classTag(".HandleMessages");
    private static final int SHOW_DIALOG = 0;
    private static final int DISMISS_DIALOG = 1;
    private static WeakReference<Context> mContext;
    private final ProgressHandler handler;

    public HandleMessages(Context context) {
        /*
         * use weakreference to avoid another memory leak
         * http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
         */
        mContext = new WeakReference<>(context);
        /*
         * the handler is bound to the looper of the thread were it was created
         * it is therefore important to initialize this class on the main thread
         */
        handler = new ProgressHandler();
    }

    public void setMessage(String title, String msg) {
        Message message = Message.obtain(handler, SHOW_DIALOG);
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", msg);
        message.setData(bundle);
        message.sendToTarget();
    }

    public void reShowMessage() {
        setMessage(handler.getTitle(), handler.getMessage());
    }

    public boolean isShowing() {
        return handler.isShowing();
    }

    public void showMessage(String title, String message) {
        setMessage(title, message);
    }

    public void endMessage() {
        Message endMessage = Message.obtain();
        endMessage.what = DISMISS_DIALOG;
        handler.sendMessage(endMessage);
    }

    /**
     * handlers should be static to avoid memory leaks
     * https://groups.google.com/forum/#!msg/android-developers/1aPZXZG6kWk/lIYDavGYn5UJ
     */
    private static class ProgressHandler extends Handler {
        private ProgressDialog progress = null;
        private String title, msg;

        @Override
        public void handleMessage(Message message) {
            title = message.getData().getString("title");
            msg = message.getData().getString("message");
            if (message.what == SHOW_DIALOG) {
                Context context;
                if (progress != null && progress.isShowing()) {
                    progress.setCanceledOnTouchOutside(false);
                    progress.setTitle(title);
                    progress.setMessage(msg);
                } else if ((context = mContext.get()) != null) {
                    // TODO: notice if a BadTokenException might seldomly occur here
                    progress = ProgressDialog.show(context, title, msg, true, false);
                    progress.setCanceledOnTouchOutside(false);
                } else {
                    Log.e(TAG, "context from weakreference is null");
                }
            } else if (message.what == DISMISS_DIALOG && progress != null) {
                try {
                    progress.dismiss();
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, String.format("Could not dismiss dialog: %s", e));
                } finally {
                    progress = null;
                }
            }
        }

        public boolean isShowing() {
            if (progress != null)
                return progress.isShowing();
            return false;
        }

        public String getTitle() {
            if (title != null)
                return title;
            return "";
        }

        public String getMessage() {
            if (msg != null)
                return msg;
            return "";
        }
    }
}
