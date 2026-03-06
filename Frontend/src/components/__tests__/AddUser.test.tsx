import { describe, it, expect, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import AddUser from "../AddUser";
import { addUser } from "../../services/usuarioService";

const navigateMock = vi.fn();

vi.mock("react-router-dom", () => ({
  useNavigate: () => navigateMock,
}));

vi.mock("../../services/usuarioService", () => ({
  addUser: vi.fn(),
}));

const addUserMock = vi.mocked(addUser);

describe("AddUser", () => {
  it("envia el formulario y llama addUser", async () => {
    addUserMock.mockResolvedValue({
      id: 1,
      name: "Juan",
      mail: "juan@x.com",
      active: true,
    });

    render(<AddUser />);

    fireEvent.change(screen.getByPlaceholderText("Ej. Juan Pérez"), {
      target: { value: "Juan Perez" },
    });
    fireEvent.change(screen.getByPlaceholderText("nombre@empresa.com"), {
      target: { value: "juan@x.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("••••••••"), {
      target: { value: "12345678" },
    });

    fireEvent.submit(screen.getByRole("button", { name: /registrar/i }));

    await waitFor(() => {
      expect(addUserMock).toHaveBeenCalledWith({
        name: "Juan Perez",
        mail: "juan@x.com",
        password: "12345678",
        active: true,
      });
    });
  });
});
