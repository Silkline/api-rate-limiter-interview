# API Rate Limiter — Python

Problem and requirements: [SPEC.md](../SPEC.md). Interview flow: [INTERVIEW.md](../INTERVIEW.md).

| | |
| --- | --- |
| **File to implement** | `rate_limiter.py` → `def rate_limiter(user_id: str) -> bool` |
| **Constants** | same file (`FREE_LIMIT`, `PAID_LIMIT`, `WINDOW_SECONDS`) |
| **User tier map** | `user_tiers` (module-level `dict[str, str]`, values `"free"` / `"paid"`); tests set it, you read it |
| **Tests** | `test_rate_limiter.py` (pytest) |
| **Requires** | Python 3.10+ (`python3 --version`) |

## Setup

```bash
cd python
python3 -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
pip install -r requirements.txt
```

Or from the repo root: `./scripts/verify.sh python` (creates `python/.venv`, installs pytest, runs the smoke test).

## Run tests

```bash
pytest -v                        # full suite, about 40 seconds (paid-window tests really wait)
pytest -v -k free_user           # only tests whose name matches
```

Or from the repo root: `./scripts/test.sh python` (uses `python/.venv` automatically), or VS Code
**Terminal → Run Task → Tests: Python**.

On a fresh clone every test except `test_harness_smoke` fails: the function is a stub. That is expected.

## Extra credit (clearly labelled in the test file)

- **`test_extra_credit_free_upgrades_to_paid`** — after a free user is switched to `"paid"` in `user_tiers`, requests
  made while free still count toward the current paid window.
- **`test_extra_credit_constants_respected`** — the test overrides `FREE_LIMIT` at runtime, so read the module-level
  constant on every call instead of copying it.
