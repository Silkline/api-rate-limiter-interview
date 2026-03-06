import { FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS, UserTier } from './constants';

/** Map from user ID to tier. Tests and callers set this; rate limiter reads it. */
export const userTiers = new Map<string, UserTier>();

/** Per-user request timestamps (ms). Used for free=lifetime count, paid=sliding window. */
const requestTimestamps = new Map<string, number[]>();

function getTier(userId: string): UserTier {
  return userTiers.get(userId) ?? 'free';
}

function getTimestamps(userId: string): number[] {
  return requestTimestamps.get(userId) ?? [];
}

/**
 * Returns true if the request is allowed, false if rate limited.
 */
export function rateLimiter(userId: string): boolean {
  const tier = getTier(userId);
  const now = Date.now();
  const windowMs = WINDOW_SECONDS * 1000;
  const cutoff = now - windowMs;

  let timestamps = getTimestamps(userId);

  if (tier === 'free') {
    const totalCount = timestamps.length;
    if (totalCount >= FREE_LIMIT) return false;
    timestamps = [...timestamps, now];
    requestTimestamps.set(userId, timestamps);
    return true;
  }

  // paid: only consider requests in the current window
  const inWindow = timestamps.filter((t) => t > cutoff);
  if (inWindow.length >= PAID_LIMIT) return false;
  timestamps = [...timestamps, now];
  requestTimestamps.set(userId, timestamps);
  return true;
}
