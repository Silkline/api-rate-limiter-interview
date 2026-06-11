import { AsyncStore } from './asyncStore';
import { userTiers } from './rateLimiter';
import { FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS } from './constants';

/**
 * HARD MODE: all rate-limit state for the async limiter lives in this store.
 * Treat it like a Redis client — every read and write of request history must
 * go through store.get / store.set; do not cache state in module variables
 * across calls.
 */
export const store = new AsyncStore<number[]>();

/**
 * HARD MODE: same rules as rateLimiter (free = FREE_LIMIT ever, paid =
 * PAID_LIMIT per WINDOW_SECONDS window, tier from userTiers), but state is
 * read and written through the async store above.
 *
 * Node is single-threaded, yet concurrent calls still race: while one call is
 * awaiting store.get (the check), the event loop runs other calls, and they
 * all see the same stale count before anyone's store.set (the record) lands.
 * When N calls run concurrently for one user, exactly the limit may succeed —
 * never more. You'll need to serialize the check-then-record critical section
 * per user (e.g. an async mutex / per-user promise chain).
 *
 * TODO: Implement.
 */
export async function rateLimiterAsync(userId: string): Promise<boolean> {
  // Stub: replace with your implementation
  void userId;
  return false;
}
