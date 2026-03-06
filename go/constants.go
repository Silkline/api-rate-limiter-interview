package ratelimit

// FREE_LIMIT is the total requests a free user is allowed ever (lifetime cap).
const FREE_LIMIT = 5

// PAID_LIMIT is the max requests a paid user gets per time window.
const PAID_LIMIT = 2

// WINDOW_SECONDS is the window length in seconds for paid users.
const WINDOW_SECONDS = 5
