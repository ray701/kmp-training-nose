package org.example.kmp.training

import android.app.Application
import org.example.kmp.training.feature.location.AndroidAppContext

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidAppContext.init(this)
    }
}