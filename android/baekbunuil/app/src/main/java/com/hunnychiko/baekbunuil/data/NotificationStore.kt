package com.hunnychiko.baekbunuil.data

import android.content.Context
import android.content.SharedPreferences
import com.hunnychiko.baekbunuil.data.model.AppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

object NotificationStore {
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    private var prefs: SharedPreferences? = null
    private const val PREFS_NAME = "notif_store"
    private const val KEY_NOTIFS = "notifs"
    private const val MAX_COUNT = 50

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _notifications.value = load()
    }

    fun add(notification: AppNotification) {
        val current = _notifications.value.toMutableList()
        current.add(0, notification)
        if (current.size > MAX_COUNT) current.removeAt(current.lastIndex)
        _notifications.value = current
        save(current)
    }

    fun markAllRead() {
        val updated = _notifications.value.map { it.copy(isRead = true) }
        _notifications.value = updated
        save(updated)
    }

    val unreadCount: Int get() = _notifications.value.count { !it.isRead }

    private fun toJson(n: AppNotification) = JSONObject().apply {
        put("id", n.id)
        put("title", n.title)
        put("body", n.body)
        put("type", n.type)
        put("roomId", n.roomId)
        put("createdAt", n.createdAt)
        put("isRead", n.isRead)
    }

    private fun fromJson(o: JSONObject) = AppNotification(
        id = o.optString("id"),
        title = o.optString("title"),
        body = o.optString("body"),
        type = o.optString("type"),
        roomId = o.optString("roomId"),
        createdAt = o.optLong("createdAt"),
        isRead = o.optBoolean("isRead")
    )

    private fun save(list: List<AppNotification>) {
        val arr = JSONArray()
        list.forEach { arr.put(toJson(it)) }
        prefs?.edit()?.putString(KEY_NOTIFS, arr.toString())?.apply()
    }

    private fun load(): List<AppNotification> {
        val json = prefs?.getString(KEY_NOTIFS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        } catch (_: Exception) { emptyList() }
    }
}
