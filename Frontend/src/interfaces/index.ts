export interface Usuario {
    id: number;
    name: string;
    mail: string;
    active: boolean;
}

export enum OrderState {
    PROCESSING = "PROCESSING",
    TRAVELING_TO_WAREHOUSE = "TRAVELING_TO_WAREHOUSE",
    IN_WAREHOUSE = "IN_WAREHOUSE",
    TRAVELING_TO_YOUR_HOUSE = "TRAVELING_TO_YOUR_HOUSE",
    ON_THE_STREET = "ON_THE_STREET",
    DELIVERED = "DELIVERED",
    CANCELED = "CANCELED",
}

export interface Pedido {
    id: number;
    name: string;
    description: string;
    idUser: number;
    state: OrderState;
    active: boolean;
}
