package com.walkroute.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WalkRouteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
