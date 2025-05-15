package com.brigadka.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import co.touchlab.kermit.Logger
import com.brigadka.app.MainActivity
import com.brigadka.app.R
import com.brigadka.app.data.repository.PushTokenRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

private val logger = Logger.withTag("BrigadkaFirebaseMessagingService")

class BrigadkaFirebaseMessagingService : FirebaseMessagingService() {

    private val pushTokenRepository: PushTokenRepository by inject()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            logger.d("Message notification payload: ${notification.body}")
            // Handle notification message
            sendNotification(
                title = notification.title ?: "Notification",
                body = notification.body ?: "",
                imageUrl = notification.imageUrl?.toString(),
                data = remoteMessage.data
            )
        } ?: run {
            logger.d("Message data payload: ${remoteMessage.data}")
            // Handle data message if no notification payload
            if (remoteMessage.data.isNotEmpty()) {
                val title = remoteMessage.data["title"] ?: "Notification"
                val body = remoteMessage.data["body"] ?: ""
                val imageUrl = remoteMessage.data["image"]

                sendNotification(title, body, imageUrl, remoteMessage.data)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logger.d("New token received: $token")
        pushTokenRepository.saveToken(token)
    }

    private fun sendNotification(
        title: String,
        body: String,
        imageUrl: String? = null,
        data: Map<String, String> = emptyMap()
    ) {
        logger.d("Sending notification title = $title, body = $body, imageUrl = $imageUrl, data = $data")
        val channelId = "brigadka_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Create intent to open app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            // Add any extra data if needed
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntentFlag =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, pendingIntentFlag
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the notification channel for Android Oreo and above
        val channel = NotificationChannel(
            channelId,
            "Brigadka Channel",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for Brigadka notifications"
            enableLights(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)

        val notificationId = System.currentTimeMillis().toInt()

        // If we have an image URL, load it with Glide and then show the notification with person avatar
        if (!imageUrl.isNullOrEmpty()) {
            // Use RequestBuilder with listener instead of deprecated SimpleTarget
            Glide.with(applicationContext)
                .asBitmap()
                .load(imageUrl)
                .circleCrop() // Transform the image to a circle
                .listener(object : com.bumptech.glide.request.RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // If image loading fails, show notification without image
                        notificationManager.notify(notificationId, notificationBuilder.build())
                        return true
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Create a messaging style notification with the avatar
                        val messagingStyle = NotificationCompat.MessagingStyle(
                            androidx.core.app.Person.Builder()
                                .setName("You")
                                .build()
                        )

                        if (resource == null) {
                            // If resource is null, show notification without image
                            notificationManager.notify(notificationId, notificationBuilder.build())
                            return true
                        }

                        // Create the person with the loaded avatar (already circular from circleCrop())
                        val sender = androidx.core.app.Person.Builder()
                            .setName(title)
                            .setIcon(androidx.core.graphics.drawable.IconCompat.createWithBitmap(resource))
                            .build()

                        // Add the message
                        messagingStyle.addMessage(
                            NotificationCompat.MessagingStyle.Message(
                                body,
                                System.currentTimeMillis(),
                                sender
                            )
                        )

                        notificationBuilder.setStyle(messagingStyle)
                        notificationManager.notify(notificationId, notificationBuilder.build())
                        return true
                    }
                })
                .submit()
        } else {
            // No image, show notification immediately
            notificationManager.notify(notificationId, notificationBuilder.build())
        }
    }
}