import React, { useState } from "react";
import SearchBar from "./SearchBar";
import {
  Package,
  Truck,
  ShoppingBag,
  CheckCircle2,
  UserPlus,
  Home,
  ClipboardList,
  User,
} from "lucide-react";

interface Pedido {
  id: string;
  email: string;
  time: string;
  status: string;
  color: "emerald" | "orange" | "slate";
  icon: React.ReactNode;
}

const Dashboard = () => {
  const [searchTerm, setSearchTerm] = useState("");

  const pedidos: Pedido[] = [
    {
      id: "3029",
      email: "juan.perez@email.com",
      time: "Hace 2h",
      status: "NUEVO",
      color: "emerald",
      icon: <Package className="text-blue-500" />,
    },
    {
      id: "3028",
      email: "maria.gomez@test.com",
      time: "Hace 5h",
      status: "EN RUTA",
      color: "orange",
      icon: <Truck className="text-orange-500" />,
    },
    {
      id: "3027",
      email: "alex.tech@corp.com",
      time: "Ayer",
      status: "PROCESADO",
      color: "slate",
      icon: <ShoppingBag className="text-blue-500" />,
    },
  ];

  const pedidosFiltrados = pedidos.filter(
    (pedido) =>
      pedido.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      pedido.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
      pedido.status.toLowerCase().includes(searchTerm.toLowerCase()),
  );

  return (
    <div className="min-h-screen bg-[#F8F9FB] pb-28 font-sans text-slate-900">
      <header className="flex items-center justify-center px-6 py-8">
        <h1 className="text-2xl font-black tracking-tight text-[#1A1C1E]">
          Dashboard
        </h1>
      </header>
      <div className="px-6 mb-6">
        <SearchBar
          value={searchTerm}
          onChange={setSearchTerm}
          placeholder="Buscar pedido por email..."
        />
      </div>
      <div className="px-6 mb-6">
        <div className="bg-white p-6 rounded-[2rem] shadow-sm border border-slate-50 relative overflow-hidden">
          <div className="flex justify-between items-start">
            <div>
              <p className="text-slate-400 font-medium text-sm mb-1">
                Pedidos Hoy
              </p>
              <h2 className="text-4xl font-black text-[#1A1C1E]">12</h2>{" "}
            </div>
            <div className="bg-emerald-50 text-emerald-600 px-2 py-1 rounded-lg flex items-center gap-1 text-xs font-bold">
              <svg
                width="10"
                height="10"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="4"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="m5 12 7-7 7 7" />
              </svg>
              +10%
            </div>
          </div>
          <div className="mt-4 h-8 w-32 bg-[#D8E4FF] rounded-lg opacity-60"></div>{" "}
        </div>
      </div>
      <div className="px-6 grid grid-cols-2 gap-4 mb-10">
        <div className="bg-white p-5 rounded-[2rem] shadow-sm border border-slate-50 relative overflow-hidden">
          <p className="text-slate-400 font-medium text-sm mb-1">Pendientes</p>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-black text-[#1A1C1E]">5</span>
            <span className="text-xs font-bold text-emerald-500">+2%</span>
          </div>
          <div className="mt-4 h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
            <div className="h-full w-[40%] bg-blue-500 rounded-full"></div>{" "}
          </div>
          <ClipboardList
            className="absolute -right-2 top-4 text-blue-50 opacity-10"
            size={80}
          />
        </div>
        <div className="bg-white p-5 rounded-[2rem] shadow-sm border border-slate-50 relative overflow-hidden">
          <p className="text-slate-400 font-medium text-sm mb-1">Completados</p>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-black text-[#1A1C1E]">28</span>{" "}
            <span className="text-xs font-bold text-emerald-500">+15%</span>
          </div>
          <div className="mt-4 h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
            <div className="h-full w-[85%] bg-emerald-500 rounded-full"></div>{" "}
          </div>
          <CheckCircle2
            className="absolute -right-2 top-4 text-emerald-50 opacity-10"
            size={80}
          />
        </div>
      </div>
      <div className="px-6">
        <div className="flex justify-between items-center mb-6">
          <h3 className="text-xl font-black text-[#1A1C1E]">
            Pedidos Recientes
          </h3>
          <button className="text-blue-500 font-bold text-sm">Ver todo</button>
        </div>
        <div className="space-y-4">
          {pedidosFiltrados.map((pedido) => (
            <PedidoItem
              key={pedido.id}
              id={pedido.id}
              email={pedido.email}
              time={pedido.time}
              status={pedido.status}
              color={pedido.color}
              icon={pedido.icon}
            />
          ))}
          {pedidosFiltrados.length === 0 && (
            <div className="text-center py-8 text-slate-400">
              No se encontraron pedidos
            </div>
          )}
        </div>
      </div>
      <nav className="fixed bottom-0 left-0 right-0 bg-white border-t border-slate-100 h-20 flex items-center justify-around px-4 z-50 shadow-[0_-4px_20px_-4px_rgba(0,0,0,0.03)]">
        <NavItem icon={<Home size={24} />} label="Inicio" active />
        <NavItem icon={<ClipboardList size={24} />} label="Pedidos" />
        <button className="flex flex-col items-center gap-1 min-w-[64px]">
          <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white shadow-md">
            <UserPlus size={20} strokeWidth={2.5} />
          </div>
          <span className="text-[10px] font-bold text-blue-500">Nuevo</span>
        </button>
        <NavItem icon={<User size={24} />} label="Perfil" />
      </nav>
    </div>
  );
};

interface PedidoItemProps {
  id: string;
  email: string;
  time: string;
  status: string;
  color: "emerald" | "orange" | "slate";
  icon: React.ReactNode;
}

const PedidoItem: React.FC<PedidoItemProps> = ({
  id,
  email,
  time,
  status,
  color,
  icon,
}) => {
  const statusColors = {
    emerald: "bg-emerald-50 text-emerald-600",
    orange: "bg-orange-50 text-orange-600",
    slate: "bg-slate-100 text-slate-600",
  };
  return (
    <div className="bg-white p-4 rounded-2xl flex items-center justify-between border border-slate-50 shadow-sm">
      <div className="flex items-center gap-4">
        <div className="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center">
          {icon}
        </div>
        <div>
          <h4 className="font-bold text-slate-900 leading-tight">
            Pedido #{id}
          </h4>
          <p className="text-slate-400 text-sm font-medium">{email}</p>{" "}
        </div>
      </div>
      <div className="flex flex-col items-end gap-2">
        <span className="text-[10px] font-bold text-slate-300">{time}</span>
        <span
          className={`text-[9px] font-black px-2 py-1 rounded-md tracking-widest ${statusColors[color]}`}
        >
          ● {status}
        </span>
      </div>
    </div>
  );
};

interface NavItemProps {
  icon: React.ReactNode;
  label: string;
  active?: boolean;
}

const NavItem: React.FC<NavItemProps> = ({ icon, label, active = false }) => (
  <button
    className={`flex flex-col items-center gap-1 min-w-[64px] ${active ? "text-blue-500" : "text-slate-400"}`}
  >
    {" "}
    {icon} <span className="text-[10px] font-bold">{label}</span>{" "}
  </button>
);
export default Dashboard;
