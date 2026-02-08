import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Mail, ArrowRight } from "lucide-react";
import FormHeader from "./FormHeader";
import FormTitleAndSubtitle from "./FormTitleAndSubtitle";

interface FormData {
  email: string;
  producto: string;
  cantidad: number;
  notas: string;
}

const AddOrder: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState<FormData>({
    email: "",
    producto: "",
    cantidad: 1,
    notas: "",
  });

  const handleSubmit = async (
    e: React.FormEvent<HTMLFormElement>,
  ): Promise<void> => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const userResponse = await fetch(
        `${import.meta.env.VITE_APIUSER}/user/${formData.email}`,
      );

      let idUser: number;
      if (userResponse.ok) {
        const userData = await userResponse.json();
        idUser = userData.id;
      } else {
        throw new Error("Usuario no encontrado. Verifica el email.");
      }

      const response = await fetch(
        `${import.meta.env.VITE_APIORDER}/order/add`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            id: 0,
            name: formData.producto,
            description: formData.notas || formData.producto,
            idUser: idUser,
            state: "PROCESSING",
            active: true,
          }),
        },
      );

      if (!response.ok) {
        throw new Error("Error al crear el pedido");
      }

      console.log("Pedido creado exitosamente");
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error desconocido");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 md:bg-slate-50 flex items-center justify-center p-4">
      <div className="w-full max-w-xl bg-white md:p-10 md:rounded-[2.5rem] md:shadow-xl md:shadow-slate-200/50">
        <FormHeader title="Nuevo Pedido" cancelText="Cancelar" showCancel />

        <div className="p-8">
          <FormTitleAndSubtitle
            title="Nuevo Pedido"
            subtitle="Introduce el correo electrónico del usuario para vincular el pedido rápidamente sin necesidad de iniciar sesión."
          />

          <form onSubmit={handleSubmit} className="space-y-8">
            {error && (
              <div className="bg-red-50 text-red-600 px-4 py-3 rounded-xl text-sm font-medium">
                {error}
              </div>
            )}
            <div className="space-y-3">
              <label className="text-sm font-bold text-slate-800 ml-1">
                Email del Usuario
              </label>
              <div className="relative group">
                <input
                  type="email"
                  placeholder="ejemplo@correo.com"
                  className="w-full px-5 py-4 bg-white border-2 border-slate-100 rounded-2xl outline-none focus:border-blue-400 transition-all text-slate-700 placeholder:text-slate-300 shadow-sm"
                  value={formData.email}
                  onChange={(e) =>
                    setFormData({ ...formData, email: e.target.value })
                  }
                  required
                />
                <Mail
                  className="absolute right-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-blue-400 transition-colors"
                  size={20}
                />
              </div>
            </div>

            <div className="space-y-3">
              <label className="text-sm font-bold text-slate-800 ml-1">
                Nombre del Producto
              </label>
              <input
                type="text"
                placeholder="Ej. Zapatillas Running X"
                className="w-full px-5 py-4 bg-slate-50 border-none rounded-2xl outline-none focus:ring-2 focus:ring-blue-100 transition-all text-slate-700 placeholder:text-slate-400"
                value={formData.producto}
                onChange={(e) =>
                  setFormData({ ...formData, producto: e.target.value })
                }
                required
              />
            </div>

            <div className="space-y-3">
              <label className="text-sm font-bold text-slate-800 ml-1">
                Notas adicionales
              </label>
              <textarea
                placeholder="Instrucciones especiales de entrega, envoltorio para regalo, etc."
                rows={4}
                className="w-full px-5 py-4 bg-slate-50 border-none rounded-2xl outline-none focus:ring-2 focus:ring-blue-100 transition-all text-slate-700 placeholder:text-slate-400 resize-none"
                value={formData.notas}
                onChange={(e) =>
                  setFormData({ ...formData, notas: e.target.value })
                }
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-5 bg-blue-500 hover:bg-blue-600 disabled:bg-blue-300 text-white font-bold rounded-3xl shadow-lg shadow-blue-200 active:scale-[0.98] transition-all flex items-center justify-center gap-2 mt-4"
            >
              {loading ? "Creando..." : "Crear Pedido"}
              {!loading && <ArrowRight size={20} />}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AddOrder;
