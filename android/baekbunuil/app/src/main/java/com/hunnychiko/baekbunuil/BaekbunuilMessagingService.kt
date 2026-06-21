package com.hunnychiko.baekbunuil

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hunnychiko.baekbunuil.data.NotificationStore
import com.hunnychiko.baekbunuil.data.model.AppNotification

class BaekbunuilMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users/$uid/fcmToken").setValue(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: return
        val body  = message.notification?.body  ?: return
        val type  = message.data["type"] ?: ""

        NotificationStore.add(
            AppNotification(
                id = System.currentTimeMillis().toString(),
                title = title,
                body = body,
                type = type,
                roomId = message.data["roomId"] ?: "",
                createdAt = System.currentTimeMillis(),
                isRead = false
            )
        )

        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notif_type",  type)
            putExtra("notif_roomId", message.data["roomId"] ?: "")
            putExtra("notif_claimId", message.data["claimId"] ?: "")
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "백분의일 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "매칭, 당첨, 배송 알림"
            enableVibration(true)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "baekbunuil_main"
    }
}
