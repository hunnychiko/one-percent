package com.hunnychiko.baekbunuil

import android.app.Application
import com.google.firebase.FirebaseApp
import com.hunnychiko.baekbunuil.data.NotificationStore
import com.hunnychiko.baekbunuil.data.UserPreferences

class BaekbunuilApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        NotificationStore.init(this)
        UserPreferences.init(this)
    }
}
