import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import AddUser from "./components/AddUser";
import AddOrder from "./components/AddOrder";
import React from "react";

const navigateMock = vi.fn();

vi.mock("react-router-dom", async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock("./services/usuarioService", () => ({
  addUser: vi.fn(),
  getUserByEmail: vi.fn(),
}));

vi.mock("./services/pedidoService", () => ({
  addOrder: vi.fn(),
}));

describe("AddUser", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renderiza el formulario de usuario", () => {
    render(
      <MemoryRouter>
        <AddUser />
      </MemoryRouter>,
    );
    expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email|correo/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/contraseña/i)).toBeInTheDocument();
  });

  it("permite ingresar datos en el formulario", () => {
    render(
      <MemoryRouter>
        <AddUser />
      </MemoryRouter>,
    );
    const nombreInput = screen.getByLabelText(/nombre/i);
    const emailInput = screen.getByLabelText(/email|correo/i);
    
    fireEvent.change(nombreInput, { target: { value: "Juan" } });
    fireEvent.change(emailInput, { target: { value: "juan@mail.com" } });
    
    expect(nombreInput.value).toBe("Juan");
    expect(emailInput.value).toBe("juan@mail.com");
  });
});

describe("AddOrder", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renderiza el formulario de pedido", () => {
    render(
      <MemoryRouter>
        <AddOrder />
      </MemoryRouter>,
    );
    expect(screen.getByLabelText(/email del usuario/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/nombre del producto/i)).toBeInTheDocument();
  });

  it("permite ingresar datos en el formulario", () => {
    render(
      <MemoryRouter>
        <AddOrder />
      </MemoryRouter>,
    );
    const emailInput = screen.getByLabelText(/email del usuario/i);
    const productoInput = screen.getByLabelText(/nombre del producto/i);
    
    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(productoInput, { target: { value: "Zapatillas" } });
    
    expect(emailInput.value).toBe("test@example.com");
    expect(productoInput.value).toBe("Zapatillas");
  });
});
