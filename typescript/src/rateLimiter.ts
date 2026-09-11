import { FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS, UserTier } from './constants';

/** Map from user ID to tier. Tests set this; the rate limiter reads it. */
export const userTiers = new Map<string, UserTier>();

/**
 * Returns true if the request is allowed, false if it is rate limited.
 *
 * TODO: implement per SPEC.md.
 *   - Look up the user's tier in `userTiers`.
 *   - 'free': allow the first FREE_LIMIT requests ever, then always deny.
 *   - 'paid': allow at most PAID_LIMIT requests per WINDOW_SECONDS-second window.
 * Keep per-user state in memory (e.g. a module-level Map).
 */
export function rateLimiter(userId: string): boolean {
  // Stub: replace with your implementation.
  void userId;
  void FREE_LIMIT;
  void PAID_LIMIT;
  void WINDOW_SECONDS;
  return false;
}
