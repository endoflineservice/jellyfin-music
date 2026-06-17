package dev.cholt.jellyfinmusic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JellyfinMusicTheme {
                JellyfinMusicApp()
            }
        }
    }
}

private val Ink = Color(0xFF171513)
private val Paper = Color(0xFFF6F8F7)
private val SurfaceWarm = Color(0xFFFFFFFF)
private val Accent = Color(0xFF1F6F64)
private val AccentSoft = Color(0xFFDDEEEA)
private val Clay = Color(0xFFE8795F)
private val Slate = Color(0xFF66768C)

private data class Track(
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val tint: Color
)

private val tracks = listOf(
    Track("Night Drive", "Local Library", "Soft Signals", "3:42", Accent),
    Track("Quiet Current", "Jellyfin Mix", "Clean Room", "4:08", Slate),
    Track("Round Corners", "Design Notes", "Material Sketches", "2:57", Clay),
    Track("Offline Cache", "Server Room", "Low Bandwidth", "5:14", Color(0xFF8B6FB2)),
    Track("Gapless Morning", "Playback Lab", "Album Flow", "3:25", Color(0xFF4E8BA6))
)

@Composable
private fun JellyfinMusicTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            secondary = Clay,
            background = Paper,
            surface = SurfaceWarm,
            onPrimary = Color.White,
            onSecondary = Ink,
            onBackground = Ink,
            onSurface = Ink
        ),
        content = content
    )
}

@Composable
private fun JellyfinMusicApp() {
    var selectedTrack by remember { mutableStateOf(tracks.first()) }
    var progress by remember { mutableFloatStateOf(0.38f) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Paper
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Header()
            Spacer(Modifier.height(18.dp))
            ServerCard()
            Spacer(Modifier.height(18.dp))
            SectionTabs()
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tracks) { track ->
                    TrackRow(
                        track = track,
                        selected = track == selectedTrack,
                        onClick = {
                            selectedTrack = track
                            progress = 0.18f + tracks.indexOf(track) * 0.12f
                        }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            MiniPlayer(
                track = selectedTrack,
                progress = progress.coerceIn(0f, 1f),
                onProgressBump = { progress = (progress + 0.08f).coerceAtMost(0.95f) }
            )
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Jellyfin Music",
                color = Ink,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp
            )
            Text(
                text = "A clean Jellyfin music player prototype",
                color = Ink.copy(alpha = 0.62f),
                fontSize = 14.sp
            )
        }
        TopBarSineVisualizer()
    }
}

@Composable
private fun TopBarSineVisualizer() {
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
        modifier = Modifier
            .width(76.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Accent)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        val centerY = size.height / 2f
        val amplitude = size.height * 0.26f
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
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ServerCard() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWarm),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("Server", color = Ink.copy(alpha = 0.58f), fontSize = 13.sp)
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Connect Jellyfin",
                color = Ink,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Next step: wire this card to the Jellyfin Kotlin SDK for login, library sync, and stream URLs.",
                color = Ink.copy(alpha = 0.68f),
                fontSize = 14.sp,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("Add server")
            }
        }
    }
}

@Composable
private fun SectionTabs() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Chip("Albums", true)
        Chip("Artists", false)
        Chip("Songs", false)
    }
}

@Composable
private fun Chip(label: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Ink else SurfaceWarm)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Ink.copy(alpha = 0.72f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TrackRow(track: Track, selected: Boolean, onClick: () -> Unit) {
    val container = if (selected) AccentSoft else SurfaceWarm
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumTile(track.tint)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Ink,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${track.artist} - ${track.album}",
                color = Ink.copy(alpha = 0.58f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(track.duration, color = Ink.copy(alpha = 0.52f), fontSize = 13.sp)
    }
}

@Composable
private fun AlbumTile(tint: Color) {
    Canvas(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(tint.copy(alpha = 0.22f))
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(tint.copy(alpha = 0.95f), tint.copy(alpha = 0.28f)),
                center = Offset(size.width * 0.34f, size.height * 0.34f),
                radius = size.minDimension
            ),
            radius = size.minDimension * 0.55f,
            center = Offset(size.width * 0.48f, size.height * 0.48f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = size.minDimension * 0.12f,
            center = center
        )
    }
}

@Composable
private fun MiniPlayer(track: Track, progress: Float, onProgressBump: () -> Unit) {
    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Ink),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlbumTile(track.tint)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = onProgressBump,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("Play", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(14.dp))
            WavyProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp),
                progressColor = Clay,
                trackColor = Color.White.copy(alpha = 0.18f)
            )
        }
    }
}

@Composable
private fun WavyProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    progressColor: Color,
    trackColor: Color
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val strokeWidth = 7.dp.toPx()
        val centerY = size.height / 2f
        val amplitude = size.height * 0.18f
        val wavelength = size.width / 4.2f
        val endX = size.width * progress.coerceIn(0f, 1f)

        fun buildPath(toX: Float, animated: Boolean): Path {
            val path = Path()
            var x = 0f
            path.moveTo(0f, centerY)
            while (x <= toX) {
                val localAmp = if (animated) amplitude else amplitude * 0.45f
                val y = centerY + sin((x / wavelength) * PI.toFloat() * 2f + if (animated) phase else 0f) * localAmp
                path.lineTo(x, y)
                x += 4f
            }
            path.lineTo(toX, centerY)
            return path
        }

        drawPath(
            path = buildPath(size.width, animated = false),
            color = trackColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawPath(
            path = buildPath(endX, animated = true),
            color = progressColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
