import React from "react";
import { useNavigate } from "react-router-dom";
import { ArrowLeft } from "lucide-react";

interface FormHeaderProps {
  title: string;
  onBack?: () => void;
  showCancel?: boolean;
  onCancel?: () => void;
  cancelText?: string;
}

const FormHeader: React.FC<FormHeaderProps> = ({
  title,
  onBack,
  showCancel = false,
  onCancel,
  cancelText = "Cancelar",
}) => {
  const navigate = useNavigate();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else {
      navigate("/");
    }
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    } else {
      navigate("/");
    }
  };

  return (
    <div className="flex items-center justify-between mb-12">
      <button
        onClick={handleBack}
        className="p-2 -ml-2 hover:bg-slate-50 rounded-full transition-colors"
      >
        <ArrowLeft className="text-slate-800" size={24} />
      </button>
      <h2 className="text-lg font-bold text-slate-800">{title}</h2>
      {showCancel ? (
        <button
          onClick={handleCancel}
          className="text-slate-400 font-medium hover:text-slate-600 px-2"
        >
          {cancelText}
        </button>
      ) : (
        <div className="w-10"></div>
      )}
    </div>
  );
};

export default FormHeader;
