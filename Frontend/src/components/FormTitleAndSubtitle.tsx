import React from "react";

interface FormTitleAndSubtitleProps {
  title: string;
  subtitle: string;
  align?: "center" | "left";
}

const FormTitleAndSubtitle: React.FC<FormTitleAndSubtitleProps> = ({
  title,
  subtitle,
  align = "center",
}) => {
  return (
    <div
      className={`mb-10 ${align === "center" ? "text-center md:text-left" : "text-left"}`}
    >
      <h1 className="text-3xl font-extrabold text-slate-900 mb-3 tracking-tight">
        {title}
      </h1>
      <p className="text-slate-500 text-lg leading-relaxed">{subtitle}</p>
    </div>
  );
};

export default FormTitleAndSubtitle;
