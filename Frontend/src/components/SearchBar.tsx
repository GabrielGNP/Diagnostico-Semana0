import React from "react";
import { Search } from "lucide-react";

interface SearchBarProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  className?: string;
}

const SearchBar: React.FC<SearchBarProps> = ({
  value,
  onChange,
  placeholder = "Buscar...",
  className = "",
}) => {
  return (
    <div className={`relative group ${className}`}>
      <Search
        className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-blue-500 transition-colors"
        size={20}
      />
      <input
        type="text"
        placeholder={placeholder}
        className="w-full pl-12 pr-4 py-4 bg-white border-none rounded-2xl shadow-sm md:shadow-md outline-none focus:ring-2 focus:ring-blue-400 transition-all text-slate-700 placeholder:text-slate-400"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
};

export default SearchBar;
