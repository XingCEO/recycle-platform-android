package com.recycle.vendor

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * On-device classifier over assets/recycle_classifier.tflite + assets/labels.txt.
 *
 * Input/output dtype is read from the model at runtime, so this works with BOTH:
 *  - the shipped placeholder (uint8-quant MobileNet) — exercises the pipeline, low confidence
 *  - a real Teachable-Machine 3-class float model — exact pet/can/carton output
 * Only the first `labels.size` logits are considered (a real model has exactly that many).
 */
class TfliteClassifier(
    context: Context,
    modelAsset: String = "recycle_classifier.tflite",
    labelsAsset: String = "labels.txt",
) {
    private val interpreter: Interpreter
    val labels: List<String>
    private val inW: Int
    private val inH: Int
    private val inputType: DataType
    private val outCount: Int
    private val outputType: DataType

    init {
        val afd = context.assets.openFd(modelAsset)
        val fis = FileInputStream(afd.fileDescriptor)
        val model: ByteBuffer = fis.channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
        fis.close(); afd.close()
        interpreter = Interpreter(model)

        labels = context.assets.open(labelsAsset).bufferedReader().readLines()
            .map { it.trim() }.filter { it.isNotEmpty() }

        val inT = interpreter.getInputTensor(0)
        val shape = inT.shape()            // [1, H, W, 3]
        inH = shape[1]; inW = shape[2]
        inputType = inT.dataType()
        val outT = interpreter.getOutputTensor(0)
        outCount = outT.shape().last()
        outputType = outT.dataType()
    }

    /** @return (categoryLabel, confidence 0..1). */
    fun classify(bitmap: Bitmap): Pair<String, Double> {
        val resized = Bitmap.createScaledBitmap(bitmap, inW, inH, true)
        val pixels = IntArray(inW * inH)
        resized.getPixels(pixels, 0, inW, 0, 0, inW, inH)

        val input: ByteBuffer = if (inputType == DataType.UINT8) {
            ByteBuffer.allocateDirect(inW * inH * 3).order(ByteOrder.nativeOrder()).apply {
                for (p in pixels) {
                    put(((p shr 16) and 0xFF).toByte())
                    put(((p shr 8) and 0xFF).toByte())
                    put((p and 0xFF).toByte())
                }
            }
        } else {
            ByteBuffer.allocateDirect(inW * inH * 3 * 4).order(ByteOrder.nativeOrder()).apply {
                for (p in pixels) {
                    putFloat(((p shr 16) and 0xFF) / 255f)
                    putFloat(((p shr 8) and 0xFF) / 255f)
                    putFloat((p and 0xFF) / 255f)
                }
            }
        }
        input.rewind()

        val probs = FloatArray(outCount)
        if (outputType == DataType.UINT8) {
            val out = Array(1) { ByteArray(outCount) }
            interpreter.run(input, out)
            for (i in 0 until outCount) probs[i] = (out[0][i].toInt() and 0xFF) / 255f
        } else {
            val out = Array(1) { FloatArray(outCount) }
            interpreter.run(input, out)
            for (i in 0 until outCount) probs[i] = out[0][i]
        }

        val limit = minOf(labels.size, outCount)
        var best = 0; var bestV = -Float.MAX_VALUE
        for (i in 0 until limit) if (probs[i] > bestV) { bestV = probs[i]; best = i }
        val label = labels.getOrElse(best) { labels.firstOrNull() ?: "pet" }
        return label to bestV.toDouble()
    }
}
