package com.silkline.ratelimit

/**
 * Rate limiter constants. Referenced by implementation and tests.
 */
object Constants {
    /** Total requests a free user is allowed ever (lifetime cap). */
    const val FREE_LIMIT = 5

    /** Max requests a paid user gets per time window. */
    const val PAID_LIMIT = 2

    /** Window length in seconds for paid users. */
    const val WINDOW_SECONDS = 5
}
