package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

private val Context.audioDataStore: DataStore<Preferences> by preferencesDataStore(name = "solstice_audio_prefs")

enum class SoundProfile(val label: String, val subtitle: String) {
    CRYSTAL_32BIT(
        label = "Cristal 32-Bit (PS1/Saturn)",
        subtitle = "Timbres harmônicos cristalinos com eco astral"
    ),
    RETRO_16BIT(
        label = "Sino Astral 16-Bit",
        subtitle = "Ondas triangulares e quadradas estilo RPG clássico"
    ),
    MINIMAL_ZEN(
        label = "Pulso Discreto",
        subtitle = "Sinais suaves de baixa reverberação para uso rápido"
    )
}

data class AudioSettings(
    val masterMuted: Boolean = false,
    val sfxEnabled: Boolean = true,
    val sfxVolume: Float = 0.75f,
    val ambienceEnabled: Boolean = true,
    val ambienceVolume: Float = 0.45f,
    val hapticsEnabled: Boolean = true,
    val soundProfile: SoundProfile = SoundProfile.CRYSTAL_32BIT
) {
    val effectiveSfxGain: Float
        get() = if (masterMuted || !sfxEnabled) 0f else sfxVolume.coerceIn(0f, 1f)

    val effectiveAmbienceGain: Float
        get() = if (masterMuted || !ambienceEnabled) 0f else ambienceVolume.coerceIn(0f, 1f)
}

class AudioPreferencesStore(private val context: Context) {
    private object Keys {
        val MASTER_MUTED = booleanPreferencesKey("audio_master_muted")
        val SFX_ENABLED = booleanPreferencesKey("audio_sfx_enabled")
        val SFX_VOLUME = floatPreferencesKey("audio_sfx_volume")
        val AMBIENCE_ENABLED = booleanPreferencesKey("audio_ambience_enabled")
        val AMBIENCE_VOLUME = floatPreferencesKey("audio_ambience_volume")
        val HAPTICS_ENABLED = booleanPreferencesKey("audio_haptics_enabled")
        val SOUND_PROFILE = stringPreferencesKey("audio_sound_profile")
    }

    val settingsFlow: Flow<AudioSettings> = context.audioDataStore.data.map { prefs ->
        val profileName = prefs[Keys.SOUND_PROFILE]
        val profile = SoundProfile.entries.firstOrNull { it.name == profileName } ?: SoundProfile.CRYSTAL_32BIT
        AudioSettings(
            masterMuted = prefs[Keys.MASTER_MUTED] ?: false,
            sfxEnabled = prefs[Keys.SFX_ENABLED] ?: true,
            sfxVolume = (prefs[Keys.SFX_VOLUME] ?: 0.75f).coerceIn(0f, 1f),
            ambienceEnabled = prefs[Keys.AMBIENCE_ENABLED] ?: true,
            ambienceVolume = (prefs[Keys.AMBIENCE_VOLUME] ?: 0.45f).coerceIn(0f, 1f),
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            soundProfile = profile
        )
    }

    suspend fun setMasterMuted(muted: Boolean) {
        context.audioDataStore.edit { it[Keys.MASTER_MUTED] = muted }
    }

    suspend fun setSfxEnabled(enabled: Boolean) {
        context.audioDataStore.edit { it[Keys.SFX_ENABLED] = enabled }
    }

    suspend fun setSfxVolume(volume: Float) {
        context.audioDataStore.edit { it[Keys.SFX_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setAmbienceEnabled(enabled: Boolean) {
        context.audioDataStore.edit { it[Keys.AMBIENCE_ENABLED] = enabled }
    }

    suspend fun setAmbienceVolume(volume: Float) {
        context.audioDataStore.edit { it[Keys.AMBIENCE_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.audioDataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setSoundProfile(profile: SoundProfile) {
        context.audioDataStore.edit { it[Keys.SOUND_PROFILE] = profile.name }
    }

    suspend fun resetDefaults() {
        context.audioDataStore.edit { it.clear() }
    }
}

/**
 * Lightweight 32-bit RPG PCM audio synthesizer using Android AudioTrack.
 * Generates clean crystalline chimes and twilight arpeggios in memory without requiring bundled audio blobs.
 */
object RpgAudioEngine {
    private const val SAMPLE_RATE = 44_100
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var activeJob: Job? = null

    fun playUiTap(settings: AudioSettings) {
        val gain = settings.effectiveSfxGain
        if (gain <= 0.01f) return
        val freq = when (settings.soundProfile) {
            SoundProfile.CRYSTAL_32BIT -> 987.77 // B5
            SoundProfile.RETRO_16BIT -> 880.0   // A5
            SoundProfile.MINIMAL_ZEN -> 659.25  // E5
        }
        playNotes(
            notes = listOf(NoteEvent(freqHz = freq, durationMs = 55, gapMs = 0)),
            gain = gain * 0.45f,
            profile = settings.soundProfile
        )
    }

    fun playLevelSwitch(settings: AudioSettings, level: Int) {
        val gain = settings.effectiveSfxGain
        if (gain <= 0.01f) return
        val notes = when (level) {
            1 -> listOf(
                NoteEvent(523.25, 85, 10),
                NoteEvent(659.25, 130, 0)
            )
            2 -> listOf(
                NoteEvent(587.33, 80, 10),
                NoteEvent(739.99, 80, 10),
                NoteEvent(880.00, 150, 0)
            )
            else -> listOf(
                NoteEvent(523.25, 75, 10),
                NoteEvent(659.25, 75, 10),
                NoteEvent(783.99, 75, 10),
                NoteEvent(1046.50, 190, 0)
            )
        }
        playNotes(notes = notes, gain = gain * 0.65f, profile = settings.soundProfile)
    }

    fun playSuccessChime(settings: AudioSettings) {
        val gain = settings.effectiveSfxGain
        if (gain <= 0.01f) return
        val notes = listOf(
            NoteEvent(523.25, 75, 12),
            NoteEvent(659.25, 75, 12),
            NoteEvent(783.99, 75, 12),
            NoteEvent(1046.50, 220, 0)
        )
        playNotes(notes = notes, gain = gain * 0.72f, profile = settings.soundProfile)
    }

    fun playAmbiencePreview(settings: AudioSettings) {
        val gain = settings.effectiveAmbienceGain
        if (gain <= 0.01f) return
        // Twilight Lydian arpeggio (Sea of Stars inspired atmospheric chord)
        val notes = listOf(
            NoteEvent(261.63, 240, 40),
            NoteEvent(329.63, 240, 40),
            NoteEvent(392.00, 240, 40),
            NoteEvent(493.88, 280, 40),
            NoteEvent(587.33, 420, 0)
        )
        playNotes(notes = notes, gain = gain * 0.60f, profile = settings.soundProfile)
    }

    private data class NoteEvent(
        val freqHz: Double,
        val durationMs: Int,
        val gapMs: Int
    )

    private fun playNotes(
        notes: List<NoteEvent>,
        gain: Float,
        profile: SoundProfile
    ) {
        activeJob?.cancel()
        activeJob = scope.launch {
            runCatching {
                val totalMs = notes.sumOf { it.durationMs + it.gapMs }.coerceAtLeast(50)
                val totalSamples = (SAMPLE_RATE * totalMs) / 1000
                val pcm = ShortArray(totalSamples)

                var cursor = 0
                for (note in notes) {
                    val noteSamples = (SAMPLE_RATE * note.durationMs) / 1000
                    val gapSamples = (SAMPLE_RATE * note.gapMs) / 1000
                    val attackSamples = (SAMPLE_RATE * 8) / 1000

                    for (i in 0 until noteSamples) {
                        val idx = cursor + i
                        if (idx >= totalSamples) break
                        val t = i.toDouble() / SAMPLE_RATE.toDouble()
                        val attack = if (i < attackSamples) i.toDouble() / attackSamples.toDouble() else 1.0
                        val decay = exp(-4.2 * (i.toDouble() / noteSamples.coerceAtLeast(1).toDouble()))

                        val wave = when (profile) {
                            SoundProfile.CRYSTAL_32BIT -> {
                                val f1 = sin(2.0 * PI * note.freqHz * t)
                                val f2 = 0.32 * sin(2.0 * PI * (note.freqHz * 2.0) * t)
                                val f3 = 0.14 * sin(2.0 * PI * (note.freqHz * 3.0) * t)
                                (f1 + f2 + f3) / 1.46
                            }
                            SoundProfile.RETRO_16BIT -> {
                                val phase = (note.freqHz * t) % 1.0
                                val tri = 4.0 * kotlin.math.abs(phase - 0.5) - 1.0
                                val sq = if (phase < 0.25) 0.35 else -0.35
                                (0.7 * tri + 0.3 * sq)
                            }
                            SoundProfile.MINIMAL_ZEN -> {
                                sin(2.0 * PI * note.freqHz * t)
                            }
                        }

                        val sample = (wave * attack * decay * gain * 18_000.0)
                            .toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        pcm[idx] = sample.toShort()
                    }
                    cursor += noteSamples + gapSamples
                }

                val bufferBytes = pcm.size * 2
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                try {
                    track.write(pcm, 0, pcm.size)
                    track.play()
                    kotlinx.coroutines.delay(totalMs.toLong() + 60L)
                } finally {
                    runCatching { track.stop() }
                    runCatching { track.release() }
                }
            }
        }
    }
}
