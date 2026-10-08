package com.clickretina.brewkery

import android.app.Application

class BrewkeryApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
