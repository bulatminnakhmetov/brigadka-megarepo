package com.brigadka.app

import android.app.Application
import com.brigadka.app.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class BrigadkaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidLogger(Level.ERROR)
            androidContext(applicationContext)
        }
    }
}