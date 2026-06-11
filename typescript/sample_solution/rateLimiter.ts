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

import { FREE_LIMIT, PAID_LIMIT, WINDOW_SECONDS, UserTier } from './constants';

/** Map from user ID to tier. Tests and callers set this; rate limiter reads it. */
export const userTiers = new Map<string, UserTier>();

// Every allowed request's timestamp (ms) per user. Keeping the full history
// makes the free lifetime cap and the upgrade extra credit (past free requests
// count toward the paid window) fall out naturally. To bound memory, store
// only the lifetime count plus the most recent PAID_LIMIT timestamps.
const history = new Map<string, number[]>();

/** Returns true if the request is allowed, false if rate limited. */
export function rateLimiter(userId: string): boolean {
  const now = Date.now();
  const calls = history.get(userId) ?? [];
  // Unknown users default to free.
  const tier = userTiers.get(userId) ?? 'free';

  const allowed =
    tier === 'paid'
      ? calls.filter((t) => t >= now - WINDOW_SECONDS * 1000).length < PAID_LIMIT
      : calls.length < FREE_LIMIT;

  if (allowed) {
    calls.push(now);
    history.set(userId, calls);
  }
  return allowed;
}
