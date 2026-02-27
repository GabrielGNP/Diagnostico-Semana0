import Swal from 'sweetalert2';
import withReactContent from 'sweetalert2-react-content';

export type AlertService = {
  success: (title: string, text?: string) => Promise<void>;
  error: (title: string, text?: string) => Promise<void>;
  info?: (title: string, text?: string) => Promise<void>;
};

const MySwal = withReactContent(Swal);

export const defaultAlertService: AlertService = {
  success: async (title: string, text?: string) => {
    await MySwal.fire({
      title,
      text,
      icon: 'success',
      confirmButtonText: 'Aceptar',
    });
  },
  error: async (title: string, text?: string) => {
    await MySwal.fire({
      title,
      text,
      icon: 'error',
      confirmButtonText: 'Cerrar',
    });
  },
};

export default defaultAlertService;
