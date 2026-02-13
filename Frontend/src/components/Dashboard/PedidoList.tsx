import React from "react";
import { Pedido, Usuario } from "../../interfaces";
import { PedidoCard } from "./PedidoCard";

interface PedidoListProps {
    pedidos: Pedido[];
    usuarios: Usuario[];
    loading: boolean;
}

export const PedidoList: React.FC<PedidoListProps> = ({
    pedidos,
    usuarios,
    loading,
}) => {
    const getUsuario = (idUser: number) => usuarios.find((u) => u.id === idUser);

    if (loading) {
        return (
            <div className="text-center py-10 bg-white rounded-3xl border border-slate-100">
                <p className="text-slate-400 font-medium">Cargando...</p>
            </div>
        );
    }

    if (pedidos.length === 0) {
        return (
            <div className="text-center py-10 bg-white rounded-3xl border border-dashed border-slate-200">
                <p className="text-slate-400 font-medium italic">
                    No hay pedidos para este usuario
                </p>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            {pedidos.map((pedido) => (
                <PedidoCard
                    key={pedido.id}
                    pedido={pedido}
                    usuario={getUsuario(pedido.idUser)}
                />
            ))}
        </div>
    );
};
