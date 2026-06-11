# ==========================================================================
#                                                                          
#      SAMPLE SOLUTION  --  INTERVIEWER REFERENCE ONLY  --  DO NOT READ    
#                                                                          
#   If you are the CANDIDATE / INTERVIEWEE: STOP. Close this file now.     
#   This folder contains the reference solution to the exercise you are    
#   being asked to solve. Reading it defeats the purpose of the interview  
#   and will be obvious in the follow-up discussion.                       
#                                                                          
# ==========================================================================
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#   (still the sample solution -- candidates, keep this file closed)
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#   (still the sample solution -- candidates, keep this file closed)
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#   (still the sample solution -- candidates, keep this file closed)
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#   (still the sample solution -- candidates, keep this file closed)
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#   (still the sample solution -- candidates, keep this file closed)
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#   (still the sample solution -- candidates, keep this file closed)
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#   (still the sample solution -- candidates, keep this file closed)
#
#
#
#
#
#
#
#
#
#
#
# ==========================================================================
#   The reference solution begins below this line.                         
# ==========================================================================

"""
API rate limiter: free = 5 requests ever, paid = 2 per 5s window.
"""

import threading
import time

# Constants (used by implementation and tests)
FREE_LIMIT = 5
PAID_LIMIT = 2
WINDOW_SECONDS = 5

# Map from user ID to tier. Tests and callers set this; rate limiter reads it.
user_tiers: dict[str, str] = {}

# _lock makes the whole check-then-record atomic, so concurrent callers admit
# exactly the limit (hard mode). A per-user lock would reduce contention; one
# global lock is the simplest correct choice.
_lock = threading.Lock()

# Every allowed request's timestamp per user. Keeping the full history makes
# the free lifetime cap and the upgrade extra credit (past free requests count
# toward the paid window) fall out naturally. To bound memory, store only the
# lifetime count plus the most recent PAID_LIMIT timestamps.
_history: dict[str, list[float]] = {}


def rate_limiter(user_id: str) -> bool:
    """Returns True if the request is allowed, False if rate limited."""
    with _lock:
        now = time.monotonic()
        calls = _history.setdefault(user_id, [])

        # FREE_LIMIT is read at call time (not captured at import) so the
        # extra-credit constants test can override it.
        if user_tiers.get(user_id, "free") == "paid":
            cutoff = now - WINDOW_SECONDS
            allowed = sum(1 for t in calls if t >= cutoff) < PAID_LIMIT
        else:
            # Unknown users default to free.
            allowed = len(calls) < FREE_LIMIT

        if allowed:
            calls.append(now)
        return allowed
