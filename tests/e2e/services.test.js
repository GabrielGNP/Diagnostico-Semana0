import { describe, it, expect, beforeAll } from 'vitest';
import axios from 'axios';

/**
 * Tests E2E de ejemplo para servicios reales
 * Estos tests verifican que los servicios están corriendo y responden
 */
describe('E2E Real Service Tests (Optional)', () => {
  
  const USUARIO_SERVICE_URL = process.env.USUARIO_SERVICE_URL || 'http://localhost:8083';
  const PEDIDO_SERVICE_URL = process.env.PEDIDO_SERVICE_URL || 'http://localhost:8082';
  const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:3000';

  // Este test se puede descomentar cuando los servicios estén corriendo
  it.skip('should verify usuario-service is running', async () => {
    const response = await axios.get(`${USUARIO_SERVICE_URL}/users`);
    expect(response.status).toBe(200);
    expect(Array.isArray(response.data)).toBe(true);
  });

  it.skip('should verify pedido-service is running', async () => {
    const response = await axios.get(`${PEDIDO_SERVICE_URL}/order/all`);
    expect(response.status).toBe(200);
  });

  it.skip('should verify frontend is running', async () => {
    const response = await axios.get(FRONTEND_URL);
    expect(response.status).toBe(200);
  });

  // Ejemplo de flujo E2E completo
  it.skip('should create user and then create order for that user', async () => {
    // 1. Crear usuario
    const newUser = {
      name: 'Test User',
      mail: 'test@example.com',
      password: 'password123',
      active: true
    };
    
    const userResponse = await axios.post(`${USUARIO_SERVICE_URL}/user/add`, newUser);
    expect(userResponse.status).toBe(201);
    const userId = userResponse.data.id;

    // 2. Crear pedido para ese usuario
    const newOrder = {
      idUser: userId,
      items: ['Item 1', 'Item 2'],
      state: 'PENDING'
    };

    const orderResponse = await axios.post(`${PEDIDO_SERVICE_URL}/order/add`, newOrder);
    expect(orderResponse.status).toBe(200);
    
    // 3. Verificar que el pedido se creó correctamente
    const orderId = orderResponse.data.id;
    const getOrderResponse = await axios.get(`${PEDIDO_SERVICE_URL}/order/${orderId}`);
    expect(getOrderResponse.data.idUser).toBe(userId);
  });
});
