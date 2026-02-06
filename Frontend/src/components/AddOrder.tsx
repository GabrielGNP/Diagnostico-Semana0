import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Mail, ArrowRight, Minus, Plus } from "lucide-react";

interface FormData {
  email: string;
  producto: string;
  cantidad: number;
  notas: string;
}

const AddOrder: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<FormData>({
    email: "",
    producto: "",
    cantidad: 1,
    notas: "",
  });

  const handleCantidad = (valor: number): void => {
    setFormData((prev) => ({
      ...prev,
      cantidad: Math.max(1, prev.cantidad + valor),
    }));
  };

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>): void => {
    e.preventDefault();
    console.log("Pedido enviado:", formData);
    // Aquí conectarías con tu endpoint POST
    navigate("/");
  };

  return (
    <div className="min-h-screen bg-slate-50 md:py-12 px-4">
      <div className="max-w-xl mx-auto bg-white min-h-screen md:min-h-fit md:rounded-[2.5rem] md:shadow-xl md:shadow-slate-200/50 overflow-hidden border border-slate-100">
        {/* Header de navegación */}
        <div className="flex justify-between items-center p-6 border-b border-slate-50">
          <button
            onClick={() => navigate("/")}
            className="p-2 hover:bg-slate-50 rounded-full transition-colors"
          >
            <ArrowLeft className="text-slate-600" size={24} />
          </button>
          <h2 className="font-bold text-slate-800 text-lg">Nuevo Pedido</h2>
          <button
            onClick={() => navigate("/")}
            className="text-slate-400 font-medium hover:text-slate-600 px-2"
          >
            Cancelar
          </button>
        </div>

        <div className="p-8">
          {/* Título y descripción */}
          <div className="mb-10">
            <h1 className="text-3xl font-extrabold text-slate-900 mb-3 tracking-tight">
              Crear Pedido
            </h1>
            <p className="text-slate-500 leading-relaxed">
              Introduce el correo electrónico del usuario para vincular el
              pedido rápidamente sin necesidad de iniciar sesión.
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-8">
            {/* Campo Email */}
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

            {/* Campo Producto */}
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

            {/* Campo Notas */}
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

            {/* Botón de envío */}
            <button
              type="submit"
              className="w-full py-5 bg-blue-500 hover:bg-blue-600 text-white font-bold rounded-3xl shadow-lg shadow-blue-200 active:scale-[0.98] transition-all flex items-center justify-center gap-2 mt-4"
            >
              Crear Pedido
              <ArrowRight size={20} />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AddOrder;
