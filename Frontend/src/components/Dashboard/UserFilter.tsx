import React from "react";
import { ChevronDown } from "lucide-react";
import { Usuario } from "../../interfaces";

interface UserFilterProps {
    usuarios: Usuario[];
    selectedUser: number | "";
    onUserChange: (id: number | "") => void;
}

export const UserFilter: React.FC<UserFilterProps> = ({
    usuarios,
    selectedUser,
    onUserChange,
}) => {
    return (
        <div className="px-6 mb-8">
            <label className="block text-xs font-black text-slate-400 uppercase tracking-widest mb-3 ml-1">
                Seleccionar Usuario
            </label>
            <div className="relative">
                <select
                    className="w-full pl-5 pr-12 py-4 bg-white border-none rounded-2xl shadow-sm appearance-none outline-none focus:ring-2 focus:ring-blue-400 font-bold text-slate-700 transition-all cursor-pointer"
                    value={selectedUser}
                    onChange={(e) =>
                        onUserChange(e.target.value ? Number(e.target.value) : "")
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
    );
};
