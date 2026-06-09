package com.aicompanion.pro.audio

import android.Manifest
import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class MicCapturer {
    companion object {
        const val SR = 16_000
        const val FRAME = 512
    }

    data class Frame(val pcm16le: ByteArray, val floats: FloatArray)

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun stream(): Flow<Frame> = flow {
        val min = AudioRecord.getMinBufferSize(
            SR, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val rec = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION, SR,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            (min * 4).coerceAtLeast(FRAME * 8)
        )
        val short = ShortArray(FRAME)
        try {
            rec.startRecording()
            while (true) {
                var read = 0
                while (read < FRAME) {
                    val n = rec.read(short, read, FRAME - read)
                    if (n <= 0) break
                    read += n
                }
                if (read < FRAME) continue
                val bytes = ByteArray(FRAME * 2)
                val floats = FloatArray(FRAME)
                for (i in 0 until FRAME) {
                    val s = short[i].toInt()
                    bytes[i * 2] = (s and 0xFF).toByte()
                    bytes[i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
                    floats[i] = s / 32768f
                }
                emit(Frame(bytes, floats))
            }
        } finally {
            rec.stop()
            rec.release()
        }
    }.flowOn(Dispatchers.IO)
}
