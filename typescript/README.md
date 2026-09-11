# API Rate Limiter — TypeScript

Problem and requirements: [SPEC.md](../SPEC.md). Interview flow: [INTERVIEW.md](../INTERVIEW.md).

| | |
| --- | --- |
| **File to implement** | `src/rateLimiter.ts` → `rateLimiter(userId: string): boolean` |
| **Constants** | `src/constants.ts` (`FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`) |
| **User tier map** | `userTiers` (exported `Map<string, 'free' \| 'paid'>` in `src/rateLimiter.ts`); tests set it, you read it |
| **Tests** | `tests/rateLimiter.test.ts` (Vitest) |
| **Requires** | Node.js 18+ (`node -v`) |

## Setup

```bash
cd typescript
npm install
```

Or from the repo root: `./scripts/verify.sh typescript` (installs, type-checks, and runs the smoke test).

## Run tests

```bash
npm test                         # full suite, about 40 seconds (paid-window tests really wait)
npm run test:watch               # re-run on save
npx vitest run -t "free user"    # only tests whose name matches
npm run typecheck                # tsc --noEmit
```

Or from the repo root: `./scripts/test.sh typescript`, or VS Code **Terminal → Run Task → Tests: TypeScript**.

On a fresh clone every test except `harness smoke` fails: the function is a stub. That is expected.

## Extra credit (clearly labelled in the test file)

- **`extra credit: free user upgrades to paid`** — after a free user is switched to `'paid'` in `userTiers`, requests
  made while free still count toward the current paid window.
- **`extra credit: constants respected`** — read `FREE_LIMIT` from `constants.ts`; change it and re-run to check.
