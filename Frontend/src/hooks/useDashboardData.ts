import { useState, useEffect, useCallback } from "react";
import { Usuario, Pedido } from "../interfaces";
import { getUsers } from "../services/usuarioService";
import { getOrders } from "../services/pedidoService";

export const useDashboardData = () => {
    const [usuarios, setUsuarios] = useState<Usuario[]>([]);
    const [pedidos, setPedidos] = useState<Pedido[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchData = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const [usersData, ordersData] = await Promise.all([
                getUsers(),
                getOrders(),
            ]);

            setUsuarios(usersData.filter((u) => u.active));
            setPedidos(ordersData.filter((o) => o.active));
        } catch (err) {
            console.error("Error fetching data:", err);
            setError("Error al cargar los datos. Por favor intente nuevamente.");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    return { usuarios, pedidos, loading, error, refreshData: fetchData };
};
