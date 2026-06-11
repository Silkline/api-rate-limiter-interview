import { beforeEach, describe, expect, it } from 'vitest';
import { rateLimiterAsync, store } from '../src/rateLimiterAsync';
import { userTiers } from '../src/rateLimiter';
import { FREE_LIMIT, PAID_LIMIT } from '../src/constants';

/**
 * HARD MODE: Concurrency safety, Node-style. Skipped unless HARD_MODE is set
 * (e.g. HARD_MODE=1 npm test).
 *
 * There are no threads here — the race comes from the async store: every call
 * awaits store.get before store.set, so the event loop interleaves the calls
 * and a naive check-then-record implementation sees stale counts and admits
 * far more than the limit.
 */
const hardMode = describe.skipIf(!process.env.HARD_MODE);

hardMode('HARD MODE: rateLimiterAsync under concurrency', () => {
  beforeEach(() => {
    userTiers.clear();
    store.clear();
  });

  async function countConcurrentAllowed(user: string, count: number): Promise<number> {
    const results = await Promise.all(
      Array.from({ length: count }, () => rateLimiterAsync(user)),
    );
    return results.filter(Boolean).length;
  }

  it('paid: 100 concurrent requests allow exactly PAID_LIMIT', async () => {
    const user = 'paid-concurrent-1';
    userTiers.set(user, 'paid');

    expect(await countConcurrentAllowed(user, 100)).toBe(PAID_LIMIT);
  });

  it('free: 100 concurrent requests allow exactly FREE_LIMIT', async () => {
    const user = 'free-concurrent-1';
    userTiers.set(user, 'free');

    expect(await countConcurrentAllowed(user, 100)).toBe(FREE_LIMIT);
  });
});
