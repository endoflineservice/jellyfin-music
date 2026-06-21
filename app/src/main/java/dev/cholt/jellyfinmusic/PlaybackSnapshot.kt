package dev.cholt.jellyfinmusic

import android.os.SystemClock

data class PlaybackSnapshot(
    val track: MusicTrack? = null,
    val session: JellyfinSession? = null,
    val isPlaying: Boolean = false,
    val status: String = "Ready",
    val progress: Float = 0f,
    val positionMs: Long = 0L,
    val durationMs: Long = track?.durationMs ?: 0L,
    val updatedAtMs: Long = SystemClock.elapsedRealtime()
) {
    val hasTrack: Boolean
        get() = track != null && session != null

    val isBuffering: Boolean
        get() = status == "Buffering" || status.startsWith("Retrying", ignoreCase = true)

    val isEnded: Boolean
        get() = status == "Ended"

    val displayProgress: Float
        get() = if (durationMs > 0L) {
            livePositionMs.toFloat() / durationMs.toFloat()
        } else {
            progress
        }.coerceIn(0f, 1f)

    val livePositionMs: Long
        get() {
            val boundedPosition = positionMs.coerceAtLeast(0L)
            if (!isPlaying || durationMs <= 0L) {
                return boundedPosition.coerceAtMost(durationMs.coerceAtLeast(0L))
            }
            val elapsedMs = (SystemClock.elapsedRealtime() - updatedAtMs).coerceAtLeast(0L)
            return (boundedPosition + elapsedMs).coerceIn(0L, durationMs)
        }

    companion object {
        fun empty(status: String = "Ready"): PlaybackSnapshot =
            PlaybackSnapshot(status = status)
    }
}
