package com.max.spirihin.mytracksdb.utilities

object Utils {

    fun lerp(x: Double, y: Double, t: Double): Double {
        return x * (1 - t) + y * t
    }

    fun inverseLerp(min: Double, max: Double, value: Double): Double {
        return if (Math.abs(max - min).equals(0.0)) min else (value - min) / (max - min)
    }

}