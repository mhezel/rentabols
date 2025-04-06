package com.mhez_dev.rentabols_v1

import android.app.Application
import com.google.firebase.FirebaseApp
import com.mhez_dev.rentabols_v1.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class RentabolsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Koin
        startKoin {
            androidLogger()
            androidContext(this@RentabolsApp)
            modules(appModule)
        }
    }
}
