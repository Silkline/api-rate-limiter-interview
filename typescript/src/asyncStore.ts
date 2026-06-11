/**
 * HARD MODE: a tiny async key-value store that simulates I/O latency, like a
 * Redis or database client. Every get/set yields to the event loop, so two
 * in-flight rateLimiterAsync calls can interleave between a get (check) and a
 * set (record) — the same check-then-act race a multi-threaded limiter has,
 * even though Node runs on a single thread.
 */
export class AsyncStore<T> {
  private data = new Map<string, T>();

  async get(key: string): Promise<T | undefined> {
    await simulatedLatency();
    return this.data.get(key);
  }

  async set(key: string, value: T): Promise<void> {
    await simulatedLatency();
    this.data.set(key, value);
  }

  /** Test helper: wipe all state between tests. */
  clear(): void {
    this.data.clear();
  }
}

function simulatedLatency(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, Math.random() * 2));
}
