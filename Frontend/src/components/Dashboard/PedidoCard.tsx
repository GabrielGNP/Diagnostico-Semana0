import React from "react";
import { Package } from "lucide-react";
import { Pedido, Usuario } from "../../interfaces";

interface PedidoCardProps {
    pedido: Pedido;
    usuario?: Usuario;
}

export const PedidoCard: React.FC<PedidoCardProps> = ({ pedido, usuario }) => {
    return (
        <div className="bg-white p-4 rounded-3xl flex items-center justify-between border border-slate-50 shadow-sm animate-in fade-in zoom-in duration-300">
            <div className="flex items-center gap-4">
                <div className="w-12 h-12 bg-slate-50 rounded-2xl flex items-center justify-center text-blue-500">
                    <Package size={24} />
                </div>
                <div>
                    <h4 className="font-bold text-slate-900 leading-tight">
                        #{pedido.id} - {pedido.name}
                    </h4>
                    <p className="text-slate-400 text-sm">
                        {usuario?.mail || "Usuario desconocido"}
                    </p>
                </div>
            </div>
            <span className="text-[9px] font-black px-2 py-1 rounded-md tracking-widest bg-slate-100 text-slate-600 uppercase">
                {pedido.state}
            </span>
        </div>
    );
};
