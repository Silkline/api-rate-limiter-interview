# ⚠️ Sample solution — interviewer reference only

**Candidates: do not open this folder.** Reading the solution defeats the purpose of the exercise.

Reference TypeScript solution passing core, extra-credit, and hard-mode (async concurrency) tests. `rateLimiter.ts` is the synchronous core solution; `rateLimiterAsync.ts` is the hard-mode solution (per-user promise-chain mutex over the async store). `constants.ts` and `asyncStore.ts` are just re-exports so this folder type-checks in place.

**To verify against the tests:** copy `rateLimiter.ts` and `rateLimiterAsync.ts` over the files in `../src/`, then from `typescript/`:

```bash
HARD_MODE=1 npm test
```
