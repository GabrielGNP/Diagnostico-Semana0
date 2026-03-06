import { describe, it, expect, vi } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { useDashboardData } from "../useDashboardData";
import { getUsers } from "../../services/usuarioService";
import { getOrders } from "../../services/pedidoService";
import { OrderState } from "../../interfaces";

vi.mock("../../services/usuarioService", () => ({
  getUsers: vi.fn(),
}));

vi.mock("../../services/pedidoService", () => ({
  getOrders: vi.fn(),
}));

const getUsersMock = vi.mocked(getUsers);
const getOrdersMock = vi.mocked(getOrders);

describe("useDashboardData", () => {
  it("carga usuarios y pedidos activos", async () => {
    getUsersMock.mockResolvedValue([
      { id: 1, name: "Ana", mail: "ana@x.com", active: true },
      { id: 2, name: "Beto", mail: "beto@x.com", active: false },
    ]);
    getOrdersMock.mockResolvedValue([
      {
        id: 1,
        name: "P1",
        description: "x",
        idUser: 1,
        state: OrderState.PROCESSING,
        active: true,
      },
      {
        id: 2,
        name: "P2",
        description: "y",
        idUser: 1,
        state: OrderState.PROCESSING,
        active: false,
      },
    ]);

    const { result } = renderHook(() => useDashboardData());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBeNull();
    expect(result.current.usuarios).toHaveLength(1);
    expect(result.current.pedidos).toHaveLength(1);
  });

  it("setea error cuando falla la carga", async () => {
    getUsersMock.mockRejectedValue(new Error("boom"));
    getOrdersMock.mockResolvedValue([]);

    const { result } = renderHook(() => useDashboardData());

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.error).toBe(
      "Error al cargar los datos. Por favor intente nuevamente.",
    );
  });
});
