# API Rate Limiter — C#

Problem and requirements: [SPEC.md](../SPEC.md). Interview flow: [INTERVIEW.md](../INTERVIEW.md).

| | |
| --- | --- |
| **File to implement** | `RateLimiter.cs` → `static bool AllowRequest(string userId)` (a method cannot share its class's name, hence not `RateLimiter`) |
| **Constants** | `Constants.cs` (`FreeLimit`, `PaidLimit`, `WindowSeconds`) |
| **User tier map** | `RateLimiter.UserTiers` (`ConcurrentDictionary<string, UserTier>`, values `Free` / `Paid`); tests set it, you read it |
| **Tests** | `RateLimiterTests.cs` (xUnit) |
| **Requires** | .NET SDK 8 or newer (`dotnet --version`). The project targets .NET 8 and rolls forward to 9/10 |

## Setup

```bash
cd csharp
dotnet restore
```

Or from the repo root: `./scripts/verify.sh csharp` (restores, builds, and runs the smoke test).

## Run tests

```bash
dotnet test                                               # full suite, about 40 seconds (paid-window tests really wait)
dotnet test --filter 'FullyQualifiedName~FreeUser'        # only tests whose name matches
dotnet test --logger 'console;verbosity=normal'           # show each test name
```

Or from the repo root: `./scripts/test.sh csharp`, or VS Code **Terminal → Run Task → Tests: C#**.
The solution file `api-rate-limiter-interview.sln` at the repo root opens this project in Visual Studio / Rider.

On a fresh clone every test except `HarnessSmoke` fails: the method is a stub. That is expected.

## Extra credit (clearly labelled in the test file)

- **`ExtraCredit_FreeUpgradesToPaid`** — after a free user is switched to `Paid` in `UserTiers`, requests made while
  free still count toward the current paid window.
- **`ExtraCredit_ConstantsRespected`** — read `Constants.FreeLimit`; change it and re-run to check.
