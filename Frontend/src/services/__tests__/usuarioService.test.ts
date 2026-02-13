import { describe, it, expect, beforeEach, vi } from "vitest";
import { getUsers } from "../usuarioService";

const globalAny: any = global;

beforeEach(() => {
  globalAny.fetch = vi.fn();
});

describe("usuarioService", () => {
  it("getUsers devuelve lista de usuarios activos desde userApi", async () => {
    const mockUsers = [
      { id: 1, name: "Ana", mail: "ana@x.com", active: true },
      { id: 2, name: "Beto", mail: "beto@x.com", active: false },
    ];
    globalAny.fetch.mockResolvedValue({ ok: true, json: () => Promise.resolve(mockUsers) });

    const res = await getUsers();
    expect(res).toEqual(mockUsers);
    expect(globalAny.fetch).toHaveBeenCalled();
  });

  it("propaga error cuando fetch devuelve non-ok", async () => {
    globalAny.fetch.mockResolvedValue({ ok: false, status: 500, statusText: "err", text: () => Promise.resolve("boom") });
    await expect(getUsers()).rejects.toThrow();
  });
});
