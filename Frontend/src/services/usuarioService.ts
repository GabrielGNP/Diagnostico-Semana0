import { userApi } from './api';
import { Usuario } from '../interfaces';

export const getUsers = async (): Promise<Usuario[]> => {
    return await userApi.get<Usuario[]>('/users');
};
