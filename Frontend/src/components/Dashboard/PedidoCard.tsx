import React from "react";
import { Package } from "lucide-react";
import { Pedido, Usuario } from "../../interfaces";

interface PedidoCardProps {
    pedido: Pedido;
    usuario?: Usuario;
}

//Insertar colores de fondo por cada estado de la orden
const OrderStateColors = {
   PROCESSING: 'bg-green-200 text-green-700',
  TRAVELING_TO_WAREHOUSE: 'bg-blue-200 text-blue-700',
  IN_WAREHOUSE: 'bg-yellow-200 text-yellow-700',
  TRAVELING_TO_YOUR_HOUSE: 'bg-indigo-200 text-indigo-700',
  ON_THE_STREET: 'bg-teal-200 text-orange-700',
  SHIPPED: 'bg-orange-200 text-teal-700',
  DELIVERED: 'bg-blue-200 text-teal-700',
  CANCELED: 'bg-red-200 text-red-700',
};

/**
 * Tarjeta que renderiza información resumida de un pedido.
 *
 * Props:
 * - `pedido`: objeto `Pedido` con los datos del pedido
 * - `usuario`: (opcional) objeto `Usuario` para mostrar el email/name
 */
export const PedidoCard: React.FC<PedidoCardProps> = ({ pedido, usuario }) => {
    const stateClasses = OrderStateColors[pedido.state] || 'bg-slate-100 text-slate-600';
    
    return (
        <div className="bg-white p-4 rounded-3xl flex items-center justify-between border border-slate-50 shadow-sm animate-in fade-in zoom-in duration-300">
            <div className="flex items-center gap-4">
                <div className="w-12 h-12 bg-slate-50 rounded-2xl flex items-center justify-center text-blue-500">
                    <Package size={24} />
                </div>
                <div>
                    <h4 className="font-bold text-slate-800 leading-tight">
                        #{pedido.id} - {pedido.name}
                    </h4>
                    <p className="text-slate-400 text-sm">
                        {usuario?.mail || "Usuario desconocido"}
                    </p>
                </div>
            </div>
            <span className={`text-[9px] font-black px-2 py-1 rounded-md tracking-widest ${stateClasses} uppercase`}>
                {pedido.state}
            </span>
        </div>
    );
};
