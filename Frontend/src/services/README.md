# Services layer

This folder exposes a small service layer over the API client.
It keeps network logic out of UI components and centralizes error handling.

## Files
- api.ts: ApiClient wrapper with get/post helpers
- usuarioService.ts: users operations (getUsers, getUserByEmail, addUser)
- pedidoService.ts: orders operations (getOrders, addOrder)

## Usage
```ts
import { getUsers } from "../services/usuarioService";
import { getOrders } from "../services/pedidoService";

const users = await getUsers();
const orders = await getOrders();
```

## Notes
- Keep all fetch calls inside services (no direct fetch in components).
- Add new endpoints here first, then update hooks/components.
