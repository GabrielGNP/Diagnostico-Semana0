import { userApi } from './api';
import { Usuario } from '../interfaces';

export const getUsers = async (): Promise<Usuario[]> => {
    return await userApi.get<Usuario[]>('/api/v1/usuarios');
};

export const getUserByEmail = async (email: string): Promise<Usuario> => {
    return await userApi.get<Usuario>(`/api/v1/usuarios/${encodeURIComponent(email)}`);
};

export const addUser = async (payload: {
    name: string;
    mail: string;
    password: string;
    active: boolean;
}): Promise<Usuario> => {
    return await userApi.post<Usuario>('/api/v1/usuarios', payload);
};
