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
import android.media.AudioManager
import android.media.MediaDescription
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.audiofx.Equalizer
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
import android.service.media.MediaBrowserService
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
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
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.common.AudioAttributes as Media3AudioAttributes
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
import kotlin.math.ln
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
        saveWidgetState(this, null, null, "Ready", 0f)
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
            val barColor = colorScheme.primary
            val barCount = 7
            val step = size.width / (barCount + 1)
            val centerY = size.height / 2f
            val maxBarHeight = size.height * 0.72f
            val activeStroke = 9.dp.toPx()

            for (index in 0 until barCount) {
                val wave = (sin(phase + index * 0.82f) + 1f) / 2f
                val accentWave = (sin(phase * 1.54f - index * 0.47f) + 1f) / 2f
                val level = (0.24f + wave * 0.56f + accentWave * 0.14f).coerceIn(0.22f, 0.94f)
                val barHeight = maxBarHeight * level
                val x = step * (index + 1)

                drawLine(
                    color = barColor.copy(alpha = 0.92f),
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
private const val PREF_FAVORITE_ALBUM_KEYS_PREFIX = "favorite_album_keys_"
private const val PREF_PINNED_LIBRARY_ITEMS_PREFIX = "pinned_library_items_"
private const val PREF_RECENT_TRACK_IDS_PREFIX = "recent_track_ids_"
private const val PREF_LOCAL_PLAYLISTS_PREFIX = "local_playlists_"
private const val PREF_OFFLINE_WIFI_ONLY = "offline_wifi_only"
private const val PREF_OFFLINE_STORAGE_LIMIT_MB = "offline_storage_limit_mb"
private const val PREF_DOWNLOADED_ONLY_MODE = "downloaded_only_mode"
private const val PREF_AUTO_SYNC_ON_LAUNCH = "auto_sync_on_launch"
private const val PREF_GAPLESS_PREBUFFER_ENABLED = "gapless_prebuffer_enabled"
private const val PREF_TRANSCODED_STREAMING_ENABLED = "transcoded_streaming_enabled"
private const val PREF_PLAYBACK_REPORTING_ENABLED = "playback_reporting_enabled"
private const val PREF_EQUALIZER_ENABLED = "equalizer_enabled"
private const val PREF_EQUALIZER_PRESET = "equalizer_preset"
private const val PREF_EQUALIZER_LEVELS = "equalizer_levels"
private const val LIBRARY_CACHE_VERSION = 2
private const val OFFLINE_DOWNLOADS_VERSION = 1
private const val LOCAL_PLAYLISTS_VERSION = 1
private const val ALBUM_ART_CACHE_DIR = "album_art_cache"
private const val OFFLINE_DOWNLOAD_DIR = "offline_audio"
private const val MAX_ALBUM_ART_CACHE_FILES = 128
private const val DEFAULT_OFFLINE_STORAGE_LIMIT_MB = 1024
private const val DISC_SCRATCH_SEEK_SCALE = 0.55f
private const val DISC_SCRATCH_DEAD_ZONE = 0.22f
private const val USER_SEEK_PROGRESS_HOLD_MS = 2_000L
private const val VISUALIZER_BAR_COUNT = 32
private const val VISUALIZER_CAPTURE_STALE_MS = 420L
private const val VISUALIZER_FALLBACK_FRAME_MS = 48L
private const val WIDGET_PROGRESS_UPDATE_MS = 2_000L
private const val EQUALIZER_BAND_COUNT = 5
private const val EQUALIZER_MIN_DB = -12f
private const val EQUALIZER_MAX_DB = 12f

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
    fun streamUrl(
        session: JellyfinSession,
        transcoded: Boolean = false,
        startPositionMs: Long = 0L
    ): String {
        val encodedId = encode(id)
        val encodedToken = encode(session.token)
        val startTimeParameter = startPositionMs
            .takeIf { it > 0L }
            ?.let { "&StartTimeTicks=${it * 10_000L}" }
            .orEmpty()
        if (transcoded) {
            return "${session.serverUrl}/Audio/$encodedId/universal" +
                "?UserId=${encode(session.userId)}" +
                "&DeviceId=${encode("jellyfin-music-${stableCacheKey("${session.serverUrl}|${session.userId}").take(16)}")}" +
                "&MaxStreamingBitrate=192000" +
                "&Container=mp3" +
                "&TranscodingContainer=mp3" +
                "&TranscodingProtocol=http" +
                "&AudioCodec=mp3" +
                startTimeParameter +
                "&api_key=$encodedToken"
        }
        return "${session.serverUrl}/Audio/$encodedId/stream?Static=true$startTimeParameter&api_key=$encodedToken"
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

private fun JellyfinSession.playbackKey(): String =
    stableCacheKey("$serverUrl|$userId|$token")

private const val PLAYBACK_NOTIFICATION_CHANNEL_ID = "playback"
private const val PLAYBACK_NOTIFICATION_ID = 1001
const val PLAYBACK_ACTION_PLAY = "dev.cholt.jellyfinmusic.action.PLAY"
const val PLAYBACK_ACTION_PAUSE = "dev.cholt.jellyfinmusic.action.PAUSE"
const val PLAYBACK_ACTION_TOGGLE = "dev.cholt.jellyfinmusic.action.TOGGLE"
const val PLAYBACK_ACTION_PREVIOUS = "dev.cholt.jellyfinmusic.action.PREVIOUS"
const val PLAYBACK_ACTION_NEXT = "dev.cholt.jellyfinmusic.action.NEXT"
const val PLAYBACK_ACTION_STOP = "dev.cholt.jellyfinmusic.action.STOP"
private const val PLAYBACK_SERVICE_ACTION_START = "dev.cholt.jellyfinmusic.service.START"
private const val PLAYBACK_SERVICE_ACTION_STOP = "dev.cholt.jellyfinmusic.service.STOP"
private const val AUTO_ROOT_ID = "auto:root"
private const val AUTO_SONGS_ID = "auto:songs"
private const val AUTO_ALBUMS_ID = "auto:albums"
private const val AUTO_ARTISTS_ID = "auto:artists"
private const val AUTO_TRACK_PREFIX = "auto:track:"
private const val AUTO_ALBUM_PREFIX = "auto:album:"
private const val AUTO_ARTIST_PREFIX = "auto:artist:"
private const val AUTO_MAX_TOP_LEVEL_ITEMS = 500

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

class AutoMediaBrowserService : MediaBrowserService() {
    private val serviceHandler = Handler(Looper.getMainLooper())
    private lateinit var autoSession: MediaSession
    private var autoQueue: List<MusicTrack> = emptyList()
    private var statePumpRunning = false
    private val statePump = object : Runnable {
        override fun run() {
            publishAutoSessionState()
            serviceHandler.postDelayed(this, 1_000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        autoSession = MediaSession(applicationContext, "Jellyfin Music Auto").apply {
            setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    playFirstOrResume()
                }

                override fun onPause() {
                    val player = autoPlayer()
                    if (player.isPlaying) {
                        player.toggle()
                    }
                    publishAutoSessionState()
                }

                override fun onStop() {
                    autoPlayer().release()
                    autoQueue = emptyList()
                    publishAutoSessionState()
                }

                override fun onSkipToNext() {
                    playAdjacentAuto(1)
                }

                override fun onSkipToPrevious() {
                    playAdjacentAuto(-1)
                }

                override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                    playFromAutoMediaId(mediaId)
                }

                override fun onPlayFromSearch(query: String?, extras: Bundle?) {
                    val searchTracks = loadAutoTracks().second.filterBy(query.orEmpty())
                    searchTracks.firstOrNull()?.let { track ->
                        playTrackFromQueue(searchTracks, track)
                    }
                }

                override fun onSeekTo(pos: Long) {
                    val track = autoPlayer().currentTrack ?: return
                    val duration = track.durationMs.takeIf { it > 0L } ?: return
                    autoPlayer().seekToFraction(pos.toFloat() / duration.toFloat())
                    publishAutoSessionState()
                }
            })
            isActive = true
        }
        sessionToken = autoSession.sessionToken
        startStatePump()
    }

    override fun onDestroy() {
        stopStatePump()
        autoSession.release()
        super.onDestroy()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot =
        BrowserRoot(AUTO_ROOT_ID, null)

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowser.MediaItem>>) {
        result.detach()
        thread(name = "jellyfin-auto-browse", isDaemon = true) {
            val items = buildAutoChildren(parentId)
            serviceHandler.post { result.sendResult(items) }
        }
    }

    private fun buildAutoChildren(parentId: String): List<MediaBrowser.MediaItem> {
        val (_, tracks) = loadAutoTracks()
        return when {
            parentId == AUTO_ROOT_ID -> buildAutoRootItems(tracks)
            parentId == AUTO_SONGS_ID -> tracks
                .take(AUTO_MAX_TOP_LEVEL_ITEMS)
                .map { it.toAutoTrackItem() }
            parentId == AUTO_ALBUMS_ID -> tracks
                .groupByAlbum()
                .take(AUTO_MAX_TOP_LEVEL_ITEMS)
                .map { it.toAutoGroupItem(AUTO_ALBUM_PREFIX, MediaBrowser.MediaItem.FLAG_BROWSABLE) }
            parentId == AUTO_ARTISTS_ID -> tracks
                .groupByArtist()
                .take(AUTO_MAX_TOP_LEVEL_ITEMS)
                .map { it.toAutoGroupItem(AUTO_ARTIST_PREFIX, MediaBrowser.MediaItem.FLAG_BROWSABLE) }
            parentId.startsWith(AUTO_ALBUM_PREFIX) -> {
                val album = Uri.decode(parentId.removePrefix(AUTO_ALBUM_PREFIX))
                tracks
                    .filter { it.album.equals(album, ignoreCase = true) }
                    .sortedWith(MusicTrackSort)
                    .map { it.toAutoTrackItem() }
            }
            parentId.startsWith(AUTO_ARTIST_PREFIX) -> {
                val artist = Uri.decode(parentId.removePrefix(AUTO_ARTIST_PREFIX))
                tracks
                    .filter { it.artist.equals(artist, ignoreCase = true) }
                    .sortedWith(
                        compareBy<MusicTrack>(
                            { it.album.lowercase(Locale.getDefault()) },
                            { it.title.lowercase(Locale.getDefault()) }
                        )
                    )
                    .map { it.toAutoTrackItem() }
            }
            else -> emptyList()
        }
    }

    private fun buildAutoRootItems(tracks: List<MusicTrack>): List<MediaBrowser.MediaItem> =
        listOf(
            MediaBrowser.MediaItem(
                MediaDescription.Builder()
                    .setMediaId(AUTO_SONGS_ID)
                    .setTitle("Songs")
                    .setSubtitle(tracks.size.countLabel("song"))
                    .build(),
                MediaBrowser.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowser.MediaItem(
                MediaDescription.Builder()
                    .setMediaId(AUTO_ALBUMS_ID)
                    .setTitle("Albums")
                    .setSubtitle(tracks.groupByAlbum().size.countLabel("album"))
                    .build(),
                MediaBrowser.MediaItem.FLAG_BROWSABLE
            ),
            MediaBrowser.MediaItem(
                MediaDescription.Builder()
                    .setMediaId(AUTO_ARTISTS_ID)
                    .setTitle("Artists")
                    .setSubtitle(tracks.groupByArtist().size.countLabel("artist"))
                    .build(),
                MediaBrowser.MediaItem.FLAG_BROWSABLE
            )
        )

    private fun playFirstOrResume() {
        val player = autoPlayer()
        val current = player.currentTrack
        if (current != null && !player.isPlaying && player.status != "Ended") {
            player.toggle()
            publishAutoSessionState()
            return
        }
        val tracks = loadAutoTracks().second
        tracks.firstOrNull()?.let { playTrackFromQueue(tracks, it) }
    }

    private fun playFromAutoMediaId(mediaId: String?) {
        val (session, tracks) = loadAutoTracks()
        if (session == null || tracks.isEmpty() || mediaId.isNullOrBlank()) return
        val queue = when {
            mediaId.startsWith(AUTO_TRACK_PREFIX) -> {
                val trackId = mediaId.removePrefix(AUTO_TRACK_PREFIX)
                tracks.firstOrNull { it.id == trackId }?.let { tracks.queueStartingAt(it) }.orEmpty()
            }
            mediaId.startsWith(AUTO_ALBUM_PREFIX) -> {
                val album = Uri.decode(mediaId.removePrefix(AUTO_ALBUM_PREFIX))
                tracks.filter { it.album.equals(album, ignoreCase = true) }.sortedWith(MusicTrackSort)
            }
            mediaId.startsWith(AUTO_ARTIST_PREFIX) -> {
                val artist = Uri.decode(mediaId.removePrefix(AUTO_ARTIST_PREFIX))
                tracks
                    .filter { it.artist.equals(artist, ignoreCase = true) }
                    .sortedWith(
                        compareBy<MusicTrack>(
                            { it.album.lowercase(Locale.getDefault()) },
                            { it.title.lowercase(Locale.getDefault()) }
                        )
                    )
            }
            mediaId == AUTO_SONGS_ID -> tracks
            else -> emptyList()
        }
        queue.firstOrNull()?.let { playTrackFromQueue(queue, it, session) }
    }

    private fun playAdjacentAuto(offset: Int) {
        val player = autoPlayer()
        val activeTrack = player.currentTrack ?: return
        val (session, tracks) = loadAutoTracks()
        if (session == null) return
        val queue = autoQueue
            .ifEmpty { tracks.queueStartingAt(activeTrack) }
            .ifEmpty { listOf(activeTrack) }
            .queueStartingAt(activeTrack)
        if (queue.isEmpty()) return
        val nextIndex = (offset + queue.size) % queue.size
        playTrackFromQueue(queue, queue[nextIndex], session)
    }

    private fun playTrackFromQueue(
        queue: List<MusicTrack>,
        track: MusicTrack,
        session: JellyfinSession? = loadAutoTracks().first
    ) {
        val activeSession = session ?: return
        autoQueue = queue.queueStartingAt(track).ifEmpty { listOf(track) }
        autoPlayer().play(track, activeSession)
        publishAutoSessionState()
    }

    private fun loadAutoTracks(): Pair<JellyfinSession?, List<MusicTrack>> {
        val session = loadSavedSession(applicationContext) ?: return null to emptyList()
        return session to loadCachedLibrary(applicationContext, session)
    }

    private fun autoPlayer(): JellyfinPlayer =
        PlaybackControllerHolder.get(applicationContext)

    private fun publishAutoSessionState() {
        val player = autoPlayer()
        player.syncProgress()
        val track = player.currentTrack
        if (track != null) {
            autoSession.setMetadata(
                MediaMetadata.Builder()
                    .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, "$AUTO_TRACK_PREFIX${track.id}")
                    .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                    .putString(MediaMetadata.METADATA_KEY_ALBUM, track.album)
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, track.durationMs.coerceAtLeast(0L))
                    .build()
            )
        }
        val state = when {
            player.status == "Buffering" -> PlaybackState.STATE_BUFFERING
            player.isPlaying -> PlaybackState.STATE_PLAYING
            player.status == "Ended" -> PlaybackState.STATE_STOPPED
            else -> PlaybackState.STATE_PAUSED
        }
        val position = track
            ?.let { (it.durationMs * player.progress).toLong().coerceAtLeast(0L) }
            ?: PlaybackState.PLAYBACK_POSITION_UNKNOWN
        autoSession.setPlaybackState(
            PlaybackState.Builder()
                .setActions(
                    PlaybackState.ACTION_PLAY or
                        PlaybackState.ACTION_PAUSE or
                        PlaybackState.ACTION_PLAY_PAUSE or
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackState.ACTION_SKIP_TO_NEXT or
                        PlaybackState.ACTION_STOP or
                        PlaybackState.ACTION_SEEK_TO or
                        PlaybackState.ACTION_PLAY_FROM_MEDIA_ID or
                        PlaybackState.ACTION_PLAY_FROM_SEARCH
                )
                .setState(state, position, if (player.isPlaying) 1f else 0f, SystemClock.elapsedRealtime())
                .build()
        )
        autoSession.isActive = true
    }

    private fun startStatePump() {
        if (statePumpRunning) return
        statePumpRunning = true
        serviceHandler.post(statePump)
    }

    private fun stopStatePump() {
        statePumpRunning = false
        serviceHandler.removeCallbacks(statePump)
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
    Artists("Artists"),
    Playlists("Playlists")
}

private enum class FavoritesTab(val label: String) {
    Tracks("Tracks"),
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
    Playlist("Playlist"),
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

private enum class SettingsPage(val label: String, val subtitle: String) {
    Account("Account", "Server, user, and sign out"),
    Library("Library & cache", "Sync, metadata, and artwork"),
    Offline("Offline", "Downloads and storage"),
    Playback("Playback", "Streaming and reporting"),
    Audio("Equalizer", "Presets, bands, and output tone"),
    Appearance("Look and feel", "Theme, colors, and visualizer"),
    About("About", "App version")
}

private data class EqualizerPreset(
    val name: String,
    val levelsDb: List<Float>
)

private val EqualizerFlatPreset = EqualizerPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f))

private data class EqualizerSettings(
    val enabled: Boolean = false,
    val presetName: String = EqualizerFlatPreset.name,
    val levelsDb: List<Float> = EqualizerFlatPreset.levelsDb
) {
    fun normalizedLevels(): List<Float> =
        (0 until EQUALIZER_BAND_COUNT).map { index ->
            levelsDb.getOrNull(index)?.coerceIn(EQUALIZER_MIN_DB, EQUALIZER_MAX_DB) ?: 0f
    }
}

private val EqualizerPresets = listOf(
    EqualizerFlatPreset,
    EqualizerPreset("Warm", listOf(3.5f, 2f, 0f, -1f, -1.5f)),
    EqualizerPreset("Bass lift", listOf(6f, 4f, 1f, -1f, -2f)),
    EqualizerPreset("Vocal", listOf(-2f, -0.5f, 3.5f, 2.5f, -1f)),
    EqualizerPreset("Sparkle", listOf(-1f, 0f, 1.5f, 4f, 5.5f)),
    EqualizerPreset("Night drive", listOf(4f, 1.5f, -1f, 2f, 3f))
)

private val EqualizerBandLabels = listOf("60", "230", "910", "3.6k", "14k")

private enum class AppThemeMode(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}

private val BottomTabDestinations = listOf(
    AppDestination.Home,
    AppDestination.Library,
    AppDestination.Liked,
    AppDestination.Profile
)

private data class LibraryGroup(
    val title: String,
    val subtitle: String,
    val tint: Color,
    val tracks: List<MusicTrack>,
    val key: String = title
)

private data class PinnedLibraryItem(
    val type: LibraryCollectionType,
    val key: String
)

private data class LibraryDetail(
    val type: LibraryCollectionType,
    val key: String
)

private data class LocalPlaylist(
    val id: String,
    val name: String,
    val folder: String,
    val trackIds: List<String>,
    val isFavorite: Boolean,
    val updatedAt: Long
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
    val artists: Int,
    val playlists: Int = 0
) {
    fun countFor(tab: LibraryTab): Int =
        when (tab) {
            LibraryTab.Songs -> songs
            LibraryTab.Albums -> albums
            LibraryTab.Artists -> artists
            LibraryTab.Playlists -> playlists
    }
}

private enum class SearchTopResultType(val label: String) {
    Song("Song"),
    Album("Album"),
    Artist("Artist"),
    Playlist("Playlist")
}

private data class SearchTopResult(
    val type: SearchTopResultType,
    val title: String,
    val subtitle: String,
    val artworkTrack: MusicTrack?,
    val artworkShape: Shape,
    val track: MusicTrack? = null,
    val detail: LibraryDetail? = null
)

private fun SearchResultCounts.summary(): String =
    listOf(
        songs.countLabel("song"),
        albums.countLabel("album"),
        artists.countLabel("artist"),
        playlists.countLabel("playlist")
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
    { it.title.libraryIndexSortWeight() },
    { it.title.lowercase(Locale.getDefault()) },
    { it.artist.lowercase(Locale.getDefault()) },
    { it.album.lowercase(Locale.getDefault()) }
)
private val LocalPlaylistSort = compareByDescending<LocalPlaylist> { it.isFavorite }
    .thenBy { it.folder.lowercase(Locale.getDefault()) }
    .thenBy { it.name.lowercase(Locale.getDefault()) }
private val SearchSeparatorRegex = Regex("[^\\p{L}\\p{N}]+")
private const val LibraryNumberIndex = '#'
private const val LibraryOtherIndex = '*'
private val LibraryAlphabetRail = listOf(LibraryNumberIndex) + ('A'..'Z').toList() + listOf(LibraryOtherIndex)

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
private fun JellyfinMusicApp() {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val libraryRailScrollJob = remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val repository = remember { JellyfinRepository(context.applicationContext) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val player = remember { PlaybackControllerHolder.get(context.applicationContext) }
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
    var favoriteAlbumKeys by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadFavoriteAlbumKeys(context, it) } ?: emptySet())
    }
    var recentTrackIds by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadRecentTrackIds(context, it) } ?: emptyList())
    }
    var pinnedLibraryItems by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadPinnedLibraryItems(context, it) } ?: emptyList())
    }
    var localPlaylists by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadLocalPlaylists(context, it) } ?: emptyList())
    }
    var offlineDownloads by remember(session?.serverUrl, session?.userId) {
        mutableStateOf(session?.let { loadOfflineDownloads(context, it) } ?: emptyMap())
    }
    var downloadProgressById by remember { mutableStateOf(emptyMap<String, OfflineDownloadProgress>()) }
    var selectedTab by remember { mutableStateOf(LibraryTab.Songs) }
    var favoritesTab by remember { mutableStateOf(FavoritesTab.Tracks) }
    var librarySearchQuery by remember { mutableStateOf("") }
    var librarySortMode by remember { mutableStateOf(LibrarySortMode.Title) }
    var libraryGridView by remember { mutableStateOf(true) }
    var activeLibraryFilters by remember { mutableStateOf(emptySet<LibraryFilter>()) }
    var libraryLetterFilter by remember { mutableStateOf<Char?>(null) }
    var downloadedOnlyMode by remember { mutableStateOf(loadDownloadedOnlyMode(context)) }
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
    var autoSyncOnLaunch by remember { mutableStateOf(loadAutoSyncOnLaunch(context)) }
    var gaplessPrebufferEnabled by remember { mutableStateOf(loadGaplessPrebufferEnabled(context)) }
    var transcodedStreamingEnabled by remember { mutableStateOf(loadTranscodedStreamingEnabled(context)) }
    var playbackReportingEnabled by remember { mutableStateOf(loadPlaybackReportingEnabled(context)) }
    var equalizerSettings by remember { mutableStateOf(loadEqualizerSettings(context)) }
    var selectedDestination by remember { mutableStateOf(AppDestination.Home) }
    var activeSettingsPage by remember { mutableStateOf<SettingsPage?>(null) }
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
        favoriteAlbumKeys = emptySet()
        recentTrackIds = emptyList()
        pinnedLibraryItems = emptyList()
        localPlaylists = emptyList()
        offlineDownloads = emptyMap()
        downloadProgressById = emptyMap()
        activeLibraryFilters = emptySet()
        libraryLetterFilter = null
        downloadedOnlyMode = false
        saveDownloadedOnlyMode(context, false)
        activeLibraryDetail = null
        lastLibrarySyncAt = null
        playQueue = emptyList()
        statusText = null
        showPlayer = false
        selectedDestination = AppDestination.Home
    }

    fun setDownloadedOnlyMode(enabled: Boolean) {
        downloadedOnlyMode = enabled
        saveDownloadedOnlyMode(context, enabled)
    }

    fun clearLibraryCache() {
        val activeSession = session ?: return
        runCatching { libraryCacheFile(context, activeSession).delete() }
        lastLibrarySyncAt = null
        statusText = "Library cache cleared"
    }

    fun clearArtworkCache() {
        AlbumArtCache.clear()
        runCatching {
            File(context.cacheDir, ALBUM_ART_CACHE_DIR)
                .takeIf { it.exists() }
                ?.deleteRecursively()
        }
        statusText = "Artwork cache cleared"
    }

    fun showNowPlayingPlayer() {
        showPlayer = true
        isPlayerOpenDragging = false
        playerOpenDragOffsetPx = 0f
        isPlayerDismissDragging = false
        playerDismissDragOffsetPx = 0f
    }

    fun playTrack(track: MusicTrack, openPlayer: Boolean = false, source: List<MusicTrack> = tracks) {
        session?.let { activeSession ->
            playQueue = source.queueStartingAt(track).ifEmpty { listOf(track) }
            if (openPlayer) {
                showNowPlayingPlayer()
            }
            player.play(track, activeSession)
            val nextRecentTrackIds = (listOf(track.id) + recentTrackIds.filterNot { it == track.id }).take(80)
            recentTrackIds = nextRecentTrackIds
            saveRecentTrackIds(context, activeSession, nextRecentTrackIds)
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

    fun removeQueueTrack(index: Int) {
        if (index <= 0 || index !in playQueue.indices) return
        playQueue = playQueue.toMutableList().apply { removeAt(index) }
    }

    fun clearQueueAfterCurrent() {
        val activeTrack = player.currentTrack
        playQueue = activeTrack?.let { listOf(it) } ?: emptyList()
        statusText = "Queue cleared"
    }

    fun reshuffleQueueAfterCurrent() {
        val activeTrack = player.currentTrack ?: return
        val baseQueue = playQueue.ifEmpty { tracks.queueStartingAt(activeTrack) }.queueStartingAt(activeTrack)
        if (baseQueue.size <= 2) return
        val shuffledRest = baseQueue.drop(1).randomizedPlaybackQueue()
        playQueue = listOf(activeTrack) + shuffledRest
        statusText = "Queue reshuffled"
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

    fun toggleFavoriteAlbum(albumKey: String) {
        if (albumKey.isBlank()) return
        val nextFavoriteAlbumKeys = if (albumKey in favoriteAlbumKeys) {
            favoriteAlbumKeys - albumKey
        } else {
            favoriteAlbumKeys + albumKey
        }
        favoriteAlbumKeys = nextFavoriteAlbumKeys
        session?.let { activeSession ->
            saveFavoriteAlbumKeys(context, activeSession, nextFavoriteAlbumKeys)
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

    fun saveLocalPlaylistsState(nextPlaylists: List<LocalPlaylist>, message: String? = null) {
        val sortedPlaylists = nextPlaylists.sortedWith(LocalPlaylistSort)
        localPlaylists = sortedPlaylists
        session?.let { activeSession ->
            saveLocalPlaylists(context, activeSession, sortedPlaylists)
        }
        if (message != null) {
            statusText = message
        }
    }

    fun createPlaylist(name: String, folder: String = "", source: List<MusicTrack> = emptyList()) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            statusText = "Playlist name is required"
            return
        }
        val trackIds = source.map { it.id }.distinct()
        val playlist = LocalPlaylist(
            id = newLocalPlaylistId(trimmedName),
            name = trimmedName,
            folder = folder.trim(),
            trackIds = trackIds,
            isFavorite = false,
            updatedAt = System.currentTimeMillis()
        )
        saveLocalPlaylistsState(
            nextPlaylists = localPlaylists + playlist,
            message = "Created ${playlist.name}"
        )
    }

    fun saveQueueAsPlaylist(queue: List<MusicTrack>) {
        val tracksToSave = queue.distinctBy { it.id }
        if (tracksToSave.isEmpty()) {
            statusText = "Queue is empty"
            return
        }
        createPlaylist(
            name = "Queue ${SimpleDateFormat("MMM d h:mm a", Locale.getDefault()).format(java.util.Date())}",
            folder = "Saved queues",
            source = tracksToSave
        )
    }

    fun addTrackToPlaylist(playlist: LocalPlaylist, track: MusicTrack) {
        val nextPlaylists = localPlaylists.map { item ->
            if (item.id != playlist.id || track.id in item.trackIds) {
                item
            } else {
                item.copy(
                    trackIds = item.trackIds + track.id,
                    updatedAt = System.currentTimeMillis()
                )
            }
        }
        saveLocalPlaylistsState(nextPlaylists, "Added to ${playlist.name}")
    }

    fun removeTrackFromPlaylist(playlistId: String, track: MusicTrack) {
        val playlist = localPlaylists.firstOrNull { it.id == playlistId } ?: return
        val nextPlaylists = localPlaylists.map { item ->
            if (item.id == playlistId) {
                item.copy(
                    trackIds = item.trackIds.filterNot { it == track.id },
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                item
            }
        }
        saveLocalPlaylistsState(nextPlaylists, "Removed from ${playlist.name}")
    }

    fun togglePlaylistFavorite(playlistId: String) {
        val nextPlaylists = localPlaylists.map { item ->
            if (item.id == playlistId) {
                item.copy(isFavorite = !item.isFavorite, updatedAt = System.currentTimeMillis())
            } else {
                item
            }
        }
        saveLocalPlaylistsState(nextPlaylists)
    }

    fun deletePlaylist(playlistId: String) {
        val playlist = localPlaylists.firstOrNull { it.id == playlistId } ?: return
        val nextPlaylists = localPlaylists.filterNot { it.id == playlistId }
        saveLocalPlaylistsState(nextPlaylists, "Deleted ${playlist.name}")
        pinnedLibraryItems = pinnedLibraryItems.filterNot {
            it.type == LibraryCollectionType.Playlist && it.key == playlistId
        }
        session?.let { activeSession ->
            savePinnedLibraryItems(context, activeSession, pinnedLibraryItems)
        }
        if (activeLibraryDetail?.type == LibraryCollectionType.Playlist && activeLibraryDetail?.key == playlistId) {
            activeLibraryDetail = null
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
            if (!player.isPlaying) {
                PlaybackNotificationActions.clear()
                player.dispose()
                PlaybackControllerHolder.clear(player)
            }
        }
    }

    LaunchedEffect(session?.token, autoSyncOnLaunch) {
        val activeSession = session
        if (activeSession != null && (tracks.isEmpty() || autoSyncOnLaunch)) {
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

    LaunchedEffect(
        player.currentTrack?.id,
        player.isPlaying,
        playQueue,
        shuffleEnabled,
        gaplessPrebufferEnabled,
        session?.serverUrl,
        session?.userId,
        session?.token
    ) {
        val activeTrack = player.currentTrack
        val activeSession = session
        if (!gaplessPrebufferEnabled || !player.isPlaying || activeTrack == null || activeSession == null || shuffleEnabled) {
            if (player.status != "Ended") {
                player.prepareNext(null, null)
            }
            return@LaunchedEffect
        }

        delay(650)
        val stillActiveTrack = player.currentTrack
        if (!player.isPlaying || stillActiveTrack?.id != activeTrack.id) return@LaunchedEffect

        val nextTrack = playQueue
            .ifEmpty { tracks.queueStartingAt(activeTrack) }
            .nextTrackAfter(activeTrack)
        player.prepareNext(nextTrack, activeSession)
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
        showPlayer = false
        isPlayerDismissDragging = false
        playerDismissDragOffsetPx = 0f
        isPlayerOpenDragging = false
        playerOpenDragOffsetPx = 0f
    }
    val showTopBar = session == null ||
        activeLibraryDetail != null ||
        selectedDestination == AppDestination.Profile
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
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val activeTrack = player.currentTrack
                val connectedSession = session
                val visibleTracks = remember(tracks, downloadedOnlyMode, offlineDownloads) {
                    if (downloadedOnlyMode) {
                        tracks.filter { it.id in offlineDownloads }
                    } else {
                        tracks
                    }
                }
                val density = LocalDensity.current
                val openSheetOffsetPx = if (isPlayerOpenDragging) {
                    with(density) { maxHeight.toPx() } + playerOpenOffsetPx
                } else {
                    0f
                }
                val fullPlayerOffsetPx = if (isPlayerOpenDragging) {
                    openSheetOffsetPx.coerceAtLeast(0f)
                } else {
                    playerDismissOffsetPx
                }

                Scaffold(
                modifier = Modifier,
                topBar = {
                if (showTopBar) {
                    TopAppBar(
                        modifier = Modifier,
                        navigationIcon = {
                            when {
                                showPlayer -> IconButton(onClick = closePlayer) {
                                        Icon(
                                            imageVector = PlayerIconVectors.ChevronDown,
                                            contentDescription = "Close player"
                                        )
                                }
                                selectedDestination == AppDestination.Profile && activeSettingsPage != null -> IconButton(
                                    onClick = { activeSettingsPage = null }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to Settings"
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
                                    selectedDestination == AppDestination.Profile -> activeSettingsPage?.label ?: "Settings"
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
                if (activeSession != null && (!showPlayer || isPlayerOpenDragging)) {
                    Column {
                        if (activeTrack != null) {
                            NowPlayingBar(
                                track = activeTrack,
                                session = activeSession,
                                isPlaying = player.isPlaying,
                                progress = player.progress,
                                status = player.status,
                                onOpen = ::showNowPlayingPlayer,
                                onOpenDragStart = {
                                    showPlayer = true
                                    isPlayerOpenDragging = true
                                    isPlayerDismissDragging = false
                                    playerDismissDragOffsetPx = 0f
                                },
                                onOpenDragOffsetChange = { offset ->
                                    playerOpenDragOffsetPx = offset
                                },
                                onOpenDragEnd = { opened ->
                                    isPlayerOpenDragging = false
                                    if (!opened) {
                                        showPlayer = false
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
                            onDestinationSelected = { destination ->
                                val reselected = destination == selectedDestination
                                val reselectedSearch =
                                    destination == AppDestination.Search && reselected
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (destination != AppDestination.Player) {
                                    selectedDestination = destination
                                }
                                when (destination) {
                                    AppDestination.Home -> {
                                        selectedTab = LibraryTab.Songs
                                        activeLibraryDetail = null
                                        activeSettingsPage = null
                                        showPlayer = false
                                        if (reselected) {
                                            coroutineScope.launch { mainListState.animateScrollToItem(0) }
                                        }
                                    }

                                    AppDestination.Search -> {
                                        activeLibraryDetail = null
                                        activeSettingsPage = null
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
                                        activeSettingsPage = null
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
                                        activeSettingsPage = null
                                        showPlayer = false
                                        if (reselected) {
                                            coroutineScope.launch { mainListState.animateScrollToItem(0) }
                                        }
                                    }

                                    AppDestination.Profile -> {
                                        activeLibraryDetail = null
                                        if (reselected && activeSettingsPage != null) {
                                            activeSettingsPage = null
                                        }
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
                                        activeSettingsPage = null
                                        coroutineScope.launch { mainListState.animateScrollToItem(0) }
                                    }
                                    AppDestination.Search -> {
                                        selectedDestination = AppDestination.Search
                                        showPlayer = false
                                        activeLibraryDetail = null
                                        activeSettingsPage = null
                                        searchFocusRequests += 1
                                    }
                                    AppDestination.Library -> {
                                        selectedDestination = AppDestination.Library
                                        showPlayer = false
                                        activeLibraryDetail = null
                                        activeSettingsPage = null
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
                                        activeSettingsPage = null
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
            val isSearchDestination = selectedDestination == AppDestination.Search
            val recentTracks = remember(visibleTracks, recentTrackIds) {
                recentTrackIds.mapNotNull { trackId -> visibleTracks.firstOrNull { it.id == trackId } }
            }
            val displayedTracks = remember(visibleTracks, searchQuery, isSearchDestination) {
                if (isSearchDestination) visibleTracks.filterBy(searchQuery) else visibleTracks
            }
            val displayedAlbumGroups = remember(displayedTracks) { displayedTracks.groupByAlbum() }
            val displayedArtistGroups = remember(displayedTracks) { displayedTracks.groupByArtist() }
            val searchAlbumGroups = remember(visibleTracks, displayedTracks, searchQuery, isSearchDestination) {
                if (!isSearchDestination) {
                    displayedAlbumGroups
                } else {
                    visibleTracks
                        .groupByAlbum()
                        .filterGroupsBy(searchQuery)
                        .ifEmpty { displayedTracks.groupByAlbum() }
                }
            }
            val searchArtistGroups = remember(visibleTracks, displayedTracks, searchQuery, isSearchDestination) {
                if (!isSearchDestination) {
                    displayedArtistGroups
                } else {
                    visibleTracks
                        .groupByArtist()
                        .filterGroupsBy(searchQuery)
                        .ifEmpty { displayedTracks.groupByArtist() }
                }
            }
            val playlistGroups = remember(localPlaylists, visibleTracks, librarySearchQuery, librarySortMode) {
                localPlaylists
                    .toLibraryGroups(visibleTracks)
                    .let { groups ->
                        if (librarySearchQuery.isBlank()) groups else groups.filterGroupsBy(librarySearchQuery)
                    }
                    .sortedGroupsForLibrary(librarySortMode)
            }
            val searchPlaylistGroups = remember(localPlaylists, visibleTracks, searchQuery, isSearchDestination) {
                val groups = localPlaylists.toLibraryGroups(visibleTracks)
                if (!isSearchDestination || searchQuery.isBlank()) {
                    groups
                } else {
                    groups.filterGroupsBy(searchQuery)
                }
            }
            val libraryFilterBaseTracks = remember(
                visibleTracks,
                activeLibraryFilters,
                likedTrackIds,
                offlineDownloads,
                recentTrackIds
            ) {
                visibleTracks
                    .filterForLibrary(
                        filters = activeLibraryFilters,
                        likedTrackIds = likedTrackIds,
                        downloadedTrackIds = offlineDownloads.keys,
                        recentTrackIds = recentTrackIds
                    )
            }
            val librarySearchBaseTracks = remember(visibleTracks, libraryFilterBaseTracks, librarySearchQuery) {
                if (librarySearchQuery.isBlank()) libraryFilterBaseTracks else visibleTracks
            }
            val libraryStackedTracks = remember(librarySearchBaseTracks, librarySearchQuery) {
                librarySearchBaseTracks.filterBy(librarySearchQuery)
            }
            val librarySearchAlbumGroups = remember(librarySearchBaseTracks, libraryStackedTracks, librarySearchQuery) {
                if (librarySearchQuery.isBlank()) {
                    libraryStackedTracks.groupByAlbum()
                } else {
                    librarySearchBaseTracks
                        .groupByAlbum()
                        .filterGroupsBy(librarySearchQuery)
                        .ifEmpty { libraryStackedTracks.groupByAlbum() }
                }
            }
            val librarySearchArtistGroups = remember(librarySearchBaseTracks, libraryStackedTracks, librarySearchQuery) {
                if (librarySearchQuery.isBlank()) {
                    libraryStackedTracks.groupByArtist()
                } else {
                    librarySearchBaseTracks
                        .groupByArtist()
                        .filterGroupsBy(librarySearchQuery)
                        .ifEmpty { libraryStackedTracks.groupByArtist() }
                }
            }
            val libraryTracks = remember(libraryStackedTracks, librarySortMode) {
                libraryStackedTracks
                    .sortedForLibrary(librarySortMode)
            }
            val libraryAlbumGroups = remember(librarySearchAlbumGroups, librarySortMode) {
                librarySearchAlbumGroups
                    .sortedGroupsForLibrary(librarySortMode)
            }
            val libraryArtistGroups = remember(librarySearchArtistGroups, librarySortMode) {
                librarySearchArtistGroups
                    .sortedGroupsForLibrary(librarySortMode)
            }
            val searchResultCounts = remember(displayedTracks, searchAlbumGroups, searchArtistGroups, searchPlaylistGroups) {
                SearchResultCounts(
                    songs = displayedTracks.size,
                    albums = searchAlbumGroups.size,
                    artists = searchArtistGroups.size,
                    playlists = searchPlaylistGroups.size
                )
            }
            val showLibraryLetterRail = connectedSession != null &&
                selectedDestination == AppDestination.Library &&
                activeLibraryDetail == null &&
                librarySearchQuery.isBlank()
            val libraryLetterRailLetters = remember(
                showLibraryLetterRail,
                selectedTab,
                libraryTracks,
                libraryAlbumGroups,
                libraryArtistGroups,
                playlistGroups
            ) {
                if (showLibraryLetterRail) {
                    availableLettersForTab(
                        selectedTab = selectedTab,
                        songs = libraryTracks,
                        albums = libraryAlbumGroups,
                        artists = libraryArtistGroups,
                        playlists = playlistGroups
                    )
                } else {
                    emptyList()
                }
            }
            val libraryLetterScrollTargets = remember(
                showLibraryLetterRail,
                selectedTab,
                libraryTracks,
                libraryAlbumGroups,
                libraryArtistGroups,
                playlistGroups,
                libraryGridView,
                pinnedLibraryItems
            ) {
                if (showLibraryLetterRail) {
                    libraryLetterScrollTargets(
                        selectedTab = selectedTab,
                        songs = libraryTracks,
                        albums = libraryAlbumGroups,
                        artists = libraryArtistGroups,
                        playlists = playlistGroups,
                        gridView = libraryGridView,
                        hasPinnedShelf = pinnedLibraryItems.isNotEmpty()
                    )
                } else {
                    emptyMap()
                }
            }
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .imePadding()
                    ) {
                    LazyColumn(
                        state = mainListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 0.dp,
                            end = if (libraryLetterRailLetters.isNotEmpty()) 38.dp else 16.dp,
                            bottom = 20.dp
                        ),
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
                            val settingsPage = activeSettingsPage
                            if (settingsPage == null) {
                                item {
                                    SettingsSummaryCard(
                                        session = connectedSession,
                                        syncedTrackCount = tracks.size,
                                        lastLibrarySyncAt = lastLibrarySyncAt,
                                        downloadedTrackCount = offlineDownloads.size,
                                        activeDownloadCount = downloadProgressById.size
                                    )
                                }
                                item {
                                    SettingsSectionHeader("Categories")
                                }
                                item {
                                    SettingsCategoryListCard(
                                        session = connectedSession,
                                        syncedTrackCount = tracks.size,
                                        lastLibrarySyncAt = lastLibrarySyncAt,
                                        downloadedTrackCount = offlineDownloads.size,
                                        downloadedBytes = offlineDownloadBytesOnDisk(context, connectedSession),
                                        activeDownloadCount = downloadProgressById.size,
                                        gaplessPrebufferEnabled = gaplessPrebufferEnabled,
                                        transcodedStreamingEnabled = transcodedStreamingEnabled,
                                        playbackReportingEnabled = playbackReportingEnabled,
                                        equalizerSettings = equalizerSettings,
                                        themeMode = themeMode,
                                        useAlbumArtColors = useAlbumArtColors,
                                        visualizerEnabled = visualizerEnabled,
                                        onPageSelected = { activeSettingsPage = it }
                                    )
                                }
                            } else {
                                when (settingsPage) {
                                    SettingsPage.Account -> item {
                                        AccountCard(
                                            session = connectedSession,
                                            onSignOut = ::signOut
                                        )
                                    }

                                    SettingsPage.Library -> item {
                                        LibrarySyncCard(
                                            isBusy = isBusy,
                                            syncedTrackCount = tracks.size,
                                            lastLibrarySyncAt = lastLibrarySyncAt,
                                            autoSyncOnLaunch = autoSyncOnLaunch,
                                            onAutoSyncOnLaunchChange = { enabled ->
                                                autoSyncOnLaunch = enabled
                                                saveAutoSyncOnLaunch(context, enabled)
                                            },
                                            onRefresh = { loadLibrary(connectedSession) },
                                            onClearLibraryCache = ::clearLibraryCache,
                                            onClearArtworkCache = ::clearArtworkCache
                                        )
                                    }

                                    SettingsPage.Offline -> item {
                                        OfflineDownloadsCard(
                                            downloadedTrackCount = offlineDownloads.size,
                                            downloadedBytes = offlineDownloadBytesOnDisk(context, connectedSession),
                                            activeDownloadCount = downloadProgressById.size,
                                            wifiOnly = offlineWifiOnly,
                                            storageLimitMb = offlineStorageLimitMb,
                                            downloadedOnlyMode = downloadedOnlyMode,
                                            onWifiOnlyChange = { enabled ->
                                                offlineWifiOnly = enabled
                                                saveOfflineWifiOnly(context, enabled)
                                            },
                                            onStorageLimitChange = { limitMb ->
                                                offlineStorageLimitMb = limitMb
                                                saveOfflineStorageLimitMb(context, limitMb)
                                            },
                                            onDownloadedOnlyChange = ::setDownloadedOnlyMode,
                                            onClearDownloads = ::clearOfflineDownloads
                                        )
                                    }

                                    SettingsPage.Playback -> item {
                                        PlaybackSettingsCard(
                                            gaplessPrebufferEnabled = gaplessPrebufferEnabled,
                                            transcodedStreamingEnabled = transcodedStreamingEnabled,
                                            playbackReportingEnabled = playbackReportingEnabled,
                                            onGaplessPrebufferChange = { enabled ->
                                                gaplessPrebufferEnabled = enabled
                                                saveGaplessPrebufferEnabled(context, enabled)
                                                if (!enabled) {
                                                    player.prepareNext(null, null)
                                                }
                                            },
                                            onTranscodedStreamingChange = { enabled ->
                                                transcodedStreamingEnabled = enabled
                                                saveTranscodedStreamingEnabled(context, enabled)
                                            },
                                            onPlaybackReportingChange = { enabled ->
                                                playbackReportingEnabled = enabled
                                                savePlaybackReportingEnabled(context, enabled)
                                            }
                                        )
                                    }

                                    SettingsPage.Audio -> item {
                                        EqualizerSettingsCard(
                                            settings = equalizerSettings,
                                            onSettingsChange = { settings ->
                                                equalizerSettings = settings
                                                player.setEqualizerSettings(settings)
                                            }
                                        )
                                    }

                                    SettingsPage.Appearance -> item {
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

                                    SettingsPage.About -> item {
                                        AboutCard()
                                    }
                                }
                            }
                        }

                        AppDestination.Player -> {
                            item {
                                EmptyPlayerCard()
                            }
                        }

                            AppDestination.Liked -> {
                                val likedTracks = visibleTracks.filter { it.id in likedTrackIds }
                                val favoriteAlbumGroups = visibleTracks
                                    .groupByAlbum()
                                    .filter { it.key in favoriteAlbumKeys }
                                val favoriteArtistGroups = (likedTracks + favoriteAlbumGroups.flatMap { it.tracks })
                                    .distinctBy { it.id }
                                    .groupByArtist()
                                    .sortedGroupsForLibrary(LibrarySortMode.Artist)
                                item {
                                    FavoritesHeader(
                                        selectedTab = favoritesTab,
                                        trackCount = likedTracks.size,
                                        albumCount = favoriteAlbumGroups.size,
                                        artistCount = favoriteArtistGroups.size,
                                        onTabSelected = { favoritesTab = it }
                                    )
                                }
                                when (favoritesTab) {
                                    FavoritesTab.Tracks -> {
                                        if (likedTracks.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = isBusy,
                                                    statusText = statusText.takeIf { tracks.isEmpty() },
                                                    emptyText = "No favorite tracks yet"
                                                )
                                            }
                                        } else {
                                            items(
                                                likedTracks.chunked(2),
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
                                                                playTrack(track, openPlayer = true, source = likedTracks)
                                                            },
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
                                    FavoritesTab.Albums -> {
                                        if (favoriteAlbumGroups.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = isBusy,
                                                    statusText = statusText.takeIf { tracks.isEmpty() },
                                                    emptyText = "No favorite albums yet"
                                                )
                                            }
                                        } else {
                                            items(
                                                favoriteAlbumGroups.chunked(2),
                                                key = { row -> row.joinToString(separator = "|") { it.key } }
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
                                                            isFavorite = true,
                                                            onToggleFavorite = { toggleFavoriteAlbum(group.key) },
                                                            onClick = {
                                                                openLibraryDetail(
                                                                    LibraryDetail(LibraryCollectionType.Album, group.key)
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
                                        }
                                    }
                                    FavoritesTab.Artists -> {
                                        if (favoriteArtistGroups.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = isBusy,
                                                    statusText = statusText.takeIf { tracks.isEmpty() },
                                                    emptyText = "No favorite artists yet"
                                                )
                                            }
                                        } else {
                                            items(
                                                favoriteArtistGroups.chunked(2),
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
                                        }
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
                                    val detailTracks = visibleTracks.tracksForDetail(detail, offlineDownloads.keys, localPlaylists)
                                    val detailTitle = detail.titleLabel(localPlaylists)
                                    val detailSubtitle = detail.subtitleLabel(detailTracks, localPlaylists)
                                    val detailPin = PinnedLibraryItem(detail.type, detail.key)
                                    item {
                                        LibraryDetailHeader(
                                            title = detailTitle,
                                            subtitle = detailSubtitle,
                                            tracks = detailTracks,
                                            session = connectedSession,
                                            artworkShape = if (detail.type == LibraryCollectionType.Artist) CircleShape else RoundedCornerShape(8.dp),
                                            isPinned = pinnedLibraryItems.any { it == detailPin },
                                            isFavorite = detail.type == LibraryCollectionType.Album && detail.key in favoriteAlbumKeys,
                                            onPlay = {
                                                detailTracks.firstOrNull()?.let {
                                                    playTrack(it, openPlayer = true, source = detailTracks)
                                                }
                                            },
                                            onShuffle = { playRandom(detailTracks) },
                                            onTogglePin = { togglePinnedLibraryItem(detailPin) },
                                            onToggleFavorite = if (detail.type == LibraryCollectionType.Album) {
                                                { toggleFavoriteAlbum(detail.key) }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                    if (detail.type == LibraryCollectionType.Playlist) {
                                        val playlist = localPlaylists.firstOrNull { it.id == detail.key }
                                        if (playlist != null) {
                                            item {
                                                PlaylistDetailControls(
                                                    playlist = playlist,
                                                    onToggleFavorite = { togglePlaylistFavorite(playlist.id) },
                                                    onDelete = { deletePlaylist(playlist.id) }
                                                )
                                            }
                                        }
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
                                                onStartRadio = { startRadioFrom(detailTracks, track) },
                                                playlists = localPlaylists,
                                                onAddToPlaylist = { playlist -> addTrackToPlaylist(playlist, track) },
                                                onRemoveFromPlaylist = if (detail.type == LibraryCollectionType.Playlist) {
                                                    { removeTrackFromPlaylist(detail.key, track) }
                                                } else {
                                                    null
                                                }
                                            )
                                        }
                                    }
                                } else {
                                val emptyLibraryText = if (librarySearchQuery.isNotBlank()) {
                                    "No library results for \"${librarySearchQuery.trim()}\""
                                } else {
                                    "Your music will appear here"
                                }
                                val isLibrarySearching = librarySearchQuery.isNotBlank()
                                    val hasLibrarySearchResults = libraryStackedTracks.isNotEmpty() ||
                                        libraryAlbumGroups.isNotEmpty() ||
                                        libraryArtistGroups.isNotEmpty() ||
                                        playlistGroups.isNotEmpty()
                                    stickyHeader {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.background)
                                        ) {
                                            LibraryToolbar(
                                                selectedTab = selectedTab,
                                                activeFilters = activeLibraryFilters,
                                                sortMode = librarySortMode,
                                                isBusy = isBusy,
                                                onSearchClick = {
                                                    searchQuery = librarySearchQuery
                                                    librarySearchQuery = ""
                                                    selectedDestination = AppDestination.Search
                                                    searchFocusRequests++
                                                },
                                                onCreateClick = {
                                                    selectedTab = LibraryTab.Playlists
                                                    libraryLetterFilter = null
                                                },
                                                onTabSelected = {
                                                    selectedTab = it
                                                    libraryLetterFilter = null
                                                },
                                                onToggleFilter = { filter ->
                                                    activeLibraryFilters = if (filter in activeLibraryFilters) {
                                                        activeLibraryFilters - filter
                                                    } else {
                                                        activeLibraryFilters + filter
                                                    }
                                                    libraryLetterFilter = null
                                                },
                                                onSortModeChange = { librarySortMode = it }
                                            )
                                        }
                                    }
                                    if (!isLibrarySearching && pinnedLibraryItems.isNotEmpty()) {
                                        item {
                                            PinnedLibraryShelf(
                                            pinnedItems = pinnedLibraryItems,
                                            tracks = visibleTracks,
                                            downloadedTrackIds = offlineDownloads.keys,
                                            playlists = localPlaylists,
                                            session = connectedSession,
                                            onOpenDetail = ::openLibraryDetail,
                                            onUnpin = ::togglePinnedLibraryItem
                                        )
                                        }
                                    }
                                    if (isLibrarySearching && !hasLibrarySearchResults) {
                                    item {
                                        EmptyLibraryMessage(
                                            isBusy = isBusy,
                                            statusText = statusText.takeIf { tracks.isEmpty() },
                                            emptyText = emptyLibraryText
                                        )
                                    }
                                }
                                if (isLibrarySearching && libraryAlbumGroups.isNotEmpty()) {
                                    item {
                                        SearchSectionHeader(
                                            title = "Albums",
                                            count = libraryAlbumGroups.size
                                        )
                                    }
                                    items(
                                        libraryAlbumGroups.take(8).chunked(2),
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
                                                    onClick = {
                                                        openLibraryDetail(
                                                            LibraryDetail(LibraryCollectionType.Album, group.key)
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
                                }
                                if (isLibrarySearching && libraryArtistGroups.isNotEmpty()) {
                                    item {
                                        SearchSectionHeader(
                                            title = "Artists",
                                            count = libraryArtistGroups.size
                                        )
                                    }
                                    items(
                                        libraryArtistGroups.take(8).chunked(2),
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
                                }
                                if (isLibrarySearching && playlistGroups.isNotEmpty()) {
                                    item {
                                        SearchSectionHeader(
                                            title = "Playlists",
                                            count = playlistGroups.size
                                        )
                                    }
                                    items(
                                        playlistGroups.take(8).chunked(2),
                                        key = { row -> row.joinToString(separator = "|") { it.key } }
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
                                                    onClick = {
                                                        openLibraryDetail(
                                                            LibraryDetail(LibraryCollectionType.Playlist, group.key)
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
                                }
                                if (isLibrarySearching && libraryStackedTracks.isNotEmpty()) {
                                    item {
                                        SearchSectionHeader(
                                            title = "Songs",
                                            count = libraryStackedTracks.size
                                        )
                                    }
                                    items(libraryStackedTracks.take(40), key = { it.id }) { track ->
                                        TrackRow(
                                            track = track,
                                            session = connectedSession,
                                            isCurrent = player.currentTrack?.id == track.id,
                                            isLiked = track.id in likedTrackIds,
                                            onToggleLiked = { toggleLiked(track) },
                                            onClick = { playTrack(track, openPlayer = true, source = libraryStackedTracks) },
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
                                            onStartRadio = { startRadioFrom(libraryStackedTracks, track) },
                                            playlists = localPlaylists,
                                            onAddToPlaylist = { playlist -> addTrackToPlaylist(playlist, track) }
                                        )
                                    }
                                }
                                if (!isLibrarySearching) {
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
                                                        playlists = localPlaylists,
                                                        onAddToPlaylist = { playlist -> addTrackToPlaylist(playlist, track) },
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
                                                                it == PinnedLibraryItem(LibraryCollectionType.Album, group.key)
                                                            },
                                                            isFavorite = group.key in favoriteAlbumKeys,
                                                            onTogglePin = {
                                                                togglePinnedLibraryItem(
                                                                    PinnedLibraryItem(LibraryCollectionType.Album, group.key)
                                                                )
                                                            },
                                                            onToggleFavorite = { toggleFavoriteAlbum(group.key) },
                                                            onClick = {
                                                                openLibraryDetail(
                                                                    LibraryDetail(LibraryCollectionType.Album, group.key)
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
                                                        it == PinnedLibraryItem(LibraryCollectionType.Album, group.key)
                                                    },
                                                    isFavorite = group.key in favoriteAlbumKeys,
                                                    onTogglePin = {
                                                        togglePinnedLibraryItem(
                                                            PinnedLibraryItem(LibraryCollectionType.Album, group.key)
                                                        )
                                                    },
                                                    onToggleFavorite = { toggleFavoriteAlbum(group.key) },
                                                    onClick = {
                                                        openLibraryDetail(
                                                            LibraryDetail(LibraryCollectionType.Album, group.key)
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

                                    LibraryTab.Playlists -> {
                                        item {
                                            PlaylistCreateCard(onCreate = ::createPlaylist)
                                        }
                                        if (playlistGroups.isEmpty()) {
                                            item {
                                                EmptyLibraryMessage(
                                                    isBusy = false,
                                                    statusText = null,
                                                    emptyText = if (localPlaylists.isEmpty()) {
                                                        "Create a playlist or save your queue"
                                                    } else {
                                                        emptyLibraryText
                                                    }
                                                )
                                            }
                                        } else if (libraryGridView) {
                                            items(
                                                playlistGroups.chunked(2),
                                                key = { row -> row.joinToString(separator = "|") { it.key } }
                                            ) { rowGroups ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    rowGroups.forEach { group ->
                                                        val pin = PinnedLibraryItem(LibraryCollectionType.Playlist, group.key)
                                                        LibraryGroupGridCard(
                                                            group = group,
                                                            session = connectedSession,
                                                            artworkShape = RoundedCornerShape(8.dp),
                                                            isPinned = pinnedLibraryItems.any { it == pin },
                                                            onTogglePin = { togglePinnedLibraryItem(pin) },
                                                            onClick = {
                                                                openLibraryDetail(
                                                                    LibraryDetail(LibraryCollectionType.Playlist, group.key)
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
                                            items(playlistGroups, key = { it.key }) { group ->
                                                val pin = PinnedLibraryItem(LibraryCollectionType.Playlist, group.key)
                                                LibraryGroupListRow(
                                                    group = group,
                                                    session = connectedSession,
                                                    artworkShape = RoundedCornerShape(8.dp),
                                                    isPinned = pinnedLibraryItems.any { it == pin },
                                                    onTogglePin = { togglePinnedLibraryItem(pin) },
                                                    onClick = {
                                                        openLibraryDetail(
                                                            LibraryDetail(LibraryCollectionType.Playlist, group.key)
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                }
                                }
                            } else {
                                val queryIsBlank = searchQuery.isBlank()
                                    val hasSearchResults = displayedTracks.isNotEmpty() ||
                                        searchAlbumGroups.isNotEmpty() ||
                                        searchArtistGroups.isNotEmpty() ||
                                        searchPlaylistGroups.isNotEmpty()
                                    val topSearchResult = topSearchResult(
                                        query = searchQuery,
                                        tracks = displayedTracks,
                                        albums = searchAlbumGroups,
                                        artists = searchArtistGroups,
                                        playlists = searchPlaylistGroups
                                    )

                                    item {
                                        SearchHeader(
                                        searchQuery = searchQuery,
                                        resultCounts = searchResultCounts,
                                        isBusy = isBusy,
                                        statusText = statusText,
                                        focusRequester = searchFocusRequester,
                                        onSearchQueryChange = { searchQuery = it },
                                        onRefresh = { loadLibrary(connectedSession) }
                                    )
                                }

                                if (queryIsBlank) {
                                    if (recentTracks.isNotEmpty()) {
                                        item {
                                            HomeTrackShelf(
                                                title = "Recently played",
                                                tracks = recentTracks.take(10),
                                                session = connectedSession,
                                                onTrackClick = { track ->
                                                    playTrack(track, openPlayer = true, source = recentTracks)
                                                }
                                            )
                                        }
                                    }
                                    if (displayedAlbumGroups.isNotEmpty()) {
                                        item {
                                            SearchSectionHeader(
                                                title = "Browse albums",
                                                count = displayedAlbumGroups.size
                                            )
                                        }
                                        items(
                                            displayedAlbumGroups.take(12).chunked(2),
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
                                    }
                                    if (searchPlaylistGroups.isNotEmpty()) {
                                        item {
                                            SearchSectionHeader(
                                                title = "Playlists",
                                                count = searchPlaylistGroups.size
                                            )
                                        }
                                        items(
                                            searchPlaylistGroups.take(10).chunked(2),
                                            key = { row -> row.joinToString(separator = "|") { it.key } }
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
                                                        onClick = {
                                                            openLibraryDetail(
                                                                LibraryDetail(LibraryCollectionType.Playlist, group.key)
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
                                    }
                                } else if (!hasSearchResults) {
                                    item {
                                        EmptyLibraryMessage(
                                            isBusy = isBusy,
                                            statusText = statusText,
                                            emptyText = emptySearchText
                                        )
                                        }
                                    } else {
                                        topSearchResult?.let { result ->
                                            item {
                                                SearchTopResultCard(
                                                    result = result,
                                                    session = connectedSession,
                                                    onClick = {
                                                        result.track?.let { topTrack ->
                                                            playTrack(topTrack, openPlayer = true, source = displayedTracks)
                                                        } ?: result.detail?.let(::openLibraryDetail)
                                                    }
                                                )
                                            }
                                        }
                                        if (searchAlbumGroups.isNotEmpty()) {
                                            item {
                                                SearchSectionHeader(
                                                title = "Albums",
                                                count = searchAlbumGroups.size
                                            )
                                        }
                                        items(
                                            searchAlbumGroups.take(12).chunked(2),
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
                                    }

                                    if (searchArtistGroups.isNotEmpty()) {
                                        item {
                                            SearchSectionHeader(
                                                title = "Artists",
                                                count = searchArtistGroups.size
                                            )
                                        }
                                        items(
                                            searchArtistGroups.take(10).chunked(2),
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
                                    }

                                    if (searchPlaylistGroups.isNotEmpty()) {
                                        item {
                                            SearchSectionHeader(
                                                title = "Playlists",
                                                count = searchPlaylistGroups.size
                                            )
                                        }
                                        items(
                                            searchPlaylistGroups.take(10).chunked(2),
                                            key = { row -> row.joinToString(separator = "|") { it.key } }
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
                                                        onClick = {
                                                            openLibraryDetail(
                                                                LibraryDetail(LibraryCollectionType.Playlist, group.key)
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
                                    }

                                    if (displayedTracks.isNotEmpty()) {
                                        item {
                                            SearchSectionHeader(
                                                title = "Songs",
                                                count = displayedTracks.size
                                            )
                                        }
                                        items(displayedTracks.take(30), key = { it.id }) { track ->
                                            TrackRow(
                                                track = track,
                                                session = connectedSession,
                                                isCurrent = player.currentTrack?.id == track.id,
                                                isLiked = track.id in likedTrackIds,
                                                onToggleLiked = { toggleLiked(track) },
                                                onClick = {
                                                    playTrack(track, openPlayer = true, source = displayedTracks)
                                                },
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
                                                onStartRadio = { startRadioFrom(displayedTracks, track) },
                                                playlists = localPlaylists,
                                                onAddToPlaylist = { playlist -> addTrackToPlaylist(playlist, track) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
                if (libraryLetterRailLetters.isNotEmpty()) {
                    LibraryAlphabetSideRail(
                        letters = libraryLetterRailLetters,
                        selectedLetter = libraryLetterFilter,
                        onLetterSelected = { letter ->
                            if (libraryLetterFilter != letter) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            libraryLetterFilter = letter
                            libraryLetterScrollTargets[letter]?.let { targetIndex ->
                                libraryRailScrollJob.value?.cancel()
                                libraryRailScrollJob.value = coroutineScope.launch {
                                    mainListState.scrollToItem(targetIndex)
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                                .padding(top = 88.dp, end = 3.dp, bottom = 112.dp)
                        )
                    }
                }
            }
            if (showPlayer && activeTrack != null) {
                val fullPlayerQueue = playQueue.ifEmpty { visibleTracks.queueStartingAt(activeTrack) }
                FullPlayerScreen(
                    track = activeTrack,
                    isPlaying = player.isPlaying,
                    progress = player.progress,
                    visualizerLevels = player.visualizerLevels,
                    status = player.status,
                    queue = fullPlayerQueue,
                    session = connectedSession,
                    modifier = Modifier
                        .matchParentSize()
                        .offset { IntOffset(0, fullPlayerOffsetPx.roundToInt()) }
                        .swipeDownToDismiss(
                            onDismiss = closePlayer,
                            startZone = 96.dp,
                            dismissDistance = 118.dp,
                            onDragStart = {
                                isPlayerDismissDragging = true
                                isPlayerOpenDragging = false
                                playerOpenDragOffsetPx = 0f
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
                        ),
                    onClose = closePlayer,
                    onToggle = { player.toggle() },
                    onSeek = { player.seekToFraction(it) },
                    onPrevious = { playAdjacent(-1) },
                    onNext = { playAdjacent(1) },
                    onReplay = { connectedSession?.let { player.play(activeTrack, it) } },
                    shuffleEnabled = shuffleEnabled,
                    repeatEnabled = repeatEnabled,
                    visualizerEnabled = visualizerEnabled,
                    isFavorite = activeTrack.id in likedTrackIds,
                    isDownloaded = activeTrack.id in offlineDownloads,
                    downloadProgress = downloadProgressById[activeTrack.id],
                    playlists = localPlaylists,
                    onToggleShuffle = { shuffleEnabled = !shuffleEnabled },
                    onToggleRepeat = { repeatEnabled = !repeatEnabled },
                    onToggleFavorite = { toggleLiked(activeTrack) },
                    onToggleDownload = { toggleOfflineDownload(activeTrack) },
                    onGoToAlbum = {
                        openLibraryDetail(LibraryDetail(LibraryCollectionType.Album, activeTrack.album))
                    },
                    onGoToArtist = {
                        openLibraryDetail(LibraryDetail(LibraryCollectionType.Artist, activeTrack.artist))
                    },
                    onStartRadio = { startRadioFrom(fullPlayerQueue, activeTrack) },
                    onAddToPlaylist = { playlist -> addTrackToPlaylist(playlist, activeTrack) },
                    onQueueTrackClick = ::playQueuedTrack,
                    onQueueMove = ::moveQueueTrack,
                    onQueuePlayNext = ::playTrackNext,
                    onQueueRemove = ::removeQueueTrack,
                    onQueueClear = ::clearQueueAfterCurrent,
                    onQueueShuffle = ::reshuffleQueueAfterCurrent,
                    onQueueSaveAsPlaylist = { saveQueueAsPlaylist(fullPlayerQueue) }
                )
            }
}
}
}
}

@Composable
private fun SettingsCategoryListCard(
    session: JellyfinSession,
    syncedTrackCount: Int,
    lastLibrarySyncAt: Long?,
    downloadedTrackCount: Int,
    downloadedBytes: Long,
    activeDownloadCount: Int,
    gaplessPrebufferEnabled: Boolean,
    transcodedStreamingEnabled: Boolean,
    playbackReportingEnabled: Boolean,
    equalizerSettings: EqualizerSettings,
    themeMode: AppThemeMode,
    useAlbumArtColors: Boolean,
    visualizerEnabled: Boolean,
    onPageSelected: (SettingsPage) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            SettingsPage.entries.forEachIndexed { index, page ->
                SettingsCategoryRow(
                    page = page,
                    detail = settingsCategoryDetail(
                        page = page,
                        session = session,
                        syncedTrackCount = syncedTrackCount,
                        lastLibrarySyncAt = lastLibrarySyncAt,
                        downloadedTrackCount = downloadedTrackCount,
                        downloadedBytes = downloadedBytes,
                        activeDownloadCount = activeDownloadCount,
                        gaplessPrebufferEnabled = gaplessPrebufferEnabled,
                        transcodedStreamingEnabled = transcodedStreamingEnabled,
                        playbackReportingEnabled = playbackReportingEnabled,
                        equalizerSettings = equalizerSettings,
                        themeMode = themeMode,
                        useAlbumArtColors = useAlbumArtColors,
                        visualizerEnabled = visualizerEnabled
                    ),
                    onClick = { onPageSelected(page) }
                )
                if (index != SettingsPage.entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCategoryRow(
    page: SettingsPage,
    detail: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = settingsCategoryIcon(page),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = page.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = page.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = PlayerIconVectors.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun settingsCategoryDetail(
    page: SettingsPage,
    session: JellyfinSession,
    syncedTrackCount: Int,
    lastLibrarySyncAt: Long?,
    downloadedTrackCount: Int,
    downloadedBytes: Long,
    activeDownloadCount: Int,
    gaplessPrebufferEnabled: Boolean,
    transcodedStreamingEnabled: Boolean,
    playbackReportingEnabled: Boolean,
    equalizerSettings: EqualizerSettings,
    themeMode: AppThemeMode,
    useAlbumArtColors: Boolean,
    visualizerEnabled: Boolean
): String =
    when (page) {
        SettingsPage.Account -> "${session.username} - ${session.serverUrl.toHostLabel()}"
        SettingsPage.Library -> "${syncedTrackCount.countLabel("song")} - ${formatLastLibrarySync(lastLibrarySyncAt)}"
        SettingsPage.Offline -> if (activeDownloadCount > 0) {
            "${downloadedTrackCount.countLabel("song")} - ${activeDownloadCount.countLabel("download")} running"
        } else {
            "${downloadedTrackCount.countLabel("song")} - ${formatDataSize(downloadedBytes)}"
        }
        SettingsPage.Playback -> "${if (gaplessPrebufferEnabled) "Pre-buffer on" else "Pre-buffer off"} - ${if (transcodedStreamingEnabled) "Transcoding on" else "Transcoding off"} - ${if (playbackReportingEnabled) "Reporting on" else "Reporting off"}"
        SettingsPage.Audio -> if (equalizerSettings.enabled) {
            "${equalizerSettings.presetName} - EQ on"
        } else {
            "Off - tap to shape playback"
        }
        SettingsPage.Appearance -> "${themeMode.label} - ${if (useAlbumArtColors) "Artwork colors" else "Static colors"} - ${if (visualizerEnabled) "Visualizer on" else "Visualizer off"}"
        SettingsPage.About -> "Version 0.1.0"
    }

private fun settingsCategoryIcon(page: SettingsPage): ImageVector =
    when (page) {
        SettingsPage.Account -> PlayerIconVectors.SettingsFilled
        SettingsPage.Library -> PlayerIconVectors.Library
        SettingsPage.Offline -> PlayerIconVectors.Download
        SettingsPage.Playback -> PlayerIconVectors.Pause
        SettingsPage.Audio -> PlayerIconVectors.Equalizer
        SettingsPage.Appearance -> PlayerIconVectors.HomeFilled
        SettingsPage.About -> PlayerIconVectors.MoreVertical
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
    syncedTrackCount: Int,
    downloadedTrackCount: Int,
    activeDownloadCount: Int
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsStatusPill(
                    label = "Library",
                    value = syncedTrackCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                SettingsStatusPill(
                    label = "Offline",
                    value = downloadedTrackCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                SettingsStatusPill(
                    label = "Active",
                    value = activeDownloadCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SettingsStatusPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
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
    autoSyncOnLaunch: Boolean,
    onAutoSyncOnLaunchChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onClearLibraryCache: () -> Unit,
    onClearArtworkCache: () -> Unit
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            SettingsSwitchRow(
                title = "Auto sync on launch",
                description = if (autoSyncOnLaunch) {
                    "Refreshes metadata when the app opens"
                } else {
                    "Uses cached metadata until you sync"
                },
                checked = autoSyncOnLaunch,
                onCheckedChange = onAutoSyncOnLaunchChange
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onClearLibraryCache,
                        enabled = syncedTrackCount > 0 || lastLibrarySyncAt != null,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Clear library")
                    }
                    TextButton(
                        onClick = onClearArtworkCache,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Clear artwork")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = onRefresh,
                        enabled = !isBusy,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if (isBusy) "Syncing" else "Sync now")
                    }
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
    downloadedOnlyMode: Boolean,
    onWifiOnlyChange: (Boolean) -> Unit,
    onStorageLimitChange: (Int) -> Unit,
    onDownloadedOnlyChange: (Boolean) -> Unit,
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            SettingsSwitchRow(
                title = "Downloaded-only mode",
                description = if (downloadedOnlyMode) {
                    "Only shows songs saved on this device"
                } else {
                    "Shows your full Jellyfin library"
                },
                checked = downloadedOnlyMode,
                onCheckedChange = onDownloadedOnlyChange
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
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
private fun PlaybackSettingsCard(
    gaplessPrebufferEnabled: Boolean,
    transcodedStreamingEnabled: Boolean,
    playbackReportingEnabled: Boolean,
    onGaplessPrebufferChange: (Boolean) -> Unit,
    onTranscodedStreamingChange: (Boolean) -> Unit,
    onPlaybackReportingChange: (Boolean) -> Unit
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
                text = "Playback",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            SettingsSwitchRow(
                title = "Pre-buffer next song",
                description = if (gaplessPrebufferEnabled) "Next song is warmed in the background" else "Next song starts only after you request it",
                checked = gaplessPrebufferEnabled,
                onCheckedChange = onGaplessPrebufferChange
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            SettingsSwitchRow(
                title = "Transcoded streaming",
                description = if (transcodedStreamingEnabled) "Uses a smaller mobile-friendly stream" else "Streams original files when possible",
                checked = transcodedStreamingEnabled,
                onCheckedChange = onTranscodedStreamingChange
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            SettingsSwitchRow(
                title = "Playback reporting",
                description = if (playbackReportingEnabled) "Updates Jellyfin play activity" else "Listening activity is kept local",
                checked = playbackReportingEnabled,
                onCheckedChange = onPlaybackReportingChange
            )
        }
    }
}

@Composable
private fun EqualizerSettingsCard(
    settings: EqualizerSettings,
    onSettingsChange: (EqualizerSettings) -> Unit
) {
    val levels = settings.normalizedLevels()
    fun updateLevels(nextLevels: List<Float>, presetName: String = "Custom") {
        onSettingsChange(
            settings.copy(
                enabled = true,
                presetName = presetName,
                levelsDb = nextLevels
            )
        )
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        EqualizerMiniIcon(levels = levels, enabled = settings.enabled)
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Equalizer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (settings.enabled) "${settings.presetName} tone shaping is active" else "Enable to shape playback output",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = { enabled ->
                        onSettingsChange(settings.copy(enabled = enabled))
                    }
                )
            }

            EqualizerPreview(levels = levels, enabled = settings.enabled)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EqualizerPresets, key = { it.name }) { preset ->
                        FilterChip(
                            selected = settings.presetName == preset.name,
                            onClick = {
                                onSettingsChange(
                                    settings.copy(
                                        enabled = true,
                                        presetName = preset.name,
                                        levelsDb = preset.levelsDb
                                    )
                                )
                            },
                            label = { Text(preset.name) }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))

            EqualizerBandLabels.forEachIndexed { index, label ->
                val level = levels[index]
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$label Hz",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatEqualizerDb(level),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = level,
                        onValueChange = { value ->
                            updateLevels(
                                levels.toMutableList().also {
                                    it[index] = (value * 2f).roundToInt() / 2f
                                }
                            )
                        },
                        valueRange = EQUALIZER_MIN_DB..EQUALIZER_MAX_DB,
                        steps = 47,
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            thumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onSettingsChange(settings.copy(presetName = EqualizerFlatPreset.name, levelsDb = EqualizerFlatPreset.levelsDb)) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

@Composable
private fun EqualizerPreview(
    levels: List<Float>,
    enabled: Boolean
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (enabled) colorScheme.primaryContainer.copy(alpha = 0.76f) else colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(106.dp)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            val baseline = size.height * 0.5f
            val spacing = size.width / EQUALIZER_BAND_COUNT
            val maxBar = size.height * 0.44f
            levels.forEachIndexed { index, level ->
                val normalized = (level / EQUALIZER_MAX_DB).coerceIn(-1f, 1f)
                val x = spacing * index + spacing / 2f
                val y = baseline - normalized * maxBar
                drawLine(
                    color = colorScheme.onPrimaryContainer.copy(alpha = if (enabled) 0.22f else 0.12f),
                    start = Offset(x, baseline - maxBar),
                    end = Offset(x, baseline + maxBar),
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = if (enabled) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    start = Offset(x, baseline),
                    end = Offset(x, y),
                    strokeWidth = 10.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = if (enabled) colorScheme.onPrimaryContainer else colorScheme.onSurfaceVariant,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            drawLine(
                color = colorScheme.onPrimaryContainer.copy(alpha = if (enabled) 0.2f else 0.08f),
                start = Offset(0f, baseline),
                end = Offset(size.width, baseline),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
private fun EqualizerMiniIcon(
    levels: List<Float>,
    enabled: Boolean
) {
    val color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.58f)
    Canvas(modifier = Modifier.size(28.dp, 22.dp)) {
        val spacing = size.width / EQUALIZER_BAND_COUNT
        levels.forEachIndexed { index, level ->
            val normalized = ((level - EQUALIZER_MIN_DB) / (EQUALIZER_MAX_DB - EQUALIZER_MIN_DB)).coerceIn(0f, 1f)
            val height = size.height * (0.25f + normalized * 0.72f)
            val x = spacing * index + spacing / 2f
            drawLine(
                color = color,
                start = Offset(x, size.height),
                end = Offset(x, size.height - height),
                strokeWidth = 2.6.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

private fun formatEqualizerDb(level: Float): String {
    val rounded = (level * 2f).roundToInt() / 2f
    return when {
        rounded > 0f -> "+${rounded.formatOneDecimal()} dB"
        rounded < 0f -> "${rounded.formatOneDecimal()} dB"
        else -> "0 dB"
    }
}

private fun Float.formatOneDecimal(): String =
    if (this % 1f == 0f) roundToInt().toString() else String.format(Locale.US, "%.1f", this)

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
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
            val horizontalPadding = 4.dp
            val itemWidth = (maxWidth - horizontalPadding * 2f) / BottomTabDestinations.size.toFloat()
            val selectedIndex = BottomTabDestinations
                .indexOf(selectedDestination)
                .coerceAtLeast(0)
            val pillOffset by animateDpAsState(
                targetValue = horizontalPadding + itemWidth * selectedIndex.toFloat(),
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                label = "bottomTabPillOffset"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = pillOffset)
                    .width(itemWidth)
                    .height(48.dp)
                    .padding(horizontal = 5.dp, vertical = 3.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTabDestinations.forEach { destination ->
                    val selected = selectedDestination == destination
                    BottomTabItem(
                        destination = destination,
                        selected = selected,
                        badgeText = bottomTabBadge(
                            destination = destination,
                            syncInProgress = syncInProgress,
                            downloadQueueCount = downloadQueueCount,
                            offlineMode = offlineMode
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
    badgeText: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
        },
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "bottomTabContentColor"
    )
    val selectedScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "bottomTabScale"
    )

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(selectedScale)
                .size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = destinationIcon(destination, selected),
                contentDescription = destination.label,
                tint = contentColor,
                modifier = Modifier.size(if (selected) 24.dp else 22.dp)
            )
            if (badgeText != null) {
                BottomTabBadge(
                    text = badgeText,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
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
    offlineMode: Boolean
): String? =
    when (destination) {
        AppDestination.Library -> when {
            downloadQueueCount > 0 -> downloadQueueCount.coerceAtMost(99).toString()
            offlineMode -> "!"
            else -> null
        }
        AppDestination.Profile -> if (syncInProgress) "!" else null
        else -> null
    }

private fun destinationIcon(destination: AppDestination, selected: Boolean): ImageVector =
    when (destination) {
        AppDestination.Home -> if (selected) PlayerIconVectors.HomeFilled else PlayerIconVectors.Home
        AppDestination.Search -> PlayerIconVectors.Search
        AppDestination.Player -> Icons.Filled.PlayArrow
        AppDestination.Library -> if (selected) PlayerIconVectors.LibraryFilled else PlayerIconVectors.Library
        AppDestination.Liked -> if (selected) PlayerIconVectors.FavoriteFilled else PlayerIconVectors.FavoriteOutline
        AppDestination.Profile -> if (selected) PlayerIconVectors.SettingsFilled else PlayerIconVectors.Settings
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search your music",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onRefresh, enabled = !isBusy) {
                    Text(if (isBusy) "Syncing" else "Sync")
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
                placeholder = { Text("Song, album, artist") },
                supportingText = {
                    Text(
                        text = resultCounts.summary(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(22.dp)
            )
            if (statusText != null) {
                Text(
                    text = statusText,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp, end = 2.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun SearchTopResultCard(
    result: SearchTopResult,
    session: JellyfinSession?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArtworkImage(
                track = result.artworkTrack,
                session = session,
                modifier = Modifier
                    .size(76.dp)
                    .clip(result.artworkShape),
                imageSize = 180,
                imageQuality = 76
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Top result",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${result.type.label} - ${result.subtitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = if (result.type == SearchTopResultType.Song) {
                    PlayerGlyph.Play.imageVector()
                } else {
                    PlayerGlyph.Next.imageVector()
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun FavoritesHeader(
    selectedTab: FavoritesTab,
    trackCount: Int,
    albumCount: Int,
    artistCount: Int,
    onTabSelected: (FavoritesTab) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Favorites",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FavoritesTab.entries) { tab ->
                LibraryToolbarPill(
                    label = tab.label,
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
        Text(
            text = when (selectedTab) {
                FavoritesTab.Tracks -> trackCount.countLabel("track")
                FavoritesTab.Albums -> albumCount.countLabel("album")
                FavoritesTab.Artists -> artistCount.countLabel("artist")
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
private fun PlaylistCreateCard(
    onCreate: (String, String, List<MusicTrack>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var folder by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "New playlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Name") }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = folder,
                    onValueChange = { folder = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Folder") }
                )
                FilledTonalButton(
                    onClick = {
                        onCreate(name, folder, emptyList())
                        name = ""
                        folder = ""
                    },
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Create")
                }
            }
        }
    }
}

@Composable
private fun PlaylistDetailControls(
    playlist: LocalPlaylist,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (playlist.isFavorite) "Favorite playlist" else "Playlist",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = playlist.folder.takeIf { it.isNotBlank() } ?: playlist.trackIds.size.countLabel("song"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (playlist.isFavorite) Icons.Filled.Favorite else PlayerIconVectors.FavoriteOutline,
                    contentDescription = if (playlist.isFavorite) "Unfavorite playlist" else "Favorite playlist",
                    tint = if (playlist.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = onDelete,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Delete")
            }
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
    playlists: List<LocalPlaylist>,
    session: JellyfinSession?,
    onOpenDetail: (LibraryDetail) -> Unit,
    onUnpin: (PinnedLibraryItem) -> Unit
) {
    val resolvedItems = remember(pinnedItems, tracks, downloadedTrackIds, playlists) {
        pinnedItems.mapNotNull { it.resolvePinnedItem(tracks, downloadedTrackIds, playlists) }
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
private fun LibraryAlphabetSideRail(
    letters: List<Char>,
    selectedLetter: Char?,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    if (letters.isEmpty()) return
    Box(
        modifier = modifier
            .width(30.dp)
            .pointerInput(letters) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var lastLetter: Char? = null

                    fun letterAt(y: Float): Char? {
                        if (letters.isEmpty() || size.height <= 0) return null
                        val index = ((y / size.height.toFloat()) * letters.size)
                            .toInt()
                            .coerceIn(0, letters.lastIndex)
                        return letters[index]
                    }

                    fun dispatch(y: Float) {
                        val letter = letterAt(y) ?: return
                        if (letter != lastLetter) {
                            lastLetter = letter
                            onLetterSelected(letter)
                        }
                    }

                    dispatch(down.position.y)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        dispatch(change.position.y)
                        change.consume()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            letters.forEach { letter ->
                val selected = selectedLetter == letter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onLetterSelected(letter) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.88f)
                                } else {
                                    Color.Transparent
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.88f)
                            },
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
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
    isFavorite: Boolean = false,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: (() -> Unit)? = null
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
            if (onToggleFavorite != null) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else PlayerIconVectors.FavoriteOutline,
                        contentDescription = if (isFavorite) "Unfavorite album" else "Favorite album",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryToolbar(
    selectedTab: LibraryTab,
    activeFilters: Set<LibraryFilter>,
    sortMode: LibrarySortMode,
    isBusy: Boolean,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onTabSelected: (LibraryTab) -> Unit,
    onToggleFilter: (LibraryFilter) -> Unit,
    onSortModeChange: (LibrarySortMode) -> Unit
) {
    var sortExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PlayerIconVectors.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Text(
                text = "Your Library",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search library",
                    modifier = Modifier.size(30.dp)
                )
            }
            IconButton(onClick = onCreateClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Create playlist",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(LibraryTab.entries) { tab ->
                LibraryToolbarPill(
                    label = tab.label,
                    selected = selectedTab == tab,
                    onClick = { onTabSelected(tab) }
                )
            }
            items(LibraryFilter.entries) { filter ->
                LibraryToolbarPill(
                    label = filter.label,
                    selected = filter in activeFilters,
                    onClick = { onToggleFilter(filter) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                TextButton(onClick = { sortExpanded = true }) {
                    Icon(
                        imageVector = PlayerIconVectors.MoveUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = sortMode.label,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Grid",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = PlayerIconVectors.Library,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        if (isBusy) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun LibraryToolbarPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
    onStartRadio: (() -> Unit)? = null,
    playlists: List<LocalPlaylist> = emptyList(),
    onAddToPlaylist: ((LocalPlaylist) -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null
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
            onStartRadio = onStartRadio,
            playlists = playlists,
            onAddToPlaylist = onAddToPlaylist,
            onRemoveFromPlaylist = onRemoveFromPlaylist
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
    onTogglePin: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
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
        if (onToggleFavorite != null) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else PlayerIconVectors.FavoriteOutline,
                    contentDescription = if (isFavorite) "Unfavorite album" else "Favorite album",
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                    },
                    modifier = Modifier.size(21.dp)
                )
            }
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
    onTogglePin: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
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
        if (onTogglePin != null || onToggleFavorite != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (onToggleFavorite != null) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else PlayerIconVectors.FavoriteOutline,
                            contentDescription = if (isFavorite) "Unfavorite album" else "Favorite album",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                if (onTogglePin != null) {
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
    onStartRadio: (() -> Unit)?,
    playlists: List<LocalPlaylist> = emptyList(),
    onAddToPlaylist: ((LocalPlaylist) -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onShowFileDetails: (() -> Unit)? = null
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
        if (onShowFileDetails != null) {
            DropdownMenuItem(
                text = { Text("File details") },
                onClick = {
                    onDismiss()
                    onShowFileDetails()
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
        if (onAddToPlaylist != null && playlists.isNotEmpty()) {
            playlists.sortedWith(LocalPlaylistSort).take(8).forEach { playlist ->
                DropdownMenuItem(
                    text = { Text("Add to ${playlist.name}") },
                    onClick = {
                        onDismiss()
                        onAddToPlaylist(playlist)
                    }
                )
            }
        }
        if (onRemoveFromPlaylist != null) {
            DropdownMenuItem(
                text = { Text("Remove from playlist") },
                onClick = {
                    onDismiss()
                    onRemoveFromPlaylist()
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
    onStartRadio: (() -> Unit)? = null,
    playlists: List<LocalPlaylist> = emptyList(),
    onAddToPlaylist: ((LocalPlaylist) -> Unit)? = null,
    onRemoveFromPlaylist: (() -> Unit)? = null
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
            onStartRadio = onStartRadio,
            playlists = playlists,
            onAddToPlaylist = onAddToPlaylist,
            onRemoveFromPlaylist = onRemoveFromPlaylist
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
            AlbumArtPlaceholder(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun AlbumArtPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(0.56f),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.78f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = PlayerIconVectors.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    modifier = Modifier.fillMaxSize(0.54f)
                )
            }
        }
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
    onClose: () -> Unit,
    onToggle: () -> Unit,
    onSeek: (Float) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    shuffleEnabled: Boolean,
    repeatEnabled: Boolean,
    visualizerEnabled: Boolean,
    isFavorite: Boolean,
    isDownloaded: Boolean,
    downloadProgress: OfflineDownloadProgress?,
    playlists: List<LocalPlaylist>,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleDownload: () -> Unit,
    onGoToAlbum: () -> Unit,
    onGoToArtist: () -> Unit,
    onStartRadio: () -> Unit,
    onAddToPlaylist: (LocalPlaylist) -> Unit,
    onQueueTrackClick: (MusicTrack) -> Unit,
    onQueueMove: (Int, Int) -> Unit,
    onQueuePlayNext: (MusicTrack) -> Unit,
    onQueueRemove: (Int) -> Unit,
    onQueueClear: () -> Unit,
    onQueueShuffle: () -> Unit,
    onQueueSaveAsPlaylist: () -> Unit
) {
    var showQueue by remember { mutableStateOf(false) }
    var actionsExpanded by remember { mutableStateOf(false) }
    var showFileDetails by remember { mutableStateOf(false) }
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            val tabletLayout = maxWidth >= 840.dp
            if (tabletLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(30.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        DiscAlbumStage(
                            track = track,
                            isPlaying = isPlaying,
                            progress = progress,
                            session = session,
                            onSeek = onSeek,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(0.92f)
                            .fillMaxHeight()
                            .padding(bottom = 96.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PlayerTitleActionsRow(
                            track = track,
                            isFavorite = isFavorite,
                            onToggleFavorite = onToggleFavorite,
                            onOptionsClick = {
                                showQueue = false
                                actionsExpanded = true
                            }
                        )
                        Spacer(Modifier.height(22.dp))
                        PlayerProgressSection(
                            track = track,
                            progress = progress,
                            visualizerEnabled = visualizerEnabled,
                            visualizerLevels = visualizerLevels,
                            isPlaying = isPlaying,
                            onSeek = onSeek
                        )
                        Spacer(Modifier.height(26.dp))
                        PlayerTransportControls(
                            status = status,
                            isPlaying = isPlaying,
                            shuffleEnabled = shuffleEnabled,
                            repeatEnabled = repeatEnabled,
                            onToggleShuffle = onToggleShuffle,
                            onPrevious = onPrevious,
                            onToggle = onToggle,
                            onReplay = onReplay,
                            onNext = onNext,
                            onToggleRepeat = onToggleRepeat
                        )
                    }
                }
            } else {
                val mobileTopInset = if (maxHeight >= 840.dp) 54.dp else 34.dp
                val mobileControlGap = if (maxHeight >= 840.dp) 42.dp else 22.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = mobileTopInset),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DiscAlbumStage(
                        track = track,
                        isPlaying = isPlaying,
                        progress = progress,
                        session = session,
                        onSeek = onSeek
                    )
                    Spacer(Modifier.height(12.dp))
                    PlayerTitleActionsRow(
                        track = track,
                        isFavorite = isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        onOptionsClick = {
                            showQueue = false
                            actionsExpanded = true
                        }
                    )
                    Spacer(Modifier.height(2.dp))
                    PlayerProgressSection(
                        track = track,
                        progress = progress,
                        visualizerEnabled = visualizerEnabled,
                        visualizerLevels = visualizerLevels,
                        isPlaying = isPlaying,
                        onSeek = onSeek
                    )
                    Spacer(Modifier.height(mobileControlGap))
                    PlayerTransportControls(
                        status = status,
                        isPlaying = isPlaying,
                        shuffleEnabled = shuffleEnabled,
                        repeatEnabled = repeatEnabled,
                        onToggleShuffle = onToggleShuffle,
                        onPrevious = onPrevious,
                        onToggle = onToggle,
                        onReplay = onReplay,
                        onNext = onNext,
                        onToggleRepeat = onToggleRepeat,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.height(72.dp))
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 8.dp)
                .size(48.dp)
                .background(colorScheme.surface.copy(alpha = 0.68f), CircleShape)
        ) {
            Icon(
                imageVector = PlayerIconVectors.ChevronDown,
                contentDescription = "Close player",
                tint = colorScheme.onSurface
            )
        }

        if (!showQueue) {
            QueuePullHandle(
                queueCount = (displayQueue.size - 1).coerceAtLeast(0),
                onOpen = { showQueue = true },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 12.dp, top = 4.dp, end = 12.dp, bottom = 0.dp)
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
                onRemove = onQueueRemove,
                onClear = onQueueClear,
                onShuffle = onQueueShuffle,
                onSaveAsPlaylist = onQueueSaveAsPlaylist,
                modifier = Modifier.fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = actionsExpanded,
            enter = fadeIn(animationSpec = tween(durationMillis = 140)),
            exit = fadeOut(animationSpec = tween(durationMillis = 180)),
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f))
                    .clickable(onClick = { actionsExpanded = false })
            )
        }

        AnimatedVisibility(
            visible = actionsExpanded,
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
            PlayerOptionsBottomSheet(
                track = track,
                session = session,
                isFavorite = isFavorite,
                isDownloaded = isDownloaded,
                downloadProgress = downloadProgress,
                playlists = playlists,
                onDismiss = { actionsExpanded = false },
                onToggleFavorite = onToggleFavorite,
                onToggleDownload = onToggleDownload,
                onGoToAlbum = onGoToAlbum,
                onGoToArtist = onGoToArtist,
                onStartRadio = onStartRadio,
                onAddToPlaylist = onAddToPlaylist,
                onShowFileDetails = { showFileDetails = true },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showFileDetails) {
            TrackFileDetailsDialog(
                track = track,
                isDownloaded = isDownloaded,
                downloadProgress = downloadProgress,
                onDismiss = { showFileDetails = false }
            )
        }
    }
}

@Composable
private fun PlayerTitleActionsRow(
    track: MusicTrack,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${track.artist} - ${track.album}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        IconButton(
            onClick = onOptionsClick,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = PlayerGlyph.More.imageVector(),
                contentDescription = "Now playing options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PlayerProgressSection(
    track: MusicTrack,
    progress: Float,
    visualizerEnabled: Boolean,
    visualizerLevels: FloatArray,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (visualizerEnabled) {
            Spacer(Modifier.height(16.dp))
            AudioBarsVisualizer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                color = MaterialTheme.colorScheme.primary,
                active = isPlaying,
                levels = visualizerLevels
            )
            Spacer(Modifier.height(4.dp))
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
    }
}

@Composable
private fun PlayerTransportControls(
    status: String,
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatEnabled: Boolean,
    onToggleShuffle: () -> Unit,
    onPrevious: () -> Unit,
    onToggle: () -> Unit,
    onReplay: () -> Unit,
    onNext: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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

@Composable
private fun PlayerOptionsBottomSheet(
    track: MusicTrack,
    session: JellyfinSession?,
    isFavorite: Boolean,
    isDownloaded: Boolean,
    downloadProgress: OfflineDownloadProgress?,
    playlists: List<LocalPlaylist>,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleDownload: () -> Unit,
    onGoToAlbum: () -> Unit,
    onGoToArtist: () -> Unit,
    onStartRadio: () -> Unit,
    onAddToPlaylist: (LocalPlaylist) -> Unit,
    onShowFileDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedPlaylists = remember(playlists) { playlists.sortedWith(LocalPlaylistSort).take(8) }
    val downloadLabel = when {
        downloadProgress != null -> "Downloading ${downloadProgress.percentLabel()}"
        isDownloaded -> "Saved on this device"
        else -> "Save for offline listening"
    }

    Surface(
        modifier = modifier
            .fillMaxHeight(0.74f)
            .navigationBarsPadding()
            .swipeDownToDismiss(
                onDismiss = onDismiss,
                startZone = 128.dp,
                dismissDistance = 72.dp
            ),
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
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
                    .width(46.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlbumTile(
                        track = track,
                        session = session,
                        modifier = Modifier.size(56.dp)
                    )
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
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
                item {
                    PlayerOptionSheetRow(
                        icon = PlayerIconVectors.Library,
                        title = "Go to album",
                        subtitle = track.album,
                        onClick = {
                            onDismiss()
                            onGoToAlbum()
                        }
                    )
                }
                item {
                    PlayerOptionSheetRow(
                        icon = PlayerIconVectors.MusicNote,
                        title = "Go to artist",
                        subtitle = track.artist,
                        onClick = {
                            onDismiss()
                            onGoToArtist()
                        }
                    )
                }
                item {
                    PlayerOptionSheetRow(
                        icon = PlayerGlyph.Shuffle.imageVector(),
                        title = "Start radio",
                        subtitle = "Make a queue from this song",
                        onClick = {
                            onDismiss()
                            onStartRadio()
                        }
                    )
                }
                item {
                    PlayerOptionSheetRow(
                        icon = if (isDownloaded) PlayerIconVectors.DownloadDone else PlayerIconVectors.Download,
                        title = if (isDownloaded) "Remove download" else "Download",
                        subtitle = downloadLabel,
                        selected = isDownloaded,
                        onClick = {
                            onDismiss()
                            onToggleDownload()
                        }
                    )
                }
                item {
                    PlayerOptionSheetRow(
                        icon = if (isFavorite) Icons.Filled.Favorite else PlayerIconVectors.FavoriteOutline,
                        title = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        subtitle = if (isFavorite) "Currently in Favorites" else "Save this track",
                        selected = isFavorite,
                        onClick = {
                            onDismiss()
                            onToggleFavorite()
                        }
                    )
                }
                item {
                    PlayerOptionSheetRow(
                        icon = PlayerGlyph.More.imageVector(),
                        title = "File details",
                        subtitle = "Jellyfin item and playback info",
                        onClick = {
                            onDismiss()
                            onShowFileDetails()
                        }
                    )
                }
                if (sortedPlaylists.isNotEmpty()) {
                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                        )
                    }
                    item {
                        Text(
                            text = "Add to playlist",
                            modifier = Modifier.padding(start = 6.dp, top = 2.dp, end = 6.dp, bottom = 2.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(sortedPlaylists, key = { it.id }) { playlist ->
                        PlayerOptionSheetRow(
                            icon = PlayerGlyph.Queue.imageVector(),
                            title = playlist.name,
                            subtitle = listOfNotNull(
                                playlist.folder.takeIf { it.isNotBlank() },
                                playlist.trackIds.size.countLabel("song")
                            ).joinToString(" - ").ifBlank { "Playlist" },
                            onClick = {
                                onDismiss()
                                onAddToPlaylist(playlist)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerOptionSheetRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.76f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackFileDetailsDialog(
    track: MusicTrack,
    isDownloaded: Boolean,
    downloadProgress: OfflineDownloadProgress?,
    onDismiss: () -> Unit
) {
    val downloadStatus = when {
        downloadProgress != null -> "Downloading ${downloadProgress.percentLabel()}"
        isDownloaded -> "Downloaded"
        else -> "Streaming from server"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("File details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TrackFileDetailRow(label = "Title", value = track.title)
                TrackFileDetailRow(label = "Artist", value = track.artist)
                TrackFileDetailRow(label = "Album", value = track.album)
                TrackFileDetailRow(label = "Duration", value = formatDuration(track.durationMs))
                TrackFileDetailRow(label = "Status", value = downloadStatus)
                TrackFileDetailRow(label = "Jellyfin ID", value = track.id)
                TrackFileDetailRow(label = "Artwork item", value = track.imageItemId ?: "None")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun TrackFileDetailRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
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
            .height(66.dp)
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
    onRemove: (Int) -> Unit,
    onClear: () -> Unit,
    onShuffle: () -> Unit,
    onSaveAsPlaylist: () -> Unit,
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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = false,
                        onClick = onShuffle,
                        enabled = queue.size > 2,
                        label = { Text("Reshuffle") }
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = onSaveAsPlaylist,
                        enabled = queue.isNotEmpty(),
                        label = { Text("Save playlist") }
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = onClear,
                        enabled = queue.size > 1,
                        label = { Text("Clear upcoming") }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(queue, key = { index, track -> "$index:${track.id}" }) { index, item ->
                    QueueTrackRow(
                        track = item,
                        session = session,
                        index = index,
                        lastIndex = queue.lastIndex,
                        isCurrent = item.id == currentTrack.id,
                        onClick = { onTrackClick(item) },
                        onMoveUp = { onMove(index, index - 1) },
                        onMoveDown = { onMove(index, index + 1) },
                        onPlayNext = { onPlayNext(item) },
                        onRemove = { onRemove(index) }
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
    onPlayNext: () -> Unit,
    onRemove: () -> Unit
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
                    DropdownMenuItem(
                        text = { Text("Remove from queue") },
                        enabled = !isCurrent,
                        onClick = {
                            menuExpanded = false
                            onRemove()
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

    val ChevronDown: ImageVector = ImageVector.Builder(
        name = "ChevronDown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 2.15f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(6f, 9f)
            lineTo(12f, 15f)
            lineTo(18f, 9f)
        }
    }.build()

    val ChevronRight: ImageVector = ImageVector.Builder(
        name = "ChevronRight",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 2.15f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(9f, 6f)
            lineTo(15f, 12f)
            lineTo(9f, 18f)
        }
    }.build()

    val Home: ImageVector = ImageVector.Builder(
        name = "Home",
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
            moveTo(4f, 11f)
            lineTo(12f, 4.5f)
            lineTo(20f, 11f)
            moveTo(6.3f, 10.6f)
            verticalLineTo(19.2f)
            curveTo(6.3f, 19.64f, 6.66f, 20f, 7.1f, 20f)
            horizontalLineTo(10f)
            verticalLineTo(15f)
            horizontalLineTo(14f)
            verticalLineTo(20f)
            horizontalLineTo(16.9f)
            curveTo(17.34f, 20f, 17.7f, 19.64f, 17.7f, 19.2f)
            verticalLineTo(10.6f)
        }
    }.build()

    val HomeFilled: ImageVector = ImageVector.Builder(
        name = "HomeFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(3.3f, 10.45f)
            lineTo(11.35f, 3.9f)
            curveTo(11.73f, 3.59f, 12.27f, 3.59f, 12.65f, 3.9f)
            lineTo(20.7f, 10.45f)
            curveTo(21.14f, 10.81f, 21.2f, 11.46f, 20.84f, 11.9f)
            curveTo(20.5f, 12.31f, 19.91f, 12.39f, 19.47f, 12.1f)
            verticalLineTo(19.1f)
            curveTo(19.47f, 20.15f, 18.62f, 21f, 17.57f, 21f)
            horizontalLineTo(14.2f)
            curveTo(13.76f, 21f, 13.4f, 20.64f, 13.4f, 20.2f)
            verticalLineTo(15.4f)
            horizontalLineTo(10.6f)
            verticalLineTo(20.2f)
            curveTo(10.6f, 20.64f, 10.24f, 21f, 9.8f, 21f)
            horizontalLineTo(6.43f)
            curveTo(5.38f, 21f, 4.53f, 20.15f, 4.53f, 19.1f)
            verticalLineTo(12.1f)
            curveTo(4.09f, 12.39f, 3.5f, 12.31f, 3.16f, 11.9f)
            curveTo(2.8f, 11.46f, 2.86f, 10.81f, 3.3f, 10.45f)
            close()
        }
    }.build()

    val Search: ImageVector = ImageVector.Builder(
        name = "Search",
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
            moveTo(11f, 5f)
            curveTo(7.69f, 5f, 5f, 7.69f, 5f, 11f)
            curveTo(5f, 14.31f, 7.69f, 17f, 11f, 17f)
            curveTo(14.31f, 17f, 17f, 14.31f, 17f, 11f)
            curveTo(17f, 7.69f, 14.31f, 5f, 11f, 5f)
            close()
            moveTo(15.4f, 15.4f)
            lineTo(20f, 20f)
        }
    }.build()

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

    val FavoriteFilled: ImageVector = ImageVector.Builder(
        name = "FavoriteFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
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
            moveTo(8f, 3.5f)
            horizontalLineTo(16f)
            moveTo(10f, 3.5f)
            verticalLineTo(9.2f)
            curveTo(10f, 10.2f, 9.2f, 11f, 8.2f, 11f)
            horizontalLineTo(7f)
            verticalLineTo(14f)
            horizontalLineTo(17f)
            verticalLineTo(11f)
            horizontalLineTo(15.8f)
            curveTo(14.8f, 11f, 14f, 10.2f, 14f, 9.2f)
            verticalLineTo(3.5f)
            moveTo(12f, 14f)
            verticalLineTo(21f)
            moveTo(9.8f, 18.8f)
            lineTo(12f, 21f)
            lineTo(14.2f, 18.8f)
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
            moveTo(7f, 3f)
            curveTo(7f, 2.45f, 7.45f, 2f, 8f, 2f)
            horizontalLineTo(16f)
            curveTo(16.55f, 2f, 17f, 2.45f, 17f, 3f)
            curveTo(17f, 3.55f, 16.55f, 4f, 16f, 4f)
            horizontalLineTo(15f)
            verticalLineTo(9.15f)
            curveTo(15f, 9.62f, 15.38f, 10f, 15.85f, 10f)
            horizontalLineTo(17.5f)
            curveTo(18.05f, 10f, 18.5f, 10.45f, 18.5f, 11f)
            verticalLineTo(13.6f)
            curveTo(18.5f, 14.15f, 18.05f, 14.6f, 17.5f, 14.6f)
            horizontalLineTo(13f)
            verticalLineTo(21.2f)
            lineTo(12f, 22.2f)
            lineTo(11f, 21.2f)
            verticalLineTo(14.6f)
            horizontalLineTo(6.5f)
            curveTo(5.95f, 14.6f, 5.5f, 14.15f, 5.5f, 13.6f)
            verticalLineTo(11f)
            curveTo(5.5f, 10.45f, 5.95f, 10f, 6.5f, 10f)
            horizontalLineTo(8.15f)
            curveTo(8.62f, 10f, 9f, 9.62f, 9f, 9.15f)
            verticalLineTo(4f)
            horizontalLineTo(8f)
            curveTo(7.45f, 4f, 7f, 3.55f, 7f, 3f)
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
        path(
            fill = null,
            stroke = stroke,
            strokeLineWidth = 1.9f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4.5f, 5f)
            horizontalLineTo(7.6f)
            verticalLineTo(19f)
            horizontalLineTo(4.5f)
            close()
            moveTo(9.4f, 5f)
            horizontalLineTo(12.5f)
            verticalLineTo(19f)
            horizontalLineTo(9.4f)
            close()
            moveTo(14.3f, 6.1f)
            lineTo(17.4f, 5.25f)
            lineTo(20.6f, 18.1f)
            lineTo(17.5f, 18.95f)
            close()
            moveTo(5.35f, 16.2f)
            horizontalLineTo(6.75f)
            moveTo(10.25f, 16.2f)
            horizontalLineTo(11.65f)
            moveTo(17.2f, 16.1f)
            lineTo(18.6f, 15.72f)
        }
    }.build()

    val LibraryFilled: ImageVector = ImageVector.Builder(
        name = "LibraryFilled",
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

    private fun ImageVector.Builder.materialSettingsPath(filled: Boolean) {
        path(
            fill = if (filled) stroke else null,
            stroke = if (filled) null else stroke,
            strokeLineWidth = 1.9f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(19.43f, 12.98f)
            curveTo(19.47f, 12.66f, 19.5f, 12.33f, 19.5f, 12f)
            curveTo(19.5f, 11.67f, 19.48f, 11.34f, 19.43f, 11.02f)
            lineTo(21.54f, 9.37f)
            curveTo(21.73f, 9.22f, 21.78f, 8.95f, 21.66f, 8.73f)
            lineTo(19.66f, 5.27f)
            curveTo(19.54f, 5.05f, 19.28f, 4.96f, 19.06f, 5.05f)
            lineTo(16.57f, 6.05f)
            curveTo(16.05f, 5.65f, 15.49f, 5.32f, 14.88f, 5.07f)
            lineTo(14.5f, 2.42f)
            curveTo(14.46f, 2.18f, 14.25f, 2f, 14f, 2f)
            horizontalLineTo(10f)
            curveTo(9.75f, 2f, 9.54f, 2.18f, 9.5f, 2.42f)
            lineTo(9.12f, 5.07f)
            curveTo(8.51f, 5.32f, 7.95f, 5.66f, 7.43f, 6.05f)
            lineTo(4.94f, 5.05f)
            curveTo(4.71f, 4.97f, 4.46f, 5.05f, 4.34f, 5.27f)
            lineTo(2.34f, 8.73f)
            curveTo(2.21f, 8.95f, 2.27f, 9.22f, 2.46f, 9.37f)
            lineTo(4.57f, 11.02f)
            curveTo(4.53f, 11.34f, 4.5f, 11.67f, 4.5f, 12f)
            curveTo(4.5f, 12.33f, 4.52f, 12.66f, 4.57f, 12.98f)
            lineTo(2.46f, 14.63f)
            curveTo(2.27f, 14.78f, 2.22f, 15.05f, 2.34f, 15.27f)
            lineTo(4.34f, 18.73f)
            curveTo(4.46f, 18.95f, 4.72f, 19.04f, 4.94f, 18.95f)
            lineTo(7.43f, 17.95f)
            curveTo(7.95f, 18.35f, 8.51f, 18.68f, 9.12f, 18.93f)
            lineTo(9.5f, 21.58f)
            curveTo(9.54f, 21.82f, 9.75f, 22f, 10f, 22f)
            horizontalLineTo(14f)
            curveTo(14.25f, 22f, 14.46f, 21.82f, 14.5f, 21.58f)
            lineTo(14.88f, 18.93f)
            curveTo(15.49f, 18.68f, 16.05f, 18.34f, 16.57f, 17.95f)
            lineTo(19.06f, 18.95f)
            curveTo(19.29f, 19.03f, 19.54f, 18.95f, 19.66f, 18.73f)
            lineTo(21.66f, 15.27f)
            curveTo(21.78f, 15.05f, 21.73f, 14.78f, 21.54f, 14.63f)
            lineTo(19.43f, 12.98f)
            close()
            moveTo(12f, 15.5f)
            curveTo(10.07f, 15.5f, 8.5f, 13.93f, 8.5f, 12f)
            curveTo(8.5f, 10.07f, 10.07f, 8.5f, 12f, 8.5f)
            curveTo(13.93f, 8.5f, 15.5f, 10.07f, 15.5f, 12f)
            curveTo(15.5f, 13.93f, 13.93f, 15.5f, 12f, 15.5f)
            close()
        }
    }

    val Settings: ImageVector = ImageVector.Builder(
        name = "Settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        materialSettingsPath(filled = false)
    }.build()

    val SettingsFilled: ImageVector = ImageVector.Builder(
        name = "SettingsFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        materialSettingsPath(filled = true)
    }.build()

    val Equalizer: ImageVector = ImageVector.Builder(
        name = "Equalizer",
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
            moveTo(5f, 20f)
            verticalLineTo(11f)
            moveTo(12f, 20f)
            verticalLineTo(4f)
            moveTo(19f, 20f)
            verticalLineTo(8f)
            moveTo(3.5f, 11f)
            horizontalLineTo(6.5f)
            moveTo(10.5f, 14f)
            horizontalLineTo(13.5f)
            moveTo(17.5f, 8f)
            horizontalLineTo(20.5f)
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

    val MusicNote: ImageVector = ImageVector.Builder(
        name = "MusicNote",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = stroke) {
            moveTo(12f, 3f)
            verticalLineTo(14.55f)
            curveTo(11.41f, 14.21f, 10.73f, 14f, 10f, 14f)
            curveTo(7.79f, 14f, 6f, 15.34f, 6f, 17f)
            curveTo(6f, 18.66f, 7.79f, 20f, 10f, 20f)
            curveTo(12.21f, 20f, 14f, 18.66f, 14f, 17f)
            verticalLineTo(7f)
            horizontalLineTo(18f)
            verticalLineTo(3f)
            horizontalLineTo(12f)
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
    modifier: Modifier = Modifier
) {
    val latestProgress by rememberUpdatedState(progress)
    val latestOnSeek by rememberUpdatedState(onSeek)
    var isScratching by remember { mutableStateOf(false) }
    var holdScrubProgress by remember { mutableStateOf(false) }
    var scratchProgress by remember { mutableFloatStateOf(progress) }
    var discRotation by remember(track.id) { mutableFloatStateOf(0f) }
    val stageProgress = if (isScratching || holdScrubProgress) scratchProgress else progress

    LaunchedEffect(progress, isScratching, holdScrubProgress) {
        if (!isScratching && !holdScrubProgress) {
            scratchProgress = progress
        }
    }

    LaunchedEffect(holdScrubProgress, track.id) {
        if (holdScrubProgress) {
            delay(900)
            holdScrubProgress = false
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
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 6.dp)
            .pointerInput(track.id) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
                    val stageWidth = size.width.toFloat()
                    val stageHeight = size.height.toFloat()
                    val centerRadius = distanceFromCenter(down.position, stageWidth, stageHeight)
                    val outerRadius = minOf(stageWidth, stageHeight) * 0.43f
                    val deadZone = minOf(stageWidth, stageHeight) * DISC_SCRATCH_DEAD_ZONE
                    if (centerRadius < deadZone || centerRadius > outerRadius) {
                        return@awaitEachGesture
                    }

                    isScratching = true
                    holdScrubProgress = false
                    scratchProgress = latestProgress
                    var lastAngle = angleForOffset(
                        offset = down.position,
                        width = stageWidth,
                        height = stageHeight
                    )

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break

                        val radiusFromCenter = distanceFromCenter(
                            offset = change.position,
                            width = stageWidth,
                            height = stageHeight
                        )
                        if (radiusFromCenter >= deadZone * 0.82f && radiusFromCenter <= outerRadius * 1.08f) {
                            val angle = angleForOffset(
                                offset = change.position,
                                width = stageWidth,
                                height = stageHeight
                            )
                            val deltaAngle = shortestAngleDelta(lastAngle, angle)
                            lastAngle = angle

                            if (abs(deltaAngle) > 0.05f) {
                                discRotation = (discRotation + deltaAngle) % 360f
                                scratchProgress = (
                                    scratchProgress +
                                        (deltaAngle / 360f) * DISC_SCRATCH_SEEK_SCALE
                                    ).coerceIn(0f, 0.995f)
                            }
                        }
                        change.consume()
                    }

                    isScratching = false
                    holdScrubProgress = true
                    latestOnSeek(scratchProgress)
                }
            },
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
            rotationDegrees = discRotation,
            modifier = Modifier.fillMaxSize(0.86f)
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
    BoxWithConstraints(modifier = modifier) {
        val p = progress.coerceIn(0f, 1f)
        val swing = ((offRecord - 0.12f) / 0.88f).coerceIn(0f, 1f)
        val lift = (offRecord / 0.46f).coerceIn(0f, 1f)
        fun mix(start: Float, end: Float, amount: Float): Float = start + (end - start) * amount

        val onRecordStartAngle = 1.68f
        val onRecordEndAngle = 2.03f
        val offRecordAngle = 1.48f
        val armAngle = mix(
            mix(onRecordStartAngle, onRecordEndAngle, p),
            offRecordAngle,
            swing
        )
        val armRotationDegrees = armAngle * 180f / PI.toFloat() - 90f
        val pivotXFraction = 0.966f
        val pivotYFraction = 0.052f
        val armHeightFraction = 0.72f
        val stageSize = minOf(maxWidth, maxHeight)
        val pivotX = maxWidth * pivotXFraction
        val pivotY = maxHeight * pivotYFraction
        val armHeight = stageSize * armHeightFraction
        val armWidth = armHeight * (172f / 565f)
        val armPivotY = 0.125f
        val armPainter = painterResource(R.drawable.turntable_arm)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val accent = colorScheme.primary
            val pivot = Offset(size.width * pivotXFraction, size.height * pivotYFraction)
            val armHeightPx = size.minDimension * armHeightFraction
            val armWidthPx = armHeightPx * (172f / 565f)
            val armRotationRadians = armRotationDegrees * PI.toFloat() / 180f
            fun rotatedArmOffset(x: Float, y: Float): Offset {
                val cosRotation = cos(armRotationRadians)
                val sinRotation = sin(armRotationRadians)
                return Offset(
                    x = x * cosRotation - y * sinRotation,
                    y = x * sinRotation + y * cosRotation
                )
            }

            drawCircle(
                color = Color.Black.copy(alpha = 0.18f + lift * 0.08f),
                radius = 34.dp.toPx(),
                center = Offset(pivot.x + 4.dp.toPx(), pivot.y + 6.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        accent.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = Offset(pivot.x - 10.dp.toPx(), pivot.y - 10.dp.toPx()),
                    radius = 42.dp.toPx()
                ),
                radius = 42.dp.toPx(),
                center = pivot
            )

            val stylusShadowCenter = pivot +
                rotatedArmOffset(armWidthPx * 0.04f, armHeightPx * 0.84f) +
                Offset(2.dp.toPx() + lift * 8.dp.toPx(), 3.dp.toPx() + lift * 8.dp.toPx())
            val wideShadowPaint = AndroidPaint().apply {
                isAntiAlias = true
                color = AndroidColor.argb(
                    ((0.34f + lift * 0.16f) * 255f).roundToInt().coerceIn(0, 255),
                    0,
                    0,
                    0
                )
                maskFilter = BlurMaskFilter((9f + lift * 10f).dp.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
            val coreShadowPaint = AndroidPaint().apply {
                isAntiAlias = true
                color = AndroidColor.argb(
                    ((0.46f + lift * 0.18f) * 255f).roundToInt().coerceIn(0, 255),
                    0,
                    0,
                    0
                )
                maskFilter = BlurMaskFilter((4f + lift * 8f).dp.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                nativeCanvas.save()
                nativeCanvas.rotate(armRotationDegrees, stylusShadowCenter.x, stylusShadowCenter.y)
                nativeCanvas.drawOval(
                    RectF(
                        stylusShadowCenter.x - 34.dp.toPx(),
                        stylusShadowCenter.y - 18.dp.toPx(),
                        stylusShadowCenter.x + 44.dp.toPx(),
                        stylusShadowCenter.y + 24.dp.toPx()
                    ),
                    wideShadowPaint
                )
                nativeCanvas.drawOval(
                    RectF(
                        stylusShadowCenter.x - 20.dp.toPx(),
                        stylusShadowCenter.y - 10.dp.toPx(),
                        stylusShadowCenter.x + 28.dp.toPx(),
                        stylusShadowCenter.y + 15.dp.toPx()
                    ),
                    coreShadowPaint
                )
                nativeCanvas.restore()
            }
        }

        Image(
            painter = armPainter,
            contentDescription = null,
            modifier = Modifier
                .size(width = armWidth, height = armHeight)
                .offset(
                    x = pivotX - armWidth * 0.5f + 2.dp + (lift * 7f).dp,
                    y = pivotY - armHeight * armPivotY + 3.dp + (lift * 6f).dp
                )
                .graphicsLayer {
                    rotationZ = armRotationDegrees
                    transformOrigin = TransformOrigin(0.5f, armPivotY)
                    alpha = 0.3f + lift * 0.18f
                }
                .blur((4.5f + lift * 5.5f).dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.Black)
        )

        Image(
            painter = armPainter,
            contentDescription = null,
            modifier = Modifier
                .size(width = armWidth, height = armHeight)
                .offset(
                    x = pivotX - armWidth * 0.5f,
                    y = pivotY - armHeight * armPivotY
                )
                .graphicsLayer {
                    rotationZ = armRotationDegrees
                    transformOrigin = TransformOrigin(0.5f, armPivotY)
                    alpha = 0.98f
                },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun VinylDisc(
    track: MusicTrack,
    session: JellyfinSession?,
    rotationDegrees: Float,
    modifier: Modifier = Modifier
) {
    val tint = track.tint
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val discSize = minOf(maxWidth, maxHeight)
        val knobSize = discSize * 0.145f

        Box(
            modifier = Modifier
                .size(discSize)
                .graphicsLayer {
                    shape = CircleShape
                    clip = true
                }
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        rotationZ = rotationDegrees
                    },
                contentAlignment = Alignment.Center
            ) {
                AlbumArtworkImage(
                    track = track,
                    session = session,
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            shape = CircleShape
                            clip = true
                        }
                        .clip(CircleShape)
                        .alpha(0.92f),
                    imageSize = 384,
                    imageQuality = 78
                )
                Canvas(modifier = Modifier.matchParentSize()) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    drawCircle(
                        color = Color.Black.copy(alpha = 0.08f),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                tint.copy(alpha = 0.08f),
                                tint.copy(alpha = 0.03f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.18f)
                            ),
                            center = Offset(size.width * 0.34f, size.height * 0.28f),
                            radius = radius * 1.1f
                        ),
                        radius = radius,
                        center = center
                    )
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.2f),
                        radius = radius * 0.948f,
                        center = center,
                        style = Stroke(width = radius * 0.052f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.045f),
                                Color.Black.copy(alpha = 0.24f),
                                Color.Black.copy(alpha = 0.44f)
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
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
                        color = Color.Black.copy(alpha = 0.22f),
                        radius = radius * 0.99f,
                        center = center,
                        style = Stroke(width = radius * 0.058f)
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
                }
            }
            Canvas(modifier = Modifier.matchParentSize()) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                drawReferenceVinylShines(center = center, radius = radius)
                drawSoftVinylShade(
                    center = center,
                    radius = radius,
                    startAngle = 198f,
                    sweepAngle = 98f,
                    width = radius * 0.18f,
                    baseAlpha = 0.09f
                )
                val softSpotCenter = Offset(size.width * 0.36f, size.height * 0.26f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE8F5FF).copy(alpha = 0.04f),
                            Color.White.copy(alpha = 0.012f),
                            Color.Transparent
                        ),
                        center = softSpotCenter,
                        radius = radius * 0.34f
                    ),
                    radius = radius * 0.34f,
                    center = softSpotCenter
                )
            }
            Image(
                painter = painterResource(R.drawable.turntable_shine),
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .alpha(0.16f),
                contentScale = ContentScale.Crop
            )
            Image(
                painter = painterResource(R.drawable.turntable_knob),
                contentDescription = null,
                modifier = Modifier
                    .size(knobSize)
                    .alpha(0.98f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.colorMatrix(
                    ColorMatrix().apply {
                        setToSaturation(0f)
                    }
                )
            )
        }
    }
}

private fun DrawScope.drawReferenceVinylShines(center: Offset, radius: Float) {
    val coolWhite = Color(0xFFE8F6FF)
    val softBlue = Color(0xFFBFD8E8)
    val shineAlphaScale = 0.6f

    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.88f,
        startAngle = 178f,
        sweepAngle = 76f,
        strokeWidth = radius * 0.58f,
        color = coolWhite.copy(alpha = 0.07f * shineAlphaScale),
        blurRadius = radius * 0.12f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.93f,
        startAngle = 186f,
        sweepAngle = 42f,
        strokeWidth = radius * 0.3f,
        color = Color.White.copy(alpha = 0.09f * shineAlphaScale),
        blurRadius = radius * 0.065f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.76f,
        startAngle = 214f,
        sweepAngle = 92f,
        strokeWidth = radius * 0.52f,
        color = coolWhite.copy(alpha = 0.07f * shineAlphaScale),
        blurRadius = radius * 0.105f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.81f,
        startAngle = 230f,
        sweepAngle = 48f,
        strokeWidth = radius * 0.22f,
        color = Color.White.copy(alpha = 0.11f * shineAlphaScale),
        blurRadius = radius * 0.052f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.58f,
        startAngle = 238f,
        sweepAngle = 36f,
        strokeWidth = radius * 0.085f,
        color = Color.White.copy(alpha = 0.1f * shineAlphaScale),
        blurRadius = radius * 0.032f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.86f,
        startAngle = 18f,
        sweepAngle = 66f,
        strokeWidth = radius * 0.4f,
        color = softBlue.copy(alpha = 0.045f * shineAlphaScale),
        blurRadius = radius * 0.086f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.74f,
        startAngle = 34f,
        sweepAngle = 38f,
        strokeWidth = radius * 0.14f,
        color = Color.White.copy(alpha = 0.075f * shineAlphaScale),
        blurRadius = radius * 0.04f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.68f,
        startAngle = 292f,
        sweepAngle = 58f,
        strokeWidth = radius * 0.34f,
        color = coolWhite.copy(alpha = 0.038f * shineAlphaScale),
        blurRadius = radius * 0.088f
    )
    drawBlurredArcStroke(
        center = center,
        radius = radius * 0.95f,
        startAngle = 300f,
        sweepAngle = 34f,
        strokeWidth = radius * 0.18f,
        color = Color.White.copy(alpha = 0.036f * shineAlphaScale),
        blurRadius = radius * 0.055f
    )

    val fineBands = listOf(
        Triple(0.94f, 192f, 0.03f),
        Triple(0.88f, 206f, 0.026f),
        Triple(0.73f, 240f, 0.034f),
        Triple(0.64f, 255f, 0.028f),
        Triple(0.9f, 42f, 0.022f),
        Triple(0.78f, 318f, 0.019f)
    )
    fineBands.forEach { (radiusScale, angle, alpha) ->
        drawBlurredArcStroke(
            center = center,
            radius = radius * radiusScale,
            startAngle = angle,
            sweepAngle = 16f,
            strokeWidth = radius * 0.018f,
            color = Color.White.copy(alpha = alpha * shineAlphaScale),
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
            var targetMin = 1f
            var targetMax = 0f
            var targetTotal = 0f
            for (index in 0 until VISUALIZER_BAR_COUNT) {
                val value = target.getOrElse(index) { 0f }.coerceIn(0f, 1f)
                targetMin = minOf(targetMin, value)
                targetMax = maxOf(targetMax, value)
                targetTotal += value
            }
            val targetAverage = targetTotal / VISUALIZER_BAR_COUNT
            val targetRange = targetMax - targetMin
            val syntheticMix = when {
                !latestActive -> 0f
                targetMax < 0.06f -> 0.9f
                targetRange < 0.14f -> 0.78f
                targetAverage < 0.18f -> 0.62f
                else -> 0.46f
            }
            val seconds = frameTime / 1_000_000_000f
            val beat = ((sin(seconds * 8.6f) + 1f) * 0.5f).let { it * it * it }
            val sweep = (sin(seconds * 3.1f) + 1f) * 0.5f

            for (index in 0 until VISUALIZER_BAR_COUNT) {
                val baseGoal = target.getOrElse(index) { 0f }.coerceIn(0f, 1f)
                val normalizedIndex = index.toFloat() / (VISUALIZER_BAR_COUNT - 1).coerceAtLeast(1)
                val bassWeight = (1f - normalizedIndex).coerceIn(0f, 1f)
                val localWave = (sin(seconds * (4.4f + normalizedIndex * 5.2f) + index * 0.82f) + 1f) * 0.5f
                val ripple = (sin(index * 0.54f - seconds * 7.5f) + 1f) * 0.5f
                val syntheticGoal = (
                    0.05f +
                        localWave * 0.2f +
                        ripple * 0.14f +
                        beat * (0.42f * bassWeight + 0.18f) +
                        sweep * 0.18f * (1f - abs(normalizedIndex - 0.5f) * 2f)
                    ).coerceIn(0f, 0.95f)
                val goal = (baseGoal * (1f - syntheticMix) + syntheticGoal * syntheticMix).coerceIn(0f, 1f)
                val level = current.getOrElse(index) { 0f }
                val speed = if (goal > level) 18f else 8.8f
                val blend = (deltaSeconds * speed).coerceIn(0.1f, 0.56f)
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

    val hasLiveLevels = displayedLevels.any { it > 0.006f }

    Canvas(modifier = modifier) {
        val barCount = VISUALIZER_BAR_COUNT
        val gap = size.width / (barCount * 2.32f)
        val strokeWidth = gap.coerceAtLeast(2.6f)
        val step = size.width / barCount
        val baseline = size.height * 0.9f
        for (index in 0 until barCount) {
            val rawLevel = displayedLevels.getOrElse(index) { 0f }
            val previous = displayedLevels.getOrElse(index - 1) { rawLevel }
            val next = displayedLevels.getOrElse(index + 1) { rawLevel }
            val level = (rawLevel * 0.8f + previous * 0.1f + next * 0.1f).coerceIn(0f, 1f)
            val height = if (hasLiveLevels) {
                size.height * (0.1f + sqrt(level) * 0.86f)
            } else {
                3.dp.toPx()
            }
            val x = step * index + step / 2f
            if (hasLiveLevels) {
                drawLine(
                    color = color.copy(alpha = 0.09f + level * 0.12f),
                    start = Offset(x, baseline),
                    end = Offset(x, baseline - height),
                    strokeWidth = strokeWidth * 2.1f,
                    cap = StrokeCap.Round
                )
            }
            drawLine(
                color = color.copy(alpha = if (hasLiveLevels) 0.54f + level * 0.44f else 0.34f),
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
    private var mediaPlayer: ExoPlayer? = null
    private var activePlayerPrepared = false
    private var visualizer: Visualizer? = null
    private var equalizer: Equalizer? = null
    private var equalizerAudioSessionId = 0
    private var equalizerSettings = loadEqualizerSettings(context)
    private var currentSession: JellyfinSession? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var resumeOnAudioFocusGain = false
    private var visualizerEnabled = false
    private var smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
    private var visualizerAutoGain = 1.6f
    private var lastVisualizerCaptureAt = 0L
    private var lastPlaybackReportAt = 0L
    private var lastWidgetProgressUpdateAt = 0L
    private var visualizerPumpRunning = false
    private var playbackGeneration = 0
    private var pendingSeekTargetMs: Long? = null
    private var pendingSeekStartedAt = 0L
    private var queuedSeekTrackId: String? = null
    private var queuedSeekPositionMs: Long? = null
    private var streamStartOffsetMs = 0L
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
        startPlayback(
            track = track,
            session = session,
            transcoded = loadTranscodedStreamingEnabled(context),
            allowTranscodedFallback = true,
            bypassOfflineFile = false,
            reportStopped = true,
            startPositionMs = queuedSeekPositionFor(track)
        )
    }

    private fun startPlayback(
        track: MusicTrack,
        session: JellyfinSession,
        transcoded: Boolean,
        allowTranscodedFallback: Boolean,
        bypassOfflineFile: Boolean,
        reportStopped: Boolean,
        startPositionMs: Long?
    ) {
        if (reportStopped) {
            reportCurrentPlaybackStopped()
        }
        val generation = ++playbackGeneration
        if (queuedSeekTrackId != null && queuedSeekTrackId != track.id) {
            clearQueuedSeekPosition()
        }
        val requestedStartPositionMs = startPositionMs
            ?.takeIf { it > 0L && track.durationMs > 0L }
            ?.coerceIn(0L, track.durationMs)
        val streamStartsAtOffset = requestedStartPositionMs != null && !canSeekInPlace(track, session)
        val mediaSourceStartPositionMs = if (streamStartsAtOffset) 0L else (requestedStartPositionMs ?: 0L)
        releasePlayerForReplacement()
        streamStartOffsetMs = if (streamStartsAtOffset) requestedStartPositionMs ?: 0L else 0L
        currentSession = session
        currentTrack = track
        status = "Buffering"
        progress = requestedStartPositionMs
            ?.let { (it.toFloat() / track.durationMs.toFloat()).coerceIn(0f, 1f) }
            ?: 0f
        pendingSeekTargetMs = requestedStartPositionMs
        pendingSeekStartedAt = requestedStartPositionMs?.let { SystemClock.elapsedRealtime() } ?: 0L
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerAutoGain = 1.6f
        lastVisualizerCaptureAt = 0L
        lastPlaybackReportAt = 0L

        if (!requestAudioFocus()) {
            isPlaying = false
            status = "Audio focus unavailable"
            publishPlaybackState(track)
            return
        }

        val nextPlayer = createExoPlayer()
        mediaPlayer = nextPlayer
        activePlayerPrepared = false
        configureActivePlayer(
            player = nextPlayer,
            generation = generation,
            track = track,
            transcoded = transcoded,
            allowTranscodedFallback = allowTranscodedFallback
        )
        runCatching {
            nextPlayer.setMediaSource(
                buildMediaSource(session, track, transcoded, bypassOfflineFile, requestedStartPositionMs),
                mediaSourceStartPositionMs
            )
            nextPlayer.prepare()
            nextPlayer.playWhenReady = true
            publishPlaybackState(track)
        }.onFailure {
            if (nextPlayer.isActivePlayback(generation)) {
                val retried = retryWithTranscodedStream(
                    nextPlayer,
                    generation,
                    track,
                    transcoded,
                    allowTranscodedFallback
                )
                if (!retried) {
                    abandonAudioFocus()
                    isPlaying = false
                    mediaPlayer = null
                    activePlayerPrepared = false
                    status = it.readableMessage()
                    publishPlaybackState(track)
                    releaseExoPlayer(nextPlayer)
                }
            } else {
                releaseExoPlayer(nextPlayer)
            }
        }
    }

    fun prepareNext(track: MusicTrack?, session: JellyfinSession?) {
        // Media3 preloading will be added after the primary playback lifecycle is settled.
    }

    fun toggle() {
        val activePlayer = mediaPlayer
        if (activePlayer == null) {
            val track = currentTrack ?: return
            val session = currentSession ?: return
            startPlayback(
                track = track,
                session = session,
                transcoded = loadTranscodedStreamingEnabled(context),
                allowTranscodedFallback = true,
                bypassOfflineFile = false,
                reportStopped = false,
                startPositionMs = queuedSeekPositionFor(track)
                    ?: track.durationMs.takeIf { it > 0L }?.let { (it * progress.coerceIn(0f, 1f)).toLong() }
            )
            return
        }
        if (status == "Buffering") {
            if (!activePlayerPrepared) {
                val track = currentTrack ?: return
                val session = currentSession ?: return
                startPlayback(
                    track = track,
                    session = session,
                    transcoded = loadTranscodedStreamingEnabled(context),
                    allowTranscodedFallback = true,
                    bypassOfflineFile = false,
                    reportStopped = false,
                    startPositionMs = queuedSeekPositionFor(track)
                        ?: track.durationMs.takeIf { it > 0L }?.let { (it * progress.coerceIn(0f, 1f)).toLong() }
                )
            }
            return
        }
        if (isPlaying) {
            activePlayer.pause()
            abandonAudioFocus()
            visualizer?.runCatching { enabled = false }
            stopVisualizerPump()
            isPlaying = false
            status = "Paused"
            publishPlaybackState()
            reportCurrentPlaybackProgress(force = true, isPaused = true)
        } else {
            if (!requestAudioFocus()) {
                status = "Audio focus unavailable"
                publishPlaybackState()
                return
            }
            activePlayer.volume = 1f
            activePlayer.play()
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
            reportCurrentPlaybackProgress(force = true, isPaused = false)
        }
    }

    fun setVisualizerEnabled(enabled: Boolean) {
        visualizerEnabled = enabled
        if (!enabled) {
            stopVisualizerPump()
            releaseVisualizer()
            smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
            visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
            visualizerAutoGain = 1.6f
        } else if (isPlaying) {
            mediaPlayer?.audioSessionId?.let(::attachVisualizer)
            startVisualizerPump()
        }
    }

    fun setEqualizerSettings(settings: EqualizerSettings) {
        equalizerSettings = settings
        saveEqualizerSettings(context, settings)
        val audioSessionId = mediaPlayer?.audioSessionId ?: 0
        if (settings.enabled) {
            attachEqualizer(audioSessionId)
        } else {
            releaseEqualizer()
        }
    }

    fun syncProgress() {
        val activePlayer = mediaPlayer ?: return
        val activeTrack = currentTrack ?: return
        if (!activePlayerPrepared || status == "Buffering") return
        runCatching {
            val duration = activePlayer.playbackDurationMs(activeTrack) ?: return@runCatching
            val currentPosition = activePlayer.playbackPositionWithOffset().coerceIn(0L, duration)
            val pendingTarget = pendingSeekTargetMs
            if (pendingTarget != null) {
                val targetThreshold = (duration / 200L).coerceAtLeast(700L)
                val seekIsSettled = abs(currentPosition - pendingTarget) <= targetThreshold
                val seekTimedOut = SystemClock.elapsedRealtime() - pendingSeekStartedAt > USER_SEEK_PROGRESS_HOLD_MS
                if (!seekIsSettled && !seekTimedOut) {
                    progress = (pendingTarget.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                    notificationController.syncPlaybackState(isPlaying, status, progress)
                    publishWidgetProgress(activeTrack)
                    return@runCatching
                }
                pendingSeekTargetMs = null
            }
            progress = (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            notificationController.syncPlaybackState(isPlaying, status, progress)
            publishWidgetProgress(activeTrack)
            reportCurrentPlaybackProgress()
        }
    }

    fun seekToFraction(fraction: Float) {
        val track = currentTrack ?: return
        val activePlayer = mediaPlayer
        val duration = track.durationMs.takeIf { it > 0L }
            ?: activePlayer?.takeIf { activePlayerPrepared }?.playbackDurationMs(track)
            ?: return
        val target = positionForFraction(fraction, duration)
        if (activePlayer == null || status == "Buffering" || !activePlayerPrepared) {
            queueSeekPosition(track, target, duration)
            if (status != "Buffering" && status != "Ended") {
                status = "Paused"
            }
            publishPlaybackState(track)
            reportCurrentPlaybackProgress(force = true, isPaused = true)
            return
        }
        val session = currentSession
        if (session != null && !canSeekInPlace(track, session)) {
            val shouldResume = isPlaying
            queueSeekPosition(track, target, duration)
            if (shouldResume) {
                startPlayback(
                    track = track,
                    session = session,
                    transcoded = loadTranscodedStreamingEnabled(context),
                    allowTranscodedFallback = true,
                    bypassOfflineFile = false,
                    reportStopped = false,
                    startPositionMs = target
                )
            } else if (status != "Ended") {
                status = "Paused"
                publishPlaybackState(track)
                reportCurrentPlaybackProgress(force = true, isPaused = true)
            }
            return
        }
        runCatching {
            activePlayer.seekToPosition(target)
            pendingSeekTargetMs = target
            pendingSeekStartedAt = SystemClock.elapsedRealtime()
            queuedSeekTrackId = null
            queuedSeekPositionMs = null
            progress = (target.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            if (!isPlaying && status != "Ended") {
                status = "Paused"
            }
            publishPlaybackState()
            reportCurrentPlaybackProgress(force = true, isPaused = !isPlaying)
        }.onFailure {
            queueSeekPosition(track, target, duration)
            status = "Paused"
            publishPlaybackState(track)
            reportCurrentPlaybackProgress(force = true, isPaused = true)
        }
    }

    fun release() {
        reportCurrentPlaybackStopped()
        playbackGeneration++
        releasePlayer()
        currentTrack = null
        currentSession = null
        status = "Ready"
        progress = 0f
        pendingSeekTargetMs = null
        pendingSeekStartedAt = 0L
        clearQueuedSeekPosition()
        publishPlaybackState(null)
    }

    fun dispose() {
        release()
        notificationController.release()
    }

    private fun reportCurrentPlaybackStarted(track: MusicTrack) {
        val session = currentSession ?: return
        val positionMs = currentPlaybackPositionMs(track) ?: 0L
        lastPlaybackReportAt = SystemClock.elapsedRealtime()
        reportPlaybackStart(context, session, track, positionMs)
    }

    private fun reportCurrentPlaybackProgress(force: Boolean = false, isPaused: Boolean = !isPlaying) {
        val track = currentTrack ?: return
        val session = currentSession ?: return
        val now = SystemClock.elapsedRealtime()
        if (!force && now - lastPlaybackReportAt < 10_000L) return
        val positionMs = currentPlaybackPositionMs(track)
            ?: (track.durationMs * progress.coerceIn(0f, 1f)).toLong()
        lastPlaybackReportAt = now
        reportPlaybackProgress(context, session, track, positionMs, isPaused)
    }

    private fun reportCurrentPlaybackStopped() {
        val track = currentTrack ?: return
        val session = currentSession ?: return
        val positionMs = currentPlaybackPositionMs(track)
            ?: (track.durationMs * progress.coerceIn(0f, 1f)).toLong()
        lastPlaybackReportAt = 0L
        reportPlaybackStopped(context, session, track, positionMs)
    }

    private fun createExoPlayer(): ExoPlayer =
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setAudioAttributes(media3PlaybackAudioAttributes(), false)
                volume = 1f
            }

    private fun buildMediaSource(
        session: JellyfinSession,
        track: MusicTrack,
        transcoded: Boolean,
        bypassOfflineFile: Boolean,
        startPositionMs: Long?
    ): ProgressiveMediaSource {
        val offlineFile = offlinePlayableFileFor(context, session, track).takeUnless { bypassOfflineFile }
        val mediaUri = offlineFile
            ?.let { Uri.fromFile(it) }
            ?: Uri.parse(
                track.streamUrl(
                    session = session,
                    transcoded = transcoded,
                    startPositionMs = startPositionMs ?: 0L
                )
            )
        val dataSourceFactory = if (offlineFile != null) {
            DefaultDataSource.Factory(context)
        } else {
            val httpFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("JellyfinMusic/0.1.0")
                .setDefaultRequestProperties(session.streamHeaders())
            DefaultDataSource.Factory(context, httpFactory)
        }
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(mediaUri))
    }

    private fun configureActivePlayer(
        player: ExoPlayer,
        generation: Int,
        track: MusicTrack,
        transcoded: Boolean,
        allowTranscodedFallback: Boolean
    ) {
        player.addListener(
            object : Player.Listener {
                private var startReported = false

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (!player.isActivePlayback(generation)) return
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            status = "Buffering"
                            publishPlaybackState(track)
                        }

                        Player.STATE_READY -> {
                            activePlayerPrepared = true
                            clearQueuedSeekPosition(track)
                            if (player.playWhenReady || player.isPlaying) {
                                status = "Playing"
                            } else if (status != "Ended") {
                                status = "Paused"
                            }
                            if (visualizerEnabled && player.audioSessionId > 0) {
                                attachVisualizer(player.audioSessionId)
                                if (player.isPlaying) startVisualizerPump()
                            }
                            if (player.audioSessionId > 0) {
                                attachEqualizer(player.audioSessionId)
                            }
                            syncProgress()
                            publishPlaybackState(track)
                        }

                        Player.STATE_ENDED -> {
                            finishCurrentTrack(track)
                        }

                        Player.STATE_IDLE -> Unit
                    }
                }

                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    if (!player.isActivePlayback(generation)) return
                    isPlaying = isPlayingNow
                    if (isPlayingNow) {
                        status = "Playing"
                        if (visualizerEnabled && player.audioSessionId > 0) {
                            attachVisualizer(player.audioSessionId)
                            startVisualizerPump()
                        }
                        if (player.audioSessionId > 0) {
                            attachEqualizer(player.audioSessionId)
                        }
                        if (!startReported) {
                            reportCurrentPlaybackStarted(track)
                            startReported = true
                        } else {
                            reportCurrentPlaybackProgress(force = true, isPaused = false)
                        }
                    } else if (status != "Buffering" && status != "Ended") {
                        status = "Paused"
                    }
                    publishPlaybackState(track)
                }

                override fun onAudioSessionIdChanged(audioSessionId: Int) {
                    if (!player.isActivePlayback(generation)) return
                    if (visualizerEnabled && audioSessionId > 0) {
                        attachVisualizer(audioSessionId)
                        if (isPlaying) startVisualizerPump()
                    }
                    if (audioSessionId > 0) {
                        attachEqualizer(audioSessionId)
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    if (!player.isActivePlayback(generation)) return
                    if (retryWithTranscodedStream(player, generation, track, transcoded, allowTranscodedFallback)) {
                        return
                    }
                    abandonAudioFocus()
                    visualizer?.runCatching { enabled = false }
                    stopVisualizerPump()
                    releaseEqualizer()
                    isPlaying = false
                    if (mediaPlayer === player) {
                        mediaPlayer = null
                        activePlayerPrepared = false
                    }
                    status = error.readablePlaybackMessage()
                    pendingSeekTargetMs = null
                    pendingSeekStartedAt = 0L
                    smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                    visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                    visualizerAutoGain = 1.6f
                    publishPlaybackState(track)
                    releaseExoPlayer(player)
                }
            }
        )
    }

    private fun finishCurrentTrack(track: MusicTrack) {
        abandonAudioFocus()
        visualizer?.runCatching { enabled = false }
        stopVisualizerPump()
        isPlaying = false
        status = "Ended"
        progress = 1f
        pendingSeekTargetMs = null
        pendingSeekStartedAt = 0L
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerAutoGain = 1.6f
        publishPlaybackState(track)
        reportCurrentPlaybackStopped()
    }

    private fun PlaybackException.readablePlaybackMessage(): String =
        when (errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> {
                "Server unreachable. Check Jellyfin or your network."
            }
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                "Jellyfin rejected the stream. Retrying may refresh it."
            }
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                "Audio file missing or stream expired."
            }
            PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
            PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES -> {
                "Format unsupported on this device."
            }
            else -> {
                cause?.readableMessage()
                    ?: message?.takeIf { it.isNotBlank() }
                    ?: "Playback failed."
            }
        }

    private fun retryWithTranscodedStream(
        player: ExoPlayer,
        generation: Int,
        track: MusicTrack,
        transcoded: Boolean,
        allowTranscodedFallback: Boolean
    ): Boolean {
        val session = currentSession ?: return false
        if (transcoded || !allowTranscodedFallback || !player.isActivePlayback(generation)) return false
        pendingSeekTargetMs = null
        pendingSeekStartedAt = 0L
        val retryStartPositionMs = currentPlaybackPositionMs(track)
            ?: track.durationMs.takeIf { it > 0L }?.let { (it * progress.coerceIn(0f, 1f)).toLong() }
        startPlayback(
            track = track,
            session = session,
            transcoded = true,
            allowTranscodedFallback = false,
            bypassOfflineFile = true,
            reportStopped = false,
            startPositionMs = retryStartPositionMs
        )
        return true
    }

    private fun ExoPlayer.isActivePlayback(generation: Int): Boolean =
        generation == playbackGeneration && mediaPlayer === this

    private fun ExoPlayer.playbackDurationMs(track: MusicTrack?): Long? {
        val trackDuration = track?.durationMs ?: 0L
        if (trackDuration > 0L) return trackDuration
        return duration
            .takeIf { it != C.TIME_UNSET && it > 0L }
    }

    private fun ExoPlayer.seekToPosition(positionMs: Long) {
        seekTo((positionMs - streamStartOffsetMs).coerceAtLeast(0L))
    }

    private fun ExoPlayer.playbackPositionWithOffset(): Long =
        (streamStartOffsetMs + currentPosition.coerceAtLeast(0L)).coerceAtLeast(0L)

    private fun positionForFraction(fraction: Float, durationMs: Long): Long =
        (durationMs.toFloat() * fraction.coerceIn(0f, 1f))
            .roundToInt()
            .toLong()
            .coerceIn(0L, durationMs)

    private fun queueSeekPosition(track: MusicTrack, positionMs: Long, durationMs: Long) {
        queuedSeekTrackId = track.id
        queuedSeekPositionMs = positionMs.coerceIn(0L, durationMs)
        pendingSeekTargetMs = queuedSeekPositionMs
        pendingSeekStartedAt = SystemClock.elapsedRealtime()
        progress = (queuedSeekPositionMs!!.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    }

    private fun queuedSeekPositionFor(track: MusicTrack): Long? =
        queuedSeekPositionMs?.takeIf { queuedSeekTrackId == track.id }

    private fun clearQueuedSeekPosition(track: MusicTrack? = null) {
        if (track != null && queuedSeekTrackId != track.id) return
        queuedSeekTrackId = null
        queuedSeekPositionMs = null
    }

    private fun canSeekInPlace(track: MusicTrack, session: JellyfinSession): Boolean =
        offlinePlayableFileFor(context, session, track) != null

    private fun currentPlaybackPositionMs(track: MusicTrack): Long? {
        val pendingTarget = pendingSeekTargetMs
        if (pendingTarget != null &&
            SystemClock.elapsedRealtime() - pendingSeekStartedAt <= USER_SEEK_PROGRESS_HOLD_MS
        ) {
            return pendingTarget
        }
        val playerPositionMs = mediaPlayer
            ?.takeIf { activePlayerPrepared }
            ?.runCatching { currentPosition.toLong().coerceAtLeast(0L) }
            ?.getOrNull()
        return playerPositionMs
            ?.let { (streamStartOffsetMs + it).coerceAtLeast(0L) }
            ?: track.durationMs.takeIf { it > 0L }?.let { (it * progress.coerceIn(0f, 1f)).toLong() }
    }

    private fun publishPlaybackState(track: MusicTrack? = currentTrack) {
        lastWidgetProgressUpdateAt = SystemClock.elapsedRealtime()
        saveWidgetState(context, track, currentSession, status, progress)
        notificationController.update(
            track = track,
            session = currentSession,
            isPlaying = isPlaying,
            status = status,
            progress = progress
        )
    }

    private fun publishWidgetProgress(track: MusicTrack) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastWidgetProgressUpdateAt < WIDGET_PROGRESS_UPDATE_MS) return
        lastWidgetProgressUpdateAt = now
        saveWidgetState(context, track, currentSession, status, progress)
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

    private fun media3PlaybackAudioAttributes(): Media3AudioAttributes =
        Media3AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
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
                activePlayer.runCatching { volume = 1f }
                if (resumeOnAudioFocusGain && !isPlaying) {
                    activePlayer.runCatching {
                        play()
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
                        reportCurrentPlaybackProgress(force = true, isPaused = false)
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
                activePlayer.runCatching { volume = 0.25f }
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
            reportCurrentPlaybackProgress(force = true, isPaused = true)
        }
    }

    private fun releasePlayerForReplacement() {
        stopVisualizerPump()
        releaseVisualizer()
        releaseEqualizer()
        releaseExoPlayer(mediaPlayer)
        mediaPlayer = null
        activePlayerPrepared = false
        isPlaying = false
        pendingSeekTargetMs = null
        pendingSeekStartedAt = 0L
        streamStartOffsetMs = 0L
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerAutoGain = 1.6f
    }

    private fun releasePlayer() {
        stopVisualizerPump()
        releaseVisualizer()
        releaseEqualizer()
        abandonAudioFocus()
        releaseExoPlayer(mediaPlayer)
        mediaPlayer = null
        activePlayerPrepared = false
        isPlaying = false
        pendingSeekTargetMs = null
        pendingSeekStartedAt = 0L
        streamStartOffsetMs = 0L
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerAutoGain = 1.6f
    }

    private fun releaseExoPlayer(player: ExoPlayer?) {
        val stalePlayer = player ?: return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            stalePlayer.runCatching { release() }
        } else {
            mainHandler.post {
                stalePlayer.runCatching { release() }
            }
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
                    (Visualizer.getMaxCaptureRate() / 2).coerceAtLeast(1_000),
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
            visualizerAutoGain = 1.6f
        }
    }

    private fun attachEqualizer(audioSessionId: Int) {
        if (!equalizerSettings.enabled) {
            releaseEqualizer()
            return
        }
        if (audioSessionId == AudioManager.ERROR || audioSessionId <= 0) return
        val activeEqualizer = equalizer
        if (activeEqualizer != null && equalizerAudioSessionId == audioSessionId) {
            runCatching {
                activeEqualizer.enabled = false
                applyEqualizerBandLevels(activeEqualizer, equalizerSettings.normalizedLevels())
                activeEqualizer.enabled = true
            }.onFailure {
                releaseEqualizer()
            }
            return
        }

        releaseEqualizer()
        runCatching {
            val nextEqualizer = Equalizer(0, audioSessionId).apply {
                enabled = false
                applyEqualizerBandLevels(this, equalizerSettings.normalizedLevels())
                enabled = true
            }
            equalizer = nextEqualizer
            equalizerAudioSessionId = audioSessionId
        }.onFailure {
            releaseEqualizer()
        }
    }

    private fun applyEqualizerBandLevels(effect: Equalizer, levelsDb: List<Float>) {
        val range = effect.bandLevelRange
        val minLevel = range.getOrNull(0)?.toInt() ?: -1200
        val maxLevel = range.getOrNull(1)?.toInt() ?: 1200
        val bandCount = effect.numberOfBands.toInt().coerceAtLeast(1)
        val levels = if (levelsDb.size == EQUALIZER_BAND_COUNT) levelsDb else EqualizerFlatPreset.levelsDb
        for (band in 0 until bandCount) {
            val position = if (bandCount == 1) {
                0f
            } else {
                band.toFloat() / (bandCount - 1).toFloat() * (EQUALIZER_BAND_COUNT - 1).toFloat()
            }
            val lowerIndex = position.toInt().coerceIn(0, EQUALIZER_BAND_COUNT - 1)
            val upperIndex = (lowerIndex + 1).coerceIn(0, EQUALIZER_BAND_COUNT - 1)
            val blend = (position - lowerIndex).coerceIn(0f, 1f)
            val levelDb = levels[lowerIndex] + (levels[upperIndex] - levels[lowerIndex]) * blend
            val milliBel = (levelDb * 100f)
                .roundToInt()
                .coerceIn(minLevel, maxLevel)
                .toShort()
            effect.setBandLevel(band.toShort(), milliBel)
        }
    }

    private fun releaseEqualizer() {
        equalizer?.runCatching {
            enabled = false
            release()
        }
        equalizer = null
        equalizerAudioSessionId = 0
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
            rawBars[bar] = sqrt((rms * 3.8f).coerceIn(0f, 1f))
        }
        for (bar in bars.indices) {
            val previous = rawBars.getOrElse(bar - 1) { rawBars[bar] }
            val next = rawBars.getOrElse(bar + 1) { rawBars[bar] }
            bars[bar] = (rawBars[bar] * 0.62f + previous * 0.19f + next * 0.19f).coerceIn(0f, 1f)
        }
        return normalizeVisualizerLevels(bars)
    }

    private fun fftToBars(fft: ByteArray): FloatArray {
        if (fft.size < 4) return FloatArray(VISUALIZER_BAR_COUNT)
        val rawBars = FloatArray(VISUALIZER_BAR_COUNT)
        val usableBins = ((fft.size / 2) - 1).coerceAtLeast(1)
        for (bar in rawBars.indices) {
            val startFraction = bar.toFloat() / VISUALIZER_BAR_COUNT
            val endFraction = (bar + 1).toFloat() / VISUALIZER_BAR_COUNT
            val startBin = 1 + (startFraction * startFraction * usableBins).toInt()
            val endBin = (1 + (endFraction * endFraction * usableBins).toInt())
                .coerceAtLeast(startBin + 1)
            var sum = 0f
            var peak = 0f
            var count = 0
            for (bin in startBin until endBin.coerceAtMost(usableBins + 1)) {
                val real = fft.getOrElse(bin * 2) { 0 }.toFloat()
                val imag = fft.getOrElse(bin * 2 + 1) { 0 }.toFloat()
                val magnitude = sqrt(real * real + imag * imag) / 128f
                sum += magnitude
                if (magnitude > peak) peak = magnitude
                count++
            }
            val average = sum / count.coerceAtLeast(1)
            val blended = average * 0.64f + peak * 0.36f
            val shaped = (ln(1f + blended * 4.8f) / ln(5.8f)).coerceIn(0f, 1f)
            val lowEndLift = 1f + (1f - bar.toFloat() / (VISUALIZER_BAR_COUNT - 1).coerceAtLeast(1)) * 0.32f
            rawBars[bar] = (shaped * lowEndLift).coerceIn(0f, 1f)
        }
        val bars = FloatArray(VISUALIZER_BAR_COUNT) { index ->
            val previous = rawBars.getOrElse(index - 1) { rawBars[index] }
            val next = rawBars.getOrElse(index + 1) { rawBars[index] }
            (rawBars[index] * 0.64f + previous * 0.18f + next * 0.18f).coerceIn(0f, 1f)
        }
        return normalizeVisualizerLevels(bars)
    }

    private fun normalizeVisualizerLevels(rawBars: FloatArray): FloatArray {
        if (rawBars.isEmpty()) return FloatArray(VISUALIZER_BAR_COUNT)
        val peak = rawBars.maxOrNull() ?: 0f
        if (peak <= 0.003f) return rawBars.copyOf()
        val average = rawBars.average().toFloat().coerceAtLeast(0.001f)
        val loudness = (peak * 0.72f + average * 0.28f).coerceAtLeast(0.04f)
        val targetGain = (0.82f / loudness).coerceIn(1.1f, 6.2f)
        val gainBlend = if (targetGain > visualizerAutoGain) 0.18f else 0.06f
        visualizerAutoGain += (targetGain - visualizerAutoGain) * gainBlend

        return FloatArray(VISUALIZER_BAR_COUNT) { index ->
            val boosted = (rawBars.getOrElse(index) { 0f } * visualizerAutoGain).coerceIn(0f, 1f)
            val shaped = sqrt(boosted).coerceIn(0f, 1f)
            val bassLift = 1f + (1f - index.toFloat() / (VISUALIZER_BAR_COUNT - 1).coerceAtLeast(1)) * 0.16f
            (shaped * bassLift).coerceIn(0.015f, 1f)
        }
    }

    private fun fallbackVisualizerLevels(nowMs: Long, trackId: String?): FloatArray {
        val time = nowMs / 1_000f
        val seed = trackId?.hashCode() ?: 0
        val center = (VISUALIZER_BAR_COUNT - 1) / 2f
        val rawBeat = (sin(time * 8.8f + seed * 0.00011f) + 1f) * 0.5f
        val beat = rawBeat * rawBeat * rawBeat
        val sideBeat = (sin(time * 5.35f + seed * 0.00023f) + 1f) * 0.5f
        val swell = (sin(time * 1.42f + seed * 0.00019f) + 1f) * 0.5f
        return FloatArray(VISUALIZER_BAR_COUNT) { index ->
            val distanceFromCenter = abs(index - center) / center.coerceAtLeast(1f)
            val centerWeight = 1f - distanceFromCenter
            val hash = speckleHash(seed, index)
            val phaseA = ((hash and 0x3FF) / 1024f) * PI.toFloat() * 2f
            val phaseB = (((hash ushr 10) and 0x3FF) / 1024f) * PI.toFloat() * 2f
            val drift = (sin(time * (2.8f + index * 0.05f) + phaseA) + 1f) * 0.5f
            val pulse = (sin(time * (4.4f + index * 0.09f) + phaseB) + 1f) * 0.5f
            val chase = (sin(index * 0.58f - time * 8.4f + phaseA * 0.18f) + 1f) * 0.5f
            val bassWeight = (1f - index.toFloat() / VISUALIZER_BAR_COUNT).coerceIn(0f, 1f)
            val trebleSpark = ((hash ushr 22) and 0xFF) / 255f
            (
                0.035f +
                    drift * 0.2f +
                    pulse * 0.16f +
                    chase * 0.16f +
                    beat * (0.42f * bassWeight + 0.18f * centerWeight) +
                    sideBeat * 0.16f * (1f - centerWeight) +
                    swell * centerWeight * 0.24f +
                    trebleSpark * 0.1f * (1f - bassWeight)
                ).coerceIn(0.025f, 0.92f)
        }
    }

    private fun smoothVisualizerLevels(target: FloatArray): FloatArray {
        if (smoothedVisualizerLevels.size != VISUALIZER_BAR_COUNT) {
            smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        }
        for (index in 0 until VISUALIZER_BAR_COUNT) {
            val current = smoothedVisualizerLevels[index]
            val next = target.getOrElse(index) { 0f }
            val smoothing = if (next > current) 0.42f else 0.2f
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
        visualizerAutoGain = 1.6f
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
        var expectedTrackCount: Int? = null
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
                if (expectedTrackCount == null) {
                    expectedTrackCount = response.optInt("TotalRecordCount", 0).takeIf { it > 0 }
                }
                val items = response.optJSONArray("Items") ?: JSONArray()
                if (items.length() == 0) {
                    throwIfLibraryReturnedTooFew(expectedTrackCount, seenTrackIds.size)
                    break
                }

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
                if (items.length() < pageSize || addedTracks == 0) {
                    throwIfLibraryReturnedTooFew(expectedTrackCount, seenTrackIds.size)
                    break
                }
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

private fun throwIfLibraryReturnedTooFew(expectedCount: Int?, actualCount: Int) {
    val expected = expectedCount ?: return
    if (expected > actualCount) {
        throw IOException("Library sync incomplete: Jellyfin reported $expected songs but returned $actualCount.")
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

private fun reportPlaybackStart(
    context: Context,
    session: JellyfinSession,
    track: MusicTrack,
    positionMs: Long
) {
    postPlaybackReport(context, session, "Playing", track, positionMs, isPaused = false)
}

private fun reportPlaybackProgress(
    context: Context,
    session: JellyfinSession,
    track: MusicTrack,
    positionMs: Long,
    isPaused: Boolean
) {
    postPlaybackReport(context, session, "Playing/Progress", track, positionMs, isPaused)
}

private fun reportPlaybackStopped(
    context: Context,
    session: JellyfinSession,
    track: MusicTrack,
    positionMs: Long
) {
    postPlaybackReport(context, session, "Playing/Stopped", track, positionMs, isPaused = true)
}

private fun postPlaybackReport(
    context: Context,
    session: JellyfinSession,
    endpoint: String,
    track: MusicTrack,
    positionMs: Long,
    isPaused: Boolean
) {
    if (!loadPlaybackReportingEnabled(context)) return
    thread(name = "jellyfin-playback-report", isDaemon = true) {
        runCatching {
            val body = JSONObject()
                .put("ItemId", track.id)
                .put("PositionTicks", positionMs.coerceAtLeast(0L) * 10_000L)
                .put("IsPaused", isPaused)
                .put("CanSeek", true)
                .put("PlayMethod", "DirectStream")
            val url = URL("${session.serverUrl}/Sessions/$endpoint")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8_000
                readTimeout = 8_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                session.streamHeaders().forEach { (key, value) -> setRequestProperty(key, value) }
            }
            try {
                connection.outputStream.bufferedWriter().use { it.write(body.toString()) }
                connection.inputStream.close()
            } finally {
                connection.disconnect()
            }
        }
    }
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
        .getBoolean(PREF_VISUALIZER_ENABLED, false)

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

private fun loadDownloadedOnlyMode(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_DOWNLOADED_ONLY_MODE, false)

private fun saveDownloadedOnlyMode(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_DOWNLOADED_ONLY_MODE, enabled)
        .apply()
}

private fun loadAutoSyncOnLaunch(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_AUTO_SYNC_ON_LAUNCH, false)

private fun saveAutoSyncOnLaunch(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_AUTO_SYNC_ON_LAUNCH, enabled)
        .apply()
}

private fun loadGaplessPrebufferEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_GAPLESS_PREBUFFER_ENABLED, true)

private fun saveGaplessPrebufferEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_GAPLESS_PREBUFFER_ENABLED, enabled)
        .apply()
}

private fun loadTranscodedStreamingEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_TRANSCODED_STREAMING_ENABLED, false)

private fun saveTranscodedStreamingEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_TRANSCODED_STREAMING_ENABLED, enabled)
        .apply()
}

private fun loadPlaybackReportingEnabled(context: Context): Boolean =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getBoolean(PREF_PLAYBACK_REPORTING_ENABLED, true)

private fun savePlaybackReportingEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_PLAYBACK_REPORTING_ENABLED, enabled)
        .apply()
}

private fun loadEqualizerSettings(context: Context): EqualizerSettings {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val presetName = prefs.getString(PREF_EQUALIZER_PRESET, EqualizerFlatPreset.name)
        ?.takeIf { name -> EqualizerPresets.any { it.name == name } || name == "Custom" }
        ?: EqualizerFlatPreset.name
    val fallbackLevels = EqualizerPresets.firstOrNull { it.name == presetName }?.levelsDb
        ?: EqualizerFlatPreset.levelsDb
    val levels = prefs.getString(PREF_EQUALIZER_LEVELS, null)
        ?.split(',')
        ?.mapNotNull { it.toFloatOrNull()?.coerceIn(EQUALIZER_MIN_DB, EQUALIZER_MAX_DB) }
        ?.takeIf { it.size == EQUALIZER_BAND_COUNT }
        ?: fallbackLevels
    return EqualizerSettings(
        enabled = prefs.getBoolean(PREF_EQUALIZER_ENABLED, false),
        presetName = presetName,
        levelsDb = levels
    )
}

private fun saveEqualizerSettings(context: Context, settings: EqualizerSettings) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(PREF_EQUALIZER_ENABLED, settings.enabled)
        .putString(PREF_EQUALIZER_PRESET, settings.presetName)
        .putString(
            PREF_EQUALIZER_LEVELS,
            settings.normalizedLevels().joinToString(separator = ",") { level ->
                ((level * 2f).roundToInt() / 2f).toString()
            }
        )
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

private fun favoriteAlbumKeysPreferenceKey(session: JellyfinSession): String =
    "$PREF_FAVORITE_ALBUM_KEYS_PREFIX${stableCacheKey("${session.serverUrl}|${session.userId}")}"

private fun loadFavoriteAlbumKeys(context: Context, session: JellyfinSession): Set<String> =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getStringSet(favoriteAlbumKeysPreferenceKey(session), emptySet())
        ?.toSet()
        ?: emptySet()

private fun saveFavoriteAlbumKeys(context: Context, session: JellyfinSession, favoriteAlbumKeys: Set<String>) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putStringSet(favoriteAlbumKeysPreferenceKey(session), favoriteAlbumKeys.toSet())
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

private fun localPlaylistsPreferenceKey(session: JellyfinSession): String =
    "$PREF_LOCAL_PLAYLISTS_PREFIX${stableCacheKey("${session.serverUrl}|${session.userId}")}"

private fun newLocalPlaylistId(name: String): String =
    stableCacheKey("$name|${System.currentTimeMillis()}|${Random.nextLong()}").take(24)

private fun loadLocalPlaylists(context: Context, session: JellyfinSession): List<LocalPlaylist> {
    val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(localPlaylistsPreferenceKey(session), null)
        ?: return emptyList()
    return runCatching {
        val root = JSONObject(raw)
        if (root.optInt("version", 0) > LOCAL_PLAYLISTS_VERSION) return@runCatching emptyList()
        val items = root.optJSONArray("playlists") ?: JSONArray()
        buildList {
            for (index in 0 until items.length()) {
                val item = items.optJSONObject(index) ?: continue
                val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue
                val name = item.optString("name").takeIf { it.isNotBlank() } ?: continue
                val trackItems = item.optJSONArray("trackIds") ?: JSONArray()
                val trackIds = buildList {
                    for (trackIndex in 0 until trackItems.length()) {
                        trackItems.optString(trackIndex).takeIf { it.isNotBlank() }?.let(::add)
                    }
                }.distinct()
                add(
                    LocalPlaylist(
                        id = id,
                        name = name,
                        folder = item.optString("folder").orEmpty(),
                        trackIds = trackIds,
                        isFavorite = item.optBoolean("isFavorite", false),
                        updatedAt = item.optLong("updatedAt", 0L)
                    )
                )
            }
        }.distinctBy { it.id }.sortedWith(LocalPlaylistSort)
    }.getOrDefault(emptyList())
}

private fun saveLocalPlaylists(
    context: Context,
    session: JellyfinSession,
    playlists: List<LocalPlaylist>
) {
    val items = JSONArray()
    playlists.sortedWith(LocalPlaylistSort).forEach { playlist ->
        val trackItems = JSONArray()
        playlist.trackIds.distinct().forEach(trackItems::put)
        items.put(
            JSONObject()
                .put("id", playlist.id)
                .put("name", playlist.name)
                .put("folder", playlist.folder)
                .put("trackIds", trackItems)
                .put("isFavorite", playlist.isFavorite)
                .put("updatedAt", playlist.updatedAt)
        )
    }
    val root = JSONObject()
        .put("version", LOCAL_PLAYLISTS_VERSION)
        .put("playlists", items)
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(localPlaylistsPreferenceKey(session), root.toString())
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
    downloadedTrackIds: Set<String>,
    playlists: List<LocalPlaylist>
): ResolvedPinnedItem? {
    val detail = asDetail()
    val detailTracks = tracks.tracksForDetail(detail, downloadedTrackIds, playlists)
    if (detail.type != LibraryCollectionType.Downloaded && detailTracks.isEmpty()) return null
    return ResolvedPinnedItem(
        item = this,
        title = detail.titleLabel(playlists),
        subtitle = detail.subtitleLabel(detailTracks, playlists),
        artworkTrack = detailTracks.firstOrNull(),
        shape = if (type == LibraryCollectionType.Artist) CircleShape else RoundedCornerShape(8.dp),
        detail = detail
    )
}

private fun LibraryDetail.titleLabel(playlists: List<LocalPlaylist> = emptyList()): String =
    when (type) {
        LibraryCollectionType.Downloaded -> "Downloaded songs"
        LibraryCollectionType.Playlist -> playlists.firstOrNull { it.id == key }?.name ?: key
        else -> key
    }

private fun LibraryDetail.subtitleLabel(
    tracks: List<MusicTrack>,
    playlists: List<LocalPlaylist> = emptyList()
): String =
    when (type) {
        LibraryCollectionType.Album -> tracks.firstOrNull()?.artist?.let { artist ->
            "${tracks.size.countLabel("song")} - $artist"
        } ?: "Album"
        LibraryCollectionType.Artist -> tracks.size.countLabel("song")
        LibraryCollectionType.Playlist -> playlists.firstOrNull { it.id == key }?.let { playlist ->
            listOfNotNull(
                playlist.folder.takeIf { it.isNotBlank() },
                tracks.size.countLabel("song")
            ).joinToString(" - ")
        } ?: tracks.size.countLabel("song")
        LibraryCollectionType.Downloaded -> "${tracks.size.countLabel("song")} saved offline"
    }

private fun List<MusicTrack>.tracksForDetail(
    detail: LibraryDetail,
    downloadedTrackIds: Set<String>,
    playlists: List<LocalPlaylist> = emptyList()
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
        LibraryCollectionType.Playlist -> playlists.firstOrNull { it.id == detail.key }
            ?.resolveTracks(this)
            ?: emptyList()
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
    artists: List<LibraryGroup>,
    playlists: List<LibraryGroup> = emptyList()
): List<Char> {
    val hasItems = when (selectedTab) {
        LibraryTab.Songs -> songs.isNotEmpty()
        LibraryTab.Albums -> albums.isNotEmpty()
        LibraryTab.Artists -> artists.isNotEmpty()
        LibraryTab.Playlists -> playlists.isNotEmpty()
    }
    return if (hasItems) LibraryAlphabetRail else emptyList()
}

private fun libraryLetterScrollTargets(
    selectedTab: LibraryTab,
    songs: List<MusicTrack>,
    albums: List<LibraryGroup>,
    artists: List<LibraryGroup>,
    playlists: List<LibraryGroup>,
    gridView: Boolean,
    hasPinnedShelf: Boolean
): Map<Char, Int> {
    var firstContentIndex = 1 // Sticky LibraryToolbar
    if (hasPinnedShelf) firstContentIndex += 1

    val exactTargets = when (selectedTab) {
        LibraryTab.Songs -> letterScrollTargetsFor(
            items = songs,
            startItemIndex = firstContentIndex + if (songs.isNotEmpty()) 1 else 0,
            gridView = gridView,
            selector = { it.title }
        )
        LibraryTab.Albums -> letterScrollTargetsFor(
            items = albums,
            startItemIndex = firstContentIndex,
            gridView = gridView,
            selector = { it.title }
        )
        LibraryTab.Artists -> letterScrollTargetsFor(
            items = artists,
            startItemIndex = firstContentIndex,
            gridView = gridView,
            selector = { it.title }
        )
        LibraryTab.Playlists -> letterScrollTargetsFor(
            items = playlists,
            startItemIndex = firstContentIndex + 1,
            gridView = gridView,
            selector = { it.title }
        )
    }
    return exactTargets.withRailFallbackTargets()
}

private fun <T> letterScrollTargetsFor(
    items: List<T>,
    startItemIndex: Int,
    gridView: Boolean,
    selector: (T) -> String
): Map<Char, Int> {
    val targets = linkedMapOf<Char, Int>()
    items.forEachIndexed { index, item ->
        val letter = selector(item).firstLibraryLetter() ?: return@forEachIndexed
        if (letter !in targets) {
            targets[letter] = startItemIndex + if (gridView) index / 2 else index
        }
    }
    return targets
}

private fun Map<Char, Int>.withRailFallbackTargets(): Map<Char, Int> {
    if (isEmpty()) return emptyMap()
    return buildMap {
        LibraryAlphabetRail.forEachIndexed { index, letter ->
            val target = this@withRailFallbackTargets[letter]
                ?: ((index + 1)..LibraryAlphabetRail.lastIndex)
                    .firstNotNullOfOrNull { nextIndex -> this@withRailFallbackTargets[LibraryAlphabetRail[nextIndex]] }
                ?: ((index - 1) downTo 0)
                    .firstNotNullOfOrNull { previousIndex -> this@withRailFallbackTargets[LibraryAlphabetRail[previousIndex]] }
            if (target != null) {
                put(letter, target)
            }
        }
    }
}

private fun String.firstLibraryLetter(): Char? {
    val first = trim().firstOrNull() ?: return null
    val upper = first.uppercaseChar()
    return when {
        first.isDigit() -> LibraryNumberIndex
        upper in 'A'..'Z' -> upper
        else -> LibraryOtherIndex
    }
}

private fun String.libraryIndexSortWeight(): Int =
    firstLibraryLetter()?.let { letter ->
        when (letter) {
            LibraryNumberIndex -> 0
            LibraryOtherIndex -> LibraryAlphabetRail.lastIndex
            in 'A'..'Z' -> letter - 'A' + 1
            else -> LibraryAlphabetRail.lastIndex
        }
    } ?: LibraryAlphabetRail.lastIndex

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

private fun List<MusicTrack>.nextTrackAfter(track: MusicTrack): MusicTrack? {
    val normalizedQueue = queueStartingAt(track)
    if (normalizedQueue.size <= 1 || normalizedQueue.firstOrNull()?.id != track.id) return null
    return normalizedQueue.drop(1).firstOrNull { it.id != track.id }
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

private fun topSearchResult(
    query: String,
    tracks: List<MusicTrack>,
    albums: List<LibraryGroup>,
    artists: List<LibraryGroup>,
    playlists: List<LibraryGroup>
): SearchTopResult? {
    val needle = query.searchKey()
    if (needle.isBlank()) return null
    val tokens = needle.split(' ').filter { it.isNotBlank() }
    if (tokens.isEmpty()) return null

    data class Candidate(
        val score: Int,
        val priority: Int,
        val result: SearchTopResult
    )

    val candidates = buildList {
        tracks.take(16).forEach { track ->
            track.searchScore(needle, tokens)?.let { score ->
                add(
                    Candidate(
                        score = score,
                        priority = 3,
                        result = SearchTopResult(
                            type = SearchTopResultType.Song,
                            title = track.title,
                            subtitle = "${track.artist} - ${track.album}",
                            artworkTrack = track,
                            artworkShape = RoundedCornerShape(8.dp),
                            track = track
                        )
                    )
                )
            }
        }
        albums.take(12).forEach { group ->
            group.searchScore(needle, tokens)?.let { score ->
                add(
                    Candidate(
                        score = score,
                        priority = 0,
                        result = SearchTopResult(
                            type = SearchTopResultType.Album,
                            title = group.title,
                            subtitle = group.subtitle,
                            artworkTrack = group.tracks.firstOrNull(),
                            artworkShape = RoundedCornerShape(8.dp),
                            detail = LibraryDetail(LibraryCollectionType.Album, group.key)
                        )
                    )
                )
            }
        }
        artists.take(12).forEach { group ->
            group.searchScore(needle, tokens)?.let { score ->
                add(
                    Candidate(
                        score = score,
                        priority = 1,
                        result = SearchTopResult(
                            type = SearchTopResultType.Artist,
                            title = group.title,
                            subtitle = group.subtitle,
                            artworkTrack = group.tracks.firstOrNull(),
                            artworkShape = CircleShape,
                            detail = LibraryDetail(LibraryCollectionType.Artist, group.key)
                        )
                    )
                )
            }
        }
        playlists.take(12).forEach { group ->
            group.searchScore(needle, tokens)?.let { score ->
                add(
                    Candidate(
                        score = score,
                        priority = 2,
                        result = SearchTopResult(
                            type = SearchTopResultType.Playlist,
                            title = group.title,
                            subtitle = group.subtitle,
                            artworkTrack = group.tracks.firstOrNull(),
                            artworkShape = RoundedCornerShape(8.dp),
                            detail = LibraryDetail(LibraryCollectionType.Playlist, group.key)
                        )
                    )
                )
            }
        }
    }

    return candidates
        .minWithOrNull(
            compareBy<Candidate> { it.score }
                .thenBy { it.priority }
                .thenBy { it.result.title.lowercase(Locale.getDefault()) }
        )
        ?.result
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

private fun List<LibraryGroup>.filterGroupsBy(query: String): List<LibraryGroup> {
    val needle = query.searchKey()
    if (needle.isBlank()) return this
    val tokens = needle.split(' ').filter { it.isNotBlank() }
    if (tokens.isEmpty()) return this
    return mapNotNull { group ->
        group.searchScore(needle, tokens)?.let { score -> group to score }
    }.sortedWith { left, right ->
        val scoreCompare = left.second.compareTo(right.second)
        if (scoreCompare != 0) {
            scoreCompare
        } else {
            left.first.title.compareTo(right.first.title, ignoreCase = true)
        }
    }.map { it.first }
}

private fun List<MusicTrack>.sortedForLibrary(sortMode: LibrarySortMode): List<MusicTrack> =
    when (sortMode) {
        LibrarySortMode.Title -> sortedWith(MusicTrackSort)
        LibrarySortMode.Artist -> sortedWith(
            compareBy<MusicTrack>(
                { it.artist.libraryIndexSortWeight() },
                { it.artist.lowercase(Locale.getDefault()) },
                { it.title.lowercase(Locale.getDefault()) },
                { it.album.lowercase(Locale.getDefault()) }
            )
        )
        LibrarySortMode.Album -> sortedWith(
            compareBy<MusicTrack>(
                { it.album.libraryIndexSortWeight() },
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
        LibrarySortMode.Album -> sortedWith(
            compareBy<LibraryGroup>(
                { it.title.libraryIndexSortWeight() },
                { it.title.lowercase(Locale.getDefault()) }
            )
        )
        LibrarySortMode.Artist -> sortedWith(
            compareBy<LibraryGroup>(
                { it.tracks.firstOrNull()?.artist?.libraryIndexSortWeight() ?: LibraryAlphabetRail.lastIndex },
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

private fun LibraryGroup.searchScore(query: String, tokens: List<String>): Int? {
    val titleKey = title.searchKey()
    val subtitleKey = subtitle.searchKey()
    val trackWords = tracks
        .take(40)
        .joinToString(separator = " ") { "${it.title} ${it.artist} ${it.album}" }
        .searchKey()
    val combinedKey = "$titleKey $subtitleKey $trackWords"
    val searchableWords = combinedKey.split(' ').filter { it.isNotBlank() }
    val fuzzyMatch = tokens.all { token ->
        combinedKey.contains(token) || searchableWords.any { word -> word.fuzzyMatches(token) }
    }
    if (!fuzzyMatch) return null
    return when {
        titleKey == query -> 0
        titleKey.startsWith(query) -> 1
        titleKey.contains(query) -> 2
        tokens.any { token -> titleKey.split(' ').any { it.fuzzyMatches(token) } } -> 3
        subtitleKey == query -> 4
        subtitleKey.startsWith(query) -> 5
        subtitleKey.contains(query) -> 6
        tokens.any { token -> subtitleKey.split(' ').any { it.fuzzyMatches(token) } } -> 7
        else -> 8
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

private fun List<LocalPlaylist>.toLibraryGroups(tracks: List<MusicTrack>): List<LibraryGroup> {
    if (isEmpty()) return emptyList()
    val tracksById = tracks.associateBy { it.id }
    return sortedWith(LocalPlaylistSort).map { playlist ->
        val playlistTracks = playlist.trackIds.mapNotNull { tracksById[it] }
        val fallbackTint = AlbumTints[abs(playlist.id.hashCode()) % AlbumTints.size]
        val folderLabel = playlist.folder.takeIf { it.isNotBlank() }
        LibraryGroup(
            title = playlist.name,
            subtitle = listOfNotNull(
                folderLabel,
                playlistTracks.size.countLabel("song"),
                "Local playlist".takeIf { playlistTracks.isEmpty() }
            ).joinToString(" - "),
            tint = playlistTracks.firstOrNull()?.tint ?: fallbackTint,
            tracks = playlistTracks,
            key = playlist.id
        )
    }
}

private fun LocalPlaylist.resolveTracks(tracks: List<MusicTrack>): List<MusicTrack> {
    if (trackIds.isEmpty() || tracks.isEmpty()) return emptyList()
    val tracksById = tracks.associateBy { it.id }
    return trackIds.mapNotNull { tracksById[it] }
}

private fun MusicTrack.toAutoTrackItem(): MediaBrowser.MediaItem =
    MediaBrowser.MediaItem(
        MediaDescription.Builder()
            .setMediaId("$AUTO_TRACK_PREFIX$id")
            .setTitle(title)
            .setSubtitle(notificationSubtitle())
            .build(),
        MediaBrowser.MediaItem.FLAG_PLAYABLE
    )

private fun LibraryGroup.toAutoGroupItem(prefix: String, flags: Int): MediaBrowser.MediaItem =
    MediaBrowser.MediaItem(
        MediaDescription.Builder()
            .setMediaId("$prefix${Uri.encode(title)}")
            .setTitle(title)
            .setSubtitle(subtitle)
            .build(),
        flags
    )

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
