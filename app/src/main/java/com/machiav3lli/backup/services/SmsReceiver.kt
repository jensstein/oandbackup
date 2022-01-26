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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ContentValues
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsMessage

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if(context == null || intent == null || intent.action == null || intent.extras == null){
            return
        }
        if (intent.action != (Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            return
        }
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (message in smsMessages) {
            putSmsToDatabase( context, message )
        }
    }

    private fun putSmsToDatabase(context: Context, sms: SmsMessage) {
        val contentResolver = context.contentResolver
        val values = ContentValues()
        values.put( "address", sms.originatingAddress)
        values.put( "date", sms.timestampMillis)
        values.put( "read", 0 );
        values.put( "status", sms.status)
        values.put( "type", 1 )
        values.put( "seen", 0 )
        values.put( "body", sms.messageBody.toString() );
        contentResolver.insert( Uri.parse( "content://sms" ), values );
    }
}
