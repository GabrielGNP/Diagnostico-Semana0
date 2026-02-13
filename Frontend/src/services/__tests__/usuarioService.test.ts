import { describe, it, expect, beforeEach, vi } from "vitest";
import { addUser, getUserByEmail, getUsers } from "../usuarioService";

beforeEach(() => {
  const fetchMock = vi.fn();
  globalThis.fetch = fetchMock as unknown as typeof fetch;
});

describe("usuarioService", () => {
  it("getUsers devuelve lista de usuarios activos desde userApi", async () => {
    const mockUsers = [
      { id: 1, name: "Ana", mail: "ana@x.com", active: true },
      { id: 2, name: "Beto", mail: "beto@x.com", active: false },
    ];
    const fetchMock = globalThis.fetch as unknown as ReturnType<typeof vi.fn>;
    fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve(mockUsers) });

    const res = await getUsers();
    expect(res).toEqual(mockUsers);
    expect(fetchMock).toHaveBeenCalled();
  });

  it("propaga error cuando fetch devuelve non-ok", async () => {
    const fetchMock = globalThis.fetch as unknown as ReturnType<typeof vi.fn>;
    fetchMock.mockResolvedValue({ ok: false, status: 500, statusText: "err", text: () => Promise.resolve("boom") });
    await expect(getUsers()).rejects.toThrow();
  });

  it("getUserByEmail devuelve el usuario", async () => {
    const mockUser = { id: 9, name: "Leo", mail: "leo@x.com", active: true };
    const fetchMock = globalThis.fetch as unknown as ReturnType<typeof vi.fn>;
    fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve(mockUser) });

    const res = await getUserByEmail("leo@x.com");
    expect(res).toEqual(mockUser);
  });

  it("addUser crea usuario y retorna respuesta", async () => {
    const mockUser = { id: 10, name: "Mia", mail: "mia@x.com", active: true };
    const fetchMock = globalThis.fetch as unknown as ReturnType<typeof vi.fn>;
    fetchMock.mockResolvedValue({ ok: true, json: () => Promise.resolve(mockUser) });

    const res = await addUser({ name: "Mia", mail: "mia@x.com", password: "12345678", active: true });
    expect(res).toEqual(mockUser);
  });
});
