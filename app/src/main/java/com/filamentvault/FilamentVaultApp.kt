package com.filamentvault

import android.app.Application
import com.filamentvault.fleet.FleetReporter
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FilamentVaultApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Crash / freeze / error reports for FleetGuardian (dev hub contract section 6).
        FleetReporter.install(this)
    }
}
