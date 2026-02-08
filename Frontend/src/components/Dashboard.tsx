import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Package,
  Home,
  ChevronDown,
  UserPlus,
  PackagePlus,
} from "lucide-react";

interface Usuario {
  id: number;
  name: string;
  mail: string;
  active: boolean;
}

interface Pedido {
  id: number;
  name: string;
  description: string;
  idUser: number;
  state: string;
  active: boolean;
}

const Dashboard = () => {
  const navigate = useNavigate();
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [pedidos, setPedidos] = useState<Pedido[]>([]);
  const [usuarioSeleccionado, setUsuarioSeleccionado] = useState<number | "">(
    "",
  );
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [usersRes, ordersRes] = await Promise.all([
        fetch(`${import.meta.env.VITE_APIUSER}/users`),
        fetch(`${import.meta.env.VITE_APIORDER}/order/all`),
      ]);

      if (usersRes.ok) {
        const usersData = await usersRes.json();
        setUsuarios(usersData.filter((u: Usuario) => u.active));
      }

      if (ordersRes.ok) {
        const ordersData = await ordersRes.json();
        setPedidos(ordersData.filter((o: Pedido) => o.active));
      }
    } catch (error) {
      console.error("Error fetching data:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const pedidosFiltrados = usuarioSeleccionado
    ? pedidos.filter((p) => p.idUser === usuarioSeleccionado)
    : pedidos;

  const getUsuarioEmail = (idUser: number): string => {
    const usuario = usuarios.find((u) => u.id === idUser);
    return usuario?.mail || "Usuario desconocido";
  };

  return (
    <div className="min-h-screen bg-[#F8F9FB] pb-28 font-sans text-slate-900">
      <header className="flex items-center justify-center px-6 py-8">
        <h1 className="text-2xl font-black tracking-tight">Dashboard</h1>
      </header>

      <div className="px-6 mb-8">
        <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-3 ml-1">
          Seleccionar Usuario
        </label>
        <div className="relative">
          <select
            className="w-full pl-5 pr-12 py-4 bg-white border-none rounded-2xl shadow-sm appearance-none outline-none focus:ring-2 focus:ring-blue-400 font-bold text-slate-700 transition-all cursor-pointer"
            value={usuarioSeleccionado}
            onChange={(e) =>
              setUsuarioSeleccionado(
                e.target.value ? Number(e.target.value) : "",
              )
            }
          >
            <option value="">Todos los clientes</option>
            {usuarios.map((u) => (
              <option key={u.id} value={u.id}>
                {u.name} ({u.mail})
              </option>
            ))}
          </select>
          <ChevronDown
            className="absolute right-5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none"
            size={20}
          />
        </div>
      </div>

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

        <div className="space-y-4">
          {loading ? (
            <div className="text-center py-10 bg-white rounded-3xl border border-slate-100">
              <p className="text-slate-400 font-medium">Cargando...</p>
            </div>
          ) : pedidosFiltrados.length > 0 ? (
            pedidosFiltrados.map((pedido) => (
              <button
                key={pedido.id}
                className="bg-white p-4 rounded-3xl flex items-center justify-between border border-slate-50 shadow-sm animate-in fade-in zoom-in duration-300 w-full text-left hover:bg-blue-50 transition-colors"
                onClick={() => navigate(`/order/${pedido.id}`)}
              >
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-slate-50 rounded-2xl flex items-center justify-center text-blue-500">
                    <Package size={24} />
                  </div>
                  <div>
                    <h4 className="font-bold text-slate-900 leading-tight">
                      #{pedido.id} - {pedido.name}
                    </h4>
                    <p className="text-slate-400 text-sm">
                      {getUsuarioEmail(pedido.idUser)}
                    </p>
                  </div>
                </div>
                <span className="text-[9px] font-black px-2 py-1 rounded-md tracking-widest bg-slate-100 text-slate-600 uppercase">
                  {pedido.state}
                </span>
              </button>
            ))
          ) : (
            <div className="text-center py-10 bg-white rounded-3xl border border-dashed border-slate-200">
              <p className="text-slate-400 font-medium italic">
                No hay pedidos para este usuario
              </p>
            </div>
          )}
        </div>
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
