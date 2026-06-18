package dev.cholt.jellyfinmusic

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.RectF
import android.media.AudioFocusRequest
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Collections
import java.util.LinkedHashMap
import java.util.Locale
import java.util.TimeZone
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import ir.mahozad.multiplatform.wavyslider.WaveDirection.HEAD
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        applySystemBarStyle(isSystemDarkMode())
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 42)
        }
        setContent {
            JellyfinMusicRoot()
        }
    }

    fun applySystemBarStyle(darkTheme: Boolean) {
        val transparent = AndroidColor.TRANSPARENT
        val style = if (darkTheme) {
            SystemBarStyle.dark(transparent)
        } else {
            SystemBarStyle.light(transparent, transparent)
        }
        enableEdgeToEdge(
            statusBarStyle = style,
            navigationBarStyle = style
        )
    }

    private fun isSystemDarkMode(): Boolean =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

@Composable
private fun JellyfinMusicRoot() {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val launchThemeMode = remember { loadThemeMode(context) }
    val darkTheme = when (launchThemeMode) {
        AppThemeMode.System -> systemDarkTheme
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }
    var showLaunchSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(900)
        showLaunchSplash = false
    }

    JellyfinMusicTheme(darkTheme = darkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            JellyfinMusicApp()
            if (showLaunchSplash) {
                LaunchVisualizerSplash()
            }
        }
    }
}

@Composable
private fun LaunchVisualizerSplash(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "launch-visualizer")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1180, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "launch-visualizer-phase"
    )
    val background = Brush.verticalGradient(
        colors = listOf(
            colorScheme.surfaceContainerLow,
            colorScheme.surface,
            colorScheme.background
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .width(172.dp)
                .height(108.dp)
        ) {
            val barColors = listOf(
                colorScheme.primary,
                colorScheme.primary.copy(alpha = 0.84f),
                colorScheme.secondary,
                colorScheme.tertiary,
                colorScheme.primary.copy(alpha = 0.88f),
                colorScheme.secondary.copy(alpha = 0.82f),
                colorScheme.primary
            )
            val barCount = barColors.size
            val step = size.width / (barCount + 1)
            val centerY = size.height / 2f
            val maxBarHeight = size.height * 0.72f
            val activeStroke = 9.dp.toPx()
            val restStroke = 12.dp.toPx()

            for (index in 0 until barCount) {
                val wave = (sin(phase + index * 0.82f) + 1f) / 2f
                val accentWave = (sin(phase * 1.54f - index * 0.47f) + 1f) / 2f
                val level = (0.24f + wave * 0.56f + accentWave * 0.14f).coerceIn(0.22f, 0.94f)
                val barHeight = maxBarHeight * level
                val x = step * (index + 1)
                val color = barColors[index]

                drawLine(
                    color = color.copy(alpha = 0.14f),
                    start = Offset(x, centerY + maxBarHeight / 2f),
                    end = Offset(x, centerY - maxBarHeight / 2f),
                    strokeWidth = restStroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color.copy(alpha = 0.94f),
                    start = Offset(x, centerY + barHeight / 2f),
                    end = Offset(x, centerY - barHeight / 2f),
                    strokeWidth = activeStroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private val SeedPrimary = Color(0xFF006B5B)
private val SeedSecondary = Color(0xFF7D5260)
private val SeedTertiary = Color(0xFF745B00)
private val AlbumTints = listOf(
    Color(0xFF006B5B),
    Color(0xFF52606D),
    Color(0xFF9A5448),
    Color(0xFF6E5B95),
    Color(0xFF2F6B83),
    Color(0xFF8A5B2D)
)

private const val PREFS_NAME = "jellyfin_music"
private const val PREF_USE_ALBUM_ART_COLORS = "use_album_art_colors"
private const val PREF_VISUALIZER_ENABLED = "visualizer_enabled"
private const val PREF_THEME_MODE = "theme_mode"
private const val PREF_LIKED_TRACK_IDS_PREFIX = "liked_track_ids_"
private const val PREF_PINNED_LIBRARY_ITEMS_PREFIX = "pinned_library_items_"
private const val PREF_RECENT_TRACK_IDS_PREFIX = "recent_track_ids_"
private const val PREF_OFFLINE_WIFI_ONLY = "offline_wifi_only"
private const val PREF_OFFLINE_STORAGE_LIMIT_MB = "offline_storage_limit_mb"
private const val LIBRARY_CACHE_VERSION = 2
private const val OFFLINE_DOWNLOADS_VERSION = 1
private const val ALBUM_ART_CACHE_DIR = "album_art_cache"
private const val OFFLINE_DOWNLOAD_DIR = "offline_audio"
private const val MAX_ALBUM_ART_CACHE_FILES = 128
private const val DEFAULT_OFFLINE_STORAGE_LIMIT_MB = 1024
private const val DISC_SCRATCH_SEEK_SCALE = 0.55f
private const val DISC_SCRATCH_DEAD_ZONE = 0.22f
private const val VISUALIZER_BAR_COUNT = 28
private const val VISUALIZER_CAPTURE_STALE_MS = 1_200L
private const val VISUALIZER_FALLBACK_FRAME_MS = 110L

private val AlbumArtCache = Collections.synchronizedMap(
    object : LinkedHashMap<String, Bitmap>(96, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Bitmap>?): Boolean =
            size > 96
    }
)

data class JellyfinSession(
    val serverUrl: String,
    val username: String,
    val userId: String,
    val token: String
)

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val dateAddedMs: Long = 0L,
    val imageItemId: String?,
    val imageTag: String?,
    val tint: Color
) {
    fun streamUrl(session: JellyfinSession): String {
        val encodedId = encode(id)
        val encodedToken = encode(session.token)
        return "${session.serverUrl}/Audio/$encodedId/stream?Static=true&api_key=$encodedToken"
    }

    fun imageUrl(session: JellyfinSession?, size: Int = 512, quality: Int = 86): String? {
        val activeSession = session ?: return null
        val itemId = imageItemId?.takeIf { it.isNotBlank() } ?: return null
        val tagParameter = imageTag
            ?.takeIf { it.isNotBlank() }
            ?.let { "&tag=${encode(it)}" }
            .orEmpty()
        val boundedQuality = quality.coerceIn(55, 92)
        return "${activeSession.serverUrl}/Items/${encode(itemId)}/Images/Primary" +
            "?fillWidth=$size&fillHeight=$size&quality=$boundedQuality$tagParameter&api_key=${encode(activeSession.token)}"
    }
}

private data class OfflineDownload(
    val trackId: String,
    val fileName: String,
    val bytes: Long,
    val updatedAt: Long
)

private data class OfflineDownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long
) {
    val fraction: Float
        get() = if (totalBytes > 0L) {
            (bytesDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}

private val OfflineStorageLimitOptionsMb = listOf(256, 512, 1024, 2048, 4096)

private fun JellyfinSession.streamHeaders(): Map<String, String> =
    mapOf(
        "X-Emby-Token" to token,
        "User-Agent" to "JellyfinMusic/0.1.0",
        "Accept" to "audio/*,*/*"
    )

private const val PLAYBACK_NOTIFICATION_CHANNEL_ID = "playback"
private const val PLAYBACK_NOTIFICATION_ID = 1001
private const val PLAYBACK_ACTION_PLAY = "dev.cholt.jellyfinmusic.action.PLAY"
private const val PLAYBACK_ACTION_PAUSE = "dev.cholt.jellyfinmusic.action.PAUSE"
private const val PLAYBACK_ACTION_TOGGLE = "dev.cholt.jellyfinmusic.action.TOGGLE"
private const val PLAYBACK_ACTION_PREVIOUS = "dev.cholt.jellyfinmusic.action.PREVIOUS"
private const val PLAYBACK_ACTION_NEXT = "dev.cholt.jellyfinmusic.action.NEXT"
private const val PLAYBACK_ACTION_STOP = "dev.cholt.jellyfinmusic.action.STOP"
private const val PLAYBACK_SERVICE_ACTION_START = "dev.cholt.jellyfinmusic.service.START"
private const val PLAYBACK_SERVICE_ACTION_STOP = "dev.cholt.jellyfinmusic.service.STOP"

private data class PlaybackActionHandlers(
    val onPlay: () -> Unit,
    val onPause: () -> Unit,
    val onToggle: () -> Unit,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
    val onSeekToFraction: (Float) -> Unit,
    val onStop: () -> Unit
)

private object PlaybackNotificationActions {
    @Volatile
    private var handlers: PlaybackActionHandlers? = null

    fun register(handlers: PlaybackActionHandlers) {
        this.handlers = handlers
    }

    fun clear() {
        handlers = null
    }

    fun handle(action: String?) {
        val activeHandlers = handlers ?: return
        when (action) {
            PLAYBACK_ACTION_PLAY -> activeHandlers.onPlay()
            PLAYBACK_ACTION_PAUSE -> activeHandlers.onPause()
            PLAYBACK_ACTION_TOGGLE -> activeHandlers.onToggle()
            PLAYBACK_ACTION_PREVIOUS -> activeHandlers.onPrevious()
            PLAYBACK_ACTION_NEXT -> activeHandlers.onNext()
            PLAYBACK_ACTION_STOP -> activeHandlers.onStop()
        }
    }

    fun seekToFraction(fraction: Float) {
        handlers?.onSeekToFraction?.invoke(fraction.coerceIn(0f, 1f))
    }
}

class PlaybackNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PlaybackNotificationActions.handle(intent.action)
    }
}

class PlaybackForegroundService : Service() {
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }
    private var latestNotification: Notification? = null
    private var isInForeground = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        createPlaybackChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == PLAYBACK_SERVICE_ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = latestNotification ?: buildFallbackNotification()
        startAsForeground(notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        if (instance === this) {
            instance = null
        }
        isInForeground = false
        super.onDestroy()
    }

    fun publish(notification: Notification, keepForeground: Boolean) {
        latestNotification = notification
        if (keepForeground) {
            startAsForeground(notification)
        } else {
            detachForeground()
            notificationManager.notify(PLAYBACK_NOTIFICATION_ID, notification)
            stopSelf()
        }
    }

    private fun startAsForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                PLAYBACK_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(PLAYBACK_NOTIFICATION_ID, notification)
        }
        isInForeground = true
    }

    private fun detachForeground() {
        if (!isInForeground) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(false)
        }
        isInForeground = false
    }

    private fun buildFallbackNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, PLAYBACK_NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setSmallIcon(R.drawable.ic_notification_music)
            .setContentTitle("Jellyfin Music")
            .setContentText("Preparing playback")
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }

    private fun createPlaybackChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            PLAYBACK_NOTIFICATION_CHANNEL_ID,
            "Music playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Playback controls for Jellyfin Music"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        @Volatile
        private var instance: PlaybackForegroundService? = null

        fun publish(context: Context, notification: Notification, keepForeground: Boolean) {
            val appContext = context.applicationContext
            if (keepForeground) {
                val startIntent = Intent(appContext, PlaybackForegroundService::class.java)
                    .setAction(PLAYBACK_SERVICE_ACTION_START)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(startIntent)
                } else {
                    appContext.startService(startIntent)
                }
            }
            instance?.publish(notification, keepForeground)
        }

        fun stop(context: Context) {
            val appContext = context.applicationContext
            instance?.stopSelf()
            appContext.stopService(
                Intent(appContext, PlaybackForegroundService::class.java)
                    .setAction(PLAYBACK_SERVICE_ACTION_STOP)
            )
        }
    }
}

private class PlaybackNotificationController(context: Context) {
    private val appContext = context.applicationContext
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentTrack: MusicTrack? = null
    private var currentSession: JellyfinSession? = null
    private var currentIsPlaying = false
    private var currentStatus = "Ready"
    private var currentProgress = 0f
    private var largeIconTrackId: String? = null
    private var largeIcon: Bitmap? = null
    private var loadingLargeIconTrackId: String? = null
    private var lastPlaybackStateUpdateAt = 0L

    private val mediaSession = MediaSession(appContext, "Jellyfin Music").apply {
        setCallback(object : MediaSession.Callback() {
            override fun onPlay() {
                PlaybackNotificationActions.handle(PLAYBACK_ACTION_PLAY)
            }

            override fun onPause() {
                PlaybackNotificationActions.handle(PLAYBACK_ACTION_PAUSE)
            }

            override fun onSkipToPrevious() {
                PlaybackNotificationActions.handle(PLAYBACK_ACTION_PREVIOUS)
            }

            override fun onSkipToNext() {
                PlaybackNotificationActions.handle(PLAYBACK_ACTION_NEXT)
            }

            override fun onSeekTo(pos: Long) {
                currentTrack?.durationMs
                    ?.takeIf { it > 0L }
                    ?.let { duration -> PlaybackNotificationActions.seekToFraction(pos.toFloat() / duration.toFloat()) }
            }

            override fun onStop() {
                PlaybackNotificationActions.handle(PLAYBACK_ACTION_STOP)
            }

            override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                } ?: return super.onMediaButtonEvent(mediaButtonIntent)

                if (keyEvent.action != KeyEvent.ACTION_UP) return true
                when (keyEvent.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY -> PlaybackNotificationActions.handle(PLAYBACK_ACTION_PLAY)
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> PlaybackNotificationActions.handle(PLAYBACK_ACTION_PAUSE)
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                    KeyEvent.KEYCODE_HEADSETHOOK -> PlaybackNotificationActions.handle(PLAYBACK_ACTION_TOGGLE)
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> PlaybackNotificationActions.handle(PLAYBACK_ACTION_PREVIOUS)
                    KeyEvent.KEYCODE_MEDIA_NEXT -> PlaybackNotificationActions.handle(PLAYBACK_ACTION_NEXT)
                    KeyEvent.KEYCODE_MEDIA_STOP -> PlaybackNotificationActions.handle(PLAYBACK_ACTION_STOP)
                    else -> return super.onMediaButtonEvent(mediaButtonIntent)
                }
                return true
            }
        })
    }

    init {
        createPlaybackChannel()
    }

    fun update(
        track: MusicTrack?,
        session: JellyfinSession?,
        isPlaying: Boolean,
        status: String,
        progress: Float
    ) {
        if (track == null || session == null) {
            cancel()
            return
        }

        val trackChanged = currentTrack?.id != track.id
        currentTrack = track
        currentSession = session
        currentIsPlaying = isPlaying
        currentStatus = status
        currentProgress = progress.coerceIn(0f, 1f)

        if (trackChanged) {
            largeIcon = null
            largeIconTrackId = null
            loadingLargeIconTrackId = null
        }

        publishMediaSessionState(force = true)
        val notification = buildNotification(track, isPlaying, status)
        PlaybackForegroundService.publish(
            context = appContext,
            notification = notification,
            keepForeground = isPlaying || status == "Buffering"
        )
        notificationManager.notify(PLAYBACK_NOTIFICATION_ID, notification)
        loadLargeIconIfNeeded(track, session)
    }

    fun syncPlaybackState(isPlaying: Boolean, status: String, progress: Float) {
        currentIsPlaying = isPlaying
        currentStatus = status
        currentProgress = progress.coerceIn(0f, 1f)
        publishMediaSessionState(force = false)
    }

    fun cancel() {
        currentTrack = null
        currentSession = null
        currentIsPlaying = false
        currentStatus = "Ready"
        currentProgress = 0f
        mediaSession.isActive = false
        PlaybackForegroundService.stop(appContext)
        notificationManager.cancel(PLAYBACK_NOTIFICATION_ID)
    }

    fun release() {
        cancel()
        mediaSession.release()
    }

    private fun createPlaybackChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            PLAYBACK_NOTIFICATION_CHANNEL_ID,
            "Music playback",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Playback controls for Jellyfin Music"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun buildNotification(track: MusicTrack, isPlaying: Boolean, status: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(appContext, PLAYBACK_NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(appContext)
        }

        builder
            .setSmallIcon(R.drawable.ic_notification_music)
            .setContentTitle(track.title)
            .setContentText(track.notificationSubtitle())
            .setSubText(status.takeUnless { it == "Playing" || it == "Paused" })
            .setContentIntent(openAppPendingIntent())
            .setDeleteIntent(actionPendingIntent(PLAYBACK_ACTION_STOP, 5))
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(isPlaying || status == "Buffering")
            .setColor(track.tint.toArgb())
            .setProgress(
                track.durationMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                (track.durationMs * currentProgress).toLong().coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                status == "Buffering"
            )

        largeIcon?.takeIf { largeIconTrackId == track.id }?.let(builder::setLargeIcon)

        builder.addAction(
            Notification.Action.Builder(
                android.R.drawable.ic_media_previous,
                "Previous",
                actionPendingIntent(PLAYBACK_ACTION_PREVIOUS, 1)
            ).build()
        )
        builder.addAction(
            Notification.Action.Builder(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying) "Pause" else "Play",
                actionPendingIntent(if (isPlaying) PLAYBACK_ACTION_PAUSE else PLAYBACK_ACTION_PLAY, 2)
            ).build()
        )
        builder.addAction(
            Notification.Action.Builder(
                android.R.drawable.ic_media_next,
                "Next",
                actionPendingIntent(PLAYBACK_ACTION_NEXT, 3)
            ).build()
        )

        builder.setStyle(
            Notification.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2)
        )

        return builder.build()
    }

    private fun publishMediaSessionState(force: Boolean) {
        val track = currentTrack ?: return
        val now = SystemClock.elapsedRealtime()
        if (!force && now - lastPlaybackStateUpdateAt < 1_500L) return
        lastPlaybackStateUpdateAt = now

        mediaSession.isActive = true
        mediaSession.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, track.durationMs.coerceAtLeast(0L))
                .apply {
                    largeIcon?.takeIf { largeIconTrackId == track.id }?.let {
                        putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, it)
                    }
                }
                .build()
        )
        mediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_STOP
                )
                .setState(
                    playbackStateFor(currentStatus, currentIsPlaying),
                    (track.durationMs * currentProgress).toLong().coerceAtLeast(0L),
                    if (currentIsPlaying) 1f else 0f,
                    now
                )
                .build()
        )
    }

    private fun playbackStateFor(status: String, isPlaying: Boolean): Int =
        when {
            status == "Buffering" -> PlaybackState.STATE_BUFFERING
            isPlaying -> PlaybackState.STATE_PLAYING
            status == "Ended" -> PlaybackState.STATE_STOPPED
            else -> PlaybackState.STATE_PAUSED
        }

    private fun loadLargeIconIfNeeded(track: MusicTrack, session: JellyfinSession) {
        if (largeIconTrackId == track.id || loadingLargeIconTrackId == track.id) return
        val imageUrl = track.imageUrl(session, size = 256, quality = 82) ?: return
        loadingLargeIconTrackId = track.id
        thread(name = "jellyfin-notification-art", isDaemon = true) {
            val bitmap = loadAlbumBitmapBlocking(appContext, imageUrl, session.token)
            mainHandler.post {
                loadingLargeIconTrackId = null
                if (currentTrack?.id == track.id && bitmap != null) {
                    largeIcon = bitmap
                    largeIconTrackId = track.id
                    publishMediaSessionState(force = true)
                    val notification = buildNotification(track, currentIsPlaying, currentStatus)
                    PlaybackForegroundService.publish(
                        context = appContext,
                        notification = notification,
                        keepForeground = currentIsPlaying || currentStatus == "Buffering"
                    )
                    notificationManager.notify(
                        PLAYBACK_NOTIFICATION_ID,
                        notification
                    )
                }
            }
        }
    }

    private fun openAppPendingIntent(): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun actionPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(appContext, PlaybackNotificationReceiver::class.java).apply {
            this.action = action
            setPackage(appContext.packageName)
        }
        return PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

private fun MusicTrack.notificationSubtitle(): String =
    listOf(artist, album)
        .filter { it.isNotBlank() }
        .joinToString(" - ")
        .ifBlank { "Jellyfin Music" }

private object PlaybackControllerHolder {
    @Volatile
    private var player: JellyfinPlayer? = null

    fun get(context: Context): JellyfinPlayer =
        player ?: synchronized(this) {
            player ?: JellyfinPlayer(context.applicationContext).also { player = it }
        }

    fun clear(activePlayer: JellyfinPlayer) {
        synchronized(this) {
            if (player === activePlayer) {
                player = null
            }
        }
    }
}

private enum class LibraryTab(val label: String) {
    Songs("Songs"),
    Albums("Albums"),
    Artists("Artists")
}

private enum class LibrarySortMode(val label: String) {
    Title("Title"),
    Artist("Artist"),
    Album("Album"),
    Duration("Duration")
}

private enum class LibraryFilter(val label: String) {
    Liked("Favorites"),
    Downloaded("Downloaded"),
    RecentPlayed("Recent"),
    RecentlyAdded("New"),
    LongTracks("Long")
}

private enum class LibraryCollectionType(val label: String) {
    Album("Album"),
    Artist("Artist"),
    Downloaded("Downloaded")
}

private enum class AppDestination(val label: String) {
    Home("Home"),
    Search("Search"),
    Player("Play"),
    Library("Library"),
    Liked("Favorites"),
    Profile("Settings")
}

private enum class AppThemeMode(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}

private val BottomTabDestinations = listOf(
    AppDestination.Home,
    AppDestination.Search,
    AppDestination.Library,
    AppDestination.Liked,
    AppDestination.Profile
)

private data class LibraryGroup(
    val title: String,
    val subtitle: String,
    val tint: Color,
    val tracks: List<MusicTrack>
)

private data class PinnedLibraryItem(
    val type: LibraryCollectionType,
    val key: String
)

private data class LibraryDetail(
    val type: LibraryCollectionType,
    val key: String
)

private data class ResolvedPinnedItem(
    val item: PinnedLibraryItem,
    val title: String,
    val subtitle: String,
    val artworkTrack: MusicTrack?,
    val shape: Shape,
    val detail: LibraryDetail
)

private data class SearchResultCounts(
    val songs: Int,
    val albums: Int,
    val artists: Int
) {
    fun countFor(tab: LibraryTab): Int =
        when (tab) {
            LibraryTab.Songs -> songs
            LibraryTab.Albums -> albums
            LibraryTab.Artists -> artists
        }
}

private fun SearchResultCounts.summary(): String =
    listOf(
        songs.countLabel("song"),
        albums.countLabel("album"),
        artists.countLabel("artist")
    ).joinToString(" - ")

private fun Int.countLabel(label: String): String =
    "$this $label${if (this == 1) "" else "s"}"

private fun formatLastLibrarySync(timestampMs: Long?): String {
    val timestamp = timestampMs?.takeIf { it > 0L } ?: return "Never"
    val elapsedMinutes = ((System.currentTimeMillis() - timestamp).coerceAtLeast(0L) / 60_000L).toInt()
    return when {
        elapsedMinutes < 1 -> "Just now"
        elapsedMinutes < 60 -> "$elapsedMinutes min ago"
        elapsedMinutes < 24 * 60 -> "${elapsedMinutes / 60} hr ago"
        else -> "${elapsedMinutes / (24 * 60)} days ago"
    }
}

private fun parseJellyfinDateMs(value: String): Long {
    val raw = value.trim()
    if (raw.isBlank()) return 0L
    val normalized = raw
        .replace(Regex("""\.(\d{3})\d+(Z|[+-]\d\d:\d\d)$"""), ".$1$2")
        .replace(Regex("""([+-]\d\d):(\d\d)$"""), "$1$2")
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    return patterns.firstNotNullOfOrNull { pattern ->
        runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(normalized)?.time
        }.getOrNull()
    } ?: 0L
}

private fun formatDataSize(bytes: Long): String {
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return if (mb >= 1024.0) {
        "${String.format(Locale.getDefault(), "%.1f", mb / 1024.0)} GB"
    } else {
        "${mb.roundToInt()} MB"
    }
}

private fun formatStorageLimit(limitMb: Int): String =
    formatDataSize(limitMb.toLong() * 1024L * 1024L)

private fun OfflineDownloadProgress.percentLabel(): String =
    if (totalBytes > 0L) {
        "${(fraction * 100f).roundToInt().coerceIn(1, 99)}%"
    } else {
        "..."
    }

private val MusicTrackSort = compareBy<MusicTrack>(
    { it.title.lowercase(Locale.getDefault()) },
    { it.artist.lowercase(Locale.getDefault()) },
    { it.album.lowercase(Locale.getDefault()) }
)
private val SearchSeparatorRegex = Regex("[^\\p{L}\\p{N}]+")

@Composable
private fun JellyfinMusicTheme(
    albumAccentColor: Color? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = albumAccentColor?.let {
        if (darkTheme) albumArtDarkColorScheme(it) else albumArtLightColorScheme(it)
    }
        ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            fallbackColorScheme(darkTheme)
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

private fun fallbackColorScheme(darkTheme: Boolean) =
    if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF77D8C4),
            secondary = Color(0xFFEAB9C8),
            tertiary = Color(0xFFE4C46A),
            background = Color(0xFF101413),
            surface = Color(0xFF101413),
            surfaceVariant = Color(0xFF3F4945),
            primaryContainer = Color(0xFF005143),
            secondaryContainer = Color(0xFF633B48),
            tertiaryContainer = Color(0xFF574500)
        )
    } else {
        lightColorScheme(
            primary = SeedPrimary,
            secondary = SeedSecondary,
            tertiary = SeedTertiary,
            background = Color(0xFFFBFCFA),
            surface = Color(0xFFFBFCFA),
            surfaceVariant = Color(0xFFE5ECE8),
            primaryContainer = Color(0xFFD8EDE7),
            secondaryContainer = Color(0xFFFFD9E3),
            tertiaryContainer = Color(0xFFFFDF9A)
        )
    }

private fun albumArtLightColorScheme(rawAccent: Color): androidx.compose.material3.ColorScheme {
    val accent = normalizeAlbumAccent(rawAccent)
    val secondary = shiftedAccent(accent, hueShift = 24f, saturationMultiplier = 0.7f)
    val tertiary = shiftedAccent(accent, hueShift = -42f, saturationMultiplier = 0.78f)
    return lightColorScheme(
    primary = accent,
    onPrimary = readableOnColor(accent),
    primaryContainer = blendColors(accent, Color.White, 0.78f),
    onPrimaryContainer = blendColors(accent, Color.Black, 0.24f),
    secondary = secondary,
    onSecondary = Color.White,
    secondaryContainer = blendColors(secondary, Color.White, 0.78f),
    onSecondaryContainer = Color(0xFF241B20),
    tertiary = tertiary,
    onTertiary = Color.White,
    tertiaryContainer = blendColors(tertiary, Color.White, 0.78f),
    onTertiaryContainer = Color(0xFF201C10),
    background = blendColors(accent, Color.White, 0.94f),
    onBackground = Color(0xFF191C1B),
    surface = blendColors(accent, Color.White, 0.96f),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = blendColors(accent, Color.White, 0.84f),
    onSurfaceVariant = Color(0xFF434844),
    outline = blendColors(accent, Color(0xFF747974), 0.54f),
    inverseSurface = Color(0xFF2D312F),
    inverseOnSurface = Color(0xFFEFF1EE),
    inversePrimary = blendColors(accent, Color.White, 0.42f)
    )
}

private fun albumArtDarkColorScheme(rawAccent: Color): androidx.compose.material3.ColorScheme {
    val accent = normalizeAlbumAccentForDark(rawAccent)
    val secondary = shiftedAccent(accent, hueShift = 24f, saturationMultiplier = 0.7f)
    val tertiary = shiftedAccent(accent, hueShift = -42f, saturationMultiplier = 0.78f)
    return darkColorScheme(
        primary = accent,
        onPrimary = readableOnColor(accent),
        primaryContainer = blendColors(accent, Color.Black, 0.42f),
        onPrimaryContainer = blendColors(accent, Color.White, 0.76f),
        secondary = blendColors(secondary, Color.White, 0.18f),
        onSecondary = readableOnColor(blendColors(secondary, Color.White, 0.18f)),
        secondaryContainer = blendColors(secondary, Color.Black, 0.48f),
        onSecondaryContainer = blendColors(secondary, Color.White, 0.76f),
        tertiary = blendColors(tertiary, Color.White, 0.18f),
        onTertiary = readableOnColor(blendColors(tertiary, Color.White, 0.18f)),
        tertiaryContainer = blendColors(tertiary, Color.Black, 0.48f),
        onTertiaryContainer = blendColors(tertiary, Color.White, 0.76f),
        background = blendColors(accent, Color.Black, 0.9f),
        onBackground = Color(0xFFE2E7E3),
        surface = blendColors(accent, Color.Black, 0.92f),
        onSurface = Color(0xFFE2E7E3),
        surfaceVariant = blendColors(accent, Color.Black, 0.72f),
        onSurfaceVariant = Color(0xFFC2CAC5),
        outline = blendColors(accent, Color(0xFF8C958F), 0.68f),
        inverseSurface = Color(0xFFE2E7E3),
        inverseOnSurface = Color(0xFF1B1F1D),
        inversePrimary = normalizeAlbumAccent(rawAccent)
    )
}

private fun Bitmap.extractAlbumAccentColor(): Color? {
    if (width <= 0 || height <= 0) return null
    val stride = maxOf(1, minOf(width, height) / 42)
    val hsv = FloatArray(3)
    var redTotal = 0.0
    var greenTotal = 0.0
    var blueTotal = 0.0
    var weightTotal = 0.0

    var y = 0
    while (y < height) {
        var x = 0
        while (x < width) {
            val pixel = getPixel(x, y)
            val alpha = AndroidColor.alpha(pixel)
            if (alpha >= 160) {
                AndroidColor.colorToHSV(pixel, hsv)
                val saturation = hsv[1]
                val value = hsv[2]
                val isUsefulNeutral = saturation >= 0.08f && value in 0.16f..0.94f
                val isUsefulColor = saturation >= 0.18f && value in 0.12f..0.96f
                if (isUsefulNeutral || isUsefulColor) {
                    val weight = (0.3f + saturation * 1.7f) * (0.55f + value)
                    redTotal += AndroidColor.red(pixel) * weight
                    greenTotal += AndroidColor.green(pixel) * weight
                    blueTotal += AndroidColor.blue(pixel) * weight
                    weightTotal += weight
                }
            }
            x += stride
        }
        y += stride
    }

    if (weightTotal <= 0.0) return null
    return normalizeAlbumAccent(
        Color(
            red = (redTotal / weightTotal / 255.0).toFloat().coerceIn(0f, 1f),
            green = (greenTotal / weightTotal / 255.0).toFloat().coerceIn(0f, 1f),
            blue = (blueTotal / weightTotal / 255.0).toFloat().coerceIn(0f, 1f),
            alpha = 1f
        )
    )
}

private fun normalizeAlbumAccent(color: Color): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    hsv[1] = hsv[1].coerceIn(0.34f, 0.76f)
    hsv[2] = hsv[2].coerceIn(0.38f, 0.68f)
    return composeColor(AndroidColor.HSVToColor(hsv))
}

private fun normalizeAlbumAccentForDark(color: Color): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    hsv[1] = hsv[1].coerceIn(0.28f, 0.72f)
    hsv[2] = hsv[2].coerceIn(0.68f, 0.88f)
    return composeColor(AndroidColor.HSVToColor(hsv))
}

private fun shiftedAccent(
    color: Color,
    hueShift: Float,
    saturationMultiplier: Float
): Color {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(color.toArgb(), hsv)
    hsv[0] = (hsv[0] + hueShift + 360f) % 360f
    hsv[1] = (hsv[1] * saturationMultiplier).coerceIn(0.28f, 0.66f)
    hsv[2] = hsv[2].coerceIn(0.36f, 0.62f)
    return composeColor(AndroidColor.HSVToColor(hsv))
}

private fun blendColors(from: Color, to: Color, amount: Float): Color {
    val t = amount.coerceIn(0f, 1f)
    return Color(
        red = from.red + (to.red - from.red) * t,
        green = from.green + (to.green - from.green) * t,
        blue = from.blue + (to.blue - from.blue) * t,
        alpha = from.alpha + (to.alpha - from.alpha) * t
    )
}

private fun readableOnColor(background: Color): Color {
    val luminance = background.red * 0.299f + background.green * 0.587f + background.blue * 0.114f
    return if (luminance > 0.55f) Color(0xFF111412) else Color.White
}

private fun composeColor(argb: Int): Color =
    Color(
        red = AndroidColor.red(argb) / 255f,
        green = AndroidColor.green(argb) / 255f,
        blue = AndroidColor.blue(argb) / 255f,
        alpha = AndroidColor.alpha(argb) / 255f
    )

private fun stableCacheKey(value: String): String =
    MessageDigest
        .getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { "%02x".format(it.toInt() and 0xFF) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun JellyfinMusicApp() {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { JellyfinRepository(context.applicationContext) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val player = remember { PlaybackControllerHolder.get(context.applicationContext) }
    val scratchEngine = remember { ScratchSoundEngine() }
    val mainListState = rememberLazyListState()

    var session by remember { mutableStateOf(loadSavedSession(context)) }
    var serverUrl by remember { mutableStateOf(session?.serverUrl.orEmpty()) }
    var username by remember { mutableStateOf(session?.username.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var tracks by remember { mutableStateOf(session?.let { loadCachedLibrary(context, it) } ?: emptyList()) }
    var lastLibrarySyncAt by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadCachedLibraryUpdatedAt(context, it) })
    }
    var likedTrackIds by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadLikedTrackIds(context, it) } ?: emptySet())
    }
    var recentTrackIds by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadRecentTrackIds(context, it) } ?: emptyList())
    }
    var pinnedLibraryItems by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadPinnedLibraryItems(context, it) } ?: emptyList())
    }
    var offlineDownloads by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadOfflineDownloads(context, it) } ?: emptyMap())
    }
    var downloadProgressById by remember { mutableStateOf(emptyMap<String, OfflineDownloadProgress>()) }
    var selectedTab by remember { mutableStateOf(LibraryTab.Songs) }
    var librarySearchQuery by remember { mutableStateOf("") }
    var librarySortMode by remember { mutableStateOf(LibrarySortMode.Title) }
    var libraryGridView by remember { mutableStateOf(false) }
    var activeLibraryFilters by remember { mutableStateOf(emptySet<LibraryFilter>()) }
    var libraryLetterFilter by remember { mutableStateOf<Char?>(null) }
    var downloadedOnlyMode by remember { mutableStateOf(false) }
    var activeLibraryDetail by remember { mutableStateOf<LibraryDetail?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }
    var searchFocusRequests by remember { mutableStateOf(0) }
    var isBusy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var playerDismissDragOffsetPx by remember { mutableFloatStateOf(0f) }
    var isPlayerDismissDragging by remember { mutableStateOf(false) }
    var playerOpenDragOffsetPx by remember { mutableFloatStateOf(0f) }
    var isPlayerOpenDragging by remember { mutableStateOf(false) }
    var playQueue by remember { mutableStateOf(emptyList<MusicTrack>()) }
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatEnabled by remember { mutableStateOf(false) }
    var useAlbumArtColors by remember { mutableStateOf(loadUseAlbumArtColors(context)) }
    var visualizerEnabled by remember { mutableStateOf(loadVisualizerEnabled(context)) }
    var themeMode by remember { mutableStateOf(loadThemeMode(context)) }
    var offlineWifiOnly by remember { mutableStateOf(loadOfflineWifiOnly(context)) }
    var offlineStorageLimitMb by remember { mutableStateOf(loadOfflineStorageLimitMb(context)) }
    var selectedDestination by remember { mutableStateOf(AppDestination.Home) }
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        AppThemeMode.System -> systemDarkTheme
        AppThemeMode.Light -> false
        AppThemeMode.Dark -> true
    }

    SideEffect {
        (context as? MainActivity)?.applySystemBarStyle(darkTheme)
    }

    LaunchedEffect(visualizerEnabled) {
        player.setVisualizerEnabled(visualizerEnabled)
    }

    fun runTask(task: () -> Unit) {
        if (!isBusy) {
            isBusy = true
            thread(name = "jellyfin-task") { task() }
        }
    }

    fun loadLibrary(activeSession: JellyfinSession) {
        runTask {
            var partialTracks = emptyList<MusicTrack>()
            var syncedAt: Long? = null
            val result = runCatching {
                repository.fetchTracks(activeSession) { loadedTracks ->
                    partialTracks = loadedTracks
                    mainHandler.post {
                        tracks = loadedTracks
                        statusText = null
                    }
                }.also { loadedTracks ->
                    syncedAt = saveCachedLibrary(context, activeSession, loadedTracks)
                }
            }
            mainHandler.post {
                isBusy = false
                result
                    .onSuccess { loadedTracks ->
                        tracks = loadedTracks
                        lastLibrarySyncAt = syncedAt ?: lastLibrarySyncAt
                        statusText = if (loadedTracks.isEmpty()) "No music found" else null
                    }
                    .onFailure {
                        statusText = if (partialTracks.isNotEmpty()) {
                            "Library refresh incomplete: ${it.readableMessage()}"
                        } else {
                            it.readableMessage()
                        }
                    }
            }
        }
    }

    fun connect() {
        if (serverUrl.isBlank() || username.isBlank()) {
            statusText = "Server URL and username are required"
            return
        }

        runTask {
            var partialTracks = emptyList<MusicTrack>()
            var syncedAt: Long? = null
            val result = runCatching {
                val activeSession = repository.login(serverUrl, username, password)
                saveSession(context, activeSession)
                mainHandler.post {
                    session = activeSession
                    serverUrl = activeSession.serverUrl
                    username = activeSession.username
                    password = ""
                }
                val loadedTracks = repository.fetchTracks(activeSession) { loadedTracks ->
                    partialTracks = loadedTracks
                    mainHandler.post {
                        tracks = loadedTracks
                        statusText = null
                    }
                }
                syncedAt = saveCachedLibrary(context, activeSession, loadedTracks)
                activeSession to loadedTracks
            }
            mainHandler.post {
                isBusy = false
                result
                    .onSuccess { (activeSession, loadedTracks) ->
                        session = activeSession
                        serverUrl = activeSession.serverUrl
                        username = activeSession.username
                        password = ""
                        tracks = loadedTracks
                        lastLibrarySyncAt = syncedAt ?: lastLibrarySyncAt
                        statusText = if (loadedTracks.isEmpty()) "Connected, but no music found" else null
                    }
                    .onFailure {
                        statusText = if (partialTracks.isNotEmpty()) {
                            "Library refresh incomplete: ${it.readableMessage()}"
                        } else {
                            it.readableMessage()
                        }
                    }
            }
        }
    }

    fun signOut() {
        player.release()
        clearSavedSession(context)
        session = null
        tracks = emptyList()
        likedTrackIds = emptySet()
        recentTrackIds = emptyList()
        pinnedLibraryItems = emptyList()
        offlineDownloads = emptyMap()
        downloadProgressById = emptyMap()
        activeLibraryFilters = emptySet()
        libraryLetterFilter = null
        downloadedOnlyMode = false
        activeLibraryDetail = null
        lastLibrarySyncAt = null
        playQueue = emptyList()
        statusText = null
        showPlayer = false
        selectedDestination = AppDestination.Home
    }

    fun showNowPlayingPlayer() {
        selectedDestination = AppDestination.Player
        showPlayer = true
        isPlayerOpenDragging = false
        playerOpenDragOffsetPx = 0f
        isPlayerDismissDragging = false
        playerDismissDragOffsetPx = 0f
    }

    fun playTrack(track: MusicTrack, openPlayer: Boolean = false, source: List<MusicTrack> = tracks) {
        session?.let { activeSession ->
            val nextRecentTrackIds = (listOf(track.id) + recentTrackIds.filterNot { it == track.id }).take(80)
            recentTrackIds = nextRecentTrackIds
            saveRecentTrackIds(context, activeSession, nextRecentTrackIds)
            playQueue = source.queueStartingAt(track).ifEmpty { listOf(track) }
            player.play(track, activeSession)
            if (openPlayer) {
                showNowPlayingPlayer()
            }
        }
    }

    fun playRandom(source: List<MusicTrack>) {
        session?.let { activeSession ->
            val randomizedQueue = source.randomizedPlaybackQueue()
            val firstTrack = randomizedQueue.firstOrNull() ?: return
            playQueue = randomizedQueue
            player.play(firstTrack, activeSession)
            showNowPlayingPlayer()
        }
    }

    fun playAdjacent(offset: Int) {
        val activeTrack = player.currentTrack ?: return
        val baseQueue = playQueue
            .ifEmpty { tracks.queueStartingAt(activeTrack) }
            .ifEmpty { listOf(activeTrack) }
            .queueStartingAt(activeTrack)
        if (baseQueue.isEmpty()) return
        val nextIndex = if (shuffleEnabled && baseQueue.size > 1 && offset > 0) {
            Random.nextInt(1, baseQueue.size)
        } else {
            (offset + baseQueue.size) % baseQueue.size
        }
        playTrack(baseQueue[nextIndex], openPlayer = true, source = baseQueue)
    }

    fun playQueuedTrack(track: MusicTrack) {
        val source = playQueue.ifEmpty { tracks }
        playTrack(track, openPlayer = true, source = source)
    }

    fun moveQueueTrack(fromIndex: Int, toIndex: Int) {
        if (fromIndex <= 0 || playQueue.size <= 1) return
        playQueue = playQueue.moveQueueItem(
            fromIndex = fromIndex,
            toIndex = toIndex.coerceIn(1, playQueue.lastIndex)
        )
    }

    fun playTrackNext(track: MusicTrack) {
        val activeTrack = player.currentTrack
        val baseQueue = if (activeTrack != null) {
            playQueue.ifEmpty { tracks.queueStartingAt(activeTrack) }.queueStartingAt(activeTrack)
        } else {
            playQueue.ifEmpty { tracks }
        }
        playQueue = baseQueue.withTrackPlayNext(track, activeTrack)
    }

    fun toggleLiked(track: MusicTrack) {
        val nextLikedTrackIds = if (track.id in likedTrackIds) {
            likedTrackIds - track.id
        } else {
            likedTrackIds + track.id
        }
        likedTrackIds = nextLikedTrackIds
        session?.let { activeSession ->
            saveLikedTrackIds(context, activeSession, nextLikedTrackIds)
        }
    }

    fun togglePinnedLibraryItem(item: PinnedLibraryItem) {
        val nextPinnedItems = if (pinnedLibraryItems.any { it == item }) {
            pinnedLibraryItems.filterNot { it == item }
        } else {
            (listOf(item) + pinnedLibraryItems).distinct().take(24)
        }
        pinnedLibraryItems = nextPinnedItems
        session?.let { activeSession ->
            savePinnedLibraryItems(context, activeSession, nextPinnedItems)
        }
    }

    fun openLibraryDetail(detail: LibraryDetail) {
        activeLibraryDetail = detail
        selectedDestination = AppDestination.Library
        showPlayer = false
    }

    fun startRadioFrom(source: List<MusicTrack>, seed: MusicTrack) {
        val sameArtist = tracks.filter { it.artist.equals(seed.artist, ignoreCase = true) }
        val sameAlbum = tracks.filter { it.album.equals(seed.album, ignoreCase = true) }
        val liked = tracks.filter { it.id in likedTrackIds }
        val queue = (listOf(seed) + source + sameArtist + sameAlbum + liked + tracks.homeMix(seed.id.hashCode().toLong()))
            .distinctBy { it.id }
            .take(50)
        val firstTrack = queue.firstOrNull() ?: return
        session?.let { activeSession ->
            playQueue = queue
            player.play(firstTrack, activeSession)
            showNowPlayingPlayer()
        }
    }

    fun toggleOfflineDownload(track: MusicTrack) {
        val activeSession = session ?: return
        if (track.id in downloadProgressById) return

        if (track.id in offlineDownloads) {
            offlineDownloads = removeOfflineDownload(context, activeSession, track)
            statusText = "Removed from downloads"
            return
        }

        val appContext = context.applicationContext
        downloadProgressById = downloadProgressById + (track.id to OfflineDownloadProgress(0L, -1L))
        statusText = "Downloading ${track.title}"
        thread(name = "jellyfin-download") {
            val result = runCatching {
                downloadTrackForOffline(
                    context = appContext,
                    session = activeSession,
                    track = track,
                    wifiOnly = offlineWifiOnly,
                    storageLimitMb = offlineStorageLimitMb
                ) { progress ->
                    mainHandler.post {
                        downloadProgressById = downloadProgressById + (track.id to progress)
                    }
                }
            }
            mainHandler.post {
                downloadProgressById = downloadProgressById - track.id
                result
                    .onSuccess {
                        offlineDownloads = loadOfflineDownloads(appContext, activeSession)
                        statusText = "Saved ${track.title} for offline playback"
                    }
                    .onFailure {
                        offlineDownloads = loadOfflineDownloads(appContext, activeSession)
                        statusText = it.readableMessage()
                    }
            }
        }
    }

    fun clearOfflineDownloads() {
        val activeSession = session ?: return
        removeAllOfflineDownloads(context, activeSession)
        offlineDownloads = emptyMap()
        downloadProgressById = emptyMap()
        statusText = "Offline downloads cleared"
    }

    SideEffect {
        PlaybackNotificationActions.register(
            PlaybackActionHandlers(
                onPlay = {
                    val activeTrack = player.currentTrack
                    val activeSession = session
                    if (activeTrack != null && !player.isPlaying) {
                        if (player.status == "Ended" && activeSession != null) {
                            player.play(activeTrack, activeSession)
                        } else {
                            player.toggle()
                        }
                    }
                },
                onPause = {
                    if (player.isPlaying) {
                        player.toggle()
                    }
                },
                onToggle = {
                    val activeTrack = player.currentTrack
                    val activeSession = session
                    if (activeTrack != null) {
                        if (!player.isPlaying && player.status == "Ended" && activeSession != null) {
                            player.play(activeTrack, activeSession)
                        } else {
                            player.toggle()
                        }
                    }
                },
                onPrevious = { playAdjacent(-1) },
                onNext = { playAdjacent(1) },
                onSeekToFraction = { player.seekToFraction(it) },
                onStop = {
                    player.dispose()
                    PlaybackControllerHolder.clear(player)
                    showPlayer = false
                    selectedDestination = AppDestination.Home
                }
            )
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            scratchEngine.release()
            if (!player.isPlaying) {
                PlaybackNotificationActions.clear()
                player.dispose()
                PlaybackControllerHolder.clear(player)
            }
        }
    }

    LaunchedEffect(session?.token) {
        val activeSession = session
        if (activeSession != null && tracks.isEmpty()) {
            loadLibrary(activeSession)
        }
    }

    LaunchedEffect(searchFocusRequests) {
        if (searchFocusRequests > 0 && selectedDestination == AppDestination.Search && session != null) {
            delay(80)
            runCatching { searchFocusRequester.requestFocus() }
            keyboardController?.show()
        }
    }

    LaunchedEffect(player.currentTrack, player.isPlaying) {
        while (true) {
            player.syncProgress()
            delay(500)
        }
    }

    LaunchedEffect(player.status) {
        val activeTrack = player.currentTrack
        val activeSession = session
        if (player.status == "Ended" && activeTrack != null && activeSession != null) {
            delay(450)
            if (repeatEnabled) {
                player.play(activeTrack, activeSession)
            } else {
                playAdjacent(1)
            }
        }
    }
    val closePlayer = {
        selectedDestination = AppDestination.Home
        showPlayer = false
        isPlayerDismissDragging = false
        playerDismissDragOffsetPx = 0f
        isPlayerOpenDragging = false
        playerOpenDragOffsetPx = 0f
    }
    val showTopBar = showPlayer || session == null || selectedDestination != AppDestination.Home
    val playerDismissOffsetPx by animateFloatAsState(
        targetValue = playerDismissDragOffsetPx,
        animationSpec = if (isPlayerDismissDragging) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        },
        label = "playerDismissOffset"
    )
    val playerOpenOffsetPx by animateFloatAsState(
        targetValue = playerOpenDragOffsetPx,
        animationSpec = if (isPlayerOpenDragging) {
            tween(durationMillis = 0)
        } else {
            tween(durationMillis = 180, easing = FastOutSlowInEasing)
        },
        label = "playerOpenOffset"
    )
    val themeTrack = player.currentTrack
    val themeImageUrl = if (useAlbumArtColors) {
        themeTrack?.imageUrl(session, size = 128)
    } else {
        null
    }
    val themeBitmap by rememberAlbumBitmap(themeImageUrl, if (useAlbumArtColors) session?.token else null)
    val albumAccentColor = remember(useAlbumArtColors, themeBitmap, themeTrack?.id) {
        if (useAlbumArtColors) {
            themeBitmap?.extractAlbumAccentColor() ?: themeTrack?.tint
        } else {
            null
        }
    }

    JellyfinMusicTheme(albumAccentColor = albumAccentColor, darkTheme = darkTheme) {
        Scaffold(
            modifier = if (showPlayer) {
                Modifier
                    .offset { IntOffset(0, playerDismissOffsetPx.roundToInt()) }
                    .swipeDownToDismiss(
                        onDismiss = closePlayer,
                        startZone = 96.dp,
                        dismissDistance = 118.dp,
                        onDragStart = {
                            isPlayerDismissDragging = true
                        },
                        onDragOffsetChange = { offset ->
                            playerDismissDragOffsetPx = offset
                        },
                        onDragEnd = { dismissed ->
                            isPlayerDismissDragging = false
                            if (!dismissed) {
                                playerDismissDragOffsetPx = 0f
                            }
                        }
                    )
            } else {
                Modifier
            },
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        modifier = Modifier,
                        navigationIcon = {
                            when {
                                showPlayer -> IconButton(onClick = closePlayer) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to Home"
                                    )
                                }
                                activeLibraryDetail != null -> IconButton(onClick = { activeLibraryDetail = null }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to Library"
                                    )
                                }
                            }
                        },
                        title = {
                            if (!showPlayer) {
                                val title = when {
                                    session == null -> "Not connected"
                                    activeLibraryDetail != null -> activeLibraryDetail?.type?.label
                                    selectedDestination == AppDestination.Profile -> "Settings"
                                    else -> null
                                }
                                if (title != null) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                val activeSession = session
                val activeTrack = player.currentTrack
                if (activeSession != null && !showPlayer) {
                    Column {
                        if (activeTrack != null) {
                            NowPlayingBar(
                                track = activeTrack,
                                session = activeSession,
                                isPlaying = player.isPlaying,
                                progress = player.progress,
                                status = player.status,
                                openDragOffsetPx = playerOpenOffsetPx,
                                onOpen = ::showNowPlayingPlayer,
                                onOpenDragStart = {
                                    isPlayerOpenDragging = true
                                },
                                onOpenDragOffsetChange = { offset ->
                                    playerOpenDragOffsetPx = offset
                                },
                                onOpenDragEnd = { opened ->
                                    isPlayerOpenDragging = false
                                    if (!opened) {
                                        playerOpenDragOffsetPx = 0f
                                    }
                                },
                                onToggle = { player.toggle() },
                                onReplay = { player.play(activeTrack, activeSession) },
                                onSeek = { player.seekToFraction(it) },
                                onPrevious = { playAdjacent(-1) },
                                onNext = { playAdjacent(1) }
                            )
                        }
                        BottomTabsBar(
                            selectedDestination = selectedDestination,
                            syncInProgress = isBusy,
                            downloadQueueCount = downloadProgressById.size,
                            offlineMode = downloadedOnlyMode,
                            favoriteCount = likedTrackIds.size,
                            onDestinationSelected = { destination ->
                                val reselected = destination == selectedDestination
                                val reselectedSearch =
                                    destination == AppDestination.Search && reselected
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedDestination = destination
                                when (destination) {
                                    AppDestination.Home -> {
                                        selectedTab = LibraryTab.Songs
                                        activeLibraryDetail = null
                                        showPlayer = false
                                        if (reselected) {
                                            coroutineScope.launch { mainListState.animateScrollToItem(0) }
                                        }
                                    }

                                    AppDestination.Search -> {
                                        activeLibraryDetail = null
                                        showPlayer = false
                                        if (reselectedSearch) {
                                            searchFocusRequests += 1
                                        }
                                    }

                                    AppDestination.Player -> {
                                        if (player.currentTrack != null) {
                                            showNowPlayingPlayer()
                                        }
                                    }

                                    AppDestination.Library -> {
                                        activeLibraryDetail = null
                                        showPlayer = false
                                        if (reselected) {
                                            if (
                                                activeLibraryFilters.isNotEmpty() ||
                                                libraryLetterFilter != null ||
                                                librarySearchQuery.isNotBlank()
                                            ) {
                                                activeLibraryFilters = emptySet()
                                                libraryLetterFilter = null
                                                librarySearchQuery = ""
                                            } else {
                                                coroutineScope.launch { mainListState.animateScrollToItem(0) }
                                            }
                                        }
                                    }

                                    AppDestination.Liked -> {
                                        activeLibraryDetail = null
                                        showPlayer = false
                                        if (reselected) {
                                            coroutineScope.launch { mainListState.animateScrollToItem(0) }
                                        }
                                    }

                                    AppDestination.Profile -> {
                                        activeLibraryDetail = null
                                        showPlayer = false
                                    }
                                }
                            },
                            onDestinationLongPress = { destination ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                when (destination) {
                                    AppDestination.Home -> {
                                        selectedDestination = AppDestination.Home
                                        showPlayer = false
                                        activeLibraryDetail = null
                                        coroutineScope.launch { mainListState.animateScrollToItem(0) }
                                    }
                                    AppDestination.Search -> {
                                        selectedDestination = AppDestination.Search
                                        showPlayer = false
                                        activeLibraryDetail = null
                                        searchFocusRequests += 1
                                    }
                                    AppDestination.Library -> {
                                        selectedDestination = AppDestination.Library
                                        showPlayer = false
                                        activeLibraryDetail = null
                                        coroutineScope.launch { mainListState.animateScrollToItem(0) }
                                    }
                                    AppDestination.Liked -> {
                                        val likedTracks = tracks.filter { it.id in likedTrackIds }
                                        if (likedTracks.isNotEmpty()) {
                                            playRandom(likedTracks)
                                        }
                                    }
                                    AppDestination.Profile -> {
                                        selectedDestination = AppDestination.Profile
                                        showPlayer = false
                                        activeLibraryDetail = null
                                        session?.let { loadLibrary(it) }
                                    }
                                    AppDestination.Player -> {
                                        if (player.currentTrack != null) {
                                            showNowPlayingPlayer()
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            val activeTrack = player.currentTrack
            val connectedSession = session
            val isSearchDestination = selectedDestination == AppDestination.Search
            val visibleTracks = remember(tracks, downloadedOnlyMode, offlineDownloads) {
                if (downloadedOnlyMode) {
                    tracks.filter { it.id in offlineDownloads }
                } else {
                    tracks
                }
            }
            val recentTracks = remember(visibleTracks, recentTrackIds) {
                recentTrackIds.mapNotNull { trackId -> visibleTracks.firstOrNull { it.id == trackId } }
            }
            val displayedTracks = remember(visibleTracks, searchQuery, isSearchDestination) {
                if (isSearchDestination) visibleTracks.filterBy(searchQuery) else visibleTracks
            }
            val displayedAlbumGroups = remember(displayedTracks) { displayedTracks.groupByAlbum() }
            val displayedArtistGroups = remember(displayedTracks) { displayedTracks.groupByArtist() }
            val libraryStackedTracks = remember(
                visibleTracks,
                librarySearchQuery,
                activeLibraryFilters,
                likedTrackIds,
                offlineDownloads,
                recentTrackIds
            ) {
                visibleTracks
                    .filterBy(librarySearchQuery)
                    .filterForLibrary(
                        filters = activeLibraryFilters,
                        likedTrackIds = likedTrackIds,
                        downloadedTrackIds = offlineDownloads.keys,
                        recentTrackIds = recentTrackIds
                    )
            }
            val libraryTracks = remember(libraryStackedTracks, librarySortMode, libraryLetterFilter, selectedTab) {
                libraryStackedTracks
                    .sortedForLibrary(librarySortMode)
                    .filterByInitial(if (selectedTab == LibraryTab.Songs) libraryLetterFilter else null) { it.title }
            }
            val libraryAlbumGroups = remember(libraryStackedTracks, librarySortMode, libraryLetterFilter, selectedTab) {
                libraryStackedTracks
                    .groupByAlbum()
                    .sortedGroupsForLibrary(librarySortMode)
                    .filterByInitial(if (selectedTab == LibraryTab.Albums) libraryLetterFilter else null) { it.title }
            }
            val libraryArtistGroups = remember(libraryStackedTracks, librarySortMode, libraryLetterFilter, selectedTab) {
                libraryStackedTracks
                    .groupByArtist()
                    .sortedGroupsForLibrary(librarySortMode)
                    .filterByInitial(if (selectedTab == LibraryTab.Artists) libraryLetterFilter else null) { it.title }
            }
            val libraryCounts = remember(libraryTracks, libraryAlbumGroups, libraryArtistGroups) {
                SearchResultCounts(
                    songs = libraryTracks.size,
                    albums = libraryAlbumGroups.size,
                    artists = libraryArtistGroups.size
                )
            }
            val searchResultCounts = remember(displayedTracks, displayedAlbumGroups, displayedArtistGroups) {
                SearchResultCounts(
                    songs = displayedTracks.size,
                    albums = displayedAlbumGroups.size,
                    artists = displayedArtistGroups.size
                )
            }
            if (showPlayer && activeTrack != null) {
                FullPlayerScreen(
                    track = activeTrack,
                    isPlaying = player.isPlaying,
                    progress = player.progress,
                    visualizerLevels = player.visualizerLevels,
                    status = player.status,
                    queue = playQueue.ifEmpty { visibleTracks.queueStartingAt(activeTrack) },
                    session = connectedSession,
                    modifier = Modifier.padding(innerPadding),
                    onToggle = { player.toggle() },
                    onSeek = { player.seekToFraction(it) },
                    onPrevious = { playAdjacent(-1) },
                    onNext = { playAdjacent(1) },
                    onReplay = { session?.let { player.play(activeTrack, it) } },
                    onScratch = { scratchEngine.playScratch(it) },
                    onScratchEnd = { scratchEngine.stop() },
                    shuffleEnabled = shuffleEnabled,
                    repeatEnabled = repeatEnabled,
                    visualizerEnabled = visualizerEnabled,
                    isFavorite = activeTrack.id in likedTrackIds,
                    onToggleShuffle = { shuffleEnabled = !shuffleEnabled },
                    onToggleRepeat = { repeatEnabled = !repeatEnabled },
                    onToggleFavorite = { toggleLiked(activeTrack) },
                    onQueueTrackClick = ::playQueuedTrack,
                    onQueueMove = ::moveQueueTrack,
                    onQueuePlayNext = ::playTrackNext
                )
            } else {
                LazyColumn(
                    state = mainListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .imePadding(),
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                if (connectedSession == null) {
                    item {
                        SettingsSectionHeader("Connection")
                    }
                    item {
                        ConnectCard(
                            serverUrl = serverUrl,
                            username = username,
                            password = password,
                            isBusy = isBusy,
                            statusText = statusText,
                            onServerUrlChange = { serverUrl = it },
                            onUsernameChange = { username = it },
                            onPasswordChange = { password = it },
                            onConnect = ::connect
                        )
                    }
                    item {
                        SettingsSectionHeader("About")
                    }
                    item {
                        AboutCard()
                    }
                } else {
                    when (selectedDestination) {
                        AppDestination.Profile -> {
                            item {
                                SettingsSummaryCard(
                                    session = connectedSession,
                                    syncedTrackCount = tracks.size,
                                    lastLibrarySyncAt = lastLibrarySyncAt
                                )
                            }
                            item {
                                AccountCard(
                                    session = connectedSession,
                                    onSignOut = ::signOut
                                )
                            }
                            item {
                                LibrarySyncCard(
                                    isBusy = isBusy,
                                    syncedTrackCount = tracks.size,
                                    lastLibrarySyncAt = lastLibrarySyncAt,
                                    onRefresh = { loadLibrary(connectedSession) }
                                )
                            }
                            item {
                                OfflineDownloadsCard(
                                    downloadedTrackCount = offlineDownloads.size,
                                    downloadedBytes = offlineDownloadBytesOnDisk(context, connectedSession),
                                    activeDownloadCount = downloadProgressById.size,
                                    wifiOnly = offlineWifiOnly,
                                    storageLimitMb = offlineStorageLimitMb,
                                    onWifiOnlyChange = { enabled ->
                                        offlineWifiOnly = enabled
                                        saveOfflineWifiOnly(context, enabled)
                                    },
                                    onStorageLimitChange = { limitMb ->
                                        offlineStorageLimitMb = limitMb
                                        saveOfflineStorageLimitMb(context, limitMb)
                                    },
                                    onClearDownloads = ::clearOfflineDownloads
                                )
                            }
                            item {
                                AppearanceCard(
                                    themeMode = themeMode,
                                    useAlbumArtColors = useAlbumArtColors,
                                    visualizerEnabled = visualizerEnabled,
                                    currentTrack = player.currentTrack,
                                    albumAccentColor = albumAccentColor,
                                    onThemeModeChange = { mode ->
                                        themeMode = mode
                                        saveThemeMode(context, mode)
                                    },
                                    onUseAlbumArtColorsChange = { enabled ->
                                        useAlbumArtColors = enabled
                                        saveUseAlbumArtColors(context, enabled)
                                    },
                                    onVisualizerEnabledChange = { enabled ->
                                        visualizerEnabled = enabled
                                        saveVisualizerEnabled(context, enabled)
                                    }
                                )
                            }
                        }

                        AppDestination.Player -> {
                            item {
                                EmptyPlayerCard()
                            }
                        }

                        AppDestination.Liked -> {
                            val likedTracks = visibleTracks.filter { it.id in likedTrackIds }
                            item {
                                LibraryHeader(
                                    title = "Favorites",
                                    showSearch = false,
                                    searchQuery = "",
                                    isBusy = isBusy,
                                    statusText = statusText,
                                    onSearchQueryChange = {},
                                    onRefresh = { loadLibrary(connectedSession) }
                                )
                            }
                            if (likedTracks.isEmpty()) {
                                item {
                                    EmptyLibraryMessage(
                                        isBusy = isBusy,
                                        statusText = statusText.takeIf { tracks.isEmpty() },
                                        emptyText = "No liked tracks yet"
                                    )
                                }
                            } else {
                                items(likedTracks, key = { it.id }) { track ->
                                    TrackRow(
                                        track = track,
                                        session = connectedSession,
                                        isCurrent = player.currentTrack?.id == track.id,
                                        isLiked = track.id in likedTrackIds,
                                        onToggleLiked = { toggleLiked(track) },
                                        onClick = { playTrack(track, openPlayer = true, source = likedTracks) },
                                        isDownloaded = track.id in offlineDownloads,
                                        downloadProgress = downloadProgressById[track.id],
                                        onToggleDownload = { toggleOfflineDownload(track) },
                                        onGoToAlbum = {
                                            openLibraryDetail(LibraryDetail(LibraryCollectionType.Album, track.album))
                                        },
                                        onGoToArtist = {
                                            openLibraryDetail(LibraryDetail(LibraryCollectionType.Artist, track.artist))
                                        },
                                        onPlayNext = { playTrackNext(track) },
                                        onStartRadio = { startRadioFrom(likedTracks, track) }
                                    )
                                }
                            }
                        }

                        else -> {
                            val isHomeTab = selectedDestination == AppDestination.Home
                            val isSearchTab = selectedDestination == AppDestination.Search
                            val emptySearchText = if (isSearchTab && searchQuery.isNotBlank()) {
                                "No results for \"${searchQuery.trim()}\""
                            } else {
                                "No matching music"
                            }
                            if (isHomeTab) {
                                val likedHomeTracks = visibleTracks.filter { it.id in likedTrackIds }
                                val quickTracks = buildHomeQuickTracks(
                                    tracks = visibleTracks,
                                    likedTracks = likedHomeTracks,
                                    currentTrack = activeTrack
                                )
                                val homeMix = visibleTracks.homeMix(seed = todayHomeSeed()).take(14)
                                if (visibleTracks.isEmpty()) {
                                    item {
                                        EmptyLibraryMessage(
                                            isBusy = isBusy,
                                            statusText = statusText,
                                            emptyText = "Your music will appear here"
                                        )
                                    }
                                } else {
                                    item {
                                        HomeQuickGrid(
                                            tracks = quickTracks,
                                            session = connectedSession,
                                            onTrackClick = { track ->
                                                playTrack(track, openPlayer = true, source = visibleTracks)
                                            }
                                        )
                                    }
                                    if (recentTracks.isNotEmpty()) {
                                        item {
                                            HomeTrackShelf(
                                                title = "Recently played",
                                                tracks = recentTracks.take(12),
                                                session = connectedSession,
                                                onTrackClick = { track ->
                                                    playTrack(track, openPlayer = true, source = recentTracks)
                                                }
                                            )
                                        }
                                    }
                                    if (likedHomeTracks.isNotEmpty()) {
                                        item {
                                            HomeTrackShelf(
                                                title = "Favorites",
                                                tracks = likedHomeTracks.take(12),
                                                session = connectedSession,
                                                onTrackClick = { track ->
                                                    playTrack(track, openPlayer = true, source = likedHomeTracks)
                                                }
                                            )
                                        }
                                    }
                                    if (displayedAlbumGroups.isNotEmpty()) {
                                    item {
                                        HomeGroupShelf(
                                            title = "Albums for you",
                                            groups = displayedAlbumGroups.take(12),
                                            session = connectedSession,
                                            artworkShape = RoundedCornerShape(8.dp),
                                            onGroupClick = { group ->
                                                    openLibraryDetail(
                                                        LibraryDetail(LibraryCollectionType.Album, group.title)
                                                    )
                                            }
                                        )
                                    }
                                    }
                                    if (displayedArtistGroups.isNotEmpty()) {
                                        item {
                                            HomeGroupShelf(
                                                title = "Artists you listen to",
                                                groups = displayedArtistGroups.take(12),
                                            session = connectedSession,
                                            artworkShape = CircleShape,
                                            onGroupClick = { group ->
                                                    openLibraryDetail(
                                                        LibraryDetail(LibraryCollectionType.Artist, group.title)
                                                    )
                                            }
                                        )
                                    }
                                    }
                                    if (homeMix.isNotEmpty()) {
                                        item {
                                            HomeTrackShelf(
                                                title = "Made from your library",
                                                tracks = homeMix,
                                                session = connectedSession,
                                                onTrackClick = { track ->
                                                    playTrack(track, openPlayer = true, source = homeMix)
                                                }
                                            )
                                        }
                                    }
                                }
                            } else if (selectedDestination == AppDestination.Library) {
                                val detail = activeLibraryDetail
                                if (detail != null) {
                                    val detailTracks = visibleTracks.tracksForDetail(detail, offlineDownloads.keys)
                                    val detailTitle = detail.titleLabel()
                                    val detailSubtitle = detail.subtitleLabel(detailTracks)
                                    val detailPin = PinnedLibraryItem(detail.type, detail.key)
                                    item {
                                        LibraryDetailHeader(
                                            title = detailTitle,
                                            subtitle = detailSubtitle,
                                            tracks = detailTracks,
                                            session = connectedSession,
                                            artworkShape = if (detail.type == LibraryCollectionType.Artist) CircleShape else RoundedCornerShape(8.dp),
                                            isPinned = pinnedLibraryItems.any { it == detailPin },
                                            onPlay = {
                                                detailTracks.firstOrNull()?.let {
                                                    playTrack(it, openPlayer = true, source = detailTracks)
                                                }
                                            },
                                            onShuffle = { playRandom(detailTracks) },
                                            onTogglePin = { togglePinnedLibraryItem(detailPin) }
                                        )
                                    }
                                    if (detailTracks.isEmpty()) {
                                        item {
                                            EmptyLibraryMessage(
                                                isBusy = false,
                                                statusText = null,
                                                emptyText = if (detail.type == LibraryCollectionType.Downloaded) {
                                                    "No downloaded songs yet"
                                                } else {
                                                    "Nothing here"
                                                }
                                            )
                                        }
                                    } else {
                                        items(detailTracks, key = { it.id }) { track ->
                                            TrackRow(
                                                track = track,
                                                session = connectedSession,
                                                isCurrent = player.currentTrack?.id == track.id,
                                                isLiked = track.id in likedTrackIds,
                                                onToggleLiked = { toggleLiked(track) },
                                                onClick = { playTrack(track, openPlayer = true, source = detailTracks) },
                                                isDownloaded = track.id in offlineDownloads,
                                                downloadProgress = downloadProgressById[track.id],
                                                onToggleDownload = { toggleOfflineDownload(track) },
                                                onGoToAlbum = {
                                                    openLibraryDetail(
                                                        LibraryDetail(LibraryCollectionType.Album, track.album)
                                                    )
                                                },
                                                onGoToArtist = {
                                                    openLibraryDetail(
                                                        LibraryDetail(LibraryCollectionType.Artist, track.artist)
                                                    )
                                                },
                                                onPlayNext = { playTrackNext(track) },
                                                onStartRadio = { startRadioFrom(detailTracks, track) }
                                            )
                                        }
                                    }
                                } else {
                                val emptyLibraryText = if (librarySearchQuery.isNotBlank()) {
                                    "No library results for \"${librarySearchQuery.trim()}\""
                                } else {
                                    "Your music will appear here"
                                }
                                item {
                                    SpotifyLibraryHeader(
                                        totalTracks = visibleTracks.size,
                                        totalAlbums = visibleTracks.groupByAlbum().size,
                                        totalArtists = visibleTracks.groupByArtist().size,
                                        isBusy = isBusy,
                                        statusText = statusText,
                                        lastLibrarySyncAt = lastLibrarySyncAt,
                                        downloadedOnlyMode = downloadedOnlyMode,
                                        onRefresh = { loadLibrary(connectedSession) },
                                        onDownloadedOnlyChange = {
                                            downloadedOnlyMode = !downloadedOnlyMode
                                            libraryLetterFilter = null
                                        }
                                    )
                                }
                                if (pinnedLibraryItems.isNotEmpty()) {
                                    item {
                                        PinnedLibraryShelf(
                                            pinnedItems = pinnedLibraryItems,
                                            tracks = visibleTracks,
                                            downloadedTrackIds = offlineDownloads.keys,
                                            session = connectedSession,
                                            onOpenDetail = ::openLibraryDetail,
                                            onUnpin = ::togglePinnedLibraryItem
                                        )
                                    }
                                }
                                item {
                                    LibraryTabs(
                                        selectedTab = selectedTab,
                                        resultCounts = libraryCounts,
                                        onTabSelected = {
                                            selectedTab = it
                                            libraryLetterFilter = null
                                        }
                                    )
                                }
                                item {
                                    LibraryStackedFilters(
                                        activeFilters = activeLibraryFilters,
                                        onToggleFilter = { filter ->
                                            activeLibraryFilters = if (filter in activeLibraryFilters) {
                                                activeLibraryFilters - filter
                                            } else {
                                                activeLibraryFilters + filter
                                            }
                                            libraryLetterFilter = null
                                        }
                                    )
                                }
                                item {
                                    LibraryToolbar(
                                        searchQuery = librarySearchQuery,
                                        sortMode = librarySortMode,
                                        gridView = libraryGridView,
                                        onSearchQueryChange = { librarySearchQuery = it },
                                        onSortModeChange = { librarySortMode = it },
                                        onGridViewChange = { libraryGridView = it }
                                    )
                                }
                                item {
                                    LibraryAlphabetJumpRow(
                                        letters = availableLettersForTab(
                                            selectedTab = selectedTab,
                                            songs = libraryStackedTracks,
                                            albums = libraryStackedTracks.groupByAlbum(),
                                            artists = libraryStackedTracks.groupByArtist()
                                        ),
                                        selectedLetter = libraryLetterFilter,
                                        onLetterSelected = { letter ->
                                            libraryLetterFilter = if (libraryLetterFilter == letter) null else letter
                                        }
                                    )
                                }

                                when (selectedTab) {
                                    LibraryTab.Songs -> {
                                        if (libraryTracks.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = isBusy,
                                                    statusText = statusText,
                                                    emptyText = emptyLibraryText
                                                )
                                            }
                                        } else {
                                            item {
                                                LibrarySongActions(
                                                    trackCount = libraryTracks.size,
                                                    sortMode = librarySortMode,
                                                    onShuffle = { playRandom(libraryTracks) },
                                                    onPlayFirst = {
                                                        libraryTracks.firstOrNull()?.let {
                                                            playTrack(it, openPlayer = true, source = libraryTracks)
                                                        }
                                                    }
                                                )
                                            }
                                            if (libraryGridView) {
                                                items(
                                                    libraryTracks.chunked(2),
                                                    key = { row -> row.joinToString(separator = "|") { it.id } }
                                                ) { rowTracks ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        rowTracks.forEach { track ->
                                                            LibraryTrackGridCard(
                                                                track = track,
                                                                session = connectedSession,
                                                                isCurrent = player.currentTrack?.id == track.id,
                                                                isDownloaded = track.id in offlineDownloads,
                                                                onClick = {
                                                                    playTrack(track, openPlayer = true, source = libraryTracks)
                                                                },
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                        if (rowTracks.size == 1) {
                                                            Spacer(Modifier.weight(1f))
                                                        }
                                                    }
                                                }
                                            } else {
                                                items(libraryTracks, key = { it.id }) { track ->
                                                    LibraryTrackListRow(
                                                        track = track,
                                                        session = connectedSession,
                                                        isCurrent = player.currentTrack?.id == track.id,
                                                        isLiked = track.id in likedTrackIds,
                                                        isDownloaded = track.id in offlineDownloads,
                                                        downloadProgress = downloadProgressById[track.id],
                                                        onToggleLiked = { toggleLiked(track) },
                                                        onToggleDownload = { toggleOfflineDownload(track) },
                                                        onGoToAlbum = {
                                                            openLibraryDetail(LibraryDetail(LibraryCollectionType.Album, track.album))
                                                        },
                                                        onGoToArtist = {
                                                            openLibraryDetail(LibraryDetail(LibraryCollectionType.Artist, track.artist))
                                                        },
                                                        onPlayNext = { playTrackNext(track) },
                                                        onStartRadio = { startRadioFrom(libraryTracks, track) },
                                                        onClick = {
                                                            playTrack(track, openPlayer = true, source = libraryTracks)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    LibraryTab.Albums -> {
                                        if (libraryAlbumGroups.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = isBusy,
                                                    statusText = statusText,
                                                    emptyText = emptyLibraryText
                                                )
                                            }
                                        } else if (libraryGridView) {
                                            items(
                                                libraryAlbumGroups.chunked(2),
                                                key = { row -> row.joinToString(separator = "|") { it.title } }
                                            ) { rowGroups ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    rowGroups.forEach { group ->
                                                        LibraryGroupGridCard(
                                                            group = group,
                                                            session = connectedSession,
                                                            artworkShape = RoundedCornerShape(8.dp),
                                                            isPinned = pinnedLibraryItems.any {
                                                                it == PinnedLibraryItem(LibraryCollectionType.Album, group.title)
                                                            },
                                                            onTogglePin = {
                                                                togglePinnedLibraryItem(
                                                                    PinnedLibraryItem(LibraryCollectionType.Album, group.title)
                                                                )
                                                            },
                                                            onClick = {
                                                                openLibraryDetail(
                                                                    LibraryDetail(LibraryCollectionType.Album, group.title)
                                                                )
                                                            },
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    if (rowGroups.size == 1) {
                                                        Spacer(Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        } else {
                                            items(libraryAlbumGroups, key = { it.title }) { group ->
                                                LibraryGroupListRow(
                                                    group = group,
                                                    session = connectedSession,
                                                    artworkShape = RoundedCornerShape(8.dp),
                                                    isPinned = pinnedLibraryItems.any {
                                                        it == PinnedLibraryItem(LibraryCollectionType.Album, group.title)
                                                    },
                                                    onTogglePin = {
                                                        togglePinnedLibraryItem(
                                                            PinnedLibraryItem(LibraryCollectionType.Album, group.title)
                                                        )
                                                    },
                                                    onClick = {
                                                        openLibraryDetail(
                                                            LibraryDetail(LibraryCollectionType.Album, group.title)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    LibraryTab.Artists -> {
                                        if (libraryArtistGroups.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = isBusy,
                                                    statusText = statusText,
                                                    emptyText = emptyLibraryText
                                                )
                                            }
                                        } else if (libraryGridView) {
                                            items(
                                                libraryArtistGroups.chunked(2),
                                                key = { row -> row.joinToString(separator = "|") { it.title } }
                                            ) { rowGroups ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    rowGroups.forEach { group ->
                                                        LibraryGroupGridCard(
                                                            group = group,
                                                            session = connectedSession,
                                                            artworkShape = CircleShape,
                                                            isPinned = pinnedLibraryItems.any {
                                                                it == PinnedLibraryItem(LibraryCollectionType.Artist, group.title)
                                                            },
                                                            onTogglePin = {
                                                                togglePinnedLibraryItem(
                                                                    PinnedLibraryItem(LibraryCollectionType.Artist, group.title)
                                                                )
                                                            },
                                                            onClick = {
                                                                openLibraryDetail(
                                                                    LibraryDetail(LibraryCollectionType.Artist, group.title)
                                                                )
                                                            },
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    if (rowGroups.size == 1) {
                                                        Spacer(Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        } else {
                                            items(libraryArtistGroups, key = { it.title }) { group ->
                                                LibraryGroupListRow(
                                                    group = group,
                                                    session = connectedSession,
                                                    artworkShape = CircleShape,
                                                    isPinned = pinnedLibraryItems.any {
                                                        it == PinnedLibraryItem(LibraryCollectionType.Artist, group.title)
                                                    },
                                                    onTogglePin = {
                                                        togglePinnedLibraryItem(
                                                            PinnedLibraryItem(LibraryCollectionType.Artist, group.title)
                                                        )
                                                    },
                                                    onClick = {
                                                        openLibraryDetail(
                                                            LibraryDetail(LibraryCollectionType.Artist, group.title)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                }
                            } else {
                                item {
                                    if (isSearchTab) {
                                        SearchHeader(
                                            searchQuery = searchQuery,
                                            resultCounts = searchResultCounts,
                                            isBusy = isBusy,
                                            statusText = statusText,
                                            focusRequester = searchFocusRequester,
                                            onSearchQueryChange = { searchQuery = it },
                                            onRefresh = { loadLibrary(connectedSession) }
                                        )
                                    } else {
                                        LibraryHeader(
                                            title = "Library",
                                            showSearch = false,
                                            searchQuery = "",
                                            isBusy = isBusy,
                                            statusText = statusText,
                                            onSearchQueryChange = {},
                                            onRefresh = { loadLibrary(connectedSession) }
                                        )
                                    }
                                }
                                item {
                                    LibraryTabs(
                                        selectedTab = selectedTab,
                                        resultCounts = if (isSearchTab) searchResultCounts else null,
                                        onTabSelected = { selectedTab = it }
                                    )
                                }

                                when (selectedTab) {
                                    LibraryTab.Songs -> {
                                        if (displayedTracks.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = isBusy,
                                                    statusText = statusText,
                                                    emptyText = emptySearchText
                                                )
                                            }
                                        } else {
                                            if (selectedDestination == AppDestination.Library) {
                                                item {
                                                    RandomPlayButton(
                                                        label = "Random play",
                                                        trackCount = displayedTracks.size,
                                                        onClick = { playRandom(displayedTracks) }
                                                    )
                                                }
                                            }
                                            items(displayedTracks, key = { it.id }) { track ->
                                                TrackRow(
                                                    track = track,
                                                    session = connectedSession,
                                                    isCurrent = player.currentTrack?.id == track.id,
                                                    isLiked = track.id in likedTrackIds,
                                                    onToggleLiked = { toggleLiked(track) },
                                                    onClick = { playTrack(track, openPlayer = true, source = displayedTracks) },
                                                    isDownloaded = track.id in offlineDownloads,
                                                    downloadProgress = downloadProgressById[track.id],
                                                    onToggleDownload = { toggleOfflineDownload(track) },
                                                    onGoToAlbum = {
                                                        openLibraryDetail(LibraryDetail(LibraryCollectionType.Album, track.album))
                                                    },
                                                    onGoToArtist = {
                                                        openLibraryDetail(LibraryDetail(LibraryCollectionType.Artist, track.artist))
                                                    },
                                                    onPlayNext = { playTrackNext(track) },
                                                    onStartRadio = { startRadioFrom(displayedTracks, track) }
                                                )
                                            }
                                        }
                                    }

                                    LibraryTab.Albums -> {
                                        val groups = displayedAlbumGroups
                                        if (groups.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = isBusy,
                                                    statusText = statusText,
                                                    emptyText = emptySearchText
                                                )
                                            }
                                        } else {
                                            items(groups, key = { it.title }) { group ->
                                                GroupRow(
                                                    group = group,
                                                    session = connectedSession,
                                                    onClick = {
                                                        openLibraryDetail(
                                                            LibraryDetail(LibraryCollectionType.Album, group.title)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    LibraryTab.Artists -> {
                                        val groups = displayedArtistGroups
                                        if (groups.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = isBusy,
                                                    statusText = statusText,
                                                    emptyText = emptySearchText
                                                )
                                            }
                                        } else {
                                            items(groups, key = { it.title }) { group ->
                                                GroupRow(
                                                    group = group,
                                                    session = connectedSession,
                                                    onClick = {
                                                        openLibraryDetail(
                                                            LibraryDetail(LibraryCollectionType.Artist, group.title)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 6.dp, top = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun AboutCard() {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "App",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            SettingsInfoRow(
                title = "Version",
                value = "0.1.0"
            )
        }
    }
}

@Composable
private fun SettingsSummaryCard(
    session: JellyfinSession,
    lastLibrarySyncAt: Long?,
    syncedTrackCount: Int
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = session.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = session.serverUrl.toHostLabel(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${syncedTrackCount.countLabel("song")} synced - ${formatLastLibrarySync(lastLibrarySyncAt)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AccountCard(
    session: JellyfinSession,
    onSignOut: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            SettingsInfoRow(
                title = "Signed in",
                value = session.username
            )
            SettingsInfoRow(
                title = "Server",
                value = session.serverUrl.toHostLabel()
            )
            TextButton(
                onClick = onSignOut,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Sign out")
            }
        }
    }
}

@Composable
private fun LibrarySyncCard(
    isBusy: Boolean,
    syncedTrackCount: Int,
    lastLibrarySyncAt: Long?,
    onRefresh: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Library sync",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Sync stores song metadata only. Audio stays on your Jellyfin server unless you choose to download it later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            SettingsInfoRow(title = "Synced songs", value = syncedTrackCount.toString())
            SettingsInfoRow(title = "Last sync", value = formatLastLibrarySync(lastLibrarySyncAt))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onRefresh,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isBusy) "Syncing" else "Sync library")
                }
            }
            if (isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun OfflineDownloadsCard(
    downloadedTrackCount: Int,
    downloadedBytes: Long,
    activeDownloadCount: Int,
    wifiOnly: Boolean,
    storageLimitMb: Int,
    onWifiOnlyChange: (Boolean) -> Unit,
    onStorageLimitChange: (Int) -> Unit,
    onClearDownloads: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Offline downloads",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Downloads are manual per song. The app will not auto-download your library.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            SettingsInfoRow(title = "Saved songs", value = downloadedTrackCount.toString())
            SettingsInfoRow(title = "Storage used", value = formatDataSize(downloadedBytes))
            SettingsInfoRow(title = "Storage limit", value = formatStorageLimit(storageLimitMb))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Wi-Fi only",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (wifiOnly) "Mobile data is protected" else "Downloads can use mobile data",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = wifiOnly,
                    onCheckedChange = onWifiOnlyChange
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Limit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(OfflineStorageLimitOptionsMb) { limitMb ->
                        FilterChip(
                            selected = storageLimitMb == limitMb,
                            onClick = { onStorageLimitChange(limitMb) },
                            label = { Text(formatStorageLimit(limitMb)) }
                        )
                    }
                }
            }
            if (activeDownloadCount > 0) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    text = "${activeDownloadCount.countLabel("download")} running",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onClearDownloads,
                    enabled = downloadedTrackCount > 0 && activeDownloadCount == 0,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Clear downloads")
                }
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.width(18.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AppearanceCard(
    themeMode: AppThemeMode,
    useAlbumArtColors: Boolean,
    visualizerEnabled: Boolean,
    currentTrack: MusicTrack?,
    albumAccentColor: Color?,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onUseAlbumArtColorsChange: (Boolean) -> Unit,
    onVisualizerEnabledChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                ThemeModeControl(
                    selectedMode = themeMode,
                    onModeSelected = onThemeModeChange
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = albumAccentColor ?: MaterialTheme.colorScheme.primary,
                    tonalElevation = 3.dp
                ) {
                    Box(Modifier.fillMaxSize())
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Album art colors",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = currentTrack?.let { "Sampling ${it.title}" } ?: "Start a song to sample artwork",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = useAlbumArtColors,
                    onCheckedChange = onUseAlbumArtColorsChange
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        VisualizerSettingIcon(color = MaterialTheme.colorScheme.primary)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Visualizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (visualizerEnabled) "Shown on Now Playing" else "Hidden on Now Playing",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = visualizerEnabled,
                    onCheckedChange = onVisualizerEnabledChange
                )
            }
        }
    }
}

@Composable
private fun ThemeModeControl(
    selectedMode: AppThemeMode,
    onModeSelected: (AppThemeMode) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppThemeMode.entries.forEach { mode ->
                val selected = selectedMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { onModeSelected(mode) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualizerSettingIcon(color: Color) {
    val transition = rememberInfiniteTransition(label = "settings-visualizer")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "settings-visualizer-phase"
    )
    Canvas(modifier = Modifier.size(28.dp, 18.dp)) {
        val barCount = 9
        val step = size.width / barCount
        val baseline = size.height * 0.82f
        for (index in 0 until barCount) {
            val level = 0.3f + ((sin(phase + index * 0.7f) + 1f) / 2f) * 0.62f
            val x = step * index + step / 2f
            drawLine(
                color = color.copy(alpha = 0.72f),
                start = Offset(x, baseline),
                end = Offset(x, baseline - size.height * level),
                strokeWidth = 2.3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun EmptyPlayerCard() {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarSineVisualizer(color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = "No song playing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Choose a song",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BottomTabsBar(
    selectedDestination: AppDestination,
    syncInProgress: Boolean,
    downloadQueueCount: Int,
    offlineMode: Boolean,
    favoriteCount: Int,
    onDestinationSelected: (AppDestination) -> Unit,
    onDestinationLongPress: (AppDestination) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f),
        tonalElevation = 2.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            val showAllLabels = maxWidth >= 560.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTabDestinations.forEach { destination ->
                    val selected = selectedDestination == destination
                    BottomTabItem(
                        destination = destination,
                        selected = selected,
                        showLabel = showAllLabels || selected,
                        badgeText = bottomTabBadge(
                            destination = destination,
                            syncInProgress = syncInProgress,
                            downloadQueueCount = downloadQueueCount,
                            offlineMode = offlineMode,
                            favoriteCount = favoriteCount
                        ),
                        onClick = { onDestinationSelected(destination) },
                        onLongClick = { onDestinationLongPress(destination) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomTabItem(
    destination: AppDestination,
    selected: Boolean,
    showLabel: Boolean,
    badgeText: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
        },
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "bottomTabContentColor"
    )
    val selectedScale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "bottomTabScale"
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .scale(selectedScale)
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Icon(
                    imageVector = destinationIcon(destination),
                    contentDescription = destination.label,
                    tint = contentColor,
                    modifier = Modifier.size(if (selected) 23.dp else 22.dp)
                )
                if (badgeText != null) {
                    BottomTabBadge(
                        text = badgeText,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
            AnimatedVisibility(
                visible = showLabel,
                enter = fadeIn(animationSpec = tween(120)),
                exit = fadeOut(animationSpec = tween(90))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomTabBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .offset(x = 8.dp, y = (-7).dp)
            .height(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = if (text.length == 1) 5.dp else 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

private fun bottomTabBadge(
    destination: AppDestination,
    syncInProgress: Boolean,
    downloadQueueCount: Int,
    offlineMode: Boolean,
    favoriteCount: Int
): String? =
    when (destination) {
        AppDestination.Library -> when {
            downloadQueueCount > 0 -> downloadQueueCount.coerceAtMost(99).toString()
            offlineMode -> "!"
            else -> null
        }
        AppDestination.Liked -> favoriteCount.takeIf { it > 0 }?.coerceAtMost(99)?.toString()
        AppDestination.Profile -> if (syncInProgress) "!" else null
        else -> null
    }

private fun destinationIcon(destination: AppDestination): ImageVector =
    when (destination) {
        AppDestination.Home -> Icons.Filled.Home
        AppDestination.Search -> Icons.Filled.Search
        AppDestination.Player -> Icons.Filled.PlayArrow
        AppDestination.Library -> PlayerIconVectors.Library
        AppDestination.Liked -> Icons.Filled.Favorite
        AppDestination.Profile -> Icons.Filled.Settings
    }

@Composable
private fun ConnectCard(
    serverUrl: String,
    username: String,
    password: String,
    isBusy: Boolean,
    statusText: String?,
    onServerUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Connect Jellyfin",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = serverUrl,
                onValueChange = onServerUrlChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Server URL") },
                placeholder = { Text("https://jellyfin.example.com") }
            )
            Text(
                text = "Use the server root URL, not the /web page.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Username") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            if (statusText != null) {
                Text(
                    text = statusText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Button(
                onClick = onConnect,
                enabled = !isBusy,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isBusy) "Connecting" else "Connect")
            }
            if (isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LibraryHeader(
    title: String,
    showSearch: Boolean,
    searchQuery: String,
    isBusy: Boolean,
    statusText: String?,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (statusText != null) {
                    Text(
                        text = statusText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            TextButton(onClick = onRefresh, enabled = !isBusy) {
                Text("Refresh")
            }
        }
        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search music") }
            )
        }
        if (isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SearchHeader(
    searchQuery: String,
    resultCounts: SearchResultCounts,
    isBusy: Boolean,
    statusText: String?,
    focusRequester: FocusRequester,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (statusText != null) {
                    Text(
                        text = statusText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            TextButton(onClick = onRefresh, enabled = !isBusy) {
                Text("Refresh")
            }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            placeholder = { Text("Songs, albums, artists") },
            supportingText = {
                Text(
                    text = resultCounts.summary(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            shape = RoundedCornerShape(24.dp)
        )
        if (isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun LibraryTabs(
    selectedTab: LibraryTab,
    resultCounts: SearchResultCounts? = null,
    onTabSelected: (LibraryTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LibraryTab.entries.forEach { tab ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        text = resultCounts?.let { "${tab.label} ${it.countFor(tab)}" } ?: tab.label,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
private fun SpotifyLibraryHeader(
    totalTracks: Int,
    totalAlbums: Int,
    totalArtists: Int,
    isBusy: Boolean,
    statusText: String?,
    lastLibrarySyncAt: Long?,
    downloadedOnlyMode: Boolean,
    onRefresh: () -> Unit,
    onDownloadedOnlyChange: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${totalTracks.countLabel("song")} - ${totalAlbums.countLabel("album")} - ${totalArtists.countLabel("artist")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(onClick = onRefresh, enabled = !isBusy) {
                Text(if (isBusy) "Syncing" else "Sync")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Last sync ${formatLastLibrarySync(lastLibrarySyncAt).lowercase(Locale.getDefault())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (statusText != null) {
                Text(
                    text = statusText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        FilterChip(
            selected = downloadedOnlyMode,
            onClick = onDownloadedOnlyChange,
            label = { Text(if (downloadedOnlyMode) "Downloaded-only mode on" else "Show downloaded only") },
            leadingIcon = {
                Icon(
                    imageVector = if (downloadedOnlyMode) PlayerIconVectors.DownloadDone else PlayerIconVectors.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        if (isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun PinnedLibraryShelf(
    pinnedItems: List<PinnedLibraryItem>,
    tracks: List<MusicTrack>,
    downloadedTrackIds: Set<String>,
    session: JellyfinSession?,
    onOpenDetail: (LibraryDetail) -> Unit,
    onUnpin: (PinnedLibraryItem) -> Unit
) {
    val resolvedItems = remember(pinnedItems, tracks, downloadedTrackIds) {
        pinnedItems.mapNotNull { it.resolvePinnedItem(tracks, downloadedTrackIds) }
    }
    if (resolvedItems.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pinned",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${resolvedItems.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(resolvedItems, key = { "${it.item.type.name}:${it.item.key}" }) { resolved ->
                Surface(
                    onClick = { onOpenDetail(resolved.detail) },
                    modifier = Modifier.width(158.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(Modifier.padding(10.dp)) {
                        AlbumArtworkImage(
                            track = resolved.artworkTrack,
                            session = session,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(resolved.shape),
                            imageSize = 220,
                            imageQuality = 80
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = resolved.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = resolved.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(
                            onClick = { onUnpin(resolved.item) },
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                        ) {
                            Text("Unpin")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryStackedFilters(
    activeFilters: Set<LibraryFilter>,
    onToggleFilter: (LibraryFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(LibraryFilter.entries) { filter ->
            FilterChip(
                selected = filter in activeFilters,
                onClick = { onToggleFilter(filter) },
                label = { Text(filter.label) }
            )
        }
    }
}

@Composable
private fun LibraryAlphabetJumpRow(
    letters: List<Char>,
    selectedLetter: Char?,
    onLetterSelected: (Char) -> Unit
) {
    if (letters.isEmpty()) return
    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items(letters, key = { it }) { letter ->
            FilterChip(
                selected = selectedLetter == letter,
                onClick = { onLetterSelected(letter) },
                label = {
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun LibraryDetailHeader(
    title: String,
    subtitle: String,
    tracks: List<MusicTrack>,
    session: JellyfinSession?,
    artworkShape: Shape,
    isPinned: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onTogglePin: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtworkImage(
                track = tracks.firstOrNull(),
                session = session,
                modifier = Modifier
                    .size(96.dp)
                    .clip(artworkShape),
                imageSize = 260,
                imageQuality = 82
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onPlay,
                enabled = tracks.isNotEmpty(),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = PlayerGlyph.Play.imageVector(),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Play")
            }
            TextButton(
                onClick = onShuffle,
                enabled = tracks.size > 1,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = PlayerGlyph.Shuffle.imageVector(),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Shuffle")
            }
            IconButton(onClick = onTogglePin) {
                Icon(
                    imageVector = if (isPinned) PlayerIconVectors.PinFilled else PlayerIconVectors.Pin,
                    contentDescription = if (isPinned) "Unpin" else "Pin",
                    tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun LibraryToolbar(
    searchQuery: String,
    sortMode: LibrarySortMode,
    gridView: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSortModeChange: (LibrarySortMode) -> Unit,
    onGridViewChange: (Boolean) -> Unit
) {
    var sortExpanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear library search"
                        )
                    }
                }
            },
            placeholder = { Text("Find in Your Library") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(24.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                TextButton(onClick = { sortExpanded = true }) {
                    Text(
                        text = "Sort: ${sortMode.label}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    LibrarySortMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.label) },
                            onClick = {
                                sortExpanded = false
                                onSortModeChange(mode)
                            }
                        )
                    }
                }
            }
            TextButton(onClick = { onGridViewChange(!gridView) }) {
                Text(if (gridView) "List view" else "Grid view")
            }
        }
    }
}

@Composable
private fun LibrarySongActions(
    trackCount: Int,
    sortMode: LibrarySortMode,
    onShuffle: () -> Unit,
    onPlayFirst: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onShuffle,
            enabled = trackCount > 0,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = PlayerGlyph.Shuffle.imageVector(),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Shuffle")
        }
        TextButton(
            onClick = onPlayFirst,
            enabled = trackCount > 0,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = PlayerGlyph.Play.imageVector(),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(sortMode.label)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryTrackListRow(
    track: MusicTrack,
    session: JellyfinSession?,
    isCurrent: Boolean,
    isLiked: Boolean,
    isDownloaded: Boolean,
    downloadProgress: OfflineDownloadProgress?,
    onToggleLiked: () -> Unit,
    onToggleDownload: () -> Unit,
    onClick: () -> Unit,
    onGoToAlbum: (() -> Unit)? = null,
    onGoToArtist: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onStartRadio: (() -> Unit)? = null
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
                    else Color.Transparent
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { actionsExpanded = true }
                )
                .padding(horizontal = 4.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtworkImage(
                track = track,
                session = session,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(6.dp)),
                imageSize = 180,
                imageQuality = 78
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} - ${track.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrent) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onToggleLiked,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else PlayerIconVectors.FavoriteOutline,
                    contentDescription = if (isLiked) "Unlike track" else "Like track",
                    tint = if (isLiked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
            OfflineTrackButton(
                isDownloaded = isDownloaded,
                progress = downloadProgress,
                onClick = onToggleDownload,
                modifier = Modifier.size(40.dp)
            )
        }
        TrackActionsMenu(
            expanded = actionsExpanded,
            isLiked = isLiked,
            isDownloaded = isDownloaded,
            onDismiss = { actionsExpanded = false },
            onToggleLiked = onToggleLiked,
            onToggleDownload = onToggleDownload,
            onGoToAlbum = onGoToAlbum,
            onGoToArtist = onGoToArtist,
            onPlayNext = onPlayNext,
            onStartRadio = onStartRadio
        )
    }
}

@Composable
private fun LibraryGroupListRow(
    group: LibraryGroup,
    session: JellyfinSession?,
    artworkShape: Shape,
    onClick: () -> Unit,
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArtworkImage(
            track = group.tracks.firstOrNull(),
            session = session,
            modifier = Modifier
                .size(58.dp)
                .clip(artworkShape),
            imageSize = 200,
            imageQuality = 78
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = group.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onTogglePin != null) {
            IconButton(
                onClick = onTogglePin,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isPinned) PlayerIconVectors.PinFilled else PlayerIconVectors.Pin,
                    contentDescription = if (isPinned) "Unpin" else "Pin",
                    tint = if (isPinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                    },
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Composable
private fun LibraryTrackGridCard(
    track: MusicTrack,
    session: JellyfinSession?,
    isCurrent: Boolean,
    isDownloaded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp)
    ) {
        AlbumArtworkImage(
            track = track,
            session = session,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            imageSize = 260,
            imageQuality = 80
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isDownloaded) {
            Text(
                text = "Downloaded",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LibraryGroupGridCard(
    group: LibraryGroup,
    session: JellyfinSession?,
    artworkShape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    onTogglePin: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 8.dp)
    ) {
        AlbumArtworkImage(
            track = group.tracks.firstOrNull(),
            session = session,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(artworkShape),
            imageSize = 280,
            imageQuality = 80
        )
        if (onTogglePin != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isPinned) PlayerIconVectors.PinFilled else PlayerIconVectors.Pin,
                        contentDescription = if (isPinned) "Unpin" else "Pin",
                        tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = group.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = group.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeQuickGrid(
    tracks: List<MusicTrack>,
    session: JellyfinSession?,
    onTrackClick: (MusicTrack) -> Unit
) {
    if (tracks.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tracks.chunked(2).forEach { rowTracks ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTracks.forEach { track ->
                    HomeQuickTile(
                        track = track,
                        session = session,
                        onClick = { onTrackClick(track) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowTracks.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun HomeQuickTile(
    track: MusicTrack,
    session: JellyfinSession?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AlbumArtworkImage(
                track = track,
                session = session,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                imageSize = 180,
                imageQuality = 76
            )
            Text(
                text = track.title,
                modifier = Modifier.padding(horizontal = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeTrackShelf(
    title: String,
    tracks: List<MusicTrack>,
    session: JellyfinSession?,
    onTrackClick: (MusicTrack) -> Unit
) {
    if (tracks.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeSectionTitle(title)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(tracks, key = { it.id }) { track ->
                HomeTrackCard(
                    track = track,
                    session = session,
                    onClick = { onTrackClick(track) }
                )
            }
        }
    }
}

@Composable
private fun HomeTrackCard(
    track: MusicTrack,
    session: JellyfinSession?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(136.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 4.dp)
    ) {
        AlbumArtworkImage(
            track = track,
            session = session,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp)),
            imageSize = 240,
            imageQuality = 78
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = track.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = track.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeGroupShelf(
    title: String,
    groups: List<LibraryGroup>,
    session: JellyfinSession?,
    artworkShape: Shape,
    onGroupClick: (LibraryGroup) -> Unit
) {
    if (groups.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeSectionTitle(title)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(groups, key = { it.title }) { group ->
                HomeGroupCard(
                    group = group,
                    session = session,
                    artworkShape = artworkShape,
                    onClick = { onGroupClick(group) }
                )
            }
        }
    }
}

@Composable
private fun HomeGroupCard(
    group: LibraryGroup,
    session: JellyfinSession?,
    artworkShape: Shape,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(142.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(bottom = 4.dp)
    ) {
        AlbumArtworkImage(
            track = group.tracks.firstOrNull(),
            session = session,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(artworkShape),
            imageSize = 260,
            imageQuality = 78
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = group.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = group.subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun RandomPlayButton(
    label: String,
    trackCount: Int,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = trackCount > 0,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Icon(
            imageVector = PlayerGlyph.Shuffle.imageVector(),
            contentDescription = null,
            modifier = Modifier.size(19.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label ($trackCount)",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun OfflineTrackButton(
    isDownloaded: Boolean,
    progress: OfflineDownloadProgress?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    if (onClick == null) return

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (progress != null) {
            Text(
                text = progress.percentLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        } else {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isDownloaded) {
                        PlayerIconVectors.DownloadDone
                    } else {
                        PlayerIconVectors.Download
                    },
                    contentDescription = if (isDownloaded) {
                        "Remove download"
                    } else {
                        "Download track"
                    },
                    tint = if (isDownloaded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TrackActionsMenu(
    expanded: Boolean,
    isLiked: Boolean,
    isDownloaded: Boolean,
    onDismiss: () -> Unit,
    onToggleLiked: () -> Unit,
    onToggleDownload: (() -> Unit)?,
    onGoToAlbum: (() -> Unit)?,
    onGoToArtist: (() -> Unit)?,
    onPlayNext: (() -> Unit)?,
    onStartRadio: (() -> Unit)?
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (onGoToAlbum != null) {
            DropdownMenuItem(
                text = { Text("Go to album") },
                onClick = {
                    onDismiss()
                    onGoToAlbum()
                }
            )
        }
        if (onGoToArtist != null) {
            DropdownMenuItem(
                text = { Text("Go to artist") },
                onClick = {
                    onDismiss()
                    onGoToArtist()
                }
            )
        }
        if (onPlayNext != null) {
            DropdownMenuItem(
                text = { Text("Play next") },
                onClick = {
                    onDismiss()
                    onPlayNext()
                }
            )
        }
        if (onToggleDownload != null) {
            DropdownMenuItem(
                text = { Text(if (isDownloaded) "Remove download" else "Download") },
                onClick = {
                    onDismiss()
                    onToggleDownload()
                }
            )
        }
        DropdownMenuItem(
            text = { Text(if (isLiked) "Remove from liked" else "Like song") },
            onClick = {
                onDismiss()
                onToggleLiked()
            }
        )
        if (onStartRadio != null) {
            DropdownMenuItem(
                text = { Text("Start radio") },
                onClick = {
                    onDismiss()
                    onStartRadio()
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackRow(
    track: MusicTrack,
    session: JellyfinSession?,
    isCurrent: Boolean,
    isLiked: Boolean,
    onToggleLiked: () -> Unit,
    onClick: () -> Unit,
    isDownloaded: Boolean = false,
    downloadProgress: OfflineDownloadProgress? = null,
    onToggleDownload: (() -> Unit)? = null,
    onGoToAlbum: (() -> Unit)? = null,
    onGoToArtist: (() -> Unit)? = null,
    onPlayNext: (() -> Unit)? = null,
    onStartRadio: (() -> Unit)? = null
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { actionsExpanded = true }
            ),
        shape = RoundedCornerShape(20.dp),
        color = if (isCurrent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        tonalElevation = if (isCurrent) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumTile(track = track, session = session, modifier = Modifier.size(48.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} - ${track.album}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onToggleLiked,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else PlayerIconVectors.FavoriteOutline,
                    contentDescription = if (isLiked) "Unlike track" else "Like track",
                    tint = if (isLiked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(22.dp)
                )
            }
            OfflineTrackButton(
                isDownloaded = isDownloaded,
                progress = downloadProgress,
                onClick = onToggleDownload,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = formatDuration(track.durationMs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
        TrackActionsMenu(
            expanded = actionsExpanded,
            isLiked = isLiked,
            isDownloaded = isDownloaded,
            onDismiss = { actionsExpanded = false },
            onToggleLiked = onToggleLiked,
            onToggleDownload = onToggleDownload,
            onGoToAlbum = onGoToAlbum,
            onGoToArtist = onGoToArtist,
            onPlayNext = onPlayNext,
            onStartRadio = onStartRadio
        )
    }
}

@Composable
private fun GroupRow(
    group: LibraryGroup,
    session: JellyfinSession?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumTile(track = group.tracks.firstOrNull(), session = session, modifier = Modifier.size(48.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = group.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AlbumTile(
    track: MusicTrack?,
    session: JellyfinSession?,
    modifier: Modifier = Modifier
) {
    AlbumArtworkImage(
        track = track,
        session = session,
        modifier = modifier.clip(RoundedCornerShape(14.dp)),
        imageSize = 160,
        imageQuality = 74
    )
}

@Composable
private fun AlbumArtworkImage(
    track: MusicTrack?,
    session: JellyfinSession?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    imageSize: Int = 512,
    imageQuality: Int = 86,
    networkDelayMs: Long = 0L
) {
    val imageUrl = track?.imageUrl(session, size = imageSize, quality = imageQuality)
    val bitmap by rememberAlbumBitmap(imageUrl, session?.token, networkDelayMs)
    Box(modifier = modifier) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            GeneratedAlbumTile(
                tint = track?.tint ?: SeedPrimary,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun GeneratedAlbumTile(tint: Color, modifier: Modifier = Modifier) {
    val centerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    Canvas(
        modifier = modifier
            .background(tint.copy(alpha = 0.18f))
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(tint.copy(alpha = 0.92f), tint.copy(alpha = 0.2f)),
                center = Offset(size.width * 0.33f, size.height * 0.28f),
                radius = size.minDimension
            ),
            radius = size.minDimension * 0.58f,
            center = Offset(size.width * 0.48f, size.height * 0.48f)
        )
        drawCircle(
            color = centerColor,
            radius = size.minDimension * 0.12f,
            center = center
        )
    }
}

@Composable
private fun rememberAlbumBitmap(
    imageUrl: String?,
    token: String?,
    networkDelayMs: Long = 0L
): State<Bitmap?> {
    val context = LocalContext.current.applicationContext
    var bitmap by remember(imageUrl) {
        mutableStateOf(imageUrl?.let { AlbumArtCache[it] })
    }

    LaunchedEffect(imageUrl, token, networkDelayMs) {
        if (imageUrl == null) {
            bitmap = null
            return@LaunchedEffect
        }
        val cachedBitmap = AlbumArtCache[imageUrl]
        if (cachedBitmap != null) {
            bitmap = cachedBitmap
            return@LaunchedEffect
        }
        if (networkDelayMs > 0L) {
            delay(networkDelayMs)
            AlbumArtCache[imageUrl]?.let {
                bitmap = it
                return@LaunchedEffect
            }
        }
        bitmap = loadAlbumBitmap(context, imageUrl, token)
    }

    return rememberUpdatedState(bitmap)
}

private suspend fun loadAlbumBitmap(context: Context, imageUrl: String, token: String?): Bitmap? = withContext(Dispatchers.IO) {
    loadAlbumBitmapBlocking(context, imageUrl, token)
}

private fun loadAlbumBitmapBlocking(context: Context, imageUrl: String, token: String?): Bitmap? {
    AlbumArtCache[imageUrl]?.let { return it }
    loadAlbumBitmapFromDisk(context, imageUrl)?.let { bitmap ->
        AlbumArtCache[imageUrl] = bitmap
        return bitmap
    }
    runCatching {
        val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6_000
            readTimeout = 10_000
            setRequestProperty("Accept", "image/*")
            token?.let { setRequestProperty("X-Emby-Token", it) }
        }
        try {
            if (connection.responseCode !in 200..299) {
                null
            } else {
                connection.inputStream.use { input ->
                    val bytes = input.readBytes()
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.also {
                        saveAlbumBitmapToDisk(context, imageUrl, bytes)
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()?.also { AlbumArtCache[imageUrl] = it }
    return AlbumArtCache[imageUrl]
}

private fun loadAlbumBitmapFromDisk(context: Context, imageUrl: String): Bitmap? {
    val file = albumArtCacheFile(context, imageUrl)
    if (!file.isFile) return null
    return BitmapFactory.decodeFile(file.absolutePath)?.also {
        file.setLastModified(System.currentTimeMillis())
    }
}

private fun saveAlbumBitmapToDisk(context: Context, imageUrl: String, bytes: ByteArray) {
    runCatching {
        val file = albumArtCacheFile(context, imageUrl)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        file.setLastModified(System.currentTimeMillis())
        pruneAlbumArtCache(file.parentFile)
    }
}

private fun albumArtCacheFile(context: Context, imageUrl: String): File =
    File(File(context.cacheDir, ALBUM_ART_CACHE_DIR), "${stableCacheKey(imageUrl)}.img")

private fun pruneAlbumArtCache(directory: File?) {
    val files = directory
        ?.listFiles()
        ?.filter { it.isFile }
        ?: return
    if (files.size <= MAX_ALBUM_ART_CACHE_FILES) return
    files
        .sortedBy { it.lastModified() }
        .take(files.size - MAX_ALBUM_ART_CACHE_FILES)
        .forEach { it.delete() }
}

@Composable
private fun EmptyLibraryMessage(
    isBusy: Boolean,
    statusText: String?,
    emptyText: String = "No matching music"
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            text = when {
                isBusy -> "Loading library"
                statusText != null -> statusText
                else -> emptyText
            },
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = if (statusText != null && !isBusy) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun Modifier.swipeDownToDismiss(
    onDismiss: () -> Unit,
    startZone: Dp,
    dismissDistance: Dp = 88.dp,
    onDragStart: () -> Unit = {},
    onDragOffsetChange: (Float) -> Unit = {},
    onDragEnd: (dismissed: Boolean) -> Unit = {}
): Modifier = pointerInput(startZone, dismissDistance) {
    val startZonePx = startZone.toPx()
    val dismissDistancePx = dismissDistance.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
        val startsInTopZone = down.position.y <= startZonePx
        var totalDragX = 0f
        var totalDragY = 0f
        var dismissed = false
        var draggingDismiss = false

        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) {
                break
            }

            if (startsInTopZone) {
                val dragAmount = change.position - change.previousPosition
                totalDragX += dragAmount.x
                totalDragY += dragAmount.y
                val downwardDrag = totalDragY > 0f && totalDragY > abs(totalDragX) * 0.6f
                if (downwardDrag || draggingDismiss) {
                    if (!draggingDismiss) {
                        draggingDismiss = true
                        onDragStart()
                    }
                    onDragOffsetChange(totalDragY.coerceAtLeast(0f))
                    change.consume()
                }
            }
        }

        if (draggingDismiss) {
            dismissed = totalDragY > dismissDistancePx && totalDragY > abs(totalDragX) * 1.2f
            onDragEnd(dismissed)
            if (dismissed) {
                onDismiss()
            }
        }
    }
}

@Composable
private fun FullPlayerScreen(
    track: MusicTrack,
    isPlaying: Boolean,
    progress: Float,
    visualizerLevels: FloatArray,
    status: String,
    queue: List<MusicTrack>,
    session: JellyfinSession?,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onSeek: (Float) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    onScratch: (Float) -> Unit,
    onScratchEnd: () -> Unit,
    shuffleEnabled: Boolean,
    repeatEnabled: Boolean,
    visualizerEnabled: Boolean,
    isFavorite: Boolean,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onQueueTrackClick: (MusicTrack) -> Unit,
    onQueueMove: (Int, Int) -> Unit,
    onQueuePlayNext: (MusicTrack) -> Unit
) {
    var showQueue by remember { mutableStateOf(false) }
    val displayQueue = queue.ifEmpty { listOf(track) }
    val colorScheme = MaterialTheme.colorScheme
    val playerBackground = Brush.verticalGradient(
        colors = listOf(
            colorScheme.background,
            blendColors(colorScheme.primary, colorScheme.background, 0.9f),
            colorScheme.background
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(playerBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DiscAlbumStage(
                track = track,
                isPlaying = isPlaying,
                progress = progress,
                session = session,
                onSeek = onSeek,
                onScratch = onScratch,
                onScratchEnd = onScratchEnd
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(42.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${track.artist} - ${track.album}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                FavoriteHeartButton(
                    isFavorite = isFavorite,
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(42.dp)
                )
            }
            if (visualizerEnabled) {
                Spacer(Modifier.height(16.dp))
                AudioBarsVisualizer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    active = isPlaying,
                    levels = visualizerLevels
                )
                Spacer(Modifier.height(2.dp))
            } else {
                Spacer(Modifier.height(20.dp))
            }
            WavySeekBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                onSeek = onSeek
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration((track.durationMs * progress.coerceIn(0f, 1f)).toLong()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(track.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(108.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundedPlaybackButton(
                        icon = PlayerGlyph.Shuffle,
                        contentDescription = if (shuffleEnabled) "Shuffle on" else "Shuffle",
                        active = shuffleEnabled,
                        onClick = onToggleShuffle,
                        modifier = Modifier.size(58.dp)
                    )
                    RoundedPlaybackButton(
                        icon = PlayerGlyph.Previous,
                        contentDescription = "Previous track",
                        onClick = onPrevious,
                        modifier = Modifier.size(58.dp)
                    )
                    RoundedPlaybackButton(
                        icon = if (isPlaying) PlayerGlyph.Pause else PlayerGlyph.Play,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        onClick = if (!isPlaying && status == "Ended") onReplay else onToggle,
                        prominent = true,
                        modifier = Modifier.size(88.dp)
                    )
                    RoundedPlaybackButton(
                        icon = PlayerGlyph.Next,
                        contentDescription = "Next track",
                        onClick = onNext,
                        modifier = Modifier.size(58.dp)
                    )
                    RoundedPlaybackButton(
                        icon = PlayerGlyph.Repeat,
                        contentDescription = if (repeatEnabled) "Repeat on" else "Repeat",
                        active = repeatEnabled,
                        onClick = onToggleRepeat,
                        modifier = Modifier.size(58.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Spacer(Modifier.height(92.dp))
        }

        if (!showQueue) {
            QueuePullHandle(
                queueCount = (displayQueue.size - 1).coerceAtLeast(0),
                onOpen = { showQueue = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        AnimatedVisibility(
            visible = showQueue,
            enter = fadeIn(animationSpec = tween(durationMillis = 140)),
            exit = fadeOut(animationSpec = tween(durationMillis = 180)),
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.24f))
                    .clickable(onClick = { showQueue = false })
            )
        }

        AnimatedVisibility(
            visible = showQueue,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 230, easing = FastOutSlowInEasing)
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            QueueBottomSheet(
                currentTrack = track,
                queue = displayQueue,
                session = session,
                onDismiss = { showQueue = false },
                onTrackClick = onQueueTrackClick,
                onMove = onQueueMove,
                onPlayNext = onQueuePlayNext,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QueuePullHandle(
    queueCount: Int,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .swipeUpToOpen(onOpen)
            .clickable(onClick = onOpen),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(27.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = PlayerGlyph.Queue.imageVector(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Up next $queueCount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun Modifier.swipeUpToOpen(
    onOpen: () -> Unit,
    openDistance: Dp = 46.dp,
    onDragStart: () -> Unit = {},
    onDragOffsetChange: (Float) -> Unit = {},
    onDragEnd: (opened: Boolean) -> Unit = {}
): Modifier = pointerInput(openDistance) {
    val openDistancePx = openDistance.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
        var totalDragX = 0f
        var totalDragY = 0f
        var draggingOpen = false

        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) {
                break
            }

            val dragAmount = change.position - change.previousPosition
            totalDragX += dragAmount.x
            totalDragY += dragAmount.y
            val upwardDrag = totalDragY < 0f && -totalDragY > abs(totalDragX) * 0.6f
            if (upwardDrag || draggingOpen) {
                if (!draggingOpen) {
                    draggingOpen = true
                    onDragStart()
                }
                onDragOffsetChange(totalDragY.coerceAtMost(0f))
                change.consume()
            }
        }

        if (draggingOpen) {
            val opened = -totalDragY > openDistancePx && -totalDragY > abs(totalDragX) * 1.2f
            onDragEnd(opened)
            if (opened) {
                onOpen()
            }
        }
    }
}

@Composable
private fun QueueBottomSheet(
    currentTrack: MusicTrack,
    queue: List<MusicTrack>,
    session: JellyfinSession?,
    onDismiss: () -> Unit,
    onTrackClick: (MusicTrack) -> Unit,
    onMove: (Int, Int) -> Unit,
    onPlayNext: (MusicTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxHeight(0.72f)
            .navigationBarsPadding()
            .swipeDownToDismiss(
                onDismiss = onDismiss,
                startZone = 118.dp,
                dismissDistance = 72.dp
            ),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(48.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Now playing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${(queue.size - 1).coerceAtLeast(0)} up next",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text("Done")
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(queue, key = { _, track -> track.id }) { index, item ->
                    QueueTrackRow(
                        track = item,
                        session = session,
                        index = index,
                        lastIndex = queue.lastIndex,
                        isCurrent = item.id == currentTrack.id,
                        onClick = { onTrackClick(item) },
                        onMoveUp = { onMove(index, index - 1) },
                        onMoveDown = { onMove(index, index + 1) },
                        onPlayNext = { onPlayNext(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueTrackRow(
    track: MusicTrack,
    session: JellyfinSession?,
    index: Int,
    lastIndex: Int,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onPlayNext: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isCurrent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        tonalElevation = if (isCurrent) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumTile(track = track, session = session, modifier = Modifier.size(46.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isCurrent) "Playing now" else "${track.artist} - ${track.album}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!isCurrent) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 1,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = PlayerGlyph.MoveUp.imageVector(),
                            contentDescription = "Move up",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < lastIndex,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = PlayerGlyph.MoveDown.imageVector(),
                            contentDescription = "Move down",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = PlayerGlyph.More.imageVector(),
                        contentDescription = "Track options",
                        modifier = Modifier.size(23.dp)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Play next") },
                        enabled = !isCurrent,
                        onClick = {
                            menuExpanded = false
                            onPlayNext()
                        }
                    )
                }
            }
        }
    }
}

private enum class PlayerGlyph {
    Shuffle,
    Previous,
    Play,
    Pause,
    Next,
    Repeat,
    Replay,
    Queue,
    More,
    MoveUp,
    MoveDown
}

@Composable
private fun FavoriteHeartButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    IconButton(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = if (isFavorite) "Unlike track" else "Like track"
        }
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else PlayerIconVectors.FavoriteOutline,
            contentDescription = null,
            tint = if (isFavorite) colorScheme.primary else colorScheme.onSurfaceVariant,
            modifier = Modifier.size(if (isFavorite) 26.dp else 27.dp)
        )
    }
}

@Composable
private fun RoundedPlaybackButton(
    icon: PlayerGlyph,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    prominent: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = when {
        prominent -> colorScheme.primary
        active -> colorScheme.primaryContainer
        else -> colorScheme.surfaceContainerHigh
    }
    val contentColor = when {
        prominent -> colorScheme.onPrimary
        active -> colorScheme.onPrimaryContainer
        else -> colorScheme.onSurfaceVariant
    }
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(if (prominent) 64.dp else 48.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = CircleShape,
        color = backgroundColor,
        tonalElevation = if (prominent || active) 4.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon.imageVector(),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(icon.playbackIconSize(prominent))
            )
        }
    }
}

private fun PlayerGlyph.playbackIconSize(prominent: Boolean): Dp =
    when {
        prominent -> 32.dp
        this == PlayerGlyph.Shuffle || this == PlayerGlyph.Repeat -> 24.dp
        else -> 27.dp
    }

private fun PlayerGlyph.imageVector(): ImageVector = when (this) {
    PlayerGlyph.Shuffle -> PlayerIconVectors.Shuffle
    PlayerGlyph.Previous -> PlayerIconVectors.SkipPrevious
    PlayerGlyph.Play -> Icons.Filled.PlayArrow
    PlayerGlyph.Pause -> PlayerIconVectors.Pause
    PlayerGlyph.Next -> PlayerIconVectors.SkipNext
    PlayerGlyph.Repeat -> PlayerIconVectors.Repeat
    PlayerGlyph.Replay -> PlayerIconVectors.Replay
    PlayerGlyph.Queue -> PlayerIconVectors.Queue
    PlayerGlyph.More -> PlayerIconVectors.MoreVertical
    PlayerGlyph.MoveUp -> PlayerIconVectors.MoveUp
    PlayerGlyph.MoveDown -> PlayerIconVectors.MoveDown
}

private object PlayerIconVectors {
    private val stroke = SolidColor(Color.Black)

    val Pause: ImageVector = ImageVector.Builder(
        name = "Pause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(6f, 5f)
            horizontalLineTo(10f)
            verticalLineTo(19f)
            horizontalLineTo(6f)
            close()
            moveTo(14f, 5f)
            horizontalLineTo(18f)
            verticalLineTo(19f)
            horizontalLineTo(14f)
            close()
        }
    }.build()

    val SkipPrevious: ImageVector = ImageVector.Builder(
        name = "SkipPrevious",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(6f, 5f)
            horizontalLineTo(8.2f)
            verticalLineTo(19f)
            horizontalLineTo(6f)
            close()
            moveTo(18f, 5f)
            verticalLineTo(19f)
            lineTo(9f, 12f)
            close()
        }
    }.build()

    val SkipNext: ImageVector = ImageVector.Builder(
        name = "SkipNext",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(15.8f, 5f)
            horizontalLineTo(18f)
            verticalLineTo(19f)
            horizontalLineTo(15.8f)
            close()
            moveTo(6f, 5f)
            verticalLineTo(19f)
            lineTo(15f, 12f)
            close()
        }
    }.build()

    val Shuffle: ImageVector = ImageVector.Builder(
        name = "Shuffle",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(10.59f, 9.17f)
            lineTo(5.41f, 4f)
            lineTo(4f, 5.41f)
            lineTo(9.17f, 10.59f)
            close()
            moveTo(14.5f, 4f)
            lineTo(16.54f, 6.04f)
            lineTo(4f, 18.59f)
            lineTo(5.41f, 20f)
            lineTo(17.96f, 7.46f)
            lineTo(20f, 9.5f)
            verticalLineTo(4f)
            close()
            moveTo(14.83f, 13.41f)
            lineTo(13.41f, 14.83f)
            lineTo(16.54f, 17.96f)
            lineTo(14.5f, 20f)
            horizontalLineTo(20f)
            verticalLineTo(14.5f)
            lineTo(17.96f, 16.54f)
            close()
        }
    }.build()

    val Repeat: ImageVector = ImageVector.Builder(
        name = "Repeat",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(7f, 7f)
            horizontalLineTo(17f)
            verticalLineTo(10f)
            lineTo(21f, 6f)
            lineTo(17f, 2f)
            verticalLineTo(5f)
            horizontalLineTo(5f)
            verticalLineTo(11f)
            horizontalLineTo(7f)
            close()
            moveTo(17f, 17f)
            horizontalLineTo(7f)
            verticalLineTo(14f)
            lineTo(3f, 18f)
            lineTo(7f, 22f)
            verticalLineTo(19f)
            horizontalLineTo(19f)
            verticalLineTo(13f)
            horizontalLineTo(17f)
            close()
        }
    }.build()

    val FavoriteOutline: ImageVector = ImageVector.Builder(
        name = "FavoriteOutline",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 1.9f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 21f)
            lineTo(10.54f, 19.68f)
            curveTo(5.38f, 15.01f, 2.3f, 12.22f, 2.3f, 8.56f)
            curveTo(2.3f, 5.58f, 4.64f, 3.25f, 7.58f, 3.25f)
            curveTo(9.28f, 3.25f, 10.9f, 4.04f, 12f, 5.28f)
            curveTo(13.1f, 4.04f, 14.72f, 3.25f, 16.42f, 3.25f)
            curveTo(19.36f, 3.25f, 21.7f, 5.58f, 21.7f, 8.56f)
            curveTo(21.7f, 12.22f, 18.62f, 15.01f, 13.46f, 19.68f)
            lineTo(12f, 21f)
            close()
        }
    }.build()

    val Download: ImageVector = ImageVector.Builder(
        name = "Download",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 4f)
            verticalLineTo(14f)
            moveTo(8f, 10f)
            lineTo(12f, 14f)
            lineTo(16f, 10f)
            moveTo(5f, 19f)
            horizontalLineTo(19f)
        }
    }.build()

    val DownloadDone: ImageVector = ImageVector.Builder(
        name = "DownloadDone",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 12.5f)
            lineTo(9.5f, 17f)
            lineTo(19f, 7f)
            moveTo(5f, 20f)
            horizontalLineTo(19f)
        }
    }.build()

    val Pin: ImageVector = ImageVector.Builder(
        name = "Pin",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(14f, 4f)
            lineTo(20f, 10f)
            moveTo(16f, 6f)
            lineTo(10f, 12f)
            lineTo(8f, 18f)
            lineTo(6f, 20f)
            moveTo(10f, 12f)
            lineTo(12f, 14f)
            lineTo(18f, 8f)
        }
    }.build()

    val PinFilled: ImageVector = ImageVector.Builder(
        name = "PinFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(14f, 3.4f)
            lineTo(20.6f, 10f)
            lineTo(18.9f, 11.7f)
            lineTo(17.2f, 10f)
            lineTo(12.7f, 14.5f)
            lineTo(9.1f, 15.7f)
            lineTo(6.4f, 20.6f)
            lineTo(5f, 19.2f)
            lineTo(9.3f, 14.9f)
            lineTo(10.5f, 11.3f)
            lineTo(15f, 6.8f)
            lineTo(13.3f, 5.1f)
            close()
        }
    }.build()

    val Replay: ImageVector = ImageVector.Builder(
        name = "Replay",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(12f, 5f)
            verticalLineTo(1f)
            lineTo(7f, 6f)
            lineTo(12f, 11f)
            verticalLineTo(7f)
            curveTo(15.31f, 7f, 18f, 9.69f, 18f, 13f)
            curveTo(18f, 16.31f, 15.31f, 19f, 12f, 19f)
            curveTo(9.16f, 19f, 6.79f, 17.03f, 6.17f, 14.39f)
            horizontalLineTo(4.11f)
            curveTo(4.78f, 18.15f, 8.07f, 21f, 12f, 21f)
            curveTo(16.42f, 21f, 20f, 17.42f, 20f, 13f)
            curveTo(20f, 8.58f, 16.42f, 5f, 12f, 5f)
        }
    }.build()

    val Library: ImageVector = ImageVector.Builder(
        name = "Library",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(4f, 5f)
            horizontalLineTo(7.5f)
            verticalLineTo(19f)
            horizontalLineTo(4f)
            close()
            moveTo(9.2f, 5f)
            horizontalLineTo(12.7f)
            verticalLineTo(19f)
            horizontalLineTo(9.2f)
            close()
            moveTo(14.3f, 6f)
            lineTo(17.4f, 5.1f)
            lineTo(21f, 18.1f)
            lineTo(17.8f, 19f)
            close()
            moveTo(5f, 16.3f)
            horizontalLineTo(6.5f)
            verticalLineTo(17.7f)
            horizontalLineTo(5f)
            close()
            moveTo(10.2f, 16.3f)
            horizontalLineTo(11.7f)
            verticalLineTo(17.7f)
            horizontalLineTo(10.2f)
            close()
        }
    }.build()

    val Queue: ImageVector = ImageVector.Builder(
        name = "Queue",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 7f)
            horizontalLineTo(19f)
            moveTo(5f, 12f)
            horizontalLineTo(15f)
            moveTo(5f, 17f)
            horizontalLineTo(12f)
            moveTo(17f, 15f)
            verticalLineTo(21f)
            lineTo(21f, 18f)
            close()
        }
    }.build()

    val MoreVertical: ImageVector = ImageVector.Builder(
        name = "MoreVertical",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(12f, 8f)
            moveToRelative(-2f, 0f)
            arcToRelative(2f, 2f, 0f, true, true, 4f, 0f)
            arcToRelative(2f, 2f, 0f, true, true, -4f, 0f)
            moveTo(12f, 14f)
            moveToRelative(-2f, 0f)
            arcToRelative(2f, 2f, 0f, true, true, 4f, 0f)
            arcToRelative(2f, 2f, 0f, true, true, -4f, 0f)
            moveTo(12f, 20f)
            moveToRelative(-2f, 0f)
            arcToRelative(2f, 2f, 0f, true, true, 4f, 0f)
            arcToRelative(2f, 2f, 0f, true, true, -4f, 0f)
        }
    }.build()

    val MoveUp: ImageVector = ImageVector.Builder(
        name = "MoveUp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 19f)
            verticalLineTo(5f)
            moveTo(6f, 11f)
            lineTo(12f, 5f)
            lineTo(18f, 11f)
        }
    }.build()

    val MoveDown: ImageVector = ImageVector.Builder(
        name = "MoveDown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 5f)
            verticalLineTo(19f)
            moveTo(6f, 13f)
            lineTo(12f, 19f)
            lineTo(18f, 13f)
        }
    }.build()
}

@Composable
private fun DiscAlbumStage(
    track: MusicTrack,
    isPlaying: Boolean,
    progress: Float,
    session: JellyfinSession?,
    onSeek: (Float) -> Unit,
    onScratch: (Float) -> Unit,
    onScratchEnd: () -> Unit
) {
    val latestProgress by rememberUpdatedState(progress)
    val latestOnSeek by rememberUpdatedState(onSeek)
    val latestOnScratch by rememberUpdatedState(onScratch)
    val latestOnScratchEnd by rememberUpdatedState(onScratchEnd)
    var isScratching by remember { mutableStateOf(false) }
    var scratchProgress by remember { mutableFloatStateOf(progress) }
    var discRotation by remember(track.id) { mutableFloatStateOf(0f) }
    val stageProgress = if (isScratching) scratchProgress else progress

    LaunchedEffect(progress, isScratching) {
        if (!isScratching) {
            scratchProgress = progress
        }
    }

    LaunchedEffect(isPlaying, isScratching, track.id) {
        if (!isPlaying || isScratching) return@LaunchedEffect
        var lastFrameTime = withFrameNanos { it }
        while (true) {
            val frameTime = withFrameNanos { it }
            val deltaSeconds = (frameTime - lastFrameTime) / 1_000_000_000f
            lastFrameTime = frameTime
            discRotation = (discRotation + deltaSeconds * (360f / 14f)) % 360f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularSeekRing(
            progress = stageProgress,
            onSeek = onSeek,
            modifier = Modifier.fillMaxSize(),
            activeColor = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
        )
        VinylDisc(
            track = track,
            session = session,
            modifier = Modifier
                .fillMaxSize(0.86f)
                .pointerInput(Unit) {
                    var lastAngle = 0f
                    var lastEventTime = 0L
                    detectDragGestures(
                        onDragStart = { offset ->
                            isScratching = true
                            scratchProgress = latestProgress
                            lastAngle = angleForOffset(
                                offset = offset,
                                width = size.width.toFloat(),
                                height = size.height.toFloat()
                            )
                            lastEventTime = SystemClock.elapsedRealtime()
                        },
                        onDragEnd = {
                            isScratching = false
                            latestOnScratchEnd()
                        },
                        onDragCancel = {
                            isScratching = false
                            latestOnScratchEnd()
                        },
                        onDrag = { change, _ ->
                            val radiusFromCenter = distanceFromCenter(
                                offset = change.position,
                                width = size.width.toFloat(),
                                height = size.height.toFloat()
                            )
                            val deadZone = minOf(size.width, size.height) * DISC_SCRATCH_DEAD_ZONE
                            if (radiusFromCenter < deadZone) {
                                change.consume()
                                return@detectDragGestures
                            }

                            val now = SystemClock.elapsedRealtime()
                            val angle = angleForOffset(
                                offset = change.position,
                                width = size.width.toFloat(),
                                height = size.height.toFloat()
                            )
                            val deltaAngle = shortestAngleDelta(lastAngle, angle)
                            val elapsedMs = (now - lastEventTime).coerceAtLeast(1L)
                            lastAngle = angle
                            lastEventTime = now

                            discRotation = (discRotation + deltaAngle) % 360f
                            scratchProgress = (
                                scratchProgress +
                                    (deltaAngle / 360f) * DISC_SCRATCH_SEEK_SCALE
                                ).coerceIn(0f, 0.995f)
                            latestOnSeek(scratchProgress)
                            latestOnScratch(deltaAngle / elapsedMs.toFloat())
                            change.consume()
                        }
                    )
                }
                .rotate(discRotation)
        )
        TurntableArmOverlay(
            progress = stageProgress,
            onRecord = isPlaying,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CircularSeekRing(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color,
    trackColor: Color
) {
    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { offset ->
                val degrees = angleForOffset(
                    offset = offset,
                    width = size.width.toFloat(),
                    height = size.height.toFloat()
                )
                onSeek((degrees / 360f).coerceIn(0f, 1f))
            }
        }
    ) {
        val strokeWidth = 10.dp.toPx()
        val inset = strokeWidth / 2f + 8.dp.toPx()
        val diameter = size.minDimension - inset * 2f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val clampedProgress = progress.coerceIn(0f, 1f)

        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = activeColor,
            startAngle = -90f,
            sweepAngle = clampedProgress * 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        val angleRadians = ((clampedProgress * 360f - 90f) * PI / 180f).toFloat()
        val radius = diameter / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val knobCenter = Offset(
            x = center.x + cos(angleRadians) * radius,
            y = center.y + sin(angleRadians) * radius
        )
        drawCircle(color = activeColor, radius = 8.dp.toPx(), center = knobCenter)
        drawCircle(color = Color.White.copy(alpha = 0.86f), radius = 4.dp.toPx(), center = knobCenter)
    }
}

private fun angleForOffset(offset: Offset, width: Float, height: Float): Float {
    val dx = offset.x - width / 2f
    val dy = offset.y - height / 2f
    var degrees = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat() + 90f
    if (degrees < 0f) degrees += 360f
    return degrees
}

private fun distanceFromCenter(offset: Offset, width: Float, height: Float): Float {
    return hypot(offset.x - width / 2f, offset.y - height / 2f)
}

private fun shortestAngleDelta(start: Float, end: Float): Float {
    var delta = end - start
    while (delta > 180f) delta -= 360f
    while (delta < -180f) delta += 360f
    return delta
}

@Composable
private fun TurntableArmOverlay(
    progress: Float,
    onRecord: Boolean,
    modifier: Modifier = Modifier
) {
    val offRecord by animateFloatAsState(
        targetValue = if (onRecord) 0f else 1f,
        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
        label = "tonearmOffRecord"
    )
    val colorScheme = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        val p = progress.coerceIn(0f, 1f)
        val accent = colorScheme.primary
        val swing = ((offRecord - 0.12f) / 0.88f).coerceIn(0f, 1f)
        val lift = (offRecord / 0.46f).coerceIn(0f, 1f)
        fun mix(start: Float, end: Float, amount: Float): Float = start + (end - start) * amount

        val pivot = Offset(size.width * 0.895f, size.height * 0.095f)
        val onRecordStartAngle = 1.68f
        val onRecordEndAngle = 2.03f
        val offRecordAngle = 1.3f
        val onRecordAngle = mix(onRecordStartAngle, onRecordEndAngle, p)
        val armAngle = mix(onRecordAngle, offRecordAngle, swing)
        val cartridgeAngle = armAngle + 0.2f
        val armLength = size.minDimension * 0.57f
        val cartridgeLength = size.minDimension * 0.082f
        val cartridgeWidth = size.minDimension * 0.03f
        val pipeDirection = Offset(cos(armAngle), sin(armAngle))
        val cartridgeDirection = Offset(cos(cartridgeAngle), sin(cartridgeAngle))
        val stylus = Offset(
            x = pivot.x + pipeDirection.x * armLength,
            y = pivot.y + pipeDirection.y * armLength
        )
        val cartridgeBack = Offset(
            x = stylus.x - cartridgeDirection.x * cartridgeLength,
            y = stylus.y - cartridgeDirection.y * cartridgeLength
        )
        val pipeInset = 24.dp.toPx()
        val pipeStart = Offset(
            x = pivot.x + pipeDirection.x * pipeInset,
            y = pivot.y + pipeDirection.y * pipeInset
        )
        val armNormal = Offset(x = -sin(armAngle), y = cos(armAngle))
        val armCurve = size.minDimension * 0.052f
        fun armPath(offset: Offset = Offset.Zero): Path = Path().apply {
            moveTo(pipeStart.x + offset.x, pipeStart.y + offset.y)
            cubicTo(
                pivot.x + pipeDirection.x * armLength * 0.24f + armNormal.x * armCurve + offset.x,
                pivot.y + pipeDirection.y * armLength * 0.24f + armNormal.y * armCurve + offset.y,
                pivot.x + pipeDirection.x * armLength * 0.66f - armNormal.x * armCurve * 0.55f + offset.x,
                pivot.y + pipeDirection.y * armLength * 0.66f - armNormal.y * armCurve * 0.55f + offset.y,
                cartridgeBack.x + offset.x,
                cartridgeBack.y + offset.y
            )
        }
        fun armNormalOffset(amount: Float): Offset = Offset(
            x = armNormal.x * amount,
            y = armNormal.y * amount
        )
        val normal = Offset(
            x = -sin(cartridgeAngle) * cartridgeWidth,
            y = cos(cartridgeAngle) * cartridgeWidth
        )
        val railGap = 3.dp.toPx()
        val baseRadius = 27.dp.toPx()
        val metalBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF202925).copy(alpha = 0.9f),
                Color(0xFFE0E9E5).copy(alpha = 0.88f),
                Color(0xFFAEBDB7).copy(alpha = 0.9f),
                Color(0xFF53615C).copy(alpha = 0.84f)
            ),
            start = Offset(pivot.x - armNormal.x * 9.dp.toPx(), pivot.y - armNormal.y * 9.dp.toPx()),
            end = Offset(pivot.x + armNormal.x * 11.dp.toPx(), pivot.y + armNormal.y * 11.dp.toPx())
        )

        val liftShadowOffset = Offset(
            x = (2.2f + lift * 2.6f).dp.toPx(),
            y = (3.2f + lift * 7.2f).dp.toPx()
        )
        val restAlpha = 0.25f + swing * 0.62f
        val restPost = Offset(size.width * 0.925f, size.height * 0.29f)
        drawLine(
            color = Color.Black.copy(alpha = 0.2f * restAlpha),
            start = Offset(restPost.x + 2.dp.toPx(), restPost.y - 16.dp.toPx()),
            end = Offset(restPost.x + 2.dp.toPx(), restPost.y + 36.dp.toPx()),
            strokeWidth = 7.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF788782).copy(alpha = restAlpha),
            start = Offset(restPost.x, restPost.y - 18.dp.toPx()),
            end = Offset(restPost.x, restPost.y + 34.dp.toPx()),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFFD8E2DE).copy(alpha = restAlpha),
            start = Offset(restPost.x - 7.dp.toPx(), restPost.y - 18.dp.toPx()),
            end = Offset(restPost.x + 8.dp.toPx(), restPost.y - 18.dp.toPx()),
            strokeWidth = 3.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.22f * restAlpha),
            radius = 6.dp.toPx(),
            center = Offset(restPost.x + 1.dp.toPx(), restPost.y + 35.dp.toPx())
        )

        val rearDirection = Offset(-cartridgeDirection.x, -cartridgeDirection.y)
        val counterweightCenter = Offset(
            x = pivot.x + rearDirection.x * 23.dp.toPx(),
            y = pivot.y + rearDirection.y * 23.dp.toPx()
        )
        drawLine(
            color = Color.Black.copy(alpha = 0.18f),
            start = Offset(pivot.x + 2.dp.toPx(), pivot.y + 3.dp.toPx()),
            end = Offset(counterweightCenter.x + 2.dp.toPx(), counterweightCenter.y + 3.dp.toPx()),
            strokeWidth = 11.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF9AA8A3).copy(alpha = 0.86f),
            start = pivot,
            end = counterweightCenter,
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.26f),
            radius = 14.dp.toPx(),
            center = Offset(counterweightCenter.x + 2.dp.toPx(), counterweightCenter.y + 3.dp.toPx())
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE7EFEB).copy(alpha = 0.92f),
                    Color(0xFF8D9B96).copy(alpha = 0.9f),
                    Color(0xFF38423F).copy(alpha = 0.9f)
                ),
                center = Offset(counterweightCenter.x - 4.dp.toPx(), counterweightCenter.y - 5.dp.toPx()),
                radius = 15.dp.toPx()
            ),
            radius = 13.dp.toPx(),
            center = counterweightCenter
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.24f),
            radius = 9.dp.toPx(),
            center = counterweightCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )

        drawPath(
            path = armPath(liftShadowOffset),
            color = Color.Black.copy(alpha = 0.2f + lift * 0.08f),
            style = Stroke(width = (11.5f + lift * 2.2f).dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(),
            color = Color.Black.copy(alpha = 0.22f),
            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(),
            brush = metalBrush,
            style = Stroke(width = 6.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(armNormalOffset(-2.2.dp.toPx())),
            color = Color.White.copy(alpha = 0.62f),
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(armNormalOffset(2.4.dp.toPx())),
            color = Color.Black.copy(alpha = 0.2f),
            style = Stroke(width = 1.1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawLine(
            color = Color.Black.copy(alpha = 0.18f),
            start = Offset(pivot.x + 2.dp.toPx(), pivot.y + 3.dp.toPx()),
            end = Offset(pipeStart.x + 2.dp.toPx(), pipeStart.y + 3.dp.toPx()),
            strokeWidth = 12.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFFB8C8C1).copy(alpha = 0.88f),
            start = pivot,
            end = pipeStart,
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.32f),
            start = Offset(
                x = pivot.x - armNormal.x * 1.8.dp.toPx(),
                y = pivot.y - armNormal.y * 1.8.dp.toPx()
            ),
            end = Offset(
                x = pipeStart.x - armNormal.x * 1.8.dp.toPx(),
                y = pipeStart.y - armNormal.y * 1.8.dp.toPx()
            ),
            strokeWidth = 0.9.dp.toPx(),
            cap = StrokeCap.Round
        )

        fun drawPipeCollar(center: Offset) {
            drawLine(
                color = Color.Black.copy(alpha = 0.34f),
                start = Offset(
                    x = center.x - armNormal.x * 5.4.dp.toPx(),
                    y = center.y - armNormal.y * 5.4.dp.toPx()
                ),
                end = Offset(
                    x = center.x + armNormal.x * 5.4.dp.toPx(),
                    y = center.y + armNormal.y * 5.4.dp.toPx()
                ),
                strokeWidth = 5.2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFDDE7E3).copy(alpha = 0.92f),
                start = Offset(
                    x = center.x - armNormal.x * 4.6.dp.toPx(),
                    y = center.y - armNormal.y * 4.6.dp.toPx()
                ),
                end = Offset(
                    x = center.x + armNormal.x * 4.6.dp.toPx(),
                    y = center.y + armNormal.y * 4.6.dp.toPx()
                ),
                strokeWidth = 2.8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        drawPipeCollar(pipeStart)
        drawPipeCollar(
            Offset(
                x = cartridgeBack.x - cartridgeDirection.x * 1.2.dp.toPx(),
                y = cartridgeBack.y - cartridgeDirection.y * 1.2.dp.toPx()
            )
        )

        val cartridge = Path().apply {
            moveTo(cartridgeBack.x - normal.x, cartridgeBack.y - normal.y)
            lineTo(cartridgeBack.x + normal.x, cartridgeBack.y + normal.y)
            lineTo(stylus.x + normal.x * 0.46f, stylus.y + normal.y * 0.46f)
            lineTo(stylus.x - normal.x * 0.46f, stylus.y - normal.y * 0.46f)
            close()
        }
        drawLine(
            color = Color.Black.copy(alpha = 0.26f),
            start = Offset(
                x = cartridgeBack.x - armNormal.x * railGap,
                y = cartridgeBack.y - armNormal.y * railGap
            ),
            end = Offset(
                x = cartridgeBack.x + armNormal.x * railGap,
                y = cartridgeBack.y + armNormal.y * railGap
            ),
            strokeWidth = 6.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawPath(
            path = Path().apply {
                moveTo(cartridgeBack.x - normal.x + 2.dp.toPx(), cartridgeBack.y - normal.y + 2.dp.toPx())
                lineTo(cartridgeBack.x + normal.x + 2.dp.toPx(), cartridgeBack.y + normal.y + 2.dp.toPx())
                lineTo(stylus.x + normal.x * 0.46f + 2.dp.toPx(), stylus.y + normal.y * 0.46f + 2.dp.toPx())
                lineTo(stylus.x - normal.x * 0.46f + 2.dp.toPx(), stylus.y - normal.y * 0.46f + 2.dp.toPx())
                close()
            },
            color = Color.Black.copy(alpha = 0.18f)
        )
        drawPath(
            path = cartridge,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF0F4F1).copy(alpha = 0.95f),
                    Color(0xFFB6C0BA).copy(alpha = 0.93f),
                    Color(0xFF58635E).copy(alpha = 0.9f)
                ),
                start = Offset(cartridgeBack.x - normal.x, cartridgeBack.y - normal.y),
                end = Offset(stylus.x + normal.x, stylus.y + normal.y)
            )
        )
        drawPath(
            path = cartridge,
            color = Color(0xFF6F7C77).copy(alpha = 0.45f),
            style = Stroke(width = 1.dp.toPx(), join = StrokeJoin.Round)
        )
        for (index in 0 until 3) {
            val along = cartridgeLength * (0.24f + index * 0.18f)
            val grooveCenter = Offset(
                x = cartridgeBack.x + cartridgeDirection.x * along,
                y = cartridgeBack.y + cartridgeDirection.y * along
            )
            drawLine(
                color = Color(0xFF6F7C77).copy(alpha = 0.38f),
                start = Offset(
                    x = grooveCenter.x - normal.x * 0.34f,
                    y = grooveCenter.y - normal.y * 0.34f
                ),
                end = Offset(
                    x = grooveCenter.x + normal.x * 0.34f,
                    y = grooveCenter.y + normal.y * 0.34f
                ),
                strokeWidth = 0.9.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        drawLine(
            color = Color(0xFF293532).copy(alpha = 0.9f),
            start = Offset(
                x = stylus.x - cartridgeDirection.x * 3.dp.toPx(),
                y = stylus.y - cartridgeDirection.y * 3.dp.toPx()
            ),
            end = Offset(
                x = stylus.x + cartridgeDirection.x * 4.dp.toPx(),
                y = stylus.y + cartridgeDirection.y * 4.dp.toPx()
            ),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = accent.copy(alpha = 0.82f - lift * 0.36f),
            radius = (1.5f + lift * 0.5f).dp.toPx(),
            center = stylus
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.22f),
            radius = baseRadius + 2.dp.toPx(),
            center = Offset(pivot.x + 1.dp.toPx(), pivot.y + 3.dp.toPx())
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.96f),
                    Color(0xFFB6C6C0).copy(alpha = 0.92f),
                    Color(0xFF64706C).copy(alpha = 0.88f)
                ),
                center = Offset(pivot.x - 7.dp.toPx(), pivot.y - 8.dp.toPx()),
                radius = baseRadius + 2.dp.toPx()
            ),
            radius = baseRadius,
            center = pivot
        )
        drawCircle(
            color = accent.copy(alpha = 0.46f - lift * 0.18f),
            radius = 20.5.dp.toPx(),
            center = pivot,
            style = Stroke(width = 2.4.dp.toPx())
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.38f),
            radius = 15.5.dp.toPx(),
            center = pivot,
            style = Stroke(width = 2.dp.toPx())
        )
        for (angleDegrees in listOf(44f, 136f, 224f, 316f)) {
            val angle = angleDegrees * PI.toFloat() / 180f
            val screwCenter = Offset(
                x = pivot.x + cos(angle) * 20.dp.toPx(),
                y = pivot.y + sin(angle) * 20.dp.toPx()
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.34f),
                radius = 3.2.dp.toPx(),
                center = screwCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.48f),
                radius = 1.4.dp.toPx(),
                center = Offset(screwCenter.x - 0.8.dp.toPx(), screwCenter.y - 0.8.dp.toPx())
            )
            drawLine(
                color = Color.Black.copy(alpha = 0.28f),
                start = Offset(screwCenter.x - 1.8.dp.toPx(), screwCenter.y),
                end = Offset(screwCenter.x + 1.8.dp.toPx(), screwCenter.y),
                strokeWidth = 0.7.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        drawCircle(
            color = blendColors(accent, Color.Black, 0.34f).copy(alpha = 0.9f),
            radius = 9.5.dp.toPx(),
            center = pivot
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.66f),
            radius = 6.dp.toPx(),
            center = pivot
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.55f),
            radius = 2.4.dp.toPx(),
            center = Offset(pivot.x - 2.dp.toPx(), pivot.y - 2.dp.toPx())
        )
    }
}

@Composable
private fun VinylDisc(
    track: MusicTrack,
    session: JellyfinSession?,
    modifier: Modifier = Modifier
) {
    val tint = track.tint
    val colorScheme = MaterialTheme.colorScheme
    val surface = colorScheme.surface
    val labelDark = blendColors(colorScheme.primaryContainer, Color.Black, 0.28f)
    Box(
        modifier = modifier
            .graphicsLayer {
                shape = CircleShape
                clip = true
            }
            .clip(CircleShape)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AlbumArtworkImage(
            track = track,
            session = session,
            modifier = Modifier
                .fillMaxSize(0.985f)
                .graphicsLayer {
                    shape = CircleShape
                    clip = true
                }
                .clip(CircleShape)
                .alpha(0.88f),
            imageSize = 520,
            imageQuality = 86,
            networkDelayMs = 1_200L
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color = Color.Black.copy(alpha = 0.1f),
                radius = radius,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tint.copy(alpha = 0.1f),
                        tint.copy(alpha = 0.035f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.22f)
                    ),
                    center = Offset(size.width * 0.34f, size.height * 0.28f),
                    radius = radius * 1.1f
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.05f),
                        Color.Black.copy(alpha = 0.3f),
                        Color.Black.copy(alpha = 0.52f)
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawReferenceVinylShines(center = center, radius = radius)
            drawSoftVinylShade(
                center = center,
                radius = radius,
                startAngle = 198f,
                sweepAngle = 98f,
                width = radius * 0.18f,
                baseAlpha = 0.16f
            )
            val softSpotCenter = Offset(size.width * 0.36f, size.height * 0.26f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE8F5FF).copy(alpha = 0.075f),
                        Color.White.copy(alpha = 0.024f),
                        Color.Transparent
                    ),
                    center = softSpotCenter,
                    radius = radius * 0.34f
                ),
                radius = radius * 0.34f,
                center = softSpotCenter
            )
            for (index in 0..184) {
                val grooveRadius = radius * (0.288f + index * 0.0036f)
                val strongGroove = index % 12 == 0
                val midGroove = index % 4 == 0
                drawCircle(
                    color = Color.Black.copy(
                        alpha = when {
                            strongGroove -> 0.16f
                            midGroove -> 0.08f
                            else -> 0.045f
                        }
                    ),
                    radius = grooveRadius,
                    center = center,
                    style = Stroke(width = if (strongGroove) 0.58.dp.toPx() else 0.32.dp.toPx())
                )
                drawCircle(
                    color = Color.White.copy(
                        alpha = when {
                            strongGroove -> 0.11f
                            midGroove -> 0.052f
                            else -> 0.03f
                        }
                    ),
                    radius = grooveRadius,
                    center = center,
                    style = Stroke(width = if (strongGroove) 0.48.dp.toPx() else 0.26.dp.toPx())
                )
            }
            drawCircle(
                color = Color.Black.copy(alpha = 0.24f),
                radius = radius * 0.982f,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius * 0.955f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = radius * 0.78f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius * 0.53f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawVinylSpeckleTexture(
                center = center,
                outerRadius = radius * 0.96f,
                innerRadius = radius * 0.29f,
                seed = track.id.hashCode()
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.22f),
                radius = radius * 0.285f,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        blendColors(tint, Color.Black, 0.52f).copy(alpha = 0.92f),
                        Color(0xFF171615).copy(alpha = 0.96f),
                        Color.Black.copy(alpha = 0.98f)
                    ),
                    center = Offset(center.x - radius * 0.08f, center.y - radius * 0.09f),
                    radius = radius * 0.28f
                ),
                radius = radius * 0.255f,
                center = center
            )
            drawCircle(
                color = tint.copy(alpha = 0.58f),
                radius = radius * 0.258f,
                center = center,
                style = Stroke(width = 2.8.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = radius * 0.214f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color = Color.Black.copy(alpha = 0.32f),
                radius = radius * 0.15f,
                center = center,
                style = Stroke(width = 1.2.dp.toPx())
            )
            for (index in 0 until 6) {
                val angle = (index * 60f + 22f) * PI.toFloat() / 180f
                drawLine(
                    color = labelDark.copy(alpha = 0.5f),
                    start = Offset(
                        x = center.x + cos(angle) * radius * 0.04f,
                        y = center.y + sin(angle) * radius * 0.04f
                    ),
                    end = Offset(
                        x = center.x + cos(angle) * radius * 0.22f,
                        y = center.y + sin(angle) * radius * 0.22f
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            drawCircle(
                color = surface.copy(alpha = 0.96f),
                radius = radius * 0.028f,
                center = center
            )
            drawCircle(
                color = labelDark,
                radius = radius * 0.014f,
                center = center
            )
        }
    }
}

private fun DrawScope.drawReferenceVinylShines(center: Offset, radius: Float) {
    val coolWhite = Color(0xFFE8F6FF)
    val softBlue = Color(0xFFBFD8E8)

    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.88f,
        startAngle = 178f,
        sweepAngle = 76f,
        strokeWidth = radius * 0.58f,
        color = coolWhite.copy(alpha = 0.105f),
        blurRadius = radius * 0.12f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.93f,
        startAngle = 186f,
        sweepAngle = 42f,
        strokeWidth = radius * 0.3f,
        color = Color.White.copy(alpha = 0.14f),
        blurRadius = radius * 0.065f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.76f,
        startAngle = 214f,
        sweepAngle = 92f,
        strokeWidth = radius * 0.52f,
        color = coolWhite.copy(alpha = 0.105f),
        blurRadius = radius * 0.105f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.81f,
        startAngle = 230f,
        sweepAngle = 48f,
        strokeWidth = radius * 0.22f,
        color = Color.White.copy(alpha = 0.17f),
        blurRadius = radius * 0.052f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.58f,
        startAngle = 238f,
        sweepAngle = 36f,
        strokeWidth = radius * 0.085f,
        color = Color.White.copy(alpha = 0.16f),
        blurRadius = radius * 0.032f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.86f,
        startAngle = 18f,
        sweepAngle = 66f,
        strokeWidth = radius * 0.4f,
        color = softBlue.copy(alpha = 0.07f),
        blurRadius = radius * 0.086f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.74f,
        startAngle = 34f,
        sweepAngle = 38f,
        strokeWidth = radius * 0.14f,
        color = Color.White.copy(alpha = 0.12f),
        blurRadius = radius * 0.04f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.68f,
        startAngle = 292f,
        sweepAngle = 58f,
        strokeWidth = radius * 0.34f,
        color = coolWhite.copy(alpha = 0.058f),
        blurRadius = radius * 0.088f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.95f,
        startAngle = 300f,
        sweepAngle = 34f,
        strokeWidth = radius * 0.18f,
        color = Color.White.copy(alpha = 0.055f),
        blurRadius = radius * 0.055f
    )

    val fineBands = listOf(
        Triple(0.94f, 192f, 0.046f),
        Triple(0.88f, 206f, 0.04f),
        Triple(0.73f, 240f, 0.052f),
        Triple(0.64f, 255f, 0.044f),
        Triple(0.9f, 42f, 0.035f),
        Triple(0.78f, 318f, 0.03f)
    )
    fineBands.forEach { (radiusScale, angle, alpha) ->
        drawBlurredArcStroke(
            center = center,
            radius = radius * radiusScale,
            startAngle = angle,
            sweepAngle = 16f,
            strokeWidth = radius * 0.018f,
            color = Color.White.copy(alpha = alpha),
            blurRadius = radius * 0.006f
        )
    }
}

private fun DrawScope.drawVinylSpecularSweep(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    baseAlpha: Float
) {
    val layers = listOf(
        Triple(0.96f, 0.34f, 0.078f),
        Triple(0.86f, 0.24f, 0.052f),
        Triple(0.72f, 0.16f, 0.036f),
        Triple(0.58f, 0.08f, 0.024f)
    )

    layers.forEachIndexed { index, (radiusScale, widthScale, blurScale) ->
        drawBlurredArcStroke(
            center = center,
            radius = radius * radiusScale,
            startAngle = startAngle - index * 6.5f,
            sweepAngle = sweepAngle + index * 13f,
            strokeWidth = radius * widthScale,
            color = Color.White.copy(alpha = baseAlpha * (0.36f - index * 0.055f)),
            blurRadius = radius * blurScale
        )
    }

    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.88f,
        startAngle = startAngle + sweepAngle * 0.18f,
        sweepAngle = sweepAngle * 0.46f,
        strokeWidth = radius * 0.045f,
        color = Color.White.copy(alpha = baseAlpha * 0.34f),
        blurRadius = radius * 0.02f
    )
}

private fun DrawScope.drawSoftVinylShine(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    width: Float,
    baseAlpha: Float
) {
    val layers = listOf(
        Triple(3.6f, 0.034f, 0.065f),
        Triple(2.55f, 0.052f, 0.046f),
        Triple(1.72f, 0.075f, 0.032f),
        Triple(1.04f, 0.09f, 0.022f),
        Triple(0.58f, 0.055f, 0.016f)
    )

    layers.forEachIndexed { index, (scale, alphaScale, blurScale) ->
        drawBlurredArcStroke(
            center = center,
            radius = radius * (0.99f - index * 0.018f),
            startAngle = startAngle - index * 3.4f,
            sweepAngle = sweepAngle + index * 6.8f,
            strokeWidth = width * scale,
            color = Color.White.copy(alpha = baseAlpha * alphaScale),
            blurRadius = radius * blurScale
        )
    }
}

private fun DrawScope.drawSoftVinylShade(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    width: Float,
    baseAlpha: Float
) {
    val diameter = radius * 2f
    val topLeft = Offset(center.x - radius, center.y - radius)
    val layers = listOf(
        3.4f to 0.08f,
        2.5f to 0.12f,
        1.65f to 0.16f,
        0.96f to 0.18f
    )

    layers.forEachIndexed { index, (scale, alphaScale) ->
        drawArc(
            color = Color.Black.copy(alpha = baseAlpha * alphaScale),
            startAngle = startAngle - index * 4.2f,
            sweepAngle = sweepAngle + index * 8.4f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = width * scale, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawMetallicVinylShine(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    baseAlpha: Float
) {
    val baseWidth = radius * 0.124f
    val layers = listOf(
        Triple(4.8f, 0.022f, 0.062f),
        Triple(3.55f, 0.036f, 0.044f),
        Triple(2.34f, 0.052f, 0.03f),
        Triple(1.36f, 0.064f, 0.02f),
        Triple(0.58f, 0.036f, 0.014f)
    )

    layers.forEachIndexed { index, (scale, alphaScale, blurScale) ->
        drawBlurredArcStroke(
            center = center,
            radius = radius * (0.94f - index * 0.025f),
            startAngle = startAngle - index * 4.6f,
            sweepAngle = sweepAngle + index * 9.2f,
            strokeWidth = baseWidth * scale,
            color = Color.White.copy(alpha = baseAlpha * alphaScale),
            blurRadius = radius * blurScale
        )
    }
}

private fun DrawScope.drawBlurredVinylGlow(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    baseAlpha: Float
) {
    val baseWidth = radius * 0.2f
    val layers = listOf(
        Triple(4.4f, 0.014f, 0.08f),
        Triple(3.2f, 0.022f, 0.06f),
        Triple(2.2f, 0.032f, 0.04f),
        Triple(1.25f, 0.034f, 0.026f)
    )

    layers.forEachIndexed { index, (scale, alphaScale, blurScale) ->
        drawBlurredArcStroke(
            center = center,
            radius = radius * (0.98f - index * 0.018f),
            startAngle = startAngle - index * 5.5f,
            sweepAngle = sweepAngle + index * 11f,
            strokeWidth = baseWidth * scale,
            color = Color.White.copy(alpha = baseAlpha * alphaScale),
            blurRadius = radius * blurScale
        )
    }
}

private fun DrawScope.drawBlurredArcStroke(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    strokeWidth: Float,
    color: Color,
    blurRadius: Float
) {
    drawIntoCanvas { canvas ->
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            style = AndroidPaint.Style.STROKE
            strokeCap = AndroidPaint.Cap.ROUND
            this.strokeWidth = strokeWidth
            this.color = color.toArgb()
            maskFilter = BlurMaskFilter(blurRadius.coerceAtLeast(0.5f), BlurMaskFilter.Blur.NORMAL)
        }
        val bounds = RectF(
            center.x - radius,
            center.y - radius,
            center.x + radius,
            center.y + radius
        )
        canvas.nativeCanvas.drawArc(bounds, startAngle, sweepAngle, false, paint)
    }
}

private fun DrawScope.drawVinylSpeckleTexture(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    seed: Int
) {
    val speckleCount = 460
    val ringArea = outerRadius - innerRadius
    for (index in 0 until speckleCount) {
        val hash = speckleHash(seed, index)
        val angle = ((hash and 0x3FF) / 1024f) * PI.toFloat() * 2f
        val radialNoise = (((hash ushr 10) and 0x3FF) / 1024f)
        val distance = innerRadius + ringArea * sqrt(radialNoise)
        val x = center.x + cos(angle) * distance
        val y = center.y + sin(angle) * distance
        val sizeNoise = ((hash ushr 20) and 0xFF) / 255f
        val alphaNoise = ((hash ushr 28) and 0x0F) / 15f
        drawCircle(
            color = Color.White.copy(alpha = 0.02f + alphaNoise * 0.05f),
            radius = 0.24.dp.toPx() + sizeNoise * 0.58.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

private fun speckleHash(seed: Int, index: Int): Int {
    var value = seed xor (index * 0x45D9F3B)
    value = value xor (value ushr 16)
    value *= 0x45D9F3B
    value = value xor (value ushr 16)
    value *= 0x45D9F3B
    return value xor (value ushr 16)
}

@Composable
private fun AudioBarsVisualizer(
    modifier: Modifier = Modifier,
    color: Color,
    active: Boolean,
    levels: FloatArray
) {
    val targetLevels = remember(active, levels) {
        if (active) levels.copyOf() else FloatArray(VISUALIZER_BAR_COUNT)
    }
    val latestTargetLevels by rememberUpdatedState(targetLevels)
    val latestActive by rememberUpdatedState(active)
    var displayedLevels by remember { mutableStateOf(FloatArray(VISUALIZER_BAR_COUNT)) }

    LaunchedEffect(Unit) {
        var lastFrameTime = withFrameNanos { it }
        while (true) {
            val frameTime = withFrameNanos { it }
            val deltaSeconds = ((frameTime - lastFrameTime) / 1_000_000_000f).coerceIn(0f, 0.05f)
            lastFrameTime = frameTime

            val current = displayedLevels
            val target = latestTargetLevels
            val next = FloatArray(VISUALIZER_BAR_COUNT)
            var maxLevel = 0f

            for (index in 0 until VISUALIZER_BAR_COUNT) {
                val goal = target.getOrElse(index) { 0f }.coerceIn(0f, 1f)
                val level = current.getOrElse(index) { 0f }
                val speed = if (goal > level) 5.8f else 3.2f
                val blend = (deltaSeconds * speed).coerceIn(0.04f, 0.28f)
                val smoothed = level + (goal - level) * blend
                next[index] = smoothed
                if (smoothed > maxLevel) maxLevel = smoothed
            }

            displayedLevels = next
            if (!latestActive && maxLevel < 0.003f) {
                delay(90L)
            }
        }
    }

    val hasLiveLevels = displayedLevels.any { it > 0.01f }

    Canvas(modifier = modifier) {
        val barCount = VISUALIZER_BAR_COUNT
        val gap = size.width / (barCount * 2.18f)
        val strokeWidth = gap.coerceAtLeast(2.4f)
        val step = size.width / barCount
        val baseline = size.height * 0.86f
        for (index in 0 until barCount) {
            val rawLevel = displayedLevels.getOrElse(index) { 0f }
            val previous = displayedLevels.getOrElse(index - 1) { rawLevel }
            val next = displayedLevels.getOrElse(index + 1) { rawLevel }
            val level = (rawLevel * 0.72f + previous * 0.14f + next * 0.14f).coerceIn(0f, 1f)
            val height = if (hasLiveLevels) {
                size.height * (0.1f + sqrt(level) * 0.72f)
            } else {
                1.5.dp.toPx()
            }
            val x = step * index + step / 2f
            drawLine(
                color = color.copy(alpha = if (hasLiveLevels) 0.48f + level * 0.46f else 0.34f),
                start = Offset(x, baseline),
                end = Offset(x, baseline - height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun NowPlayingBar(
    track: MusicTrack,
    session: JellyfinSession?,
    isPlaying: Boolean,
    progress: Float,
    status: String,
    openDragOffsetPx: Float,
    onOpen: () -> Unit,
    onOpenDragStart: () -> Unit,
    onOpenDragOffsetChange: (Float) -> Unit,
    onOpenDragEnd: (opened: Boolean) -> Unit,
    onToggle: () -> Unit,
    onReplay: () -> Unit,
    onSeek: (Float) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, openDragOffsetPx.roundToInt()) }
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .swipeUpToOpen(
                onOpen = onOpen,
                openDistance = 96.dp,
                onDragStart = onOpenDragStart,
                onDragOffsetChange = onOpenDragOffsetChange,
                onDragEnd = onOpenDragEnd
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumTile(track = track, session = session, modifier = Modifier.size(46.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} - $status",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = if (!isPlaying && status == "Ended") onReplay else onToggle,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = if (!isPlaying && status == "Ended") {
                            PlayerGlyph.Replay.imageVector()
                        } else if (isPlaying) {
                            PlayerGlyph.Pause.imageVector()
                        } else {
                            PlayerGlyph.Play.imageVector()
                        },
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(25.dp)
                    )
                }
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = PlayerGlyph.Next.imageVector(),
                        contentDescription = "Next track",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            WavySeekBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                onSeek = onSeek
            )
        }
    }
}

@Composable
private fun TopBarSineVisualizer(modifier: Modifier = Modifier, color: Color) {
    val transition = rememberInfiniteTransition(label = "top-bar-wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "top-bar-phase"
    )

    Canvas(
        modifier = modifier
            .width(64.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        val centerY = size.height / 2f
        val amplitude = size.height * 0.2f
        val wavelength = size.width / 1.55f
        val path = Path()
        var x = 0f

        path.moveTo(0f, centerY)
        while (x <= size.width) {
            val primary = sin((x / wavelength) * PI.toFloat() * 2f + phase)
            val texture = sin((x / size.width) * PI.toFloat() * 5.5f - phase * 0.42f) * 0.32f
            val y = centerY + (primary + texture) * amplitude
            path.lineTo(x, y)
            x += 4f
        }

        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.92f),
            style = Stroke(width = 3.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WavySeekBar(
    progress: Float,
    modifier: Modifier = Modifier,
    onSeek: (Float) -> Unit
) {
    WavySlider(
        value = progress.coerceIn(0f, 1f),
        onValueChange = onSeek,
        modifier = modifier,
        colors = SliderDefaults.colors(
            activeTrackColor = MaterialTheme.colorScheme.primary,
            inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f)
        ),
        waveLength = 18.dp,
        waveHeight = 8.dp,
        waveVelocity = 22.dp to HEAD,
        waveThickness = 5.dp,
        trackThickness = 5.dp,
        incremental = true,
        thumb = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    )
}

private class JellyfinPlayer(private val context: Context) {
    var currentTrack by mutableStateOf<MusicTrack?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var status by mutableStateOf("Ready")
        private set
    var progress by mutableFloatStateOf(0f)
        private set
    var visualizerLevels by mutableStateOf(FloatArray(VISUALIZER_BAR_COUNT))
        private set

    private val mainHandler = Handler(Looper.getMainLooper())
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationController = PlaybackNotificationController(context)
    private var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null
    private var currentSession: JellyfinSession? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var resumeOnAudioFocusGain = false
    private var visualizerEnabled = true
    private var smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
    private var lastVisualizerCaptureAt = 0L
    private var visualizerPumpRunning = false
    private var playbackGeneration = 0
    private val visualizerPump = object : Runnable {
        override fun run() {
            if (!isPlaying || !visualizerEnabled) {
                visualizerPumpRunning = false
                return
            }
            val now = SystemClock.elapsedRealtime()
            if (now - lastVisualizerCaptureAt > VISUALIZER_CAPTURE_STALE_MS) {
                visualizerLevels = fallbackVisualizerLevels(now, currentTrack?.id)
            }
            mainHandler.postDelayed(this, VISUALIZER_FALLBACK_FRAME_MS)
        }
    }
    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        mainHandler.post { handleAudioFocusChange(focusChange) }
    }

    fun play(track: MusicTrack, session: JellyfinSession) {
        val generation = ++playbackGeneration
        releasePlayerForReplacement()
        currentSession = session
        currentTrack = track
        status = "Buffering"
        progress = 0f
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        lastVisualizerCaptureAt = 0L
        publishPlaybackState(track)

        val nextPlayer = MediaPlayer()
        mediaPlayer = nextPlayer
        runCatching {
            nextPlayer.setAudioAttributes(
                playbackAudioAttributes()
            )
            val offlineFile = offlinePlayableFileFor(context, session, track)
            if (offlineFile != null) {
                nextPlayer.setDataSource(offlineFile.absolutePath)
            } else {
                nextPlayer.setDataSource(
                    context,
                    Uri.parse(track.streamUrl(session)),
                    session.streamHeaders()
                )
            }
            nextPlayer.setOnPreparedListener {
                if (!it.isActivePlayback(generation)) {
                    releaseMediaPlayerAsync(it)
                    return@setOnPreparedListener
                }
                if (!requestAudioFocus()) {
                    isPlaying = false
                    status = "Audio focus unavailable"
                    publishPlaybackState(track)
                    return@setOnPreparedListener
                }
                it.setVolume(1f, 1f)
                it.start()
                isPlaying = true
                status = "Playing"
                if (visualizerEnabled) {
                    attachVisualizer(it.audioSessionId)
                    startVisualizerPump()
                }
                syncProgress()
                publishPlaybackState(track)
            }
            nextPlayer.setOnCompletionListener {
                if (!it.isActivePlayback(generation)) return@setOnCompletionListener
                abandonAudioFocus()
                visualizer?.runCatching { enabled = false }
                stopVisualizerPump()
                isPlaying = false
                status = "Ended"
                progress = 1f
                smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                publishPlaybackState(track)
            }
            nextPlayer.setOnErrorListener { errorPlayer, _, _ ->
                if (!errorPlayer.isActivePlayback(generation)) return@setOnErrorListener true
                abandonAudioFocus()
                visualizer?.runCatching { enabled = false }
                stopVisualizerPump()
                isPlaying = false
                status = "Playback error"
                smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                publishPlaybackState(track)
                true
            }
            nextPlayer.prepareAsync()
        }.onFailure {
            if (nextPlayer.isActivePlayback(generation)) {
                isPlaying = false
                mediaPlayer = null
                status = it.readableMessage()
                publishPlaybackState(track)
            }
            releaseMediaPlayerAsync(nextPlayer)
        }
    }

    fun toggle() {
        val activePlayer = mediaPlayer ?: return
        if (isPlaying) {
            activePlayer.pause()
            abandonAudioFocus()
            visualizer?.runCatching { enabled = false }
            stopVisualizerPump()
            isPlaying = false
            status = "Paused"
            publishPlaybackState()
        } else {
            if (!requestAudioFocus()) {
                status = "Audio focus unavailable"
                publishPlaybackState()
                return
            }
            activePlayer.setVolume(1f, 1f)
            activePlayer.start()
            isPlaying = true
            status = "Playing"
            if (visualizerEnabled) {
                if (visualizer == null) {
                    attachVisualizer(activePlayer.audioSessionId)
                } else {
                    visualizer?.runCatching { enabled = true }
                }
                startVisualizerPump()
            }
            publishPlaybackState()
        }
    }

    fun setVisualizerEnabled(enabled: Boolean) {
        visualizerEnabled = enabled
        if (!enabled) {
            stopVisualizerPump()
            releaseVisualizer()
            smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
            visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        } else if (isPlaying) {
            mediaPlayer?.audioSessionId?.let(::attachVisualizer)
            startVisualizerPump()
        }
    }

    fun syncProgress() {
        val activePlayer = mediaPlayer ?: return
        runCatching {
            val duration = activePlayer.duration
            if (duration > 0) {
                progress = activePlayer.currentPosition.toFloat() / duration.toFloat()
                notificationController.syncPlaybackState(isPlaying, status, progress)
            }
        }
    }

    fun seekToFraction(fraction: Float) {
        val activePlayer = mediaPlayer ?: return
        runCatching {
            val duration = activePlayer.duration
            if (duration > 0) {
                val target = (duration * fraction.coerceIn(0f, 1f)).toInt()
                activePlayer.seekTo(target)
                progress = target.toFloat() / duration.toFloat()
                if (!isPlaying && status != "Ended") {
                    status = "Paused"
                }
                publishPlaybackState()
            }
        }
    }

    fun release() {
        playbackGeneration++
        releasePlayer()
        currentTrack = null
        currentSession = null
        status = "Ready"
        progress = 0f
        publishPlaybackState(null)
    }

    fun dispose() {
        release()
        notificationController.release()
    }

    private fun MediaPlayer.isActivePlayback(generation: Int): Boolean =
        generation == playbackGeneration && mediaPlayer === this

    private fun publishPlaybackState(track: MusicTrack? = currentTrack) {
        saveWidgetState(context, track, status, progress)
        notificationController.update(
            track = track,
            session = currentSession,
            isPlaying = isPlaying,
            status = status,
            progress = progress
        )
    }

    private fun playbackAudioAttributes(): AudioAttributes =
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
                }
            }
            .build()

    private fun requestAudioFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = audioFocusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAudioAttributes())
                .setOnAudioFocusChangeListener(audioFocusChangeListener, mainHandler)
                .setWillPauseWhenDucked(true)
                .build()
                .also { audioFocusRequest = it }
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        resumeOnAudioFocusGain = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let(audioManager::abandonAudioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        val activePlayer = mediaPlayer ?: return
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                activePlayer.runCatching { setVolume(1f, 1f) }
                if (resumeOnAudioFocusGain && !isPlaying) {
                    activePlayer.runCatching {
                        start()
                        this@JellyfinPlayer.isPlaying = true
                        status = "Playing"
                        if (visualizerEnabled) {
                            if (visualizer == null) {
                                attachVisualizer(audioSessionId)
                            } else {
                                visualizer?.runCatching { enabled = true }
                            }
                            startVisualizerPump()
                        }
                        publishPlaybackState()
                    }
                }
                resumeOnAudioFocusGain = false
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                resumeOnAudioFocusGain = false
                pauseForAudioFocusLoss()
                abandonAudioFocus()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                resumeOnAudioFocusGain = isPlaying
                pauseForAudioFocusLoss()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                activePlayer.runCatching { setVolume(0.25f, 0.25f) }
            }
        }
    }

    private fun pauseForAudioFocusLoss() {
        val activePlayer = mediaPlayer ?: return
        activePlayer.runCatching {
            if (isPlaying) pause()
        }
        visualizer?.runCatching { enabled = false }
        stopVisualizerPump()
        if (isPlaying) {
            isPlaying = false
            status = "Paused"
            publishPlaybackState()
        }
    }

    private fun releasePlayerForReplacement() {
        stopVisualizerPump()
        releaseVisualizer()
        releaseMediaPlayerAsync(mediaPlayer)
        mediaPlayer = null
        isPlaying = false
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
    }

    private fun releasePlayer() {
        stopVisualizerPump()
        releaseVisualizer()
        abandonAudioFocus()
        releaseMediaPlayerAsync(mediaPlayer)
        mediaPlayer = null
        isPlaying = false
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
    }

    private fun releaseMediaPlayerAsync(player: MediaPlayer?) {
        val stalePlayer = player ?: return
        stalePlayer.runCatching { setOnPreparedListener(null) }
        stalePlayer.runCatching { setOnCompletionListener(null) }
        stalePlayer.runCatching { setOnErrorListener(null) }
        thread(name = "jellyfin-player-release", isDaemon = true) {
            stalePlayer.runCatching { reset() }
            stalePlayer.runCatching { release() }
        }
    }

    private fun attachVisualizer(audioSessionId: Int) {
        releaseVisualizer()
        if (!visualizerEnabled) return
        if (audioSessionId == AudioManager.ERROR || audioSessionId == 0) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        runCatching {
            val captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
            val nextVisualizer = Visualizer(audioSessionId).apply {
                enabled = false
                setCaptureSize(captureSize)
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            val data = waveform ?: return
                            publishNativeVisualizerLevels(waveformToBars(data))
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            val data = fft ?: return
                            publishNativeVisualizerLevels(fftToBars(data))
                        }
                    },
                    (Visualizer.getMaxCaptureRate() / 3).coerceAtLeast(1_000),
                    false,
                    true
                )
                enabled = true
            }
            visualizer = nextVisualizer
        }.onFailure {
            visualizer = null
            smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
            visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        }
    }

    private fun publishNativeVisualizerLevels(target: FloatArray) {
        val nextLevels = smoothVisualizerLevels(target)
        lastVisualizerCaptureAt = SystemClock.elapsedRealtime()
        mainHandler.post { visualizerLevels = nextLevels }
    }

    private fun waveformToBars(waveform: ByteArray): FloatArray {
        if (waveform.isEmpty()) return FloatArray(VISUALIZER_BAR_COUNT)
        val rawBars = FloatArray(VISUALIZER_BAR_COUNT)
        val bars = FloatArray(VISUALIZER_BAR_COUNT)
        val samplesPerBar = (waveform.size / VISUALIZER_BAR_COUNT).coerceAtLeast(1)
        for (bar in rawBars.indices) {
            val start = bar * samplesPerBar
            val end = (start + samplesPerBar).coerceAtMost(waveform.size)
            var sum = 0f
            for (index in start until end) {
                val sample = (waveform[index].toInt() and 0xFF) - 128
                val normalized = sample / 128f
                sum += normalized * normalized
            }
            val rms = sqrt(sum / (end - start).coerceAtLeast(1))
            rawBars[bar] = ((rms - 0.012f) / 0.34f).coerceIn(0f, 1f)
        }
        for (bar in bars.indices) {
            val previous = rawBars.getOrElse(bar - 1) { rawBars[bar] }
            val next = rawBars.getOrElse(bar + 1) { rawBars[bar] }
            bars[bar] = (rawBars[bar] * 0.62f + previous * 0.19f + next * 0.19f).coerceIn(0f, 1f)
        }
        return bars
    }

    private fun fftToBars(fft: ByteArray): FloatArray {
        if (fft.size < 4) return FloatArray(VISUALIZER_BAR_COUNT)
        val rawBars = FloatArray(VISUALIZER_BAR_COUNT)
        val usableBins = ((fft.size / 2) - 1).coerceAtLeast(1)
        for (bar in rawBars.indices) {
            val startBin = 1 + (bar * usableBins / VISUALIZER_BAR_COUNT)
            val endBin = 1 + ((bar + 1) * usableBins / VISUALIZER_BAR_COUNT).coerceAtLeast(startBin + 1)
            var sum = 0f
            var count = 0
            for (bin in startBin until endBin.coerceAtMost(usableBins + 1)) {
                val real = fft.getOrElse(bin * 2) { 0 }.toFloat()
                val imag = fft.getOrElse(bin * 2 + 1) { 0 }.toFloat()
                sum += sqrt(real * real + imag * imag) / 128f
                count++
            }
            val average = sum / count.coerceAtLeast(1)
            rawBars[bar] = ((average - 0.02f) / 0.72f).coerceIn(0f, 1f)
        }
        return rawBars
    }

    private fun fallbackVisualizerLevels(nowMs: Long, trackId: String?): FloatArray {
        val time = nowMs / 1_000f
        val seed = trackId?.hashCode() ?: 0
        val center = (VISUALIZER_BAR_COUNT - 1) / 2f
        return FloatArray(VISUALIZER_BAR_COUNT) { index ->
            val distanceFromCenter = abs(index - center) / center.coerceAtLeast(1f)
            val centerWeight = 1f - distanceFromCenter
            val hash = speckleHash(seed, index)
            val phaseA = ((hash and 0x3FF) / 1024f) * PI.toFloat() * 2f
            val phaseB = (((hash ushr 10) and 0x3FF) / 1024f) * PI.toFloat() * 2f
            val drift = (sin(time * 1.35f + phaseA) + 1f) * 0.5f
            val pulse = (sin(time * 1.9f + phaseB) + 1f) * 0.5f
            val lift = (sin(time * 1.1f + seed * 0.00019f) + 1f) * 0.5f
            (0.025f + drift * 0.18f + pulse * 0.08f + lift * centerWeight * 0.16f)
                .coerceIn(0.02f, 0.55f)
        }
    }

    private fun smoothVisualizerLevels(target: FloatArray): FloatArray {
        if (smoothedVisualizerLevels.size != VISUALIZER_BAR_COUNT) {
            smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        }
        for (index in 0 until VISUALIZER_BAR_COUNT) {
            val current = smoothedVisualizerLevels[index]
            val next = target.getOrElse(index) { 0f }
            val smoothing = if (next > current) 0.24f else 0.12f
            smoothedVisualizerLevels[index] = current + (next - current) * smoothing
        }
        return smoothedVisualizerLevels.copyOf()
    }

    private fun startVisualizerPump() {
        if (visualizerPumpRunning || !visualizerEnabled) return
        visualizerPumpRunning = true
        mainHandler.post(visualizerPump)
    }

    private fun stopVisualizerPump() {
        visualizerPumpRunning = false
        mainHandler.removeCallbacks(visualizerPump)
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        lastVisualizerCaptureAt = 0L
    }

    private fun releaseVisualizer() {
        visualizer?.runCatching {
            enabled = false
            release()
        }
        visualizer = null
    }
}

private class ScratchSoundEngine {
    private val sampleRate = 44_100
    private val channelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val encoding = AudioFormat.ENCODING_PCM_16BIT
    private val minBufferSize = AudioTrack
        .getMinBufferSize(sampleRate, channelConfig, encoding)
        .takeIf { it > 0 }
        ?: sampleRate / 5
    private val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(encoding)
                .setSampleRate(sampleRate)
                .setChannelMask(channelConfig)
                .build()
        )
        .setBufferSizeInBytes(minBufferSize.coerceAtLeast(sampleRate / 6))
        .setTransferMode(AudioTrack.MODE_STREAM)
        .setSessionId(AudioManager.AUDIO_SESSION_ID_GENERATE)
        .build()
    private val scratchBuffer = ShortArray((sampleRate * 0.065f).toInt())
    private var lastScratchAt = 0L

    fun playScratch(angularVelocity: Float) {
        if (audioTrack.state != AudioTrack.STATE_INITIALIZED) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastScratchAt < 18L) return
        lastScratchAt = now

        runCatching {
            val velocity = angularVelocity.coerceIn(-2.4f, 2.4f)
            val intensity = (abs(velocity) / 1.25f).coerceIn(0.18f, 1f)
            val direction = if (velocity >= 0f) 1f else -1f
            val frameCount = (sampleRate * (0.026f + intensity * 0.035f))
                .toInt()
                .coerceAtMost(scratchBuffer.size)
            var carrierPhase = 0f
            var gatePhase = 0f
            val baseFrequency = 380f + intensity * 1_850f

            for (index in 0 until frameCount) {
                val t = index / frameCount.toFloat()
                val envelope = sin((PI * t).toFloat()).coerceAtLeast(0f)
                val sweep = if (direction > 0f) t else 1f - t
                val frequency = baseFrequency + sweep * intensity * 1_500f
                carrierPhase = (carrierPhase + frequency / sampleRate) % 1f
                gatePhase = (gatePhase + (28f + intensity * 94f) / sampleRate) % 1f

                val saw = carrierPhase * 2f - 1f
                val noise = Random.nextFloat() * 2f - 1f
                val gate = if (gatePhase < 0.48f) 1f else -0.55f
                val sample = (noise * 0.62f + saw * 0.38f) *
                    gate *
                    envelope *
                    (0.22f + intensity * 0.42f)
                scratchBuffer[index] = (sample * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
            }

            if (audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.play()
            }
            audioTrack.write(scratchBuffer, 0, frameCount, AudioTrack.WRITE_NON_BLOCKING)
        }
    }

    fun stop() {
        runCatching {
            if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.pause()
                audioTrack.flush()
            }
        }
    }

    fun release() {
        runCatching {
            stop()
            audioTrack.release()
        }
    }
}

private class JellyfinRepository(private val context: Context) {
    private val deviceId: String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "android"

    fun login(serverUrl: String, username: String, password: String): JellyfinSession {
        val normalizedUrl = normalizeServerUrl(serverUrl)
        val payload = JSONObject()
            .put("Username", username.trim())
            .put("Pw", password)
            .put("Password", password)
            .toString()

        val response = request(
            url = "$normalizedUrl/Users/AuthenticateByName",
            method = "POST",
            body = payload,
            session = null
        )
        val json = JSONObject(response)
        val user = json.getJSONObject("User")
        return JellyfinSession(
            serverUrl = normalizedUrl,
            username = user.optString("Name", username.trim()),
            userId = user.getString("Id"),
            token = json.getString("AccessToken")
        )
    }

    fun fetchTracks(
        session: JellyfinSession,
        onPartial: ((List<MusicTrack>) -> Unit)? = null
    ): List<MusicTrack> {
        val pageSize = 500
        return buildList {
            var startIndex = 0
            val seenTrackIds = mutableSetOf<String>()
            while (true) {
                val url = buildString {
                    append(session.serverUrl)
                    append("/Users/")
                    append(encode(session.userId))
                    append("/Items?Recursive=true")
                    append("&IncludeItemTypes=Audio")
                    append("&Fields=Album,AlbumId,AlbumPrimaryImageTag,Artists,DateCreated,ImageTags,PrimaryImageItemId,RunTimeTicks")
                    append("&StartIndex=$startIndex")
                    append("&Limit=$pageSize")
                }
                val response = JSONObject(request(url = url, method = "GET", body = null, session = session))
                val items = response.optJSONArray("Items") ?: JSONArray()
                if (items.length() == 0) break

                var addedTracks = 0
                for (index in 0 until items.length()) {
                    val item = items.optJSONObject(index) ?: continue
                    val id = item.optString("Id")
                    if (id.isBlank()) continue
                    if (!seenTrackIds.add(id)) continue
                    val artist = item.optJSONArray("Artists").joinOrFallback(item.optString("AlbumArtist", "Unknown artist"))
                    val imageTags = item.optJSONObject("ImageTags")
                    val primaryImageTag = imageTags?.optString("Primary").orEmpty()
                    val primaryImageItemId = item.optString("PrimaryImageItemId")
                    val albumId = item.optString("AlbumId")
                    val albumImageTag = item.optString("AlbumPrimaryImageTag")
                    val imageItemId = when {
                        primaryImageTag.isNotBlank() -> id
                        primaryImageItemId.isNotBlank() -> primaryImageItemId
                        albumId.isNotBlank() && albumImageTag.isNotBlank() -> albumId
                        else -> null
                    }
                    val imageTag = when {
                        primaryImageTag.isNotBlank() -> primaryImageTag
                        albumImageTag.isNotBlank() -> albumImageTag
                        else -> null
                    }
                    add(
                        MusicTrack(
                            id = id,
                            title = item.optString("Name", "Untitled"),
                            artist = artist,
                            album = item.optString("Album", "Unknown album").ifBlank { "Unknown album" },
                            durationMs = item.optLong("RunTimeTicks", 0L) / 10_000L,
                            dateAddedMs = parseJellyfinDateMs(item.optString("DateCreated")),
                            imageItemId = imageItemId,
                            imageTag = imageTag,
                            tint = tintFor(id)
                        )
                    )
                    addedTracks += 1
                }
                startIndex += items.length()
                if (isNotEmpty()) {
                    onPartial?.invoke(toList().sortedWith(MusicTrackSort))
                }
                if (items.length() < pageSize || addedTracks == 0) break
            }
        }.sortedWith(MusicTrackSort)
    }

    private fun request(
        url: String,
        method: String,
        body: String?,
        session: JellyfinSession?
    ): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8_000
            readTimeout = 15_000
            val authHeader = authorizationHeader(session)
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", authHeader)
            setRequestProperty("X-Emby-Authorization", authHeader)
            setRequestProperty("User-Agent", "Jellyfin Music/0.1.0 Android")
            session?.token?.let { setRequestProperty("X-Emby-Token", it) }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }

        try {
            if (body != null) {
                connection.outputStream.use { output ->
                    output.write(body.toByteArray(Charsets.UTF_8))
                }
            }

            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                throw JellyfinHttpException(code, response, url)
            }
            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun authorizationHeader(session: JellyfinSession?): String {
        val base = "MediaBrowser Client=\"Jellyfin Music\", Device=\"Android\", DeviceId=\"$deviceId\", Version=\"0.1.0\""
        return session?.let { "$base, Token=\"${it.token}\"" } ?: base
    }
}

private class JellyfinHttpException(
    val code: Int,
    private val response: String,
    private val requestUrl: String
) : IOException("HTTP $code") {
    fun displayMessage(): String =
        when (code) {
            401 -> "401 Unauthorized. Check username/password and use the Jellyfin server root URL, not /web. Reverse-proxy login can also block app clients."
            403 -> "403 Forbidden. Your server or reverse proxy blocked this app request."
            404 -> "404 Not Found. Check the server URL; if you copied Jellyfin from a browser, remove /web."
            else -> "Jellyfin returned HTTP $code ${response.take(160)}".trim()
        }.plus(if (requestUrl.contains("/web", ignoreCase = true)) " URL still contains /web." else "")
}

private fun loadSavedSession(context: Context): JellyfinSession? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val serverUrl = prefs.getString("serverUrl", null) ?: return null
    val username = prefs.getString("username", null) ?: return null
    val userId = prefs.getString("userId", null) ?: return null
    val token = prefs.getString("token", null) ?: return null
    return JellyfinSession(serverUrl = serverUrl, username = username, userId = userId, token = token)
}

private fun saveSession(context: Context, session: JellyfinSession) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString("serverUrl", session.serverUrl)
        .putString("username", session.username)
        .putString("userId", session.userId)
        .putString("token", session.token)
        .apply()
}

private fun loadCachedLibrary(context: Context, session: JellyfinSession): List<MusicTrack> {
    val file = libraryCacheFile(context, session)
    if (!file.isFile) return emptyList()
    return runCatching {
        val root = JSONObject(file.readText())
        if (root.optInt("version") != LIBRARY_CACHE_VERSION) return@runCatching emptyList()
        val items = root.optJSONArray("tracks") ?: return@runCatching emptyList()
        buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val id = item.optString("id")
                if (id.isBlank()) continue
                add(
                    MusicTrack(
                        id = id,
                        title = item.optString("title", "Untitled"),
                        artist = item.optString("artist", "Unknown artist"),
                        album = item.optString("album", "Unknown album"),
                        durationMs = item.optLong("durationMs", 0L),
                        dateAddedMs = item.optLong("dateAddedMs", 0L),
                        imageItemId = item.optString("imageItemId").takeIf { it.isNotBlank() },
                        imageTag = item.optString("imageTag").takeIf { it.isNotBlank() },
                        tint = if (item.has("tintArgb")) composeColor(item.optInt("tintArgb")) else tintFor(id)
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun loadCachedLibraryUpdatedAt(context: Context, session: JellyfinSession): Long? {
    val file = libraryCacheFile(context, session)
    if (!file.isFile) return null
    return runCatching {
        val root = JSONObject(file.readText())
        if (root.optInt("version") != LIBRARY_CACHE_VERSION) return@runCatching null
        root.optLong("updatedAt", 0L).takeIf { it > 0L }
    }.getOrNull()
}

private fun saveCachedLibrary(context: Context, session: JellyfinSession, tracks: List<MusicTrack>): Long? =
    runCatching {
        val items = JSONArray()
        tracks.forEach { track ->
            items.put(
                JSONObject()
                    .put("id", track.id)
                    .put("title", track.title)
                    .put("artist", track.artist)
                    .put("album", track.album)
                    .put("durationMs", track.durationMs)
                    .put("dateAddedMs", track.dateAddedMs)
                    .put("imageItemId", track.imageItemId.orEmpty())
                    .put("imageTag", track.imageTag.orEmpty())
                    .put("tintArgb", track.tint.toArgb())
            )
        }
        val updatedAt = System.currentTimeMillis()
        val root = JSONObject()
            .put("version", LIBRARY_CACHE_VERSION)
            .put("updatedAt", updatedAt)
            .put("tracks", items)
        libraryCacheFile(context, session).writeText(root.toString())
        updatedAt
    }.getOrNull()

private fun libraryCacheFile(context: Context, session: JellyfinSession): File =
    File(context.filesDir, "library-${stableCacheKey("${session.serverUrl}|${session.userId}")}.json")

private fun offlineSessionKey(session: JellyfinSession): String =
    stableCacheKey("${session.serverUrl}|${session.userId}")

private fun offlineDownloadsFile(context: Context, session: JellyfinSession): File =
    File(context.filesDir, "offline-downloads-${offlineSessionKey(session)}.json")

private fun offlineDownloadRoot(context: Context, session: JellyfinSession): File {
    val baseDir = context.getExternalFilesDir(OFFLINE_DOWNLOAD_DIR)
        ?: File(context.filesDir, OFFLINE_DOWNLOAD_DIR)
    return File(baseDir, offlineSessionKey(session))
}

private fun offlineAudioFileFor(context: Context, session: JellyfinSession, track: MusicTrack): File =
    File(offlineDownloadRoot(context, session), "${stableCacheKey(track.id)}.audio")

private fun offlineAudioFileFor(context: Context, session: JellyfinSession, download: OfflineDownload): File =
    File(offlineDownloadRoot(context, session), download.fileName)

private fun offlinePlayableFileFor(context: Context, session: JellyfinSession, track: MusicTrack): File? =
    offlineAudioFileFor(context, session, track).takeIf { it.isFile && it.length() > 0L }

private fun loadOfflineDownloads(context: Context, session: JellyfinSession): Map<String, OfflineDownload> {
    val file = offlineDownloadsFile(context, session)
    if (!file.isFile) return emptyMap()
    return runCatching {
        val root = JSONObject(file.readText())
        if (root.optInt("version") != OFFLINE_DOWNLOADS_VERSION) return@runCatching emptyMap()
        val items = root.optJSONArray("downloads") ?: return@runCatching emptyMap()
        buildMap {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val trackId = item.optString("trackId").takeIf { it.isNotBlank() } ?: continue
                val fileName = item.optString("fileName").takeIf { it.isNotBlank() } ?: continue
                val download = OfflineDownload(
                    trackId = trackId,
                    fileName = fileName,
                    bytes = item.optLong("bytes", 0L),
                    updatedAt = item.optLong("updatedAt", 0L)
                )
                val audioFile = offlineAudioFileFor(context, session, download)
                if (audioFile.isFile && audioFile.length() > 0L) {
                    put(trackId, download.copy(bytes = audioFile.length()))
                }
            }
        }
    }.getOrDefault(emptyMap())
}

private fun saveOfflineDownloads(
    context: Context,
    session: JellyfinSession,
    downloads: Map<String, OfflineDownload>
) {
    val items = JSONArray()
    downloads.values.sortedBy { it.updatedAt }.forEach { download ->
        items.put(
            JSONObject()
                .put("trackId", download.trackId)
                .put("fileName", download.fileName)
                .put("bytes", download.bytes)
                .put("updatedAt", download.updatedAt)
        )
    }
    val root = JSONObject()
        .put("version", OFFLINE_DOWNLOADS_VERSION)
        .put("downloads", items)
    offlineDownloadsFile(context, session).writeText(root.toString())
}

private fun offlineDownloadBytesOnDisk(context: Context, session: JellyfinSession): Long =
    offlineDownloadRoot(context, session)
        .listFiles()
        ?.filter { it.isFile && !it.name.endsWith(".download") }
        ?.sumOf { it.length() }
        ?: 0L

private fun downloadTrackForOffline(
    context: Context,
    session: JellyfinSession,
    track: MusicTrack,
    wifiOnly: Boolean,
    storageLimitMb: Int,
    onProgress: (OfflineDownloadProgress) -> Unit
): OfflineDownload {
    if (wifiOnly && !isWifiConnected(context)) {
        throw IOException("Connect to Wi-Fi or turn off Wi-Fi only downloads in Settings.")
    }

    val root = offlineDownloadRoot(context, session).apply { mkdirs() }
    val existingDownloads = loadOfflineDownloads(context, session)
    val existing = existingDownloads[track.id]
    if (existing != null && offlineAudioFileFor(context, session, existing).isFile) {
        return existing
    }

    val storageLimitBytes = storageLimitMb.toLong() * 1024L * 1024L
    val currentBytes = offlineDownloadBytesOnDisk(context, session)
    if (currentBytes >= storageLimitBytes) {
        throw IOException("Offline storage limit reached. Raise the limit or clear downloads.")
    }

    val fileName = "${stableCacheKey(track.id)}.audio"
    val finalFile = File(root, fileName)
    val tempFile = File(root, "$fileName.download")
    tempFile.delete()

    val connection = (URL(track.streamUrl(session)).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 15_000
        readTimeout = 30_000
        session.streamHeaders().forEach { (key, value) -> setRequestProperty(key, value) }
    }

    try {
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            throw IOException("Jellyfin returned HTTP $responseCode while downloading.")
        }

        val totalBytes = connection.contentLengthLong.takeIf { it > 0L } ?: -1L
        if (totalBytes > 0L && currentBytes + totalBytes > storageLimitBytes) {
            throw IOException("This track would pass the ${formatDataSize(storageLimitBytes)} offline limit.")
        }

        connection.inputStream.buffered().use { input ->
            tempFile.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesDownloaded = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    bytesDownloaded += read.toLong()
                    if (currentBytes + bytesDownloaded > storageLimitBytes) {
                        throw IOException("Offline storage limit reached during download.")
                    }
                    output.write(buffer, 0, read)
                    onProgress(OfflineDownloadProgress(bytesDownloaded, totalBytes))
                }
            }
        }

        finalFile.delete()
        if (!tempFile.renameTo(finalFile)) {
            tempFile.copyTo(finalFile, overwrite = true)
            tempFile.delete()
        }

        val download = OfflineDownload(
            trackId = track.id,
            fileName = fileName,
            bytes = finalFile.length(),
            updatedAt = System.currentTimeMillis()
        )
        saveOfflineDownloads(context, session, existingDownloads + (track.id to download))
        return download
    } finally {
        connection.disconnect()
        if (tempFile.isFile) tempFile.delete()
    }
}

private fun removeOfflineDownload(
    context: Context,
    session: JellyfinSession,
    track: MusicTrack
): Map<String, OfflineDownload> {
    val existingDownloads = loadOfflineDownloads(context, session)
    val existing = existingDownloads[track.id]
    val file = if (existing != null) {
        offlineAudioFileFor(context, session, existing)
    } else {
        offlineAudioFileFor(context, session, track)
    }
    file.delete()
    return (existingDownloads - track.id).also {
        saveOfflineDownloads(context, session, it)
    }
}

private fun removeAllOfflineDownloads(context: Context, session: JellyfinSession) {
    offlineDownloadRoot(context, session)
        .listFiles()
        ?.forEach { file -> file.delete() }
    saveOfflineDownloads(context, session, emptyMap())
}

private fun isWifiConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false
    val activeNetwork = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}

private fun clearSavedSession(context: Context) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove("serverUrl")
        .remove("username")
        .remove("userId")
        .remove("token")
        .apply()
}

private fun loadUseAlbumArtColors(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_USE_ALBUM_ART_COLORS, false)

private fun saveUseAlbumArtColors(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_USE_ALBUM_ART_COLORS, enabled)
        .apply()
}

private fun loadVisualizerEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_VISUALIZER_ENABLED, true)

private fun saveVisualizerEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_VISUALIZER_ENABLED, enabled)
        .apply()
}

private fun loadThemeMode(context: Context): AppThemeMode {
    val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(PREF_THEME_MODE, AppThemeMode.System.name)
    return runCatching {
        AppThemeMode.valueOf(saved ?: AppThemeMode.System.name)
    }.getOrDefault(AppThemeMode.System)
}

private fun saveThemeMode(context: Context, mode: AppThemeMode) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(PREF_THEME_MODE, mode.name)
        .apply()
}

private fun loadOfflineWifiOnly(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_OFFLINE_WIFI_ONLY, true)

private fun saveOfflineWifiOnly(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_OFFLINE_WIFI_ONLY, enabled)
        .apply()
}

private fun loadOfflineStorageLimitMb(context: Context): Int {
    val saved = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getInt(PREF_OFFLINE_STORAGE_LIMIT_MB, DEFAULT_OFFLINE_STORAGE_LIMIT_MB)
    return OfflineStorageLimitOptionsMb.minBy { abs(it - saved) }
}

private fun saveOfflineStorageLimitMb(context: Context, limitMb: Int) {
    val boundedLimit = if (limitMb in OfflineStorageLimitOptionsMb) {
        limitMb
    } else {
        DEFAULT_OFFLINE_STORAGE_LIMIT_MB
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(PREF_OFFLINE_STORAGE_LIMIT_MB, boundedLimit)
        .apply()
}

private fun likedTracksPreferenceKey(session: JellyfinSession): String =
    "$PREF_LIKED_TRACK_IDS_PREFIX${stableCacheKey("${session.serverUrl}|${session.userId}")}"

private fun loadLikedTrackIds(context: Context, session: JellyfinSession): Set<String> =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getStringSet(likedTracksPreferenceKey(session), emptySet())
        ?.toSet()
        ?: emptySet()

private fun saveLikedTrackIds(context: Context, session: JellyfinSession, likedTrackIds: Set<String>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(likedTracksPreferenceKey(session), likedTrackIds.toSet())
        .apply()
}

private fun pinnedLibraryItemsPreferenceKey(session: JellyfinSession): String =
    "$PREF_PINNED_LIBRARY_ITEMS_PREFIX${stableCacheKey("${session.serverUrl}|${session.userId}")}"

private fun loadPinnedLibraryItems(context: Context, session: JellyfinSession): List<PinnedLibraryItem> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(pinnedLibraryItemsPreferenceKey(session), null)
        ?: return emptyList()
    return runCatching {
        val items = JSONArray(raw)
        buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val type = runCatching {
                    LibraryCollectionType.valueOf(item.optString("type"))
                }.getOrNull() ?: continue
                val key = item.optString("key").takeIf { it.isNotBlank() } ?: continue
                add(PinnedLibraryItem(type = type, key = key))
            }
        }.distinct()
    }.getOrDefault(emptyList())
}

private fun savePinnedLibraryItems(
    context: Context,
    session: JellyfinSession,
    pinnedItems: List<PinnedLibraryItem>
) {
    val items = JSONArray()
    pinnedItems.distinct().forEach { item ->
        items.put(
            JSONObject()
                .put("type", item.type.name)
                .put("key", item.key)
        )
    }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(pinnedLibraryItemsPreferenceKey(session), items.toString())
        .apply()
}

private fun recentTrackIdsPreferenceKey(session: JellyfinSession): String =
    "$PREF_RECENT_TRACK_IDS_PREFIX${stableCacheKey("${session.serverUrl}|${session.userId}")}"

private fun loadRecentTrackIds(context: Context, session: JellyfinSession): List<String> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(recentTrackIdsPreferenceKey(session), null)
        ?: return emptyList()
    return runCatching {
        val items = JSONArray(raw)
        buildList {
            for (index in 0 until items.length()) {
                items.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }.distinct().take(80)
    }.getOrDefault(emptyList())
}

private fun saveRecentTrackIds(
    context: Context,
    session: JellyfinSession,
    recentTrackIds: List<String>
) {
    val items = JSONArray()
    recentTrackIds.distinct().take(80).forEach { items.put(it) }
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(recentTrackIdsPreferenceKey(session), items.toString())
        .apply()
}

private fun buildHomeQuickTracks(
    tracks: List<MusicTrack>,
    likedTracks: List<MusicTrack>,
    currentTrack: MusicTrack?
): List<MusicTrack> {
    val activeTrack = currentTrack?.takeIf { active ->
        tracks.any { it.id == active.id }
    }
    return (listOfNotNull(activeTrack) + likedTracks + tracks.homeMix(seed = 4_209L))
        .distinctBy { it.id }
        .take(6)
}

private fun todayHomeSeed(): Long =
    31_337L + System.currentTimeMillis() / 86_400_000L

private fun PinnedLibraryItem.asDetail(): LibraryDetail =
    LibraryDetail(type = type, key = key)

private fun PinnedLibraryItem.resolvePinnedItem(
    tracks: List<MusicTrack>,
    downloadedTrackIds: Set<String>
): ResolvedPinnedItem? {
    val detail = asDetail()
    val detailTracks = tracks.tracksForDetail(detail, downloadedTrackIds)
    if (detail.type != LibraryCollectionType.Downloaded && detailTracks.isEmpty()) return null
    return ResolvedPinnedItem(
        item = this,
        title = detail.titleLabel(),
        subtitle = detail.subtitleLabel(detailTracks),
        artworkTrack = detailTracks.firstOrNull(),
        shape = if (type == LibraryCollectionType.Artist) CircleShape else RoundedCornerShape(8.dp),
        detail = detail
    )
}

private fun LibraryDetail.titleLabel(): String =
    when (type) {
        LibraryCollectionType.Downloaded -> "Downloaded songs"
        else -> key
    }

private fun LibraryDetail.subtitleLabel(tracks: List<MusicTrack>): String =
    when (type) {
        LibraryCollectionType.Album -> tracks.firstOrNull()?.artist?.let { artist ->
            "${tracks.size.countLabel("song")} - $artist"
        } ?: "Album"
        LibraryCollectionType.Artist -> tracks.size.countLabel("song")
        LibraryCollectionType.Downloaded -> "${tracks.size.countLabel("song")} saved offline"
    }

private fun List<MusicTrack>.tracksForDetail(
    detail: LibraryDetail,
    downloadedTrackIds: Set<String>
): List<MusicTrack> =
    when (detail.type) {
        LibraryCollectionType.Album -> filter { it.album.equals(detail.key, ignoreCase = true) }
            .sortedWith(MusicTrackSort)
        LibraryCollectionType.Artist -> filter { it.artist.equals(detail.key, ignoreCase = true) }
            .sortedWith(
                compareBy<MusicTrack>(
                    { it.album.lowercase(Locale.getDefault()) },
                    { it.title.lowercase(Locale.getDefault()) }
                )
            )
        LibraryCollectionType.Downloaded -> filter { it.id in downloadedTrackIds }
            .sortedWith(MusicTrackSort)
    }

private fun List<MusicTrack>.filterForLibrary(
    filters: Set<LibraryFilter>,
    likedTrackIds: Set<String>,
    downloadedTrackIds: Set<String>,
    recentTrackIds: List<String>
): List<MusicTrack> {
    if (filters.isEmpty()) return this
    val recentSet = recentTrackIds.toSet()
    val newestCutoff = System.currentTimeMillis() - 45L * 24L * 60L * 60L * 1000L
    return filter { track ->
        filters.all { filter ->
            when (filter) {
                LibraryFilter.Liked -> track.id in likedTrackIds
                LibraryFilter.Downloaded -> track.id in downloadedTrackIds
                LibraryFilter.RecentPlayed -> track.id in recentSet
                LibraryFilter.RecentlyAdded -> track.dateAddedMs > 0L && track.dateAddedMs >= newestCutoff
                LibraryFilter.LongTracks -> track.durationMs >= 7L * 60L * 1000L
            }
        }
    }
}

private fun <T> List<T>.filterByInitial(letter: Char?, selector: (T) -> String): List<T> {
    val selected = letter ?: return this
    return filter { item -> selector(item).firstLibraryLetter() == selected }
}

private fun availableLettersForTab(
    selectedTab: LibraryTab,
    songs: List<MusicTrack>,
    albums: List<LibraryGroup>,
    artists: List<LibraryGroup>
): List<Char> =
    when (selectedTab) {
        LibraryTab.Songs -> songs.mapNotNull { it.title.firstLibraryLetter() }
        LibraryTab.Albums -> albums.mapNotNull { it.title.firstLibraryLetter() }
        LibraryTab.Artists -> artists.mapNotNull { it.title.firstLibraryLetter() }
    }.distinct().sorted()

private fun String.firstLibraryLetter(): Char? =
    trim()
        .firstOrNull { it.isLetterOrDigit() }
        ?.uppercaseChar()
        ?.let { if (it.isLetter()) it else '#' }

private fun List<MusicTrack>.homeMix(seed: Long): List<MusicTrack> {
    val queue = distinctBy { it.id }.toMutableList()
    if (queue.size <= 1) return queue
    Collections.shuffle(queue, java.util.Random(seed + queue.size * 97L))
    return queue
}

private fun List<MusicTrack>.randomizedPlaybackQueue(): List<MusicTrack> {
    val queue = distinctBy { it.id }.toMutableList()
    if (queue.size <= 1) return queue
    val seed = SystemClock.elapsedRealtimeNanos() xor java.lang.System.nanoTime()
    Collections.shuffle(queue, java.util.Random(seed))
    return queue
}

private fun List<MusicTrack>.queueStartingAt(track: MusicTrack): List<MusicTrack> {
    if (isEmpty()) return emptyList()
    val startIndex = indexOfFirst { it.id == track.id }
    if (startIndex < 0) return listOf(track) + filterNot { it.id == track.id }
    return drop(startIndex) + take(startIndex)
}

private fun List<MusicTrack>.moveQueueItem(fromIndex: Int, toIndex: Int): List<MusicTrack> {
    if (fromIndex !in indices || toIndex !in indices || fromIndex == toIndex) return this
    return toMutableList().apply {
        val item = removeAt(fromIndex)
        add(toIndex, item)
    }
}

private fun List<MusicTrack>.withTrackPlayNext(track: MusicTrack, currentTrack: MusicTrack?): List<MusicTrack> {
    if (currentTrack?.id == track.id) return this
    val normalized = currentTrack?.let { queueStartingAt(it) } ?: this
    val withoutTrack = normalized.filterNot { it.id == track.id }.toMutableList()
    val currentIndex = currentTrack?.let { active ->
        withoutTrack.indexOfFirst { it.id == active.id }
    } ?: -1
    val insertIndex = if (currentIndex >= 0) currentIndex + 1 else 0
    withoutTrack.add(insertIndex.coerceIn(0, withoutTrack.size), track)
    return withoutTrack
}

private fun normalizeServerUrl(input: String): String {
    val withScheme = input.trim().let { raw ->
        if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
            raw
        } else {
            "https://$raw"
        }
    }
    return withScheme
        .substringBefore("#")
        .substringBefore("?")
        .removeSuffix("/")
        .replace(Regex("/web(/index\\.html)?$", RegexOption.IGNORE_CASE), "")
        .removeSuffix("/")
}

private fun List<MusicTrack>.filterBy(query: String): List<MusicTrack> {
    val needle = query.searchKey()
    if (needle.isBlank()) return this
    val tokens = needle.split(' ').filter { it.isNotBlank() }
    if (tokens.isEmpty()) return this
    return mapNotNull { track ->
        track.searchScore(needle, tokens)?.let { score -> track to score }
    }.sortedWith { left, right ->
        val scoreCompare = left.second.compareTo(right.second)
        if (scoreCompare != 0) {
            scoreCompare
        } else {
            MusicTrackSort.compare(left.first, right.first)
        }
    }.map { it.first }
}

private fun List<MusicTrack>.sortedForLibrary(sortMode: LibrarySortMode): List<MusicTrack> =
    when (sortMode) {
        LibrarySortMode.Title -> sortedWith(MusicTrackSort)
        LibrarySortMode.Artist -> sortedWith(
            compareBy<MusicTrack>(
                { it.artist.lowercase(Locale.getDefault()) },
                { it.title.lowercase(Locale.getDefault()) },
                { it.album.lowercase(Locale.getDefault()) }
            )
        )
        LibrarySortMode.Album -> sortedWith(
            compareBy<MusicTrack>(
                { it.album.lowercase(Locale.getDefault()) },
                { it.artist.lowercase(Locale.getDefault()) },
                { it.title.lowercase(Locale.getDefault()) }
            )
        )
        LibrarySortMode.Duration -> sortedWith(
            compareByDescending<MusicTrack> { it.durationMs }
                .thenBy { it.title.lowercase(Locale.getDefault()) }
        )
    }

private fun List<LibraryGroup>.sortedGroupsForLibrary(sortMode: LibrarySortMode): List<LibraryGroup> =
    when (sortMode) {
        LibrarySortMode.Title,
        LibrarySortMode.Album -> sortedBy { it.title.lowercase(Locale.getDefault()) }
        LibrarySortMode.Artist -> sortedWith(
            compareBy<LibraryGroup>(
                { it.tracks.firstOrNull()?.artist?.lowercase(Locale.getDefault()).orEmpty() },
                { it.title.lowercase(Locale.getDefault()) }
            )
        )
        LibrarySortMode.Duration -> sortedWith(
            compareByDescending<LibraryGroup> { group -> group.tracks.sumOf { it.durationMs } }
                .thenBy { it.title.lowercase(Locale.getDefault()) }
        )
    }

private fun MusicTrack.searchScore(query: String, tokens: List<String>): Int? {
    val titleKey = title.searchKey()
    val artistKey = artist.searchKey()
    val albumKey = album.searchKey()
    val combinedKey = "$titleKey $artistKey $albumKey"
    val searchableWords = combinedKey.split(' ').filter { it.isNotBlank() }
    val fuzzyMatch = tokens.all { token ->
        combinedKey.contains(token) || searchableWords.any { word -> word.fuzzyMatches(token) }
    }
    if (!fuzzyMatch) return null
    return when {
        titleKey == query -> 0
        artistKey == query -> 1
        albumKey == query -> 2
        titleKey.startsWith(query) -> 3
        artistKey.startsWith(query) -> 4
        albumKey.startsWith(query) -> 5
        titleKey.contains(query) -> 6
        artistKey.contains(query) -> 7
        albumKey.contains(query) -> 8
        tokens.any { token -> titleKey.split(' ').any { it.fuzzyMatches(token) } } -> 9
        tokens.any { token -> artistKey.split(' ').any { it.fuzzyMatches(token) } } -> 10
        tokens.any { token -> albumKey.split(' ').any { it.fuzzyMatches(token) } } -> 11
        else -> 12
    }
}

private fun String.searchKey(): String =
    lowercase(Locale.getDefault())
        .replace(SearchSeparatorRegex, " ")
        .trim()

private fun String.fuzzyMatches(query: String): Boolean {
    if (query.length < 3 || isBlank()) return false
    if (startsWith(query) || query.startsWith(this)) return true
    val maxDistance = if (query.length >= 7) 2 else 1
    return levenshteinDistanceCapped(this, query, maxDistance) <= maxDistance
}

private fun levenshteinDistanceCapped(left: String, right: String, cap: Int): Int {
    if (abs(left.length - right.length) > cap) return cap + 1
    var previous = IntArray(right.length + 1) { it }
    var current = IntArray(right.length + 1)
    for (leftIndex in left.indices) {
        current[0] = leftIndex + 1
        var rowMin = current[0]
        for (rightIndex in right.indices) {
            val cost = if (left[leftIndex] == right[rightIndex]) 0 else 1
            current[rightIndex + 1] = minOf(
                current[rightIndex] + 1,
                previous[rightIndex + 1] + 1,
                previous[rightIndex] + cost
            )
            rowMin = minOf(rowMin, current[rightIndex + 1])
        }
        if (rowMin > cap) return cap + 1
        val swap = previous
        previous = current
        current = swap
    }
    return previous[right.length]
}

private fun List<MusicTrack>.groupByAlbum(): List<LibraryGroup> =
    groupBy { it.album }
        .toSortedMap(String.CASE_INSENSITIVE_ORDER)
        .map { (album, albumTracks) ->
            LibraryGroup(
                title = album,
                subtitle = "${albumTracks.size} songs - ${albumTracks.first().artist}",
                tint = albumTracks.first().tint,
                tracks = albumTracks
            )
        }

private fun List<MusicTrack>.groupByArtist(): List<LibraryGroup> =
    groupBy { it.artist }
        .toSortedMap(String.CASE_INSENSITIVE_ORDER)
        .map { (artist, artistTracks) ->
            LibraryGroup(
                title = artist,
                subtitle = "${artistTracks.size} songs",
                tint = artistTracks.first().tint,
                tracks = artistTracks
            )
        }

private fun JSONArray?.joinOrFallback(fallback: String): String {
    if (this == null || length() == 0) return fallback.ifBlank { "Unknown artist" }
    return buildList {
        for (index in 0 until length()) {
            optString(index).takeIf { it.isNotBlank() }?.let(::add)
        }
    }.joinToString(", ").ifBlank { fallback.ifBlank { "Unknown artist" } }
}

private fun tintFor(id: String): Color {
    val index = (id.hashCode() and Int.MAX_VALUE) % AlbumTints.size
    return AlbumTints[index]
}

private fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "--:--"
    val totalSeconds = durationMs / 1000L
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

private fun String.toHostLabel(): String =
    runCatching { Uri.parse(this).host ?: this }.getOrDefault(this)

private fun Throwable.readableMessage(): String =
    when (this) {
        is JellyfinHttpException -> displayMessage()
        is SocketTimeoutException -> "Jellyfin is not responding. Check that the server is awake and reachable from this device."
        else -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong"
    }

private fun encode(value: String): String =
    URLEncoder.encode(value, Charsets.UTF_8.name())
