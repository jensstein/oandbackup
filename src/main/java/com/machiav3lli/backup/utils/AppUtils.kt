package com.machiav3lli.backup.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.machiav3lli.backup.NeoApp
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import kotlin.system.exitProcess

fun Context.restartApp(data: String? = null) {
    Timber.w(
        "restarting application${
            data?.let { " at $data" } ?: { "" }
        }"
    )
    val context = this.applicationContext

    context.packageManager
        ?.getLaunchIntentForPackage(context.packageName)
        ?.let { intent ->

            // finish all activities
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            // eventually start with navigation data
            if (data != null)
                intent.setData(Uri.parse(data))

            context.startActivity(intent)

            exitProcess(0)
        }

    exitProcess(99)
}

var recreateActivitiesJob: Job? = null

fun recreateActivities() {
    runBlocking {
        recreateActivitiesJob?.cancel()
    }
    recreateActivitiesJob = MainScope().launch {
        //Timber.w("recreating activities...")
        delay(500)
        Timber.w(
            "recreating activities ${
                NeoApp.activities.map {
                    "${it.javaClass.simpleName}@${Integer.toHexString(it.hashCode())}"
                }.joinToString(" ")
            }"
        )
        NeoApp.activities
            .forEach {
                runCatching {
                    it.recreate()
                }
            }
    }
}

fun Context.launchView(url: String) {
    startActivity(
        Intent(
            Intent.ACTION_VIEW,
            url.toUri()
        )
    )
}
