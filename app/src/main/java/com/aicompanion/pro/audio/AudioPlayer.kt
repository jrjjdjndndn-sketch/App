package com.aicompanion.pro.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

class AudioPlayer {
    private val sr = 24_000
    private val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(sr)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(
            AudioTrack.getMinBufferSize(
                sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            ) * 4
        )
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    val speaking = MutableStateFlow(false)
    private val queue = Channel<ByteArray>(Channel.UNLIMITED)

    init {
        track.play()
        Thread({
            while (true) {
                val chunk = runCatching { runBlocking { queue.receive() } }.getOrNull() ?: break
                speaking.value = true
                var off = 0
                while (off < chunk.size) {
                    off += track.write(chunk, off, chunk.size - off)
                }
                if (queue.isEmpty) speaking.value = false
            }
        }, "audio-player").start()
    }

    fun enqueue(pcm: ByteArray) { queue.trySend(pcm) }

    fun flush() {
        track.pause()
        track.flush()
        track.play()
        speaking.value = false
    }

    fun release() {
        track.stop()
        track.release()
        queue.close()
    }
}
