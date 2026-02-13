import { describe, it, expect, beforeEach, vi } from "vitest";
import { ApiClient } from "../api";

const globalAny: any = global;

beforeEach(() => {
  globalAny.fetch = vi.fn();
});

describe("ApiClient.get", () => {
  it("retorna JSON cuando response.ok es true", async () => {
    const client = new ApiClient("https://api.test");
    globalAny.fetch.mockResolvedValue({ ok: true, json: () => Promise.resolve({ ok: true }) });
    const res = await client.get("/foo");
    expect(res).toEqual({ ok: true });
  });

  it("lanza error cuando response.ok es false", async () => {
    const client = new ApiClient("https://api.test");
    globalAny.fetch.mockResolvedValue({ ok: false, status: 500, statusText: "err", text: () => Promise.resolve("boom") });
    await expect(client.get("/foo")).rejects.toThrow();
  });
});

describe("ApiClient.post", () => {
  it("retorna JSON cuando response.ok es true", async () => {
    const client = new ApiClient("https://api.test");
    globalAny.fetch.mockResolvedValue({ ok: true, json: () => Promise.resolve({ ok: true }) });
    const res = await client.post("/foo", { a: 1 });
    expect(res).toEqual({ ok: true });
  });

  it("lanza error cuando response.ok es false", async () => {
    const client = new ApiClient("https://api.test");
    globalAny.fetch.mockResolvedValue({ ok: false, status: 500, statusText: "err", text: () => Promise.resolve("boom") });
    await expect(client.post("/foo", { a: 1 })).rejects.toThrow();
  });
});
