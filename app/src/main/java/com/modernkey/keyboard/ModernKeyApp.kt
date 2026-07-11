package com.modernkey.keyboard

import android.app.Application
import com.modernkey.keyboard.data.db.AppDatabase
import com.modernkey.keyboard.data.prefs.KeyboardPreferences

class ModernKeyApp : Application() {

    companion object {
        lateinit var instance: ModernKeyApp
            private set
    }

    lateinit var database: AppDatabase
        private set

    lateinit var preferences: KeyboardPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        preferences = KeyboardPreferences(this)
    }
}
