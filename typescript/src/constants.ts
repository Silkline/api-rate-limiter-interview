/** Total requests a free user is allowed ever (lifetime cap). */
export const FREE_LIMIT = 5;

/** Max requests a paid user gets per time window. */
export const PAID_LIMIT = 2;

/** Window length in seconds for paid users. */
export const WINDOW_SECONDS = 5;

export type UserTier = 'free' | 'paid';
