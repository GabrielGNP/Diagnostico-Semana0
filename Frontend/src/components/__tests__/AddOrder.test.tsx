import { describe, it, expect, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import AddOrder from "../AddOrder";
import { getUserByEmail } from "../../services/usuarioService";
import { addOrder } from "../../services/pedidoService";
import { OrderState } from "../../interfaces";

const navigateMock = vi.fn();

vi.mock("react-router-dom", () => ({
  useNavigate: () => navigateMock,
}));

vi.mock("../../services/usuarioService", () => ({
  getUserByEmail: vi.fn(),
}));

vi.mock("../../services/pedidoService", () => ({
  addOrder: vi.fn(),
}));

const getUserByEmailMock = vi.mocked(getUserByEmail);
const addOrderMock = vi.mocked(addOrder);

describe("AddOrder", () => {
  it("envia el formulario y crea pedido", async () => {
    getUserByEmailMock.mockResolvedValue({
      id: 5,
      name: "Ana",
      mail: "ana@x.com",
      active: true,
    });
    addOrderMock.mockResolvedValue({
      id: 10,
      name: "Zapatillas",
      description: "Nota",
      idUser: 5,
      state: OrderState.PROCESSING,
      active: true,
    });

    render(<AddOrder />);

    fireEvent.change(screen.getByPlaceholderText("ejemplo@correo.com"), {
      target: { value: "ana@x.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Ej. Zapatillas Running X"), {
      target: { value: "Zapatillas" },
    });
    fireEvent.change(
      screen.getByPlaceholderText(
        "Instrucciones especiales de entrega, envoltorio para regalo, etc.",
      ),
      { target: { value: "Nota" } },
    );

    fireEvent.submit(screen.getByRole("button", { name: /crear pedido/i }));

    await waitFor(() => {
      expect(getUserByEmailMock).toHaveBeenCalledWith("ana@x.com");
      expect(addOrderMock).toHaveBeenCalledWith({
        id: 0,
        name: "Zapatillas",
        description: "Nota",
        idUser: 5,
        state: OrderState.PROCESSING,
        active: true,
      });
    });
  });
});
