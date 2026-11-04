package com.example.eventpass.ui.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Analizador de CameraX con ML Kit Barcode Scanning.
 * Incluye mecanismo de debounce y bandera atómica para evitar procesar
 * múltiples veces el mismo código QR mientras se analiza o valida.
 */
class QrCodeAnalyzer(
    private val debounceDelayMs: Long = 2000L,
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val isProcessing = AtomicBoolean(false)
    private var lastScannedTimestamp: Long = 0L

    // Configuración para escanear exclusivamente códigos QR
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()

        // Mecanismo de debounce y throttling:
        // Si está en procesamiento o aún no ha transcurrido el tiempo mínimo, descartamos el frame
        if (isProcessing.get() || (currentTime - lastScannedTimestamp < debounceDelayMs)) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            if (!isProcessing.compareAndSet(false, true)) {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val qrValue = barcode.rawValue
                        if (!qrValue.isNullOrBlank()) {
                            lastScannedTimestamp = System.currentTimeMillis()
                            onQrCodeScanned(qrValue)
                            break
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    // Liberar bandera de análisis en curso y cerrar el fotograma para no congelar CameraX
                    isProcessing.set(false)
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    /**
     * Permite reiniciar manualmente el analizador si es requerido.
     */
    fun resetAnalyzer() {
        isProcessing.set(false)
        lastScannedTimestamp = 0L
    }
}