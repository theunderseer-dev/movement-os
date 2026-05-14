package com.theunderseer.movementos.domain.model.values

import kotlin.jvm.JvmInline

/**
 * Time duration in seconds for movement sessions and exercises.
 *
 * Wrapping primitive Int prevents accidental mixing with other Int values
 * (e.g., difficulty levels, exercise counts) at compile time.
 */
@JvmInline
value class Duration(
    val seconds: Int,
) {
    init {
        require(seconds >= 0) { "Duration cannot be negative: $seconds" }
    }

    val minutes: Int get() = seconds / SECONDS_PER_MINUTE

    operator fun plus(other: Duration): Duration = Duration(seconds + other.seconds)

    operator fun compareTo(other: Duration): Int = seconds.compareTo(other.seconds)

    companion object {
        private const val SECONDS_PER_MINUTE = 60
        val ZERO = Duration(0)

        fun ofMinutes(minutes: Int): Duration = Duration(minutes * SECONDS_PER_MINUTE)
    }
}
