package cz.jakubricar.zradelnik

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cz.jakubricar.zradelnik.ui.MainActivity
import java.io.IOException
import java.net.URL

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class ZradelnikFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            val intent = Intent(this, MainActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("recipe_id", remoteMessage.data["recipe_id"])
                }
            val pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)

            val channelId = it.channelId ?: getString(R.string.new_recipes_notification_channel_id)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle(it.title)
                .setContentText(it.body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)

            it.imageUrl?.let { imageUrl ->
                val bitmap = try {
                    val input = URL(imageUrl.toString()).openStream()
                    BitmapFactory.decodeStream(input)
                } catch (e: IOException) {
                    null
                }

                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null)
                ).setLargeIcon(bitmap)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(channelId,
                getString(R.string.new_recipes_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)

            notificationManager.notify(0, notificationBuilder.build())
        }
    }
}
