import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Dashboard from './components/Dashboard';
import { OrderState } from './interfaces';

const navigateMock = vi.fn();

vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal();
  return {
    ...actual,
    useNavigate: () => navigateMock,
  };
});

vi.mock('./hooks/useDashboardData', () => {
  const useDashboardData = vi.fn(() => ({
    usuarios: [
      { id: 1, name: 'Juan', mail: 'juan@test.com', active: true },
      { id: 2, name: 'María', mail: 'maria@test.com', active: true },
    ],
    pedidos: [
      {
        id: 101,
        name: 'Pedido 1',
        description: 'Zapatillas',
        idUser: 1,
        state: OrderState.PROCESSING,
        active: true,
      },
      {
        id: 102,
        name: 'Pedido 2',
        description: 'Remera',
        idUser: 2,
        state: OrderState.DELIVERED,
        active: true,
      },
      {
        id: 103,
        name: 'Pedido 3',
        description: 'Pantalones',
        idUser: 1,
        state: OrderState.IN_WAREHOUSE,
        active: true,
      },
    ],
    loading: false,
    error: null,
    refreshData: vi.fn(),
  }));
  return { useDashboardData };
});

describe('Dashboard Component Integration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('debería filtrar pedidos cuando se selecciona un usuario', async () => {
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>,
    );

    // Verificar que se muestren todos los pedidos al inicio (3 pedidos)
    await waitFor(() => {
      expect(screen.getByText(/Todos los Pedidos/i)).toBeInTheDocument();
      expect(screen.getByText(/3 items/i)).toBeInTheDocument();
    });

    // Seleccionar el usuario Juan (id: 1)
    const select = screen.getByDisplayValue('Todos los clientes');
    fireEvent.change(select, { target: { value: '1' } });

    // Verificar que se filtren los pedidos de Juan (2 pedidos)
    await waitFor(() => {
      expect(screen.getByText(/Pedidos del usuario #1/i)).toBeInTheDocument();
      expect(screen.getByText(/2 items/i)).toBeInTheDocument();
      // Verificar que se muestren los pedidos de Juan (Pedido 1 y Pedido 3)
      expect(screen.getByText(/Pedido 1/i)).toBeInTheDocument();
      expect(screen.getByText(/Pedido 3/i)).toBeInTheDocument();
    });

    // No debería mostrar el pedido 2 de María
    expect(screen.queryByText(/Pedido 2/i)).not.toBeInTheDocument();
  });

  it('debería navegar a AddUser cuando se hace clic en el botón de agregar usuario', async () => {
    render(
      <MemoryRouter>
        <Dashboard />
      </MemoryRouter>,
    );

    // Esperar a que el Dashboard esté renderizado
    await waitFor(() => {
      expect(screen.getByText(/Dashboard/i)).toBeInTheDocument();
    });

    // Buscar y hacer clic en el botón "Agregar Usuario"
    const agregarUsuarioBtn = screen.getByRole('button', { name: /agregar usuario/i });
    fireEvent.click(agregarUsuarioBtn);

    // Verificar que se llamó a navigate con la ruta correcta
    expect(navigateMock).toHaveBeenCalledWith('/addUser');
  });
});
