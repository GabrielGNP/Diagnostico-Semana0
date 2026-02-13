export interface Usuario {
    id: number;
    name: string;
    mail: string;
    active: boolean;
}

export interface Pedido {
    id: number;
    name: string;
    description: string;
    idUser: number;
    state: string;
    active: boolean;
}
