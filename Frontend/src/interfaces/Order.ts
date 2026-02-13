// DEPRECATED: moved to `src/interfaces/index.ts` — use `Pedido` and `OrderState` from there as source of truth.
// This file kept as a compatibility shim for a short transition period.

import { Pedido, OrderState } from "./index";

export type IOrder = Pedido;
export { OrderState as state };
