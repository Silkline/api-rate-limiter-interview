# API Rate Limiter — C#

C# implementation of the API rate limiter. See [SPEC.md](../SPEC.md) for the full problem and requirements.

## Setup (VS Code / GitHub Codespaces)

1. Open this folder in VS Code (or open the repo in [GitHub Codespaces](https://github.com/features/codespaces)).
2. Ensure .NET 10 SDK is installed (`dotnet --version`).
3. Restore packages (optional; `dotnet test` will restore):

   ```bash
   cd csharp
   dotnet restore
   ```

## Run tests

From the `csharp` directory:

```bash
dotnet test
```

## User tier map

Tests set a user's tier via `RateLimiter.UserTiers` (e.g. `RateLimiter.UserTiers["user1"] = UserTier.Free`). The rate limiter reads from this map to decide which limits apply.

## Extra credit

The test **`ExtraCredit_FreeUpgradesToPaid`** is optional. It verifies that when a free user is upgraded to paid, past requests (made when free) still count toward the paid 2-per-window limit. It is clearly labeled as extra credit in the test name and summary.

## Hard mode: concurrency

The tests **`HardMode_*`** are skipped by default. They release 100 threads at once against one user and require that **exactly** the limit is allowed — the naive check-then-record pattern races between reading the count and recording the request. Enable with:

```bash
HARD_MODE=1 dotnet test
```
