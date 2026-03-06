package com.silkline.ratelimit;

public final class Constants {
    private Constants() {}

    /** Total requests a free user is allowed ever (lifetime cap). */
    public static final int FREE_LIMIT = 5;

    /** Max requests a paid user gets per time window. */
    public static final int PAID_LIMIT = 2;

    /** Window length in seconds for paid users. */
    public static final int WINDOW_SECONDS = 5;
}
