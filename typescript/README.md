# API Rate Limiter — TypeScript

TypeScript implementation of the API rate limiter. See [SPEC.md](../SPEC.md) for the full problem and requirements.

## Setup (VS Code / GitHub Codespaces)

1. Open this folder in VS Code (or open the repo in [GitHub Codespaces](https://github.com/features/codespaces)).
2. Ensure Node.js 18+ is installed (`node -v`).
3. Install dependencies:

   ```bash
   cd typescript
   npm install
   ```

## Run tests

```bash
npm test
```

Watch mode:

```bash
npm run test:watch
```

## User tier map

Tests set a user's tier via the exported `userTiers` Map (e.g. `userTiers.set("user1", "free")`). The rate limiter reads from this map to decide which limits apply.

## Extra credit

The test **"extra credit: free user upgrades to paid"** is optional. It verifies that when a free user is upgraded to paid, past requests (made when free) still count toward the paid 2-per-window limit. It is clearly labeled as extra credit in the test file.

## Hard mode: concurrency (async)

Node is single-threaded, so the synchronous `rateLimiter` cannot race. Hard mode instead asks you to implement **`rateLimiterAsync`** in [src/rateLimiterAsync.ts](src/rateLimiterAsync.ts), keeping all state in the provided `AsyncStore` (every `get`/`set` awaits simulated I/O, like a Redis client). 100 calls fired with `Promise.all` interleave on the event loop, so the naive check-then-record implementation sees stale counts and admits far more than the limit — you'll need to serialize the critical section per user (an async mutex / per-user promise chain). The tests are skipped by default; enable with:

```bash
HARD_MODE=1 npm test
```
