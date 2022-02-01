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
package com.machiav3lli.backup.services

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract.PhoneLookup
import android.provider.Telephony
import android.telephony.SmsMessage
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.machiav3lli.backup.activities.MainActivityX
import com.machiav3lli.backup.handler.showNotification
import timber.log.Timber

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.tag("SmsReceiver").v("onReceive")
        if(context == null || intent == null || intent.action == null || intent.extras == null){
            return
        }
        if (intent.action != (Telephony.Sms.Intents.SMS_DELIVER_ACTION)) {
            return
        }
        if (
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.READ_SMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.SEND_SMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECEIVE_MMS) == PermissionChecker.PERMISSION_DENIED) ||
                (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECEIVE_WAP_PUSH) == PermissionChecker.PERMISSION_DENIED)
        ) {
            return
        }
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (message in smsMessages) {
            putSmsToDatabase( context, message )
        }
    }

    private fun putSmsToDatabase(context: Context, sms: SmsMessage) {
        Timber.tag("SmsReceiver").v("putSmsToDatabase")
        val contentResolver = context.contentResolver
        val values = ContentValues()
        val notificationId = System.currentTimeMillis()
        val message = sms.displayMessageBody.toString()
        var sender = sms.displayOriginatingAddress ?: ""

        if (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.READ_CONTACTS) == PermissionChecker.PERMISSION_DENIED) {
            val uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(sender))
            val projection = arrayOf(PhoneLookup.DISPLAY_NAME)
            try {
                val cursor = contentResolver.query(uri, projection, null, null, null)
                cursor.use {
                    if (cursor?.moveToFirst() == true) {
                        if (cursor.getString(0) != "") {
                            sender = cursor.getString(0)
                        }
                    }
                }
            } catch (e: Exception) {}
        }

        values.put( "address", sms.displayOriginatingAddress)
        values.put( "date", sms.timestampMillis)
        values.put( "read", 0 )
        values.put( "status", sms.status)
        values.put( "type", 1 )
        values.put( "seen", 0 )
        values.put( "body", message )
        values.put( "subject", sms.pseudoSubject)
        contentResolver.insert( Telephony.Sms.CONTENT_URI, values )
        showNotification(
            context, MainActivityX::class.java, notificationId.toInt(),
                sender, message, true
        )
    }
}
