package com.example.converter // Your package name

import android.graphics.Color
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var radius: Float,
    var color: Int,
    var alpha: Int = 255,
    var xVelocity: Float,
    var yVelocity: Float,
    var lifetime: Long, // milliseconds
    var createdAt: Long = System.currentTimeMillis()
) {
    fun isAlive(): Boolean = (System.currentTimeMillis() - createdAt) < lifetime
}

fun createRandomParticle(startX: Float, startY: Float): Particle {
    val angle = Random.nextDouble(0.0, 2.0 * Math.PI)
    val speed = Random.nextFloat() * 5f + 2f
    return Particle(
        x = startX,
        y = startY,
        radius = Random.nextFloat() * 8f + 4f,
        color = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)),
        xVelocity = kotlin.math.cos(angle).toFloat() * speed,
        yVelocity = kotlin.math.sin(angle).toFloat() * speed,
        lifetime = Random.nextLong(500, 1500)
    )
}