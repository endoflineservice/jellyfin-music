package dev.cholt.jellyfinmusic

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Collections
import kotlin.concurrent.thread
import kotlin.math.max

class MusicWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            manager.updateAppWidget(id, buildRemoteViews(context, R.layout.widget_music_card))
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, MusicWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { id ->
                manager.updateAppWidget(id, buildRemoteViews(context, R.layout.widget_music_card))
            }
        }
    }
}

class CompactMusicWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            manager.updateAppWidget(id, buildRemoteViews(context, R.layout.widget_music_compact))
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, CompactMusicWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { id ->
                manager.updateAppWidget(id, buildRemoteViews(context, R.layout.widget_music_compact))
            }
        }
    }
}

class PillMusicWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            manager.updateAppWidget(id, buildRemoteViews(context, R.layout.widget_music_pill))
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PillMusicWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { id ->
                manager.updateAppWidget(id, buildRemoteViews(context, R.layout.widget_music_pill))
            }
        }
    }
}

fun updateMusicWidgets(context: Context) {
    MusicWidgetProvider.updateAll(context)
    CompactMusicWidgetProvider.updateAll(context)
    PillMusicWidgetProvider.updateAll(context)
}

private fun buildRemoteViews(context: Context, layoutId: Int): RemoteViews {
    val state = loadWidgetState(context)
    return RemoteViews(context.packageName, layoutId).apply {
        setTextViewText(R.id.widget_title, state.title)
        setTextViewText(R.id.widget_artist, state.artist)
        setTextViewText(R.id.widget_status, state.status)
        val playbackVisibility = if (state.hasTrack) View.VISIBLE else View.INVISIBLE
        setViewVisibility(R.id.widget_progress, playbackVisibility)
        setViewVisibility(R.id.widget_play, playbackVisibility)
        setViewVisibility(R.id.widget_previous, playbackVisibility)
        setViewVisibility(R.id.widget_next, playbackVisibility)
        setImageViewResource(
            R.id.widget_play,
            if (state.isPlaying) R.drawable.widget_icon_pause else R.drawable.widget_icon_play
        )
        val artwork = state.imageUrl?.let { loadWidgetArtworkBitmap(context, it) }
        if (artwork != null) {
            setImageViewBitmap(R.id.widget_album, artwork)
        } else {
            setImageViewResource(R.id.widget_album, R.drawable.widget_vinyl)
        }
        setProgressBar(R.id.widget_progress, 1000, (state.progress * 1000).toInt().coerceIn(0, 1000), false)

        val openIntent = Intent(context, MainActivity::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        val openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, flags)
        setOnClickPendingIntent(R.id.widget_root, openPendingIntent)
        setOnClickPendingIntent(R.id.widget_play, playbackPendingIntent(context, PLAYBACK_ACTION_TOGGLE, 1))
        setOnClickPendingIntent(R.id.widget_previous, playbackPendingIntent(context, PLAYBACK_ACTION_PREVIOUS, 2))
        setOnClickPendingIntent(R.id.widget_next, playbackPendingIntent(context, PLAYBACK_ACTION_NEXT, 3))
    }
}

private fun playbackPendingIntent(context: Context, action: String, requestCode: Int): PendingIntent {
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        0
    }
    val intent = Intent(context, PlaybackNotificationReceiver::class.java).apply {
        setAction(action)
    }
    return PendingIntent.getBroadcast(context, requestCode, intent, flags)
}

private data class WidgetState(
    val title: String,
    val artist: String,
    val status: String,
    val storedProgress: Float,
    val isPlaying: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAtMs: Long,
    val imageUrl: String?,
    val hasTrack: Boolean
) {
    val progress: Float
        get() {
            if (!hasTrack || durationMs <= 0L) return storedProgress.coerceIn(0f, 1f)
            val elapsedMs = if (isPlaying) {
                (SystemClock.elapsedRealtime() - updatedAtMs).coerceAtLeast(0L)
            } else {
                0L
            }
            return ((positionMs + elapsedMs).coerceIn(0L, durationMs).toFloat() / durationMs.toFloat())
                .coerceIn(0f, 1f)
        }
}

private fun loadWidgetState(context: Context): WidgetState {
    val prefs = context.getSharedPreferences("jellyfin_music_widget", Context.MODE_PRIVATE)
    val hasTrack = prefs.getBoolean("hasTrack", false)
    val status = prefs.getString("status", null)?.takeIf { it.isNotBlank() } ?: "Ready"
    return WidgetState(
        title = if (hasTrack) {
            prefs.getString("title", null)?.takeIf { it.isNotBlank() } ?: "Unknown song"
        } else {
            "Jellyfin Music"
        },
        artist = if (hasTrack) {
            prefs.getString("artist", null)?.takeIf { it.isNotBlank() } ?: "Unknown artist"
        } else {
            "Tap to open"
        },
        status = status,
        storedProgress = prefs.getFloat("progress", 0f),
        isPlaying = hasTrack && prefs.getBoolean("isPlaying", status.equals("Playing", ignoreCase = true)),
        positionMs = prefs.getLong("positionMs", 0L),
        durationMs = prefs.getLong("durationMs", 0L),
        updatedAtMs = prefs.getLong("updatedAtMs", 0L),
        imageUrl = prefs.getString("imageUrl", null)?.takeIf { hasTrack && it.isNotBlank() },
        hasTrack = hasTrack
    )
}

fun saveWidgetState(context: Context, snapshot: PlaybackSnapshot) {
    val prefs = context.getSharedPreferences("jellyfin_music_widget", Context.MODE_PRIVATE)
    val track = snapshot.track
    val session = snapshot.session
    val imageUrl = if (track != null && session != null) {
        track.imageUrl(session, size = 320, quality = 84)
    } else {
        null
    }
    val previousImageUrl = prefs.getString("imageUrl", null)
    val editor = prefs.edit()
        .putString("title", track?.title ?: "Jellyfin Music")
        .putString("artist", track?.artist ?: "Tap to open")
        .putString("status", if (track == null) "Ready" else snapshot.status)
        .putFloat("progress", snapshot.displayProgress)
        .putBoolean("isPlaying", snapshot.isPlaying)
        .putLong("positionMs", snapshot.positionMs.coerceAtLeast(0L))
        .putLong("durationMs", snapshot.durationMs.coerceAtLeast(0L))
        .putLong("updatedAtMs", snapshot.updatedAtMs)
        .putBoolean("hasTrack", track != null)
    if (imageUrl != null) {
        editor.putString("imageUrl", imageUrl)
    } else {
        editor.remove("imageUrl")
    }
    editor.apply()
    updateMusicWidgets(context)
    if (imageUrl != null && (imageUrl != previousImageUrl || !widgetArtworkFile(context, imageUrl).isFile)) {
        cacheWidgetArtworkAsync(context.applicationContext, imageUrl)
    }
}

private val WidgetArtworkDownloads = Collections.synchronizedSet(mutableSetOf<String>())

private fun cacheWidgetArtworkAsync(context: Context, imageUrl: String) {
    if (!WidgetArtworkDownloads.add(imageUrl)) return
    thread(name = "jellyfin-widget-art", isDaemon = true) {
        runCatching {
            if (!widgetArtworkFile(context, imageUrl).isFile) {
                downloadWidgetArtwork(context, imageUrl)
            }
        }
        WidgetArtworkDownloads.remove(imageUrl)
        updateMusicWidgets(context)
    }
}

private fun loadWidgetArtworkBitmap(context: Context, imageUrl: String): Bitmap? {
    val file = widgetArtworkFile(context, imageUrl)
    if (!file.isFile) return null
    return BitmapFactory.decodeFile(file.absolutePath)
}

private fun downloadWidgetArtwork(context: Context, imageUrl: String) {
    val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 5_000
        readTimeout = 8_000
        setRequestProperty("Accept", "image/*")
    }
    try {
        if (connection.responseCode !in 200..299) return
        val source = connection.inputStream.use { input ->
            val bytes = input.readBytes()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } ?: return
        val widgetBitmap = createWidgetAlbumBitmap(source)
        val file = widgetArtworkFile(context, imageUrl)
        file.parentFile?.mkdirs()
        file.outputStream().use { output ->
            widgetBitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    } finally {
        connection.disconnect()
    }
}

private fun createWidgetAlbumBitmap(source: Bitmap): Bitmap {
    val size = 256
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val rect = RectF(0f, 0f, size.toFloat(), size.toFloat())
    val radius = 42f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    val scale = max(size.toFloat() / source.width.toFloat(), size.toFloat() / source.height.toFloat())
    val matrix = Matrix().apply {
        setScale(scale, scale)
        postTranslate(
            (size - source.width * scale) * 0.5f,
            (size - source.height * scale) * 0.5f
        )
    }
    shader.setLocalMatrix(matrix)
    paint.shader = shader
    canvas.drawRoundRect(rect, radius, radius, paint)

    paint.shader = null
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 5f
    paint.color = Color.argb(90, 255, 255, 255)
    canvas.drawRoundRect(RectF(2.5f, 2.5f, size - 2.5f, size - 2.5f), radius, radius, paint)

    paint.style = Paint.Style.FILL
    paint.color = Color.argb(34, 0, 0, 0)
    canvas.drawRoundRect(rect, radius, radius, paint)
    return output
}

private fun widgetArtworkFile(context: Context, imageUrl: String): File =
    File(File(context.cacheDir, "widget_album_art"), "${widgetCacheKey(imageUrl)}.png")

private fun widgetCacheKey(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}
