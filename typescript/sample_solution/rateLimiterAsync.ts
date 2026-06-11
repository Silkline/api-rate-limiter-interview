// ==========================================================================
//                                                                          
//      SAMPLE SOLUTION  --  INTERVIEWER REFERENCE ONLY  --  DO NOT READ    
//                                                                          
//   If you are the CANDIDATE / INTERVIEWEE: STOP. Close this file now.     
//   This folder contains the reference solution to the exercise you are    
//   being asked to solve. Reading it defeats the purpose of the interview  
//   and will be obvious in the follow-up discussion.                       
//                                                                          
// ==========================================================================
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//   (still the sample solution -- candidates, keep this file closed)
//
//
//
//
//
//
//
//
//
//
//
// ==========================================================================
//   The reference solution begins below this line.                         
// ==========================================================================

import { AsyncStore } from './asyncStore';
import { userTiers } from './rateLimiter';
import { FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS } from './constants';

/** All rate-limit state for the async limiter lives here — treat it like Redis. */
export const store = new AsyncStore<number[]>();

// Per-user promise-chain mutex. Each user's calls are chained one after
// another, so the awaits inside the critical section (store.get → store.set)
// can never interleave with another call for the same user — that interleaving
// is exactly the check-then-record race the hard-mode tests catch. Different
// users still run concurrently.
const locks = new Map<string, Promise<unknown>>();

function withUserLock<T>(userId: string, fn: () => Promise<T>): Promise<T> {
  const prev = locks.get(userId) ?? Promise.resolve();
  // Chain regardless of whether the previous call succeeded or failed.
  const next = prev.then(fn, fn);
  locks.set(userId, next);
  return next;
}

/** Returns true if the request is allowed, false if rate limited. */
export async function rateLimiterAsync(userId: string): Promise<boolean> {
  return withUserLock(userId, async () => {
    const now = Date.now();
    const calls = (await store.get(userId)) ?? [];
    // Unknown users default to free.
    const tier = userTiers.get(userId) ?? 'free';

    const allowed =
      tier === 'paid'
        ? calls.filter((t) => t >= now - WINDOW_SECONDS * 1000).length < PAID_LIMIT
        : calls.length < FREE_LIMIT;

    if (allowed) {
      await store.set(userId, [...calls, now]);
    }
    return allowed;
  });
}
