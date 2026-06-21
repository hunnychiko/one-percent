package com.hunnychiko.baekbunuil

import android.app.Application
import com.google.firebase.FirebaseApp

class BaekbunuilApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
