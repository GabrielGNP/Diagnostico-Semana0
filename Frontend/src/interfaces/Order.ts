export interface IOrder {
  id: number;
  name: string;
  description: string;
  userId: number;
  state: state;
  active: boolean;
}

export enum state {
  PROCESSING = "PROCESANDO",
  TRAVELINGTOWAREHOUSE = "VIAJANDO AL DEPOSITO",
  IN_WAREHOUSE = "EN DEPOSITO",
  TRAVELINGTOYOURHOUSE = "VIAJANDO A TU CASA",
  ONTHESTREET = "EN CAMINO",
  DELIVERED = "ENTREGADO",
  CANCELED = "CANCELADO",
}
