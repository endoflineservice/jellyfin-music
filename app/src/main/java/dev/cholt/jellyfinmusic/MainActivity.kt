package dev.cholt.jellyfinmusic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.Collections
import java.util.LinkedHashMap
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
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
            JellyfinMusicTheme {
                JellyfinMusicApp()
            }
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
private const val LIBRARY_CACHE_VERSION = 1
private const val ALBUM_ART_CACHE_DIR = "album_art_cache"
private const val MAX_ALBUM_ART_CACHE_FILES = 320
private const val DISC_SCRATCH_SEEK_SCALE = 0.55f
private const val DISC_SCRATCH_DEAD_ZONE = 0.22f
private const val VISUALIZER_BAR_COUNT = 28
private const val VISUALIZER_CAPTURE_STALE_MS = 650L
private const val VISUALIZER_FALLBACK_FRAME_MS = 66L

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

private enum class LibraryTab(val label: String) {
    Songs("Songs"),
    Albums("Albums"),
    Artists("Artists")
}

private enum class AppDestination(val label: String) {
    Home("Home"),
    Search("Search"),
    Player("Play"),
    Library("Library"),
    Profile("Me")
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
    AppDestination.Profile
)

private data class LibraryGroup(
    val title: String,
    val subtitle: String,
    val tint: Color,
    val tracks: List<MusicTrack>
)

private val MusicTrackSort = compareBy<MusicTrack>(
    { it.title.lowercase(Locale.getDefault()) },
    { it.artist.lowercase(Locale.getDefault()) },
    { it.album.lowercase(Locale.getDefault()) }
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JellyfinMusicApp() {
    val context = LocalContext.current
    val repository = remember { JellyfinRepository(context.applicationContext) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val player = remember { JellyfinPlayer(context.applicationContext) }
    val scratchEngine = remember { ScratchSoundEngine() }

    var session by remember { mutableStateOf(loadSavedSession(context)) }
    var serverUrl by remember { mutableStateOf(session?.serverUrl.orEmpty()) }
    var username by remember { mutableStateOf(session?.username.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var tracks by remember { mutableStateOf(session?.let { loadCachedLibrary(context, it) } ?: emptyList()) }
    var selectedTab by remember { mutableStateOf(LibraryTab.Songs) }
    var searchQuery by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var playQueue by remember { mutableStateOf(emptyList<MusicTrack>()) }
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatEnabled by remember { mutableStateOf(false) }
    var useAlbumArtColors by remember { mutableStateOf(loadUseAlbumArtColors(context)) }
    var visualizerEnabled by remember { mutableStateOf(loadVisualizerEnabled(context)) }
    var themeMode by remember { mutableStateOf(loadThemeMode(context)) }
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
            val result = runCatching {
                repository.fetchTracks(activeSession) { loadedTracks ->
                    partialTracks = loadedTracks
                    saveCachedLibrary(context, activeSession, loadedTracks)
                    mainHandler.post {
                        tracks = loadedTracks
                        statusText = null
                    }
                }.also { loadedTracks ->
                    saveCachedLibrary(context, activeSession, loadedTracks)
                    warmAlbumArtCache(context, activeSession, loadedTracks)
                }
            }
            mainHandler.post {
                isBusy = false
                result
                    .onSuccess { loadedTracks ->
                        tracks = loadedTracks
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
                    saveCachedLibrary(context, activeSession, loadedTracks)
                    mainHandler.post {
                        tracks = loadedTracks
                        statusText = null
                    }
                }
                saveCachedLibrary(context, activeSession, loadedTracks)
                warmAlbumArtCache(context, activeSession, loadedTracks)
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
        playQueue = emptyList()
        statusText = null
        showPlayer = false
        selectedDestination = AppDestination.Home
    }

    fun playTrack(track: MusicTrack, openPlayer: Boolean = false, source: List<MusicTrack> = tracks) {
        session?.let { activeSession ->
            playQueue = source.queueStartingAt(track).ifEmpty { listOf(track) }
            player.play(track, activeSession)
            if (openPlayer) {
                selectedDestination = AppDestination.Player
                showPlayer = true
            }
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

    DisposableEffect(Unit) {
        onDispose {
            scratchEngine.release()
            player.release()
        }
    }

    LaunchedEffect(session?.token) {
        val activeSession = session
        if (activeSession != null && tracks.isEmpty()) {
            loadLibrary(activeSession)
        } else if (activeSession != null && tracks.isNotEmpty()) {
            warmAlbumArtCache(context, activeSession, tracks)
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
    }
    val showTopBar = showPlayer || session == null || selectedDestination != AppDestination.Home
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
                Modifier.swipeDownToDismiss(
                    onDismiss = closePlayer,
                    startZone = 128.dp,
                    dismissDistance = 72.dp
                )
            } else {
                Modifier
            },
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        modifier = if (showPlayer) {
                            Modifier.swipeDownToDismiss(closePlayer, startZone = 96.dp)
                        } else {
                            Modifier
                        },
                        navigationIcon = {
                            if (showPlayer) {
                                IconButton(onClick = closePlayer) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to Home"
                                    )
                                }
                            }
                        },
                        title = {
                            if (!showPlayer) {
                                val subtitle = when {
                                    session == null -> "Not connected"
                                    selectedDestination == AppDestination.Profile -> "Settings"
                                    else -> null
                                }
                                Column {
                                    Text(
                                        text = "Jellyfin Music",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    if (subtitle != null) {
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
                                onOpen = {
                                    selectedDestination = AppDestination.Player
                                    showPlayer = true
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
                            onDestinationSelected = { destination ->
                                selectedDestination = destination
                                when (destination) {
                                    AppDestination.Home -> {
                                        selectedTab = LibraryTab.Songs
                                        showPlayer = false
                                    }

                                    AppDestination.Search -> {
                                        showPlayer = false
                                    }

                                    AppDestination.Player -> {
                                        if (player.currentTrack != null) {
                                            showPlayer = true
                                        }
                                    }

                                    AppDestination.Library -> {
                                        showPlayer = false
                                    }

                                    AppDestination.Profile -> {
                                        showPlayer = false
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
            if (showPlayer && activeTrack != null) {
                FullPlayerScreen(
                    track = activeTrack,
                    isPlaying = player.isPlaying,
                    progress = player.progress,
                    visualizerLevels = player.visualizerLevels,
                    status = player.status,
                    queue = playQueue.ifEmpty { tracks.queueStartingAt(activeTrack) },
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
                    onToggleShuffle = { shuffleEnabled = !shuffleEnabled },
                    onToggleRepeat = { repeatEnabled = !repeatEnabled },
                    onQueueTrackClick = ::playQueuedTrack,
                    onQueueMove = ::moveQueueTrack,
                    onQueuePlayNext = ::playTrackNext,
                    onDismiss = closePlayer
                )
            } else {
                LazyColumn(
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
                                SettingsSectionHeader("Connection")
                            }
                            item {
                                AccountCard(
                                    session = connectedSession,
                                    isBusy = isBusy,
                                    onRefresh = { loadLibrary(connectedSession) },
                                    onSignOut = ::signOut
                                )
                            }
                            item {
                                SettingsSectionHeader("Appearance")
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
                            item {
                                SettingsSectionHeader("About")
                            }
                            item {
                                AboutCard()
                            }
                        }

                        AppDestination.Player -> {
                            item {
                                EmptyPlayerCard()
                            }
                        }

                        else -> {
                            val isHomeTab = selectedDestination == AppDestination.Home
                            val isSearchTab = selectedDestination == AppDestination.Search
                            if (!isHomeTab) {
                                item {
                                    LibraryHeader(
                                        title = if (isSearchTab) "Search" else "Library",
                                        showSearch = isSearchTab,
                                        searchQuery = searchQuery,
                                        isBusy = isBusy,
                                        statusText = statusText,
                                        onSearchQueryChange = { searchQuery = it },
                                        onRefresh = { loadLibrary(connectedSession) }
                                    )
                                }
                            }
                            item {
                                LibraryTabs(
                                    selectedTab = selectedTab,
                                    onTabSelected = { selectedTab = it }
                                )
                            }

                            val filteredTracks = if (isSearchTab) {
                                tracks.filterBy(searchQuery)
                            } else {
                                tracks
                            }
                            when (selectedTab) {
                                LibraryTab.Songs -> {
                                    if (filteredTracks.isEmpty()) {
                                        item { EmptyLibraryMessage(isBusy = isBusy, statusText = statusText) }
                                    } else {
                                        items(filteredTracks, key = { it.id }) { track ->
                                            TrackRow(
                                                track = track,
                                                session = connectedSession,
                                                isCurrent = player.currentTrack?.id == track.id,
                                                onClick = { playTrack(track, openPlayer = true, source = filteredTracks) }
                                            )
                                        }
                                    }
                                }

                                LibraryTab.Albums -> {
                                    val groups = filteredTracks.groupByAlbum()
                                    if (groups.isEmpty()) {
                                        item { EmptyLibraryMessage(isBusy = isBusy, statusText = statusText) }
                                    } else {
                                        items(groups, key = { it.title }) { group ->
                                            GroupRow(
                                                group = group,
                                                session = connectedSession,
                                                onClick = {
                                                    playTrack(
                                                        group.tracks.first(),
                                                        openPlayer = true,
                                                        source = group.tracks
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }

                                LibraryTab.Artists -> {
                                    val groups = filteredTracks.groupByArtist()
                                    if (groups.isEmpty()) {
                                        item { EmptyLibraryMessage(isBusy = isBusy, statusText = statusText) }
                                    } else {
                                        items(groups, key = { it.title }) { group ->
                                            GroupRow(
                                                group = group,
                                                session = connectedSession,
                                                onClick = {
                                                    playTrack(
                                                        group.tracks.first(),
                                                        openPlayer = true,
                                                        source = group.tracks
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
                text = "Jellyfin Music",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            SettingsInfoRow(
                title = "License",
                value = "FOSS project"
            )
            SettingsInfoRow(
                title = "Author",
                value = "Corry Holt"
            )
            SettingsInfoRow(
                title = "Contact",
                value = "corryrholt@gamil.com"
            )
        }
    }
}

@Composable
private fun AccountCard(
    session: JellyfinSession,
    isBusy: Boolean,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Jellyfin account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            SettingsInfoRow(
                title = "Signed in",
                value = session.username
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
            SettingsInfoRow(
                title = "Server",
                value = session.serverUrl.toHostLabel()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onRefresh,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Refresh")
                }
                TextButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Sign out")
                }
            }
            if (isBusy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        text = if (visualizerEnabled) "Show audio motion on Now Playing" else "Hide audio motion",
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
    onDestinationSelected: (AppDestination) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTabDestinations.forEach { destination ->
                val selected = selectedDestination == destination
                BottomTabItem(
                    destination = destination,
                    selected = selected,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.weight(if (selected) 1.35f else 1f)
                )
            }
        }
    }
}

@Composable
private fun BottomTabItem(
    destination: AppDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = destinationIcon(destination),
                        contentDescription = destination.label,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        } else {
            Icon(
                imageVector = destinationIcon(destination),
                contentDescription = destination.label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun destinationIcon(destination: AppDestination): ImageVector =
    when (destination) {
        AppDestination.Home -> Icons.Filled.Home
        AppDestination.Search -> Icons.Filled.Search
        AppDestination.Player -> Icons.Filled.PlayArrow
        AppDestination.Library -> PlayerIconVectors.Library
        AppDestination.Profile -> Icons.Filled.Person
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
private fun LibraryTabs(selectedTab: LibraryTab, onTabSelected: (LibraryTab) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LibraryTab.entries.forEach { tab ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                label = { Text(tab.label) }
            )
        }
    }
}

@Composable
private fun TrackRow(
    track: MusicTrack,
    session: JellyfinSession?,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
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
            Text(
                text = formatDuration(track.durationMs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
private fun EmptyLibraryMessage(isBusy: Boolean, statusText: String?) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            text = when {
                isBusy -> "Loading library"
                statusText != null -> statusText
                else -> "No matching music"
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
    dismissDistance: Dp = 88.dp
): Modifier = pointerInput(onDismiss, startZone) {
    val startZonePx = startZone.toPx()
    val dismissDistancePx = dismissDistance.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(pass = PointerEventPass.Initial, requireUnconsumed = false)
        val startsInTopZone = down.position.y <= startZonePx
        var totalDragX = 0f
        var totalDragY = 0f
        var dismissed = false

        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break

            if (startsInTopZone) {
                val dragAmount = change.position - change.previousPosition
                totalDragX += dragAmount.x
                totalDragY += dragAmount.y
                if (totalDragY > 0f) {
                    change.consume()
                }
                if (
                    !dismissed &&
                    totalDragY > dismissDistancePx &&
                    totalDragY > abs(totalDragX) * 1.2f
                ) {
                    dismissed = true
                    onDismiss()
                    change.consume()
                    break
                }
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
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onQueueTrackClick: (MusicTrack) -> Unit,
    onQueueMove: (Int, Int) -> Unit,
    onQueuePlayNext: (MusicTrack) -> Unit,
    onDismiss: () -> Unit
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
            .swipeDownToDismiss(
                onDismiss = onDismiss,
                startZone = 220.dp,
                dismissDistance = 110.dp
            )
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
                    RoundedPlaybackButton(
                        icon = PlayerGlyph.Favorite,
                        contentDescription = "Favorite track",
                        onClick = {},
                        modifier = Modifier.size(58.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            QueuePullHandle(
                queueCount = (displayQueue.size - 1).coerceAtLeast(0),
                onOpen = { showQueue = true }
            )
        }

        if (showQueue) {
            QueueBottomSheet(
                currentTrack = track,
                queue = displayQueue,
                session = session,
                onDismiss = { showQueue = false },
                onTrackClick = onQueueTrackClick,
                onMove = onQueueMove,
                onPlayNext = onQueuePlayNext,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun QueuePullHandle(
    queueCount: Int,
    onOpen: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .swipeUpToOpen(onOpen)
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(23.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
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

private fun Modifier.swipeUpToOpen(
    onOpen: () -> Unit,
    openDistance: Dp = 46.dp
): Modifier = pointerInput(onOpen, openDistance) {
    val openDistancePx = openDistance.toPx()
    var totalDragY = 0f

    detectDragGestures(
        onDragStart = { totalDragY = 0f },
        onDragEnd = {
            if (totalDragY < -openDistancePx) {
                onOpen()
            }
            totalDragY = 0f
        },
        onDragCancel = { totalDragY = 0f },
        onDrag = { change, dragAmount ->
            totalDragY += dragAmount.y
            if (dragAmount.y < 0f) {
                change.consume()
            }
        }
    )
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
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.24f))
                .clickable(onClick = onDismiss)
        )
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .navigationBarsPadding(),
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
    Favorite,
    Replay,
    Queue,
    More,
    MoveUp,
    MoveDown
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
        modifier = modifier
            .size(if (prominent) 64.dp else 48.dp)
            .semantics { this.contentDescription = contentDescription }
            .clickable(onClick = onClick),
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
        this == PlayerGlyph.Favorite -> 25.dp
        else -> 27.dp
    }

private fun PlayerGlyph.imageVector(): ImageVector = when (this) {
    PlayerGlyph.Shuffle -> PlayerIconVectors.Shuffle
    PlayerGlyph.Previous -> PlayerIconVectors.SkipPrevious
    PlayerGlyph.Play -> Icons.Filled.PlayArrow
    PlayerGlyph.Pause -> PlayerIconVectors.Pause
    PlayerGlyph.Next -> PlayerIconVectors.SkipNext
    PlayerGlyph.Repeat -> PlayerIconVectors.Repeat
    PlayerGlyph.Favorite -> Icons.Filled.Favorite
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
private fun TurntableArmOverlay(progress: Float, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Canvas(modifier = modifier) {
        val p = progress.coerceIn(0f, 1f)
        val accent = colorScheme.primary
        val pivot = Offset(size.width * 0.205f, size.height * 0.14f)
        val drop = Offset(size.width * (0.18f + p * 0.01f), size.height * (0.36f + p * 0.008f))
        val elbow = Offset(size.width * (0.235f + p * 0.035f), size.height * (0.51f - p * 0.01f))
        val stylus = Offset(size.width * (0.285f + p * 0.095f), size.height * (0.635f - p * 0.08f))
        val cartridgeAngle = 0.82f
        val cartridgeLength = size.minDimension * 0.105f
        val cartridgeWidth = size.minDimension * 0.04f
        val cartridgeDirection = Offset(cos(cartridgeAngle), sin(cartridgeAngle))
        val cartridgeBack = Offset(
            x = stylus.x - cos(cartridgeAngle) * cartridgeLength,
            y = stylus.y - sin(cartridgeAngle) * cartridgeLength
        )
        fun armPath(offset: Offset = Offset.Zero): Path = Path().apply {
            moveTo(pivot.x + offset.x, pivot.y + offset.y)
            cubicTo(
                pivot.x - size.width * 0.04f + offset.x,
                pivot.y + size.height * 0.11f + offset.y,
                drop.x - size.width * 0.02f + offset.x,
                drop.y - size.height * 0.03f + offset.y,
                drop.x + offset.x,
                drop.y + offset.y
            )
            cubicTo(
                drop.x + size.width * 0.012f + offset.x,
                drop.y + size.height * 0.06f + offset.y,
                elbow.x - size.width * 0.014f + offset.x,
                elbow.y - size.height * 0.035f + offset.y,
                elbow.x + offset.x,
                elbow.y + offset.y
            )
            lineTo(cartridgeBack.x + offset.x, cartridgeBack.y + offset.y)
        }
        val normal = Offset(
            x = -sin(cartridgeAngle) * cartridgeWidth,
            y = cos(cartridgeAngle) * cartridgeWidth
        )
        val railGap = 3.2.dp.toPx()
        val metalBrush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.9f),
                Color(0xFFB7C7C1).copy(alpha = 0.86f),
                Color(0xFF53605C).copy(alpha = 0.7f)
            ),
            start = Offset(pivot.x - 18.dp.toPx(), pivot.y),
            end = Offset(stylus.x, stylus.y)
        )

        drawPath(
            path = armPath(Offset(2.dp.toPx(), 3.dp.toPx())),
            color = Color.Black.copy(alpha = 0.32f),
            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(),
            color = Color.Black.copy(alpha = 0.18f),
            style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(Offset(-railGap, 0f)),
            color = Color.Black.copy(alpha = 0.42f),
            style = Stroke(width = 4.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(Offset(railGap, 0f)),
            color = Color.Black.copy(alpha = 0.42f),
            style = Stroke(width = 4.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(Offset(-railGap, 0f)),
            brush = metalBrush,
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(Offset(railGap, 0f)),
            brush = metalBrush,
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(Offset(-railGap - 0.7.dp.toPx(), 0f)),
            color = Color.White.copy(alpha = 0.44f),
            style = Stroke(width = 0.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = armPath(Offset(railGap - 0.7.dp.toPx(), 0f)),
            color = Color.White.copy(alpha = 0.34f),
            style = Stroke(width = 0.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
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
            start = Offset(cartridgeBack.x - railGap, cartridgeBack.y),
            end = Offset(cartridgeBack.x + railGap, cartridgeBack.y),
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
        drawPath(path = cartridge, color = Color(0xFFEAF2ED).copy(alpha = 0.9f))
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
            color = accent.copy(alpha = 0.82f),
            radius = 2.dp.toPx(),
            center = stylus
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.22f),
            radius = 31.dp.toPx(),
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
                radius = 31.dp.toPx()
            ),
            radius = 29.dp.toPx(),
            center = pivot
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.38f),
            radius = 18.dp.toPx(),
            center = pivot,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = blendColors(accent, Color.Black, 0.34f).copy(alpha = 0.9f),
            radius = 11.dp.toPx(),
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
            .clip(CircleShape)
            .background(Color.Black)
    ) {
        AlbumArtworkImage(
            track = track,
            session = session,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .alpha(0.18f),
            imageSize = 160,
            imageQuality = 74,
            networkDelayMs = 1_200L
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                color = Color.Black.copy(alpha = 0.64f),
                radius = radius,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tint.copy(alpha = 0.1f),
                        Color(0xFF1D2A26).copy(alpha = 0.24f),
                        Color.Black.copy(alpha = 0.52f),
                        Color.Black.copy(alpha = 0.82f)
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
                        Color.Black.copy(alpha = 0.24f),
                        Color.Black.copy(alpha = 0.58f)
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawArc(
                color = Color.White.copy(alpha = 0.16f),
                startAngle = -76f,
                sweepAngle = 48f,
                useCenter = false,
                topLeft = Offset.Zero,
                size = Size(size.minDimension, size.minDimension),
                style = Stroke(width = radius * 0.2f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color.White.copy(alpha = 0.06f),
                startAngle = 136f,
                sweepAngle = 56f,
                useCenter = false,
                topLeft = Offset.Zero,
                size = Size(size.minDimension, size.minDimension),
                style = Stroke(width = radius * 0.16f, cap = StrokeCap.Round)
            )
            drawArc(
                color = Color.Black.copy(alpha = 0.34f),
                startAngle = 206f,
                sweepAngle = 80f,
                useCenter = true,
                topLeft = Offset.Zero,
                size = Size(size.minDimension, size.minDimension)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = radius * 0.18f,
                center = Offset(size.width * 0.36f, size.height * 0.26f)
            )
            for (index in 0..42) {
                val grooveRadius = radius * (0.16f + index * 0.019f)
                drawCircle(
                    color = Color.White.copy(alpha = if (index % 6 == 0) 0.14f else 0.052f),
                    radius = grooveRadius,
                    center = center,
                    style = Stroke(width = if (index % 6 == 0) 0.85.dp.toPx() else 0.45.dp.toPx())
                )
            }
            drawCircle(
                color = Color.Black.copy(alpha = 0.24f),
                radius = radius * 0.97f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
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
                outerRadius = radius * 0.94f,
                innerRadius = radius * 0.29f,
                seed = track.id.hashCode()
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.22f),
                radius = radius * 0.27f,
                center = center
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.54f),
                radius = radius * 0.24f,
                center = center
            )
        }
        AlbumArtworkImage(
            track = track,
            session = session,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize(0.27f)
                .clip(CircleShape),
            imageSize = 160,
            imageQuality = 78,
            networkDelayMs = 1_200L
        )
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

private fun DrawScope.drawVinylSpeckleTexture(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    seed: Int
) {
    val speckleCount = 150
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
            color = Color.White.copy(alpha = 0.018f + alphaNoise * 0.034f),
            radius = 0.45.dp.toPx() + sizeNoise * 0.75.dp.toPx(),
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
    val hasLiveLevels = active && levels.any { it > 0.008f }

    Canvas(modifier = modifier) {
        val barCount = VISUALIZER_BAR_COUNT
        val gap = size.width / (barCount * 2.05f)
        val strokeWidth = gap.coerceAtLeast(2.5f)
        val step = size.width / barCount
        val baseline = size.height * 0.86f
        for (index in 0 until barCount) {
            val level = when {
                hasLiveLevels -> levels.getOrElse(index) { 0f }
                else -> 0f
            }.coerceIn(0f, 1f)
            val height = if (hasLiveLevels) {
                size.height * (0.08f + level * 0.78f)
            } else {
                1.5.dp.toPx()
            }
            val x = step * index + step / 2f
            drawLine(
                color = color,
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
    onToggle: () -> Unit,
    onReplay: () -> Unit,
    onSeek: (Float) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .swipeUpToOpen(onOpen),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.artist} - $status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(12.dp))
                AlbumTile(track = track, session = session, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(8.dp))
            WavySeekBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                onSeek = onSeek
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundedPlaybackButton(
                    icon = PlayerGlyph.Previous,
                    contentDescription = "Previous track",
                    onClick = onPrevious,
                    modifier = Modifier.size(48.dp)
                )
                RoundedPlaybackButton(
                    icon = if (isPlaying) PlayerGlyph.Pause else PlayerGlyph.Play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    onClick = onToggle,
                    prominent = true,
                    modifier = Modifier.size(58.dp)
                )
                RoundedPlaybackButton(
                    icon = PlayerGlyph.Next,
                    contentDescription = "Next track",
                    onClick = onNext,
                    modifier = Modifier.size(48.dp)
                )
            }
            if (!isPlaying && status == "Ended") {
                RoundedPlaybackButton(
                    icon = PlayerGlyph.Replay,
                    contentDescription = "Replay track",
                    onClick = onReplay,
                    modifier = Modifier
                        .align(Alignment.End)
                        .size(44.dp)
                )
            }
        }
    }
}

@Composable
private fun TopBarSineVisualizer(modifier: Modifier = Modifier, color: Color) {
    val transition = rememberInfiniteTransition(label = "top-bar-sine")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
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
        val amplitude = size.height * 0.24f
        val wavelength = size.width / 1.7f
        val path = Path()
        var x = 0f

        path.moveTo(0f, centerY)
        while (x <= size.width) {
            val y = centerY + sin((x / wavelength) * PI.toFloat() * 2f + phase) * amplitude
            path.lineTo(x, y)
            x += 3f
        }

        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
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
    private var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null
    private var visualizerEnabled = true
    private var smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
    private var lastVisualizerCaptureAt = 0L
    private var visualizerPumpRunning = false
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

    fun play(track: MusicTrack, session: JellyfinSession) {
        releasePlayer()
        currentTrack = track
        status = "Buffering"
        progress = 0f
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        lastVisualizerCaptureAt = 0L
        saveWidgetState(context, track, status, progress)

        val nextPlayer = MediaPlayer()
        mediaPlayer = nextPlayer
        runCatching {
            nextPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .apply {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
                        }
                    }
                    .build()
            )
            nextPlayer.setDataSource(
                track.streamUrl(session)
            )
            nextPlayer.setOnPreparedListener {
                it.start()
                isPlaying = true
                status = "Playing"
                if (visualizerEnabled) {
                    attachVisualizer(it.audioSessionId)
                    startVisualizerPump()
                }
                syncProgress()
                saveWidgetState(context, track, status, progress)
            }
            nextPlayer.setOnCompletionListener {
                visualizer?.runCatching { enabled = false }
                stopVisualizerPump()
                isPlaying = false
                status = "Ended"
                progress = 1f
                smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                saveWidgetState(context, track, status, progress)
            }
            nextPlayer.setOnErrorListener { _, _, _ ->
                visualizer?.runCatching { enabled = false }
                stopVisualizerPump()
                isPlaying = false
                status = "Playback error"
                smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                saveWidgetState(context, track, status, progress)
                true
            }
            nextPlayer.prepareAsync()
        }.onFailure {
            isPlaying = false
            status = it.readableMessage()
            saveWidgetState(context, track, status, progress)
        }
    }

    fun toggle() {
        val activePlayer = mediaPlayer ?: return
        if (isPlaying) {
            activePlayer.pause()
            visualizer?.runCatching { enabled = false }
            stopVisualizerPump()
            isPlaying = false
            status = "Paused"
            saveWidgetState(context, currentTrack, status, progress)
        } else {
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
            saveWidgetState(context, currentTrack, status, progress)
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
                saveWidgetState(context, currentTrack, status, progress)
            }
        }
    }

    fun release() {
        releasePlayer()
        currentTrack = null
        status = "Ready"
        progress = 0f
        saveWidgetState(context, null, status, progress)
    }

    private fun releasePlayer() {
        stopVisualizerPump()
        releaseVisualizer()
        mediaPlayer?.runCatching {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
        isPlaying = false
        smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
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
                    true,
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
        if (nextLevels.any { it > 0.015f }) {
            lastVisualizerCaptureAt = SystemClock.elapsedRealtime()
        }
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
        val seed = (trackId?.hashCode() ?: 0) * 0.00031f
        val center = (VISUALIZER_BAR_COUNT - 1) / 2f
        return FloatArray(VISUALIZER_BAR_COUNT) { index ->
            val distanceFromCenter = abs(index - center) / center.coerceAtLeast(1f)
            val centerWeight = 1f - distanceFromCenter
            val slowWave = (sin(time * 3.7f + index * 0.58f + seed) + 1f) * 0.5f
            val fastWave = (sin(time * 7.1f + index * 0.31f + seed * 1.7f) + 1f) * 0.5f
            val pulse = (sin(time * 5.2f + seed) + 1f) * 0.5f
            (0.05f + slowWave * 0.26f + fastWave * 0.14f + pulse * centerWeight * 0.24f)
                .coerceIn(0.035f, 0.78f)
        }
    }

    private fun smoothVisualizerLevels(target: FloatArray): FloatArray {
        if (smoothedVisualizerLevels.size != VISUALIZER_BAR_COUNT) {
            smoothedVisualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        }
        for (index in 0 until VISUALIZER_BAR_COUNT) {
            val current = smoothedVisualizerLevels[index]
            val next = target.getOrElse(index) { 0f }
            val smoothing = if (next > current) 0.36f else 0.18f
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
        val pageSize = 250
        return buildList {
            var startIndex = 0
            var totalRecordCount = Int.MAX_VALUE
            while (startIndex < totalRecordCount) {
                val url = buildString {
                    append(session.serverUrl)
                    append("/Users/")
                    append(encode(session.userId))
                    append("/Items?Recursive=true")
                    append("&IncludeItemTypes=Audio")
                    append("&Fields=Album,AlbumId,AlbumPrimaryImageTag,Artists,ImageTags,PrimaryImageItemId,RunTimeTicks")
                    append("&StartIndex=$startIndex")
                    append("&Limit=$pageSize")
                }
                val response = JSONObject(request(url = url, method = "GET", body = null, session = session))
                totalRecordCount = response.optInt("TotalRecordCount", totalRecordCount)
                val items = response.optJSONArray("Items") ?: JSONArray()
                if (items.length() == 0) break

                for (index in 0 until items.length()) {
                    val item = items.optJSONObject(index) ?: continue
                    val id = item.optString("Id")
                    if (id.isBlank()) continue
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
                            imageItemId = imageItemId,
                            imageTag = imageTag,
                            tint = tintFor(id)
                        )
                    )
                }
                startIndex += items.length()
                if (isNotEmpty()) {
                    onPartial?.invoke(toList().sortedWith(MusicTrackSort))
                }
                if (items.length() < pageSize) break
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
                        imageItemId = item.optString("imageItemId").takeIf { it.isNotBlank() },
                        imageTag = item.optString("imageTag").takeIf { it.isNotBlank() },
                        tint = if (item.has("tintArgb")) composeColor(item.optInt("tintArgb")) else tintFor(id)
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun saveCachedLibrary(context: Context, session: JellyfinSession, tracks: List<MusicTrack>) {
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
                    .put("imageItemId", track.imageItemId.orEmpty())
                    .put("imageTag", track.imageTag.orEmpty())
                    .put("tintArgb", track.tint.toArgb())
            )
        }
        val root = JSONObject()
            .put("version", LIBRARY_CACHE_VERSION)
            .put("updatedAt", System.currentTimeMillis())
            .put("tracks", items)
        libraryCacheFile(context, session).writeText(root.toString())
    }
}

private fun libraryCacheFile(context: Context, session: JellyfinSession): File =
    File(context.filesDir, "library-${stableCacheKey("${session.serverUrl}|${session.userId}")}.json")

private fun warmAlbumArtCache(context: Context, session: JellyfinSession, tracks: List<MusicTrack>) {
    val appContext = context.applicationContext
    val urls = tracks
        .asSequence()
        .mapNotNull { it.imageUrl(session, size = 160, quality = 74) }
        .distinct()
        .take(64)
        .toList()
    if (urls.isEmpty()) return
    thread(name = "album-art-cache-warmup") {
        urls.forEach { imageUrl ->
            loadAlbumBitmapBlocking(appContext, imageUrl, session.token)
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
    val needle = query.trim()
    if (needle.isBlank()) return this
    return filter { track ->
        track.title.contains(needle, ignoreCase = true) ||
            track.artist.contains(needle, ignoreCase = true) ||
            track.album.contains(needle, ignoreCase = true)
    }
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
