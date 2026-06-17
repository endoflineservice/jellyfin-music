package dev.cholt.jellyfinmusic

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random
import ir.mahozad.multiplatform.wavyslider.WaveDirection.HEAD
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
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
    val tint: Color
) {
    fun streamUrl(session: JellyfinSession): String {
        val encodedId = encode(id)
        val encodedToken = encode(session.token)
        return "${session.serverUrl}/Audio/$encodedId/stream?Static=true&api_key=$encodedToken"
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

private data class LibraryGroup(
    val title: String,
    val subtitle: String,
    val tint: Color,
    val tracks: List<MusicTrack>
)

@Composable
private fun JellyfinMusicTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JellyfinMusicApp() {
    val context = LocalContext.current
    val repository = remember { JellyfinRepository(context.applicationContext) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val player = remember { JellyfinPlayer(context.applicationContext) }

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
    var shuffleEnabled by remember { mutableStateOf(false) }
    var repeatEnabled by remember { mutableStateOf(false) }
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
        statusText = null
        showPlayer = false
        selectedDestination = AppDestination.Home
    }

    fun playTrack(track: MusicTrack, openPlayer: Boolean = false) {
        session?.let { activeSession ->
            player.play(track, activeSession)
            if (openPlayer) {
                selectedDestination = AppDestination.Player
                showPlayer = true
            }
        }
    }

    fun playAdjacent(offset: Int) {
        val activeTrack = player.currentTrack ?: return
        if (tracks.isEmpty()) return
        val index = tracks.indexOfFirst { it.id == activeTrack.id }.takeIf { it >= 0 } ?: return
        val nextIndex = if (shuffleEnabled && tracks.size > 1) {
            (index + Random.nextInt(1, tracks.size)) % tracks.size
        } else {
            (index + offset + tracks.size) % tracks.size
        }
        playTrack(tracks[nextIndex], openPlayer = true)
    }

    DisposableEffect(Unit) {
        onDispose { player.release() }
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

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (showPlayer) {
                        TextButton(onClick = {
                            showPlayer = false
                            selectedDestination = AppDestination.Home
                        }) {
                            Text("<")
                        }
                    }
                },
                title = {
                    Column {
                        Text(
                            text = if (showPlayer) "Now Playing" else "Jellyfin Music",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = session?.serverUrl?.toHostLabel() ?: "Not connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    TopBarSineVisualizer(
                        modifier = Modifier.padding(end = 12.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
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
                status = player.status,
                modifier = Modifier.padding(innerPadding),
                onToggle = { player.toggle() },
                onSeek = { player.seekToFraction(it) },
                onPrevious = { playAdjacent(-1) },
                onNext = { playAdjacent(1) },
                onReplay = { session?.let { player.play(activeTrack, it) } },
                shuffleEnabled = shuffleEnabled,
                repeatEnabled = repeatEnabled,
                onToggleShuffle = { shuffleEnabled = !shuffleEnabled },
                onToggleRepeat = { repeatEnabled = !repeatEnabled }
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
                                AboutCard()
                            }
                        }

                        AppDestination.Player -> {
                            item {
                                EmptyPlayerCard()
                            }
                        }

                        else -> {
                            item {
                                LibraryHeader(
                                    searchQuery = searchQuery,
                                    isBusy = isBusy,
                                    statusText = statusText,
                                    onSearchQueryChange = { searchQuery = it },
                                    onRefresh = { loadLibrary(connectedSession) },
                                    onSignOut = ::signOut
                                )
                            }
                            item {
                                LibraryTabs(
                                    selectedTab = selectedTab,
                                    onTabSelected = { selectedTab = it }
                                )
                            }

                            val filteredTracks = tracks.filterBy(searchQuery)
                            when (selectedTab) {
                                LibraryTab.Songs -> {
                                    if (filteredTracks.isEmpty()) {
                                        item { EmptyLibraryMessage(isBusy = isBusy) }
                                    } else {
                                        items(filteredTracks, key = { it.id }) { track ->
                                            TrackRow(
                                                track = track,
                                                isCurrent = player.currentTrack?.id == track.id,
                                                onClick = { playTrack(track, openPlayer = true) }
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
                                                onClick = { playTrack(group.tracks.first(), openPlayer = true) }
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
                                                onClick = { playTrack(group.tracks.first(), openPlayer = true) }
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
            AppDestination.entries.forEach { destination ->
                BottomTabItem(
                    destination = destination,
                    selected = selectedDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    modifier = Modifier.weight(1f)
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
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = destinationIcon(destination),
                        contentDescription = destination.label,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = destination.label,
                        style = MaterialTheme.typography.labelMedium,
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
    searchQuery: String,
    isBusy: Boolean,
    statusText: String?,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Library",
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
            TextButton(onClick = onSignOut) {
                Text("Sign out")
            }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Search music") }
        )
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
private fun TrackRow(track: MusicTrack, isCurrent: Boolean, onClick: () -> Unit) {
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
            AlbumTile(tint = track.tint, modifier = Modifier.size(48.dp))
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
private fun GroupRow(group: LibraryGroup, onClick: () -> Unit) {
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
            AlbumTile(tint = group.tint, modifier = Modifier.size(48.dp))
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
private fun AlbumTile(tint: Color, modifier: Modifier = Modifier) {
    val centerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
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

@Composable
private fun FullPlayerScreen(
    track: MusicTrack,
    isPlaying: Boolean,
    progress: Float,
    status: String,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit,
    onSeek: (Float) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onReplay: () -> Unit,
    shuffleEnabled: Boolean,
    repeatEnabled: Boolean,
            onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LargeAlbumStage(
            track = track,
            isPlaying = isPlaying,
            onToggle = onToggle
        )
        Spacer(Modifier.height(22.dp))
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
        Spacer(Modifier.height(20.dp))
        AudioBarsVisualizer(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            color = MaterialTheme.colorScheme.primary,
            active = isPlaying
        )
        Spacer(Modifier.height(8.dp))
        WavySeekBar(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
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
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onToggleShuffle) {
                Text(if (shuffleEnabled) "Mix on" else "Mix")
            }
            FilledTonalButton(onClick = onPrevious, shape = RoundedCornerShape(18.dp)) {
                Text("|<")
            }
            Button(
                onClick = if (!isPlaying && status == "Ended") onReplay else onToggle,
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = 26.dp, vertical = 14.dp)
            ) {
                Text(if (isPlaying) "Pause" else "Play")
            }
            FilledTonalButton(onClick = onNext, shape = RoundedCornerShape(18.dp)) {
                Text(">|")
            }
            TextButton(onClick = onToggleRepeat) {
                Text(if (repeatEnabled) "Loop on" else "Loop")
            }
        }
    }
}

@Composable
private fun LargeAlbumStage(track: MusicTrack, isPlaying: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(34.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center
    ) {
        AlbumBackdrop(tint = track.tint, modifier = Modifier.fillMaxSize())
        AudioBarsVisualizer(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(86.dp)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
            active = isPlaying
        )
        FilledTonalButton(
            onClick = onToggle,
            modifier = Modifier.size(76.dp),
            shape = RoundedCornerShape(38.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(if (isPlaying) "II" else ">", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun AlbumBackdrop(tint: Color, modifier: Modifier = Modifier) {
    val surface = MaterialTheme.colorScheme.surface
    Canvas(modifier = modifier.background(tint.copy(alpha = 0.22f))) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(tint.copy(alpha = 0.92f), surface.copy(alpha = 0.18f)),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.16f),
            radius = size.minDimension * 0.46f,
            center = Offset(size.width * 0.78f, size.height * 0.22f)
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.12f),
            radius = size.minDimension * 0.58f,
            center = Offset(size.width * 0.18f, size.height * 0.92f)
        )
    }
}

@Composable
private fun AudioBarsVisualizer(modifier: Modifier = Modifier, color: Color, active: Boolean) {
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

    Canvas(modifier = modifier) {
        val barCount = 30
        val gap = size.width / (barCount * 1.8f)
        val strokeWidth = gap.coerceAtLeast(3f)
        val step = size.width / barCount
        val baseline = size.height * 0.88f
        for (index in 0 until barCount) {
            val wave = (sin(phase + index * 0.63f) + 1f) / 2f
            val stable = ((index * 37) % 9) / 10f
            val height = size.height * (0.18f + (if (active) wave else stable) * 0.68f)
            val x = step * index + step / 2f
            drawLine(
                color = color.copy(alpha = 0.28f + wave * 0.62f),
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
            .padding(horizontal = 12.dp, vertical = 6.dp),
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
                AlbumTile(tint = track.tint, modifier = Modifier.size(64.dp))
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
                TextButton(onClick = onPrevious) { Text("|<") }
                FilledTonalButton(
                    onClick = onToggle,
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp)
                ) {
                    Text(if (isPlaying) "Pause" else "Play")
                }
                TextButton(onClick = onNext) { Text(">|") }
            }
            if (!isPlaying && status == "Ended") {
                TextButton(onClick = onReplay, modifier = Modifier.align(Alignment.End)) {
                    Text("Replay")
                }
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
        thumb = {}
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

    private var mediaPlayer: MediaPlayer? = null

    fun play(track: MusicTrack, session: JellyfinSession) {
        releasePlayer()
        currentTrack = track
        status = "Buffering"
        progress = 0f
        saveWidgetState(context, track, status, progress)

        val nextPlayer = MediaPlayer()
        mediaPlayer = nextPlayer
        runCatching {
            nextPlayer.setDataSource(
                context,
                Uri.parse(track.streamUrl(session)),
                mapOf("X-Emby-Token" to session.token)
            )
            nextPlayer.setOnPreparedListener {
                it.start()
                isPlaying = true
                status = "Playing"
                syncProgress()
                saveWidgetState(context, track, status, progress)
            }
            nextPlayer.setOnCompletionListener {
                isPlaying = false
                status = "Ended"
                progress = 1f
                saveWidgetState(context, track, status, progress)
            }
            nextPlayer.setOnErrorListener { _, _, _ ->
                isPlaying = false
                status = "Playback error"
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
            isPlaying = false
            status = "Paused"
            saveWidgetState(context, currentTrack, status, progress)
        } else {
            activePlayer.start()
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
        mediaPlayer?.runCatching {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
        isPlaying = false
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
            append("&Fields=Album,Artists,RunTimeTicks")
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
                add(
                    MusicTrack(
                        id = id,
                        title = item.optString("Name", "Untitled"),
                        artist = artist,
                        album = item.optString("Album", "Unknown album").ifBlank { "Unknown album" },
                        durationMs = item.optLong("RunTimeTicks", 0L) / 10_000L,
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
        .clear()
        .apply()
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
