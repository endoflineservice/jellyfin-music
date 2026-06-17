package dev.cholt.jellyfinmusic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
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
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
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
import kotlin.random.Random
import ir.mahozad.multiplatform.wavyslider.WaveDirection.HEAD
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
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
private const val DISC_SCRATCH_SEEK_SCALE = 0.55f
private const val DISC_SCRATCH_DEAD_ZONE = 0.22f
private const val VISUALIZER_BAR_COUNT = 28

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

    fun imageUrl(session: JellyfinSession?, size: Int = 512): String? {
        val activeSession = session ?: return null
        val itemId = imageItemId?.takeIf { it.isNotBlank() } ?: return null
        val tagParameter = imageTag
            ?.takeIf { it.isNotBlank() }
            ?.let { "&tag=${encode(it)}" }
            .orEmpty()
        return "${activeSession.serverUrl}/Items/${encode(itemId)}/Images/Primary" +
            "?fillWidth=$size&fillHeight=$size&quality=90$tagParameter&api_key=${encode(activeSession.token)}"
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
    Library("Lib"),
    Profile("Me")
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

@Composable
private fun JellyfinMusicTheme(
    albumAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = albumAccentColor?.let(::albumArtLightColorScheme)
        ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
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

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

private fun albumArtLightColorScheme(rawAccent: Color) = lightColorScheme(
    primary = normalizeAlbumAccent(rawAccent),
    onPrimary = readableOnColor(normalizeAlbumAccent(rawAccent)),
    primaryContainer = blendColors(normalizeAlbumAccent(rawAccent), Color.White, 0.78f),
    onPrimaryContainer = blendColors(normalizeAlbumAccent(rawAccent), Color.Black, 0.24f),
    secondary = shiftedAccent(normalizeAlbumAccent(rawAccent), hueShift = 24f, saturationMultiplier = 0.7f),
    onSecondary = Color.White,
    secondaryContainer = blendColors(shiftedAccent(normalizeAlbumAccent(rawAccent), 24f, 0.7f), Color.White, 0.78f),
    onSecondaryContainer = Color(0xFF241B20),
    tertiary = shiftedAccent(normalizeAlbumAccent(rawAccent), hueShift = -42f, saturationMultiplier = 0.78f),
    onTertiary = Color.White,
    tertiaryContainer = blendColors(shiftedAccent(normalizeAlbumAccent(rawAccent), -42f, 0.78f), Color.White, 0.78f),
    onTertiaryContainer = Color(0xFF201C10),
    background = blendColors(normalizeAlbumAccent(rawAccent), Color.White, 0.94f),
    onBackground = Color(0xFF191C1B),
    surface = blendColors(normalizeAlbumAccent(rawAccent), Color.White, 0.96f),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = blendColors(normalizeAlbumAccent(rawAccent), Color.White, 0.84f),
    onSurfaceVariant = Color(0xFF434844),
    outline = blendColors(normalizeAlbumAccent(rawAccent), Color(0xFF747974), 0.54f),
    inverseSurface = Color(0xFF2D312F),
    inverseOnSurface = Color(0xFFEFF1EE),
    inversePrimary = blendColors(normalizeAlbumAccent(rawAccent), Color.White, 0.42f)
)

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
    var tracks by remember { mutableStateOf(emptyList<MusicTrack>()) }
    var selectedTab by remember { mutableStateOf(LibraryTab.Songs) }
    var searchQuery by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf<String?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var playQueue by remember { mutableStateOf(emptyList<MusicTrack>()) }
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatEnabled by remember { mutableStateOf(false) }
    var useAlbumArtColors by remember { mutableStateOf(loadUseAlbumArtColors(context)) }
    var selectedDestination by remember { mutableStateOf(AppDestination.Home) }

    fun runTask(task: () -> Unit) {
        if (!isBusy) {
            isBusy = true
            thread(name = "jellyfin-task") { task() }
        }
    }

    fun loadLibrary(activeSession: JellyfinSession) {
        runTask {
            val result = runCatching { repository.fetchTracks(activeSession) }
            mainHandler.post {
                isBusy = false
                result
                    .onSuccess { loadedTracks ->
                        tracks = loadedTracks
                        statusText = if (loadedTracks.isEmpty()) "No music found" else null
                    }
                    .onFailure { statusText = it.readableMessage() }
            }
        }
    }

    fun connect() {
        if (serverUrl.isBlank() || username.isBlank()) {
            statusText = "Server URL and username are required"
            return
        }

        runTask {
            val result = runCatching {
                val activeSession = repository.login(serverUrl, username, password)
                saveSession(context, activeSession)
                activeSession to repository.fetchTracks(activeSession)
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
                    .onFailure { statusText = it.readableMessage() }
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

    JellyfinMusicTheme(albumAccentColor = albumAccentColor) {
        Scaffold(
        topBar = {
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
                    val subtitle = when {
                        showPlayer -> player.currentTrack?.artist
                        session == null -> "Not connected"
                        selectedDestination == AppDestination.Profile -> "Settings"
                        else -> null
                    }
                    Column {
                        Text(
                            text = if (showPlayer) "Now Playing" else "Jellyfin Music",
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
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
                                    selectedTab = LibraryTab.Albums
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
                        AboutCard()
                    }
                } else {
                    when (selectedDestination) {
                        AppDestination.Profile -> {
                            item {
                                AccountCard(
                                    session = connectedSession,
                                    isBusy = isBusy,
                                    onRefresh = { loadLibrary(connectedSession) },
                                    onSignOut = ::signOut
                                )
                            }
                            item {
                                AppearanceCard(
                                    useAlbumArtColors = useAlbumArtColors,
                                    currentTrack = player.currentTrack,
                                    albumAccentColor = albumAccentColor,
                                    onUseAlbumArtColorsChange = { enabled ->
                                        useAlbumArtColors = enabled
                                        saveUseAlbumArtColors(context, enabled)
                                    }
                                )
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
                            val isSearchTab = selectedDestination == AppDestination.Search
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
                                        item { EmptyLibraryMessage(isBusy = isBusy) }
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
                                        item { EmptyLibraryMessage(isBusy = isBusy) }
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
                                        item { EmptyLibraryMessage(isBusy = isBusy) }
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
private fun AboutCard() {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Jellyfin Music",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Author: Corry Holt",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "corryrholt@gamil.com",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = session.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = session.serverUrl.toHostLabel(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = onRefresh, enabled = !isBusy) {
                    Text("Refresh")
                }
                TextButton(onClick = onSignOut) {
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
private fun AppearanceCard(
    useAlbumArtColors: Boolean,
    currentTrack: MusicTrack?,
    albumAccentColor: Color?,
    onUseAlbumArtColorsChange: (Boolean) -> Unit
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
                text = "Appearance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
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
        color = MaterialTheme.colorScheme.inverseSurface,
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
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                tint = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.78f),
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
        AppDestination.Library -> Icons.Filled.Favorite
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
        modifier = modifier.clip(RoundedCornerShape(14.dp))
    )
}

@Composable
private fun AlbumArtworkImage(
    track: MusicTrack?,
    session: JellyfinSession?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val imageUrl = track?.imageUrl(session)
    val bitmap by rememberAlbumBitmap(imageUrl, session?.token)
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
private fun rememberAlbumBitmap(imageUrl: String?, token: String?): State<Bitmap?> {
    var bitmap by remember(imageUrl) {
        mutableStateOf(imageUrl?.let { AlbumArtCache[it] })
    }

    LaunchedEffect(imageUrl, token) {
        if (imageUrl == null) {
            bitmap = null
            return@LaunchedEffect
        }
        bitmap = AlbumArtCache[imageUrl] ?: loadAlbumBitmap(imageUrl, token)
    }

    return rememberUpdatedState(bitmap)
}

private suspend fun loadAlbumBitmap(imageUrl: String, token: String?): Bitmap? = withContext(Dispatchers.IO) {
    AlbumArtCache[imageUrl]?.let { return@withContext it }
    runCatching {
        val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 18_000
            setRequestProperty("Accept", "image/*")
            token?.let { setRequestProperty("X-Emby-Token", it) }
        }
        try {
            if (connection.responseCode !in 200..299) {
                null
            } else {
                connection.inputStream.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()?.also { AlbumArtCache[imageUrl] = it }
}

@Composable
private fun EmptyLibraryMessage(isBusy: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Text(
            text = if (isBusy) "Loading library" else "No matching music",
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    var startsInTopZone = false
    var totalDragX = 0f
    var totalDragY = 0f

    detectDragGestures(
        onDragStart = { offset ->
            startsInTopZone = offset.y <= startZonePx
            totalDragX = 0f
            totalDragY = 0f
        },
        onDragEnd = {
            if (
                startsInTopZone &&
                totalDragY > dismissDistancePx &&
                totalDragY > abs(totalDragX) * 1.4f
            ) {
                onDismiss()
            }
            startsInTopZone = false
        },
        onDragCancel = {
            startsInTopZone = false
        },
        onDrag = { change, dragAmount ->
            if (startsInTopZone) {
                totalDragX += dragAmount.x
                totalDragY += dragAmount.y
                if (totalDragY > 0f) {
                    change.consume()
                }
            }
        }
    )
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
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onQueueTrackClick: (MusicTrack) -> Unit,
    onQueueMove: (Int, Int) -> Unit,
    onQueuePlayNext: (MusicTrack) -> Unit,
    onDismiss: () -> Unit
) {
    var showQueue by remember { mutableStateOf(false) }
    val displayQueue = queue.ifEmpty { listOf(track) }

    Box(
        modifier = modifier
            .fillMaxSize()
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
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundedPlaybackButton(
                    icon = PlayerGlyph.Shuffle,
                    contentDescription = if (shuffleEnabled) "Shuffle on" else "Shuffle",
                    active = shuffleEnabled,
                    onClick = onToggleShuffle
                )
                RoundedPlaybackButton(
                    icon = PlayerGlyph.Previous,
                    contentDescription = "Previous track",
                    onClick = onPrevious
                )
                RoundedPlaybackButton(
                    icon = if (isPlaying) PlayerGlyph.Pause else PlayerGlyph.Play,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    onClick = if (!isPlaying && status == "Ended") onReplay else onToggle,
                    prominent = true,
                    modifier = Modifier.size(70.dp)
                )
                RoundedPlaybackButton(
                    icon = PlayerGlyph.Next,
                    contentDescription = "Next track",
                    onClick = onNext
                )
                RoundedPlaybackButton(
                    icon = PlayerGlyph.Repeat,
                    contentDescription = if (repeatEnabled) "Repeat on" else "Repeat",
                    active = repeatEnabled,
                    onClick = onToggleRepeat
                )
            }
            Spacer(Modifier.height(12.dp))
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
            .size(if (prominent) 64.dp else 52.dp)
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
    val transition = rememberInfiniteTransition(label = "disc-spin")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc-rotation"
    )
    val latestProgress by rememberUpdatedState(progress)
    val latestOnSeek by rememberUpdatedState(onSeek)
    val latestOnScratch by rememberUpdatedState(onScratch)
    val latestOnScratchEnd by rememberUpdatedState(onScratchEnd)
    var isScratching by remember { mutableStateOf(false) }
    var scratchProgress by remember { mutableFloatStateOf(progress) }
    var scratchRotation by remember { mutableFloatStateOf(0f) }
    val stageProgress = if (isScratching) scratchProgress else progress
    val discRotation = scratchRotation + if (isPlaying && !isScratching) rotation else 0f

    LaunchedEffect(progress, isScratching) {
        if (!isScratching) {
            scratchProgress = progress
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
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 38.dp, end = 38.dp)
                .size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            tonalElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
        VinylDisc(
            track = track,
            session = session,
            modifier = Modifier
                .fillMaxSize(0.74f)
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

                            scratchRotation += deltaAngle
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
            modifier = Modifier.fillMaxSize(0.86f)
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
        val pivot = Offset(size.width * 0.18f, size.height * 0.16f)
        val elbow = Offset(size.width * (0.16f + p * 0.025f), size.height * (0.34f + p * 0.02f))
        val stylus = Offset(size.width * (0.24f + p * 0.08f), size.height * (0.56f - p * 0.035f))
        val armPath = Path().apply {
            moveTo(pivot.x, pivot.y)
            cubicTo(
                size.width * 0.12f,
                size.height * 0.24f,
                elbow.x,
                elbow.y,
                stylus.x,
                stylus.y
            )
        }
        drawPath(
            path = armPath,
            color = Color.Black.copy(alpha = 0.2f),
            style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = armPath,
            color = colorScheme.surface.copy(alpha = 0.94f),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = armPath,
            color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        val cartridge = Path().apply {
            moveTo(stylus.x - size.width * 0.018f, stylus.y - size.height * 0.045f)
            lineTo(stylus.x + size.width * 0.035f, stylus.y - size.height * 0.026f)
            lineTo(stylus.x + size.width * 0.023f, stylus.y + size.height * 0.036f)
            lineTo(stylus.x - size.width * 0.03f, stylus.y + size.height * 0.022f)
            close()
        }
        drawPath(path = cartridge, color = Color.Black.copy(alpha = 0.22f))
        drawPath(path = cartridge, color = colorScheme.onSurfaceVariant.copy(alpha = 0.68f))
        drawCircle(
            color = accent,
            radius = 19.dp.toPx(),
            center = pivot
        )
        drawCircle(
            color = colorScheme.surface,
            radius = 12.dp.toPx(),
            center = pivot
        )
        drawCircle(
            color = accent.copy(alpha = 0.88f),
            radius = 6.dp.toPx(),
            center = pivot
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
    val labelColor = colorScheme.primary
    val labelDark = colorScheme.primaryContainer
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.18f))
    ) {
        AlbumArtworkImage(
            track = track,
            session = session,
            modifier = Modifier.fillMaxSize().clip(CircleShape)
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tint.copy(alpha = 0.2f),
                        Color(0xFFEBD7FF).copy(alpha = 0.14f),
                        Color(0xFF4AD9C7).copy(alpha = 0.1f),
                        tint.copy(alpha = 0.18f)
                    ),
                    center = Offset(size.width * 0.31f, size.height * 0.24f),
                    radius = radius * 1.24f
                ),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = radius * 0.34f,
                center = Offset(size.width * 0.36f, size.height * 0.3f)
            )
            drawArc(
                color = tint.copy(alpha = 0.18f),
                startAngle = 206f,
                sweepAngle = 112f,
                useCenter = true,
                topLeft = Offset.Zero,
                size = Size(size.minDimension, size.minDimension)
            )
            drawArc(
                color = Color(0xFF6CE5DC).copy(alpha = 0.12f),
                startAngle = 36f,
                sweepAngle = 98f,
                useCenter = true,
                topLeft = Offset.Zero,
                size = Size(size.minDimension, size.minDimension)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF141219).copy(alpha = 0.2f),
                        Color(0xFF211B25).copy(alpha = 0.28f),
                        Color(0xFF0E0D12).copy(alpha = 0.38f)
                    ),
                    center = Offset(size.width * 0.34f, size.height * 0.24f),
                    radius = radius * 1.18f
                ),
                radius = radius,
                center = center
            )
            drawArc(
                color = Color.White.copy(alpha = 0.18f),
                startAngle = -33f,
                sweepAngle = 47f,
                useCenter = true,
                topLeft = Offset.Zero,
                size = Size(size.minDimension, size.minDimension)
            )
            for (index in 0..16) {
                drawCircle(
                    color = Color.White.copy(alpha = if (index % 4 == 0) 0.2f else 0.085f),
                    radius = radius * (0.28f + index * 0.038f),
                    center = center,
                    style = Stroke(width = if (index % 4 == 0) 1.dp.toPx() else 0.65.dp.toPx())
                )
            }
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = radius * 0.82f,
                center = center,
                style = Stroke(width = 1.4.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.13f),
                radius = radius * 0.62f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = radius * 0.43f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.22f),
                radius = radius * 0.27f,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(labelColor, labelDark),
                    center = center,
                    radius = radius * 0.28f
                ),
                radius = radius * 0.24f,
                center = center
            )
            for (index in 0 until 6) {
                val angle = (index * 60f + 22f) * PI.toFloat() / 180f
                drawLine(
                    color = labelDark.copy(alpha = 0.72f),
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
                radius = radius * 0.03f,
                center = center
            )
            drawCircle(
                color = labelDark,
                radius = radius * 0.015f,
                center = center
            )
        }
    }
}

@Composable
private fun AudioBarsVisualizer(
    modifier: Modifier = Modifier,
    color: Color,
    active: Boolean,
    levels: FloatArray
) {
    val transition = rememberInfiniteTransition(label = "bars")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (active) 900 else 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bars-phase"
    )
    val hasLiveLevels = active && levels.any { it > 0.035f }

    Canvas(modifier = modifier) {
        val barCount = 28
        val gap = size.width / (barCount * 2.05f)
        val strokeWidth = gap.coerceAtLeast(2.5f)
        val step = size.width / barCount
        val baseline = size.height * 0.86f
        for (index in 0 until barCount) {
            val fallbackWave = (sin(phase + index * 0.63f) + 1f) / 2f
            val stable = ((index * 37) % 9) / 10f
            val level = when {
                hasLiveLevels -> levels.getOrElse(index) { 0f }
                active -> fallbackWave * 0.72f
                else -> stable * 0.42f
            }.coerceIn(0f, 1f)
            val height = size.height * (0.16f + level * 0.76f)
            val x = step * index + step / 2f
            drawLine(
                color = color.copy(alpha = 0.26f + level * 0.66f),
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
            inactiveTrackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f)
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
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
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

    fun play(track: MusicTrack, session: JellyfinSession) {
        releasePlayer()
        currentTrack = track
        status = "Buffering"
        progress = 0f
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
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
                context,
                Uri.parse(track.streamUrl(session)),
                mapOf("X-Emby-Token" to session.token)
            )
            nextPlayer.setOnPreparedListener {
                it.start()
                isPlaying = true
                status = "Playing"
                attachVisualizer(it.audioSessionId)
                syncProgress()
                saveWidgetState(context, track, status, progress)
            }
            nextPlayer.setOnCompletionListener {
                visualizer?.runCatching { enabled = false }
                isPlaying = false
                status = "Ended"
                progress = 1f
                visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
                saveWidgetState(context, track, status, progress)
            }
            nextPlayer.setOnErrorListener { _, _, _ ->
                visualizer?.runCatching { enabled = false }
                isPlaying = false
                status = "Playback error"
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
            isPlaying = false
            status = "Paused"
            saveWidgetState(context, currentTrack, status, progress)
        } else {
            activePlayer.start()
            visualizer?.runCatching { enabled = true }
            isPlaying = true
            status = "Playing"
            saveWidgetState(context, currentTrack, status, progress)
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
        releaseVisualizer()
        mediaPlayer?.runCatching {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
        isPlaying = false
        visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
    }

    private fun attachVisualizer(audioSessionId: Int) {
        releaseVisualizer()
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
                            val nextLevels = waveformToBars(data)
                            mainHandler.post { visualizerLevels = nextLevels }
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) = Unit
                    },
                    (Visualizer.getMaxCaptureRate() / 3).coerceAtLeast(1_000),
                    true,
                    false
                )
                enabled = true
            }
            visualizer = nextVisualizer
        }.onFailure {
            visualizer = null
            visualizerLevels = FloatArray(VISUALIZER_BAR_COUNT)
        }
    }

    private fun waveformToBars(waveform: ByteArray): FloatArray {
        if (waveform.isEmpty()) return FloatArray(VISUALIZER_BAR_COUNT)
        val bars = FloatArray(VISUALIZER_BAR_COUNT)
        val samplesPerBar = (waveform.size / VISUALIZER_BAR_COUNT).coerceAtLeast(1)
        for (bar in bars.indices) {
            val start = bar * samplesPerBar
            val end = (start + samplesPerBar).coerceAtMost(waveform.size)
            var sum = 0f
            for (index in start until end) {
                val sample = (waveform[index].toInt() and 0xFF) - 128
                sum += abs(sample) / 128f
            }
            val average = sum / (end - start).coerceAtLeast(1)
            bars[bar] = average.coerceIn(0f, 1f)
        }
        return bars
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

    fun fetchTracks(session: JellyfinSession): List<MusicTrack> {
        val url = buildString {
            append(session.serverUrl)
            append("/Users/")
            append(encode(session.userId))
            append("/Items?Recursive=true")
            append("&IncludeItemTypes=Audio")
            append("&SortBy=SortName")
            append("&SortOrder=Ascending")
            append("&Fields=Album,AlbumId,AlbumPrimaryImageTag,Artists,ImageTags,PrimaryImageItemId,RunTimeTicks")
            append("&Limit=200")
        }
        val response = request(url = url, method = "GET", body = null, session = session)
        val items = JSONObject(response).optJSONArray("Items") ?: JSONArray()
        return buildList {
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
        }
    }

    private fun request(
        url: String,
        method: String,
        body: String?,
        session: JellyfinSession?
    ): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 12_000
            readTimeout = 20_000
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
        else -> message?.takeIf { it.isNotBlank() } ?: "Something went wrong"
    }

private fun encode(value: String): String =
    URLEncoder.encode(value, Charsets.UTF_8.name())
