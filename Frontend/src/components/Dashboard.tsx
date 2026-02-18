import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Home, UserPlus, PackagePlus } from "lucide-react";
import { useDashboardData } from "../hooks/useDashboardData";
import { UserFilter } from "./Dashboard/UserFilter";
import { PedidoList } from "./Dashboard/PedidoList";

/**
 * Vista principal del Dashboard que muestra filtros de usuarios y la lista
 * de pedidos.
 *
 * - Usa `useDashboardData` para la carga inicial y estados de error/carga.
 * - Permite filtrar pedidos por usuario seleccionado.
 */
const Dashboard = () => {
  const navigate = useNavigate();
  const { usuarios, pedidos, loading, error } = useDashboardData();
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState<number | "">(
    "",
  );

  const pedidosFiltrados = usuarioSeleccionado
    ? pedidos.filter((p) => p.idUser === usuarioSeleccionado)
    : pedidos;

  return (
    <div className="min-h-screen bg-[#F8F9FB] pb-28 font-sans text-slate-900">
      <header className="flex items-center justify-center px-6 py-8">
        <h1 className="text-2xl font-black tracking-tight">Dashboard</h1>
      </header>

      {error && (
        <div className="px-6 mb-4">
          <div className="bg-red-50 text-red-600 px-4 py-3 rounded-xl text-sm font-medium">
            {error}
          </div>
        </div>
      )}

      <UserFilter
        usuarios={usuarios}
        selectedUser={usuarioSeleccionado}
        onUserChange={setUsuarioSeleccionado}
      />

      <div className="px-6">
        <div className="flex justify-between items-center mb-6">
          <h3 className="text-xl font-black text-[#1A1C1E]">
            {usuarioSeleccionado
              ? `Pedidos del usuario #${usuarioSeleccionado}`
              : "Todos los Pedidos"}
          </h3>
          <span className="bg-blue-50 text-blue-600 px-3 py-1 rounded-full text-xs font-bold">
            {pedidosFiltrados.length} items
          </span>
        </div>

        <PedidoList
          pedidos={pedidosFiltrados}
          usuarios={usuarios}
          loading={loading}
        />
      </div>

      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-slate-100 h-20 flex items-center justify-around z-50">
        <button className="flex flex-col items-center gap-1 text-blue-500">
          <Home size={24} />
          <span className="text-[10px] font-bold">Inicio</span>
        </button>
        <button
          onClick={() => navigate("/addUser")}
          className="flex flex-col items-center gap-1 text-slate-600 hover:text-blue-500 transition-colors"
        >
          <UserPlus size={24} />
          <span className="text-[10px] font-bold">Agregar Usuario</span>
        </button>
        <button
          onClick={() => navigate("/addOrder")}
          className="flex flex-col items-center gap-1 text-slate-600 hover:text-blue-500 transition-colors"
        >
          <PackagePlus size={24} />
          <span className="text-[10px] font-bold">Agregar Pedido</span>
        </button>
      </nav>
    </div>
  );
};

export default Dashboard;
