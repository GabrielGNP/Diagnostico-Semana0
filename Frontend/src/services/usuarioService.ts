import { userApi } from './api';
import { Usuario } from '../interfaces';

export const getUsers = async (): Promise<Usuario[]> => {
    return await userApi.get<Usuario[]>('/users');
};

export const getUserByEmail = async (email: string): Promise<Usuario> => {
    return await userApi.get<Usuario>(`/user/${encodeURIComponent(email)}`);
};

export const addUser = async (payload: {
    name: string;
    mail: string;
    password: string;
    active: boolean;
}): Promise<Usuario> => {
    return await userApi.post<Usuario>('/user/add', payload);
};
