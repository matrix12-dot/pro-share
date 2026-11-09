package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.MainActivity
import com.example.R
import com.example.data.model.ReceivedFile
import com.example.data.model.SharedFile
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {

    private const val CHANNEL_ID = "swiftshare_transfer_channel"
    private const val CHANNEL_NAME = "إشعارات النقل والإرسال"
    private const val CHANNEL_DESC = "تنبيهات عند إرسال أو استلام الملفات والنصوص مع الحاسوب"
    private val notificationIdCounter = AtomicInteger(1000)

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 150, 100, 150)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun notifyFileReceived(context: Context, file: ReceivedFile) {
        if (!canPostNotifications(context)) return

        createNotificationChannel(context)

        // PendingIntent to directly open the received file
        val openIntent = try {
            val localFile = File(file.filePath)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                localFile
            )
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, file.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (e: Exception) {
            Intent(context, MainActivity::class.java)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationIdCounter.incrementAndGet(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("📥 تم استلام ملف جديد من الحاسوب")
            .setContentText("${file.name} (${file.formattedSize})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("تم حفظ الملف بنجاح في هاتفك:\n${file.name}\nالحجم: ${file.formattedSize}\nاضغط هنا لفتح الملف مباشرة.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter.incrementAndGet(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun notifyFileSent(context: Context, file: SharedFile) {
        if (!canPostNotifications(context)) return

        createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationIdCounter.incrementAndGet(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("📤 تم تنزيل الملف على الحاسوب")
            .setContentText("${file.name} (${file.formattedSize})")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("تم إكمال إرسال الملف إلى متصفح الحاسوب بنجاح:\n${file.name}\nالحجم: ${file.formattedSize}")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter.incrementAndGet(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun notifyZipSent(context: Context, count: Int) {
        if (!canPostNotifications(context)) return

        createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationIdCounter.incrementAndGet(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("📦 تم تنزيل ملف ZIP على الحاسوب")
            .setContentText("تم إرسال حزمة من $count ملفات مضغوطة بنجاح")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter.incrementAndGet(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun notifyClipboardReceived(context: Context, text: String) {
        if (!canPostNotifications(context)) return

        createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationIdCounter.incrementAndGet(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val preview = if (text.length > 60) text.take(60) + "..." else text
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentTitle("📋 تم نسخ نص جديد من الحاسوب")
            .setContentText(preview)
            .setStyle(NotificationCompat.BigTextStyle().bigText("تم نسخ النص من الحاسوب لحافظتك:\n$text"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationIdCounter.incrementAndGet(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
