import { orderApi } from './api';
import { Pedido, OrderState } from '../interfaces';

export const getOrders = async (): Promise<Pedido[]> => {
    return await orderApi.get<Pedido[]>('/order/all');
};

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
