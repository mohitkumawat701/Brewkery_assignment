package com.brewkery.app

import android.app.Application
import com.brewkery.app.di.AppContainer

class BrewkeryApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
