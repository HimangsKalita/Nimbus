package com.himangskalita.nimbus

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NimbusApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}