import { beforeEach, describe, expect, it } from 'vitest';
import { rateLimiter, userTiers } from '../src/rateLimiter';
import { FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS } from '../src/constants';

beforeEach(() => {
  userTiers.clear();
});

describe('rateLimiter', () => {
  describe('free user', () => {
    it('allows first FREE_LIMIT requests then denies', () => {
      const user = 'free-user-1';
      userTiers.set(user, 'free');

      for (let i = 0; i < FREE_LIMIT; i++) {
        expect(rateLimiter(user)).toBe(true);
      }
      expect(rateLimiter(user)).toBe(false);
      expect(rateLimiter(user)).toBe(false);
    });

    it('never resets (lifetime cap)', () => {
      const user = 'free-user-2';
      userTiers.set(user, 'free');

      for (let i = 0; i < FREE_LIMIT; i++) {
        expect(rateLimiter(user)).toBe(true);
      }
      expect(rateLimiter(user)).toBe(false);
      // Wait would not help free users; no time window
      expect(rateLimiter(user)).toBe(false);
    });
  });

  describe('paid user', () => {
    it('allows first PAID_LIMIT requests in window then denies', () => {
      const user = 'paid-user-1';
      userTiers.set(user, 'paid');

      expect(rateLimiter(user)).toBe(true);
      expect(rateLimiter(user)).toBe(true);
      expect(rateLimiter(user)).toBe(false);
    });

    it('allows again after window passes', async () => {
      const user = 'paid-user-2';
      userTiers.set(user, 'paid');

      expect(rateLimiter(user)).toBe(true);
      expect(rateLimiter(user)).toBe(true);
      expect(rateLimiter(user)).toBe(false);

      await new Promise((r) => setTimeout(r, (WINDOW_SECONDS + 0.5) * 1000));

      expect(rateLimiter(user)).toBe(true);
      expect(rateLimiter(user)).toBe(true);
      expect(rateLimiter(user)).toBe(false);
    }, (WINDOW_SECONDS + 2) * 1000);
  });

  /**
   * EXTRA CREDIT: Free user upgrades to paid.
   * After upgrade, paid rules apply and past requests (made when free) must count
   * toward the paid 2-per-window limit, so the user does NOT get a fresh paid window.
   */
  describe('extra credit: free user upgrades to paid', () => {
    it('applies paid limit and counts past free requests in window', async () => {
      const user = 'upgrade-user-1';
      userTiers.set(user, 'free');

      // Use 2 requests as free (within a 5s window)
      expect(rateLimiter(user)).toBe(true);
      expect(rateLimiter(user)).toBe(true);

      // Upgrade to paid
      userTiers.set(user, 'paid');

      // Paid limit is 2 per window; we already have 2 requests in this window → denied
      expect(rateLimiter(user)).toBe(false);

      // After window passes, paid user gets 2 more
      await new Promise((r) => setTimeout(r, (WINDOW_SECONDS + 0.5) * 1000));
      expect(rateLimiter(user)).toBe(true);
      expect(rateLimiter(user)).toBe(true);
      expect(rateLimiter(user)).toBe(false);
    }, (WINDOW_SECONDS + 2) * 1000);
  });
});
