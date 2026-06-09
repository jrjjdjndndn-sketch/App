package com.aicompanion.pro.audio

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import java.nio.FloatBuffer
import java.nio.LongBuffer

class SileroVad(context: Context, private val threshold: Float = 0.5f) {
    private val env = OrtEnvironment.getEnvironment()
    private val session: OrtSession
    private var state = FloatArray(2 * 1 * 128)
    private val sr = LongBuffer.wrap(longArrayOf(16000L))

    init {
        val bytes = context.assets.open("silero_vad.onnx").use { it.readBytes() }
        session = env.createSession(bytes, OrtSession.SessionOptions())
    }

    fun infer(frame: FloatArray): Float {
        val input = OnnxTensor.createTensor(
            env, FloatBuffer.wrap(frame), longArrayOf(1, frame.size.toLong())
        )
        val st = OnnxTensor.createTensor(
            env, FloatBuffer.wrap(state), longArrayOf(2, 1, 128)
        )
        val srT = OnnxTensor.createTensor(env, sr, longArrayOf(1))
        val out = session.run(mapOf("input" to input, "state" to st, "sr" to srT))
        @Suppress("UNCHECKED_CAST")
        val prob = (out[0].value as Array<FloatArray>)[0][0]
        @Suppress("UNCHECKED_CAST")
        val newState = out[1].value as Array<Array<FloatArray>>
        var k = 0
        for (i in 0..1) for (j in 0..0) for (z in 0..127) state[k++] = newState[i][j][z]
        input.close()
        st.close()
        srT.close()
        out.close()
        return prob
    }

    fun isSpeech(frame: FloatArray): Boolean = infer(frame) > threshold

    fun close() { session.close() }
}
