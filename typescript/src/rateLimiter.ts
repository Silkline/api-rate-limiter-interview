import { FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS, UserTier } from './constants';

/** Map from user ID to tier. Tests and callers set this; rate limiter reads it. */
export const userTiers = new Map<string, UserTier>();

/**
 * Returns true if the request is allowed, false if rate limited.
 * TODO: Implement per SPEC — free = lifetime cap of FREE_LIMIT, paid = PAID_LIMIT per WINDOW_SECONDS window.
 */
export function rateLimiter(userId: string): boolean {
  // Stub: replace with your implementation
  return false;
}
