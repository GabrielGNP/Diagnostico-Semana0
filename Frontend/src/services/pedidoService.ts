import { orderApi } from './api';
import { Pedido, OrderState } from '../interfaces';

/**
 * Fetch all orders from the backend order service.
 * @returns Promise resolving to an array of `Pedido` objects.
 */
export const getOrders = async (): Promise<Pedido[]> => {
    return await orderApi.get<Pedido[]>('/order/all');
};

/**
 * Create a new order by posting to the backend service.
 * @param payload order payload matching the backend DTO shape
 */
export const addOrder = async (payload: {
    id: number;
    name: string;
    description: string;
    idUser: number;
    state: OrderState;
    active: boolean;
}): Promise<Pedido> => {
    return await orderApi.post<Pedido>('/order/add', payload);
};
