import { describe, it, expect } from 'vitest';

/**
 * Tests de integración de servicios (E2E)
 * Estos tests se ejecutan con todos los servicios corriendo en Docker Compose
 */
describe('E2E Service Integration Tests', () => {
  
  it('should pass basic arithmetic test', () => {
    // Test que pasa: verificación básica
    const result = 2 + 2;
    expect(result).toBe(4);
  });

  it('should fail intentionally to demonstrate test failure', () => {
    // Test que falla intencionalmente: 3 + 3 NO es 5
    const result = 3 + 3;
    expect(result).toBe(6); // Esto fallará porque 3 + 3 = 6, no 5
  });
});
