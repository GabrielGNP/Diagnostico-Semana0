import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";

interface Pedido {
  id: number;
  name: string;
  description: string;
  idUser: number;
  state: string;
  active: boolean;
}

const OrderDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [pedido, setPedido] = useState<Pedido | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPedido = async () => {
      setLoading(true);
      try {
        const res = await fetch(`${import.meta.env.VITE_APIORDER}/order/${id}`);
        if (!res.ok) throw new Error("Pedido no encontrado");
        const data = await res.json();
        setPedido(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : String(err));
      } finally {
        setLoading(false);
      }
    };
    fetchPedido();
  }, [id]);

  if (loading) return <div className="p-10 text-center">Cargando...</div>;
  if (error)
    return <div className="p-10 text-center text-red-500">{error}</div>;
  if (!pedido) return null;

  return (
    <div className="min-h-screen bg-[#F8F9FB] flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-xl bg-white p-10 rounded-3xl shadow-xl">
        <button
          onClick={() => navigate(-1)}
          className="mb-6 p-2 hover:bg-slate-50 rounded-full"
        >
          <ArrowLeft className="text-slate-800" size={24} />
        </button>
        <h2 className="text-2xl font-black mb-4">Pedido #{pedido.id}</h2>
        <p className="text-lg font-bold mb-2">{pedido.name}</p>
        <p className="text-slate-500 mb-4">{pedido.description}</p>
        <div className="mb-2">
          <span className="font-bold text-slate-700">Estado:</span>{" "}
          {pedido.state}
        </div>
        <div>
          <span className="font-bold text-slate-700">Usuario ID:</span>{" "}
          {pedido.idUser}
        </div>
      </div>
    </div>
  );
};

export default OrderDetail;
