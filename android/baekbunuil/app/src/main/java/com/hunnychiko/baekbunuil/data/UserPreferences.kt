package com.hunnychiko.baekbunuil.data

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    var avatarId: Int
        get() = prefs?.getInt("avatar_id", 0) ?: 0
        set(value) { prefs?.edit()?.putInt("avatar_id", value)?.apply() }

    var photoUri: String
        get() = prefs?.getString("photo_uri", "") ?: ""
        set(value) { prefs?.edit()?.putString("photo_uri", value)?.apply() }
}
