/**
 * Tests for the API rate limiter. See ../../SPEC.md section 3 for what each test verifies.
 *
 * All expectations are derived from FREE_LIMIT / PAID_LIMIT / WINDOW_SECONDS, so changing
 * those constants in src/constants.ts changes what the tests expect.
 *
 * Tests that wait for the paid window sleep for real, so the full suite takes roughly
 * 6 * (WINDOW_SECONDS + 1) seconds (about 36s with the defaults).
 */
import { beforeEach, describe, expect, it } from 'vitest';
import { rateLimiter, userTiers } from '../src/rateLimiter';
import { FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS } from '../src/constants';

/** Wait a little longer than the window so we are safely on the other side of it. */
const WAIT_MS = (WINDOW_SECONDS + 1) * 1000;
const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

function expectAllowed(user: string, count: number, label: string) {
  for (let i = 0; i < count; i++) {
    expect(rateLimiter(user), `${label}: request ${i + 1} of ${count} should be allowed`).toBe(true);
  }
}

beforeEach(() => {
  userTiers.clear();
});

describe('harness smoke', () => {
  // Passes even with the unimplemented stub. If this fails, your environment is broken.
  it('constants and tier map are wired up', () => {
    expect(FREE_LIMIT).toBeGreaterThan(0);
    expect(PAID_LIMIT).toBeGreaterThan(0);
    expect(WINDOW_SECONDS).toBeGreaterThan(0);
    expect(userTiers.size).toBe(0);
    expect(typeof rateLimiter('smoke-user')).toBe('boolean');
  });
});

describe('free user', () => {
  it('allows the first FREE_LIMIT requests, then denies', () => {
    const user = 'free-user-1';
    userTiers.set(user, 'free');

    expectAllowed(user, FREE_LIMIT, 'free');
    expect(rateLimiter(user), 'request beyond FREE_LIMIT should be denied').toBe(false);
    expect(rateLimiter(user)).toBe(false);
  });

  it('never resets, even after waiting past a window', async () => {
    const user = 'free-user-2';
    userTiers.set(user, 'free');

    expectAllowed(user, FREE_LIMIT, 'free');
    expect(rateLimiter(user)).toBe(false);

    await sleep(WAIT_MS);
    expect(rateLimiter(user), 'free cap must not reset after waiting').toBe(false);
    expect(rateLimiter(user)).toBe(false);
  });
});

describe('paid user', () => {
  it('allows PAID_LIMIT requests in a window, then denies', () => {
    const user = 'paid-user-1';
    userTiers.set(user, 'paid');

    expectAllowed(user, PAID_LIMIT, 'paid');
    expect(rateLimiter(user), 'request beyond PAID_LIMIT should be denied').toBe(false);
  });

  it('allows again after the window passes', async () => {
    const user = 'paid-user-2';
    userTiers.set(user, 'paid');

    expectAllowed(user, PAID_LIMIT, 'window 1');
    expect(rateLimiter(user)).toBe(false);

    await sleep(WAIT_MS);

    expectAllowed(user, PAID_LIMIT, 'after window');
    expect(rateLimiter(user)).toBe(false);
  });

  it('single request, then window expiry, then a full PAID_LIMIT again', async () => {
    const user = 'paid-single-window';
    userTiers.set(user, 'paid');

    expect(rateLimiter(user)).toBe(true);
    await sleep(WAIT_MS);
    expectAllowed(user, PAID_LIMIT, 'old request must have expired');
    expect(rateLimiter(user)).toBe(false);
  });

  it('resets correctly across multiple windows', async () => {
    const user = 'paid-two-windows';
    userTiers.set(user, 'paid');

    for (let w = 1; w <= 3; w++) {
      expectAllowed(user, PAID_LIMIT, `window ${w}`);
      expect(rateLimiter(user), `window ${w}: request beyond PAID_LIMIT should be denied`).toBe(false);
      if (w < 3) await sleep(WAIT_MS);
    }
  });
});

describe('isolation between users', () => {
  it('free users have independent lifetime caps', () => {
    const userA = 'free-isolation-a';
    const userB = 'free-isolation-b';
    userTiers.set(userA, 'free');
    userTiers.set(userB, 'free');

    expectAllowed(userA, FREE_LIMIT, 'user A');
    expect(rateLimiter(userA)).toBe(false);

    expectAllowed(userB, FREE_LIMIT, 'user B must not be affected by user A');
    expect(rateLimiter(userB)).toBe(false);
  });

  it('paid users have independent windows', () => {
    const userA = 'paid-isolation-a';
    const userB = 'paid-isolation-b';
    userTiers.set(userA, 'paid');
    userTiers.set(userB, 'paid');

    expectAllowed(userA, PAID_LIMIT, 'user A');
    expect(rateLimiter(userA)).toBe(false);

    expectAllowed(userB, PAID_LIMIT, 'user B must not be affected by user A');
    expect(rateLimiter(userB)).toBe(false);
  });
});

/**
 * EXTRA CREDIT: free user upgrades to paid.
 * After the upgrade, paid rules apply AND requests made while free still count toward the
 * current paid window, so the user does NOT get a fresh window just by upgrading.
 */
describe('extra credit: free user upgrades to paid', () => {
  it.skipIf(PAID_LIMIT > FREE_LIMIT)('counts past free requests toward the paid window', async () => {
    const user = 'upgrade-user-1';
    userTiers.set(user, 'free');

    expectAllowed(user, PAID_LIMIT, 'as free');

    userTiers.set(user, 'paid');

    // Already made PAID_LIMIT requests inside this window -> denied.
    expect(rateLimiter(user), 'past free requests must count toward the paid window').toBe(false);

    await sleep(WAIT_MS);
    expectAllowed(user, PAID_LIMIT, 'after window');
    expect(rateLimiter(user)).toBe(false);
  });
});

/**
 * EXTRA CREDIT (constants): the implementation must read FREE_LIMIT from constants.ts
 * rather than hard-coding 5. TypeScript `const` exports cannot be overridden at runtime,
 * so this test only counts; to really check, change FREE_LIMIT in constants.ts and re-run.
 */
describe('extra credit: constants respected', () => {
  it('allows exactly FREE_LIMIT free requests', () => {
    const user = 'free-constants-check';
    userTiers.set(user, 'free');
    let allowed = 0;
    for (let i = 0; i < FREE_LIMIT + 2; i++) {
      if (rateLimiter(user)) allowed++;
    }
    expect(allowed).toBe(FREE_LIMIT);
  });
});
