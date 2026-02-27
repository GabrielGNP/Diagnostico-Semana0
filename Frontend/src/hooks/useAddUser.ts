import { useState } from 'react';
import { addUser as defaultAddUser } from '../services/usuarioService';
import defaultAlertService, { AlertService } from '../services/alertService';
import type { Usuario } from '../interfaces';

type AddUserPayload = {
  nombre: string;
  email: string;
  password: string;
};

export const useAddUser = (
  addUserFn: typeof defaultAddUser = defaultAddUser,
  alertService: AlertService = defaultAlertService,
) => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createUser = async (payload: AddUserPayload): Promise<Usuario | null> => {
    setLoading(true);
    setError(null);
    try {
      const user = await addUserFn({
        name: payload.nombre,
        mail: payload.email,
        password: payload.password,
        active: true,
      });

      await alertService.success('Usuario creado', 'El usuario fue registrado correctamente.');

      return user;
    } catch (err: any) {
      const message = err instanceof Error ? err.message : 'Error desconocido';
      setError(message);
      await alertService.error('Error al crear usuario', message);
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { loading, error, createUser } as const;
};

export default useAddUser;
