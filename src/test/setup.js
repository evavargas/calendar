class MemoryStorage {
  #items = new Map();

  getItem(key) {
    return this.#items.get(String(key)) ?? null;
  }

  setItem(key, value) {
    this.#items.set(String(key), String(value));
  }

  removeItem(key) {
    this.#items.delete(String(key));
  }

  clear() {
    this.#items.clear();
  }
}

const storage = new MemoryStorage();

Object.defineProperty(window, "localStorage", {
  configurable: true,
  value: storage,
});
Object.defineProperty(globalThis, "localStorage", {
  configurable: true,
  value: storage,
});
