import { orderApi } from './api';
import { Pedido } from '../interfaces';

export const getOrders = async (): Promise<Pedido[]> => {
    return await orderApi.get<Pedido[]>('/order/all');
};
