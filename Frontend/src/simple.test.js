import { describe, it, expect } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import AddUser from "./components/AddUser";
import AddOrder from "./components/AddOrder";
import Dashboard from "./components/Dashboard";
import React from "react";

describe("AddUser", () => {
  it("renderiza el formulario de usuario", () => {
    render(<AddUser />);
    expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email|correo/i)).toBeInTheDocument();
  });

  it("permite ingresar datos y enviar", async () => {
    render(<AddUser />);
    fireEvent.change(screen.getByLabelText(/nombre/i), {
      target: { value: "Juan" },
    });
    fireEvent.change(screen.getByLabelText(/email|correo/i), {
      target: { value: "juan@mail.com" },
    });
    fireEvent.click(screen.getByRole("button", { name: /agregar|crear/i }));
    // Aquí podrías mockear la función de envío y verificar que se llama
  });

  it("muestra error si faltan campos", async () => {
    render(<AddUser />);
    fireEvent.click(screen.getByRole("button", { name: /agregar|crear/i }));
    expect(
      await screen.findByText(/obligatorio|requerido/i),
    ).toBeInTheDocument();
  });
});

describe("AddOrder", () => {
  it("renderiza el formulario de pedido", () => {
    render(<AddOrder />);
    expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument();
    expect(
      screen.getByLabelText(/descripción|descripcion/i),
    ).toBeInTheDocument();
  });

  it("permite ingresar datos y enviar", async () => {
    render(<AddOrder />);
    fireEvent.change(screen.getByLabelText(/nombre/i), {
      target: { value: "Pedido 1" },
    });
    fireEvent.change(screen.getByLabelText(/descripción|descripcion/i), {
      target: { value: "Detalle" },
    });
    fireEvent.click(screen.getByRole("button", { name: /agregar|crear/i }));
    // Mockear función de envío si es necesario
  });

  it("muestra error si faltan campos", async () => {
    render(<AddOrder />);
    fireEvent.click(screen.getByRole("button", { name: /agregar|crear/i }));
    expect(
      await screen.findByText(/obligatorio|requerido/i),
    ).toBeInTheDocument();
  });
});

describe("Dashboard", () => {
  beforeEach(() => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        json: () => Promise.resolve([]),
        ok: true,
      }),
    );
  });

  it("muestra mensaje de carga", () => {
    render(<Dashboard />);
    expect(screen.getByText(/cargando|loading/i)).toBeInTheDocument();
  });

  it("renderiza listas de usuarios y pedidos", async () => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        json: () => Promise.resolve([{ id: 1, name: "Juan" }]),
        ok: true,
      }),
    );
    render(<Dashboard />);
    await waitFor(() => expect(screen.getByText(/juan/i)).toBeInTheDocument());
  });
});
describe("Simple", () => {
  it("2 + 2 es igual a 4", () => {
    expect(2 + 2).toBe(4);
  });
});
