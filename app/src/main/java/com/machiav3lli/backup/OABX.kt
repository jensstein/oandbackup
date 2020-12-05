package com.machiav3lli.backup

import android.app.Application
import timber.log.Timber

class OABX : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}