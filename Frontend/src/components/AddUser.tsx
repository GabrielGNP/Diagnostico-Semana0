import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft, Mail, Lock, Eye, EyeOff } from "lucide-react";

interface FormData {
  nombre: string;
  email: string;
  password: string;
}

const AddUser: React.FC = () => {
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState<FormData>({
    nombre: "",
    email: "",
    password: "",
  });

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>): void => {
    e.preventDefault();
    console.log("Registro enviado:", formData);
    // Aquí conectarías con tu endpoint POST /usuarios
    navigate("/");
  };

  return (
    <div className="min-h-screen bg-white md:bg-slate-50 flex items-center justify-center p-4">
      {/* Contenedor Principal con efecto responsive */}
      <div className="w-full max-w-md bg-white md:p-10 md:rounded-[2.5rem] md:shadow-xl md:shadow-slate-200/50">
        {/* Header con botón atrás */}
        <div className="flex items-center justify-between mb-12">
          <button
            onClick={() => navigate("/")}
            className="p-2 -ml-2 hover:bg-slate-50 rounded-full transition-colors"
          >
            <ArrowLeft className="text-slate-800" size={24} />
          </button>
          <h2 className="text-lg font-bold text-slate-800">Crear cuenta</h2>
          <div className="w-10"></div>
          {/* Espaciador para centrar el título */}
        </div>

        {/* Títulos */}
        <div className="mb-10 text-center md:text-left">
          <h1 className="text-3xl font-extrabold text-slate-900 mb-3 tracking-tight">
            Registro de Administrador
          </h1>
          <p className="text-slate-500 text-lg leading-relaxed">
            Ingresa tus datos para gestionar pedidos y clientes.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Campo Nombre */}
          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-800 ml-1">
              Nombre completo
            </label>
            <input
              type="text"
              placeholder="Ej. Juan Pérez"
              className="w-full px-6 py-4 bg-slate-50 border-none rounded-2xl outline-none focus:ring-2 focus:ring-blue-100 transition-all text-slate-700 placeholder:text-slate-400"
              value={formData.nombre}
              onChange={(e) =>
                setFormData({ ...formData, nombre: e.target.value })
              }
              required
            />
          </div>

          {/* Campo Email */}
          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-800 ml-1">
              Correo electrónico
            </label>
            <div className="relative group">
              <input
                type="email"
                placeholder="nombre@empresa.com"
                className="w-full pl-14 pr-6 py-4 bg-slate-50 border-none rounded-2xl outline-none focus:ring-2 focus:ring-blue-100 transition-all text-slate-700 placeholder:text-slate-400"
                value={formData.email}
                onChange={(e) =>
                  setFormData({ ...formData, email: e.target.value })
                }
                required
              />
              <Mail
                className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-400"
                size={20}
              />
            </div>
          </div>

          {/* Campo Contraseña */}
          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-800 ml-1">
              Contraseña
            </label>
            <div className="relative group">
              <input
                type={showPassword ? "text" : "password"}
                placeholder="••••••••"
                className="w-full pl-14 pr-14 py-4 bg-slate-50 border-none rounded-2xl outline-none focus:ring-2 focus:ring-blue-100 transition-all text-slate-700 placeholder:text-slate-400"
                value={formData.password}
                onChange={(e) =>
                  setFormData({ ...formData, password: e.target.value })
                }
                required
                minLength={8}
              />
              <Lock
                className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-400"
                size={20}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
              >
                {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </div>
            <p className="text-xs text-slate-400 ml-1">Mínimo 8 caracteres</p>
          </div>

          {/* Botón Registrar */}
          <button
            type="submit"
            className="w-full py-5 bg-[#2589f5] hover:bg-blue-600 text-white font-bold rounded-2xl shadow-lg shadow-blue-200 active:scale-[0.98] transition-all flex items-center justify-center mt-4"
          >
            Registrar Administrador
          </button>
        </form>

        {/* Footer del formulario */}
        <div className="mt-12 text-center">
          <p className="text-slate-500">
            ¿Ya tienes una cuenta?{" "}
            <button className="text-[#2589f5] font-bold hover:underline">
              Inicia sesión
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default AddUser;
