package com.spmadrid.vrepo.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.createBitmap
import com.spmadrid.vrepo.domain.dtos.BoundingBox
import com.spmadrid.vrepo.domain.interfaces.IObjectDetector
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.File

class ObjectDetectorImpl(
    private val context: Context,
    private val modelPath: String,
    private val labelPath: String,
) : IObjectDetector {
    private lateinit var interpreter: Interpreter
    private val labels = mutableListOf<String>()

    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    private lateinit var tensorImage: TensorImage
    private lateinit var outputBuffer: TensorBuffer

    private lateinit var reusableBitmap: Bitmap

    init { this.initialize() }

    override suspend fun detect(frame: Bitmap): List<BoundingBox>?  = withContext(Dispatchers.IO) {
        if (!::tensorImage.isInitialized || !::outputBuffer.isInitialized) return@withContext null

        // Reuse the preallocated bitmap instead of creating a new one
        val canvas = Canvas(reusableBitmap)
        canvas.drawBitmap(frame, null, Rect(0, 0, tensorWidth, tensorHeight), null)

        // Load and process the tensor image
        tensorImage.load(reusableBitmap)
        val processedImage = imageProcessor.process(tensorImage)

        // Use the same ByteBuffer for inference (reduce memory allocation)
        interpreter.run(processedImage.buffer, outputBuffer.buffer)

        return@withContext bestBox(outputBuffer.floatArray)
    }

    override fun close() {
        interpreter.close()
    }

    fun initializeInterpreter(): Interpreter {
        val options = Interpreter.Options()

        try {
            val compatibilityList = CompatibilityList()

            if (compatibilityList.isDelegateSupportedOnThisDevice) {
                // Prefer OpenCL (default TensorFlow Lite behavior)
                val gpuDelegate = GpuDelegate(compatibilityList.bestOptionsForThisDevice)
                options.addDelegate(gpuDelegate)
                println("Using OpenCL GPU Delegate")
                return Interpreter(FileUtil.loadMappedFile(context, modelPath), options)
            }
        } catch (e: Exception) {
            println("OpenCL failed: ${e.message}, trying Vulkan...")
        }

        try {
            // Try Vulkan if OpenCL is not available
            System.setProperty("org.tensorflow.lite.gpu.disable_opencl", "true") // Disable OpenCL explicitly
            val gpuOptions = GpuDelegate.Options().apply {
                this.setInferencePreference(GpuDelegate.Options.INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER)
            }
            val gpuDelegate = GpuDelegate(gpuOptions)
            options.addDelegate(gpuDelegate)
            println("Using Vulkan GPU Delegate")
            return Interpreter(FileUtil.loadMappedFile(context, modelPath), options)
        } catch (e: Exception) {
            println("Vulkan failed: ${e.message}, trying NNAPI...")
        }

        try {
            // Try NNAPI as a fallback
            val nnapiDelegate = NnApiDelegate()
            options.addDelegate(nnapiDelegate)
            println("Using NNAPI Delegate")
            return Interpreter(FileUtil.loadMappedFile(context, modelPath), options)
        } catch (e: Exception) {
            println("NNAPI failed: ${e.message}, using CPU fallback")
        }

        // If all fails, fall back to CPU
        options.setNumThreads(Runtime.getRuntime().availableProcessors())
        return Interpreter(FileUtil.loadMappedFile(context, modelPath), options)
    }

    override fun initialize() {
        interpreter = initializeInterpreter()

        val inputShape = interpreter.getInputTensor(0)?.shape()
        val outputShape = interpreter.getOutputTensor(0)?.shape()

        if (inputShape != null) {
            tensorWidth = inputShape[1]
            tensorHeight = inputShape[2]

            // If in case input shape is in format of [1, 3, ..., ...]
            if (inputShape[1] == 3) {
                tensorWidth = inputShape[2]
                tensorHeight = inputShape[3]
            }
        }

        if (outputShape != null) {
            numChannel = outputShape[1]
            numElements = outputShape[2]
        }

        // Initialize buffers once
        tensorImage = TensorImage(INPUT_IMAGE_TYPE)
        outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, numChannel, numElements), OUTPUT_IMAGE_TYPE)
        reusableBitmap = createBitmap(tensorWidth, tensorHeight)

        try {
            val inputStream: InputStream = context.assets.open(labelPath)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = reader.readLine()
            while (line != null && line != "") {
                labels.add(line)
                line = reader.readLine()
            }

            reader.close()
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun isOpenCLAvailable(): Boolean {
        return try {
            val openCLPaths = arrayOf(
                "/system/lib/libOpenCL.so",
                "/system/lib64/libOpenCL.so",
                "/vendor/lib/libOpenCL.so",
                "/vendor/lib64/libOpenCL.so"
            )

            val available = openCLPaths.any { File(it).exists() }
            Log.d("OpenCLCheck", "OpenCL Available: $available")
            available
        } catch (e: Exception) {
            Log.e("OpenCLCheck", "Error checking OpenCL availability", e)
            false
        }
    }

    private fun bestBox(array: FloatArray) : List<BoundingBox>? {

        val boundingBoxes = mutableListOf<BoundingBox>()

        for (c in 0 until numElements) {
            var maxConf = CONFIDENCE_THRESHOLD
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j
            while (j < numChannel){
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                val clsName = labels[maxIdx]
                val cx = array[c] // 0
                val cy = array[c + numElements] // 1
                val w = array[c + numElements * 2]
                val h = array[c + numElements * 3]
                val x1 = cx - (w/2F)
                val y1 = cy - (h/2F)
                val x2 = cx + (w/2F)
                val y2 = cy + (h/2F)
                if (x1 < 0F || x1 > 1F) continue
                if (y1 < 0F || y1 > 1F) continue
                if (x2 < 0F || x2 > 1F) continue
                if (y2 < 0F || y2 > 1F) continue

                boundingBoxes.add(
                    BoundingBox(
                        x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                        cx = cx, cy = cy, w = w, h = h,
                        cnf = maxConf, cls = maxIdx, clsName = clsName
                    )
                )
            }
        }
        if (boundingBoxes.isEmpty()) return null
        return applyNMS(boundingBoxes)
    }

    private fun applyNMS(boxes: List<BoundingBox>) : MutableList<BoundingBox> {
        val sortedBoxes = boxes.sortedByDescending { it.cnf }.toMutableList()
        val selectedBoxes = mutableListOf<BoundingBox>()

        while(sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.first()
            selectedBoxes.add(first)
            sortedBoxes.remove(first)

            val iterator = sortedBoxes.iterator()
            while (iterator.hasNext()) {
                val nextBox = iterator.next()
                val iou = calculateIoU(first, nextBox)
                if (iou >= IOU_THRESHOLD) {
                    iterator.remove()
                }
            }
        }

        return selectedBoxes
    }

    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)
        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h
        return intersectionArea / (box1Area + box2Area - intersectionArea)
    }

    companion object {
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32
        private const val CONFIDENCE_THRESHOLD = 0.3F
        private const val IOU_THRESHOLD = 0.5F
        const val NUM_THREADS = 5
    }
}