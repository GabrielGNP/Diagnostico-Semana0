import '@testing-library/jest-dom'
import { vi } from 'vitest'

// Mock SweetAlert2 to avoid jsdom unhandled promise handlers during tests
const mockFire = vi.fn(() => Promise.resolve({ isConfirmed: true }))

// Provide both default and named exports because some modules import the default
// (e.g. `import Swal from 'sweetalert2'`) while others may import named exports.
vi.mock('sweetalert2', () => {
	const mockModule = {
		fire: mockFire,
		close: vi.fn(),
	}
	return {
		default: mockModule,
		...mockModule,
	}
})

// Mock the React wrapper to forward calls to the mocked Swal.fire
vi.mock('sweetalert2-react-content', async (importOriginal) => {
	const original = await importOriginal()
	return {
		default: (Swal) => ({
			fire: (...args) => Swal.fire(...args),
			// keep original properties if tests rely on them
			...(original && typeof original === 'object' ? original : {}),
		}),
	}
})
