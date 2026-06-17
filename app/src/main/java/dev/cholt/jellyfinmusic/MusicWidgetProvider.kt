package dev.cholt.jellyfinmusic

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews

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

fun updateMusicWidgets(context: Context) {
    MusicWidgetProvider.updateAll(context)
    CompactMusicWidgetProvider.updateAll(context)
}

private fun buildRemoteViews(context: Context, layoutId: Int): RemoteViews {
    val state = loadWidgetState(context)
    return RemoteViews(context.packageName, layoutId).apply {
        setTextViewText(R.id.widget_title, state.title)
        setTextViewText(R.id.widget_artist, state.artist)
        setTextViewText(R.id.widget_status, state.status)
        setProgressBar(R.id.widget_progress, 100, (state.progress * 100).toInt().coerceIn(0, 100), false)

        val openIntent = Intent(context, MainActivity::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        val openPendingIntent = PendingIntent.getActivity(context, 0, openIntent, flags)
        setOnClickPendingIntent(R.id.widget_root, openPendingIntent)
        setOnClickPendingIntent(R.id.widget_play, openPendingIntent)
        setOnClickPendingIntent(R.id.widget_previous, openPendingIntent)
        setOnClickPendingIntent(R.id.widget_next, openPendingIntent)
    }
}

private data class WidgetState(
    val title: String,
    val artist: String,
    val status: String,
    val progress: Float
)

private fun loadWidgetState(context: Context): WidgetState {
    val prefs = context.getSharedPreferences("jellyfin_music_widget", Context.MODE_PRIVATE)
    return WidgetState(
        title = prefs.getString("title", null)?.takeIf { it.isNotBlank() } ?: "Jellyfin Music",
        artist = prefs.getString("artist", null)?.takeIf { it.isNotBlank() } ?: "Tap to open",
        status = prefs.getString("status", null)?.takeIf { it.isNotBlank() } ?: "Ready",
        progress = prefs.getFloat("progress", 0f)
    )
}

fun saveWidgetState(context: Context, track: MusicTrack?, status: String, progress: Float) {
    context.getSharedPreferences("jellyfin_music_widget", Context.MODE_PRIVATE)
        .edit()
        .putString("title", track?.title ?: "Jellyfin Music")
        .putString("artist", track?.artist ?: "Tap to open")
        .putString("status", status)
        .putFloat("progress", progress.coerceIn(0f, 1f))
        .apply()
    updateMusicWidgets(context)
}
