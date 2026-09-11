import { defineConfig } from 'vitest/config';
import { WINDOW_SECONDS } from './src/constants';

// The longest test waits for the paid window twice. Derive the timeout from the
// constant so changing WINDOW_SECONDS never causes spurious timeouts.
const WAIT_MS = (WINDOW_SECONDS + 1) * 1000;

export default defineConfig({
  test: {
    testTimeout: 3 * WAIT_MS + 5_000,
    // Tests share one module-level userTiers Map, so run the file single-threaded.
    fileParallelism: false,
  },
});
