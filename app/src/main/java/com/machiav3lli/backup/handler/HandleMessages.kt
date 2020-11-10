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
package com.machiav3lli.backup.handler

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.machiav3lli.backup.Constants.classTag
import java.lang.ref.WeakReference

// TODO replace HandleMessages with other ways of communication as Handler(s) seem to be deprecated
class HandleMessages(context: Context) {
    private val handler: ProgressHandler
    fun setMessage(title: String?, msg: String?) {
        val message = Message.obtain(handler, SHOW_DIALOG)
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("message", msg)
        message.data = bundle
        message.sendToTarget()
    }

    fun showMessage(title: String?, message: String?) {
        setMessage(title, message)
    }

    fun endMessage() {
        val endMessage = Message.obtain()
        endMessage.what = DISMISS_DIALOG
        handler.sendMessage(endMessage)
    }

    /**
     * handlers should be static to avoid memory leaks
     * https://groups.google.com/forum/#!msg/android-developers/1aPZXZG6kWk/lIYDavGYn5UJ
     */
    private class ProgressHandler : Handler() {
        private var progress: ProgressDialog? = null
        private var title: String? = null
        private var msg: String? = null
        override fun handleMessage(message: Message) {
            title = message.data.getString("title")
            msg = message.data.getString("message")
            val context: Context? = mContext.get()
            if (message.what == SHOW_DIALOG) {
                if (progress != null && progress!!.isShowing) {
                    progress!!.setCanceledOnTouchOutside(false)
                    progress!!.setTitle(title)
                    progress!!.setMessage(msg)
                } else if (context != null) {
                    // TODO: notice if a BadTokenException might seldomly occur here
                    progress = ProgressDialog.show(context, title, msg, true, false)
                    progress!!.setCanceledOnTouchOutside(false)
                } else {
                    Log.e(TAG, "context from weakreference is null")
                }
            } else if (message.what == DISMISS_DIALOG && progress != null) {
                try {
                    progress!!.dismiss()
                } catch (e: IllegalArgumentException) {
                    Log.e(TAG, String.format("Could not dismiss dialog: %s", e))
                } finally {
                    progress = null
                }
            }
        }
    }

    companion object {
        private val TAG = classTag(".HandleMessages")
        private const val SHOW_DIALOG = 0
        private const val DISMISS_DIALOG = 1
        private lateinit var mContext: WeakReference<Context>
    }

    init {
        /*
         * use weakreference to avoid another memory leak
         * http://www.androiddesignpatterns.com/2013/01/inner-class-handler-memory-leak.html
         */
        mContext = WeakReference(context)
        /*
         * the handler is bound to the looper of the thread were it was created
         * it is therefore important to initialize this class on the main thread
         */handler = ProgressHandler()
    }
}