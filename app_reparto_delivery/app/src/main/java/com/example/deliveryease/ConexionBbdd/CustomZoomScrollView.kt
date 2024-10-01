package com.example.deliveryease.ConexionBbdd

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ScrollView

class CustomZoomScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ScrollView(context, attrs) {

    private var scaleXFactor = 1.0f
    private var scaleYFactor = 1.0f
    private val scaleGestureDetector: ScaleGestureDetector
    private var zoomEnabled = true // Variable para activar/desactivar el zoom

    init {
        // Inicializamos el detector de gestos de zoom
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    // Método para habilitar/deshabilitar el zoom
    fun setZoomEnabled(enabled: Boolean) {
        zoomEnabled = enabled
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Si el zoom está activado, detectar gestos de zoom
        if (zoomEnabled) {
            scaleGestureDetector.onTouchEvent(ev)
        }
        return super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Si el zoom está activado, detectar gestos de zoom
        if (zoomEnabled) {
            scaleGestureDetector.onTouchEvent(ev)
        }
        return super.onInterceptTouchEvent(ev)
    }

    // Clase interna para manejar los gestos de zoom
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Si el zoom está habilitado, ajustar el factor de zoom en ambos ejes
            if (zoomEnabled) {
                scaleXFactor *= detector.scaleFactor
                scaleYFactor *= detector.scaleFactor

                // Limitar el zoom entre 1x y 5x en ambos ejes
                scaleXFactor = Math.max(1.0f, Math.min(scaleXFactor, 5.0f))
                scaleYFactor = Math.max(1.0f, Math.min(scaleYFactor, 5.0f))

                // Aplicar los factores de zoom
                scaleX = scaleXFactor
                scaleY = scaleYFactor
            }
            return true
        }
    }
}