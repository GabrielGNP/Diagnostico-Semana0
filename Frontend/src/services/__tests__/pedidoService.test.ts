import { describe, it, expect, beforeEach, vi } from "vitest";
import { getOrders } from "../pedidoService";

const globalAny: any = global;

beforeEach(() => {
  globalAny.fetch = vi.fn();
});

describe("pedidoService", () => {
  it("getOrders devuelve lista de pedidos desde orderApi", async () => {
    const mockOrders = [
      { id: 1, name: "P1", description: "x", idUser: 1, state: "PROCESSING", active: true },
    ];
    globalAny.fetch.mockResolvedValue({ ok: true, json: () => Promise.resolve(mockOrders) });

    const res = await getOrders();
    expect(res).toEqual(mockOrders);
    expect(globalAny.fetch).toHaveBeenCalled();
  });

  it("propaga error cuando fetch devuelve non-ok", async () => {
    globalAny.fetch.mockResolvedValue({ ok: false, status: 500, statusText: "err", text: () => Promise.resolve("boom") });
    await expect(getOrders()).rejects.toThrow();
  });
});
