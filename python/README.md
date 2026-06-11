# API Rate Limiter — Python

Python implementation of the API rate limiter. See [SPEC.md](../SPEC.md) for the full problem and requirements.

## Setup (VS Code / GitHub Codespaces)

1. Open this folder in VS Code (or open the repo in [GitHub Codespaces](https://github.com/features/codespaces)).
2. Ensure Python 3.10+ is installed (`python3 --version`).
3. Create a virtual environment (recommended) and install dependencies:

   ```bash
   cd python
   python3 -m venv .venv
   source .venv/bin/activate   # Windows: .venv\Scripts\activate
   pip install -r requirements.txt
   ```

## Run tests

From the `python` directory (with venv activated):

```bash
pytest -v
```

For the test that sleeps for the window, default timeout is fine. To run with a longer timeout:

```bash
pytest -v --timeout=15
```

(Requires `pytest-timeout`; optional.)

## User tier map

Tests set a user's tier via the module-level `user_tiers` dict (e.g. `user_tiers["user1"] = "free"`). The rate limiter reads from this map to decide which limits apply.

## Extra credit

The test **`test_extra_credit_free_upgrades_to_paid`** is optional. It verifies that when a free user is upgraded to paid, past requests (made when free) still count toward the paid 2-per-window limit. It is clearly labeled as extra credit in the test name and comment.

## Hard mode: concurrency

The tests **`test_hard_mode_*`** are skipped by default. They release 50 threads at once against one user and require that **exactly** the limit is allowed. The test shrinks the interpreter's thread switch interval to make the check-then-record race likely despite the GIL; note an unsafe implementation may still pass occasionally, so be ready to defend the locking strategy. Enable with:

```bash
HARD_MODE=1 pytest -v
```
