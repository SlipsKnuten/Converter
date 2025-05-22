package com.example.converter // Your package name

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.*

class ParticleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint().apply {
        isAntiAlias = true
    }
    private var animationJob: Job? = null
    private val particleScope = CoroutineScope(Dispatchers.Main + SupervisorJob()) // Added SupervisorJob

    fun addExplosion(x: Float, y: Float, count: Int = 20) {
        repeat(count) {
            particles.add(createRandomParticle(x, y))
        }
        if (animationJob == null || animationJob?.isActive == false) {
            startAnimationLoop()
        }
    }

    private fun startAnimationLoop() {
        animationJob?.cancel()
        animationJob = particleScope.launch {
            while (isActive && particles.isNotEmpty()) {
                updateParticles()
                invalidate()
                delay(16) // ~60 FPS
            }
            animationJob = null
        }
    }

    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            if (particle.isAlive()) {
                particle.x += particle.xVelocity
                particle.y += particle.yVelocity
                val aliveRatio = (System.currentTimeMillis() - particle.createdAt).toFloat() / particle.lifetime
                particle.alpha = (255 * (1f - aliveRatio)).toInt().coerceIn(0, 255)
            } else {
                iterator.remove()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (particle in particles) { // Use simple for loop for potentially concurrent modification safety
            if (particle.isAlive()) {
                paint.color = particle.color
                paint.alpha = particle.alpha
                canvas.drawCircle(particle.x, particle.y, particle.radius, paint)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        particleScope.cancel() // Cancel the scope and all its children (animationJob)
    }
}