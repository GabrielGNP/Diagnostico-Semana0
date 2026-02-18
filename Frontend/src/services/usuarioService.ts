import { userApi } from './api';
import { Usuario } from '../interfaces';

/**
 * Retrieve all users from the usuario-service.
 */
export const getUsers = async (): Promise<Usuario[]> => {
    return await userApi.get<Usuario[]>('/api/v1/usuarios');
};

/**
 * Retrieve a user by email. The email is URL-encoded.
 * @param email user's email address
 */
export const getUserByEmail = async (email: string): Promise<Usuario> => {
    return await userApi.get<Usuario>(`/api/v1/usuarios/${encodeURIComponent(email)}`);
};

/**
 * Create a new user in the usuario-service.
 */
export const addUser = async (payload: {
    name: string;
    mail: string;
    password: string;
    active: boolean;
}): Promise<Usuario> => {
    return await userApi.post<Usuario>('/api/v1/usuarios', payload);
};
