import '@testing-library/jest-dom'
import { vi } from 'vitest'

// Mock SweetAlert2 to avoid jsdom unhandled promise handlers during tests
const mockFire = vi.fn(() => Promise.resolve({ isConfirmed: true }))

vi.mock('sweetalert2', () => ({
	fire: mockFire,
	// reexport other helpers if needed by tests
	close: vi.fn(),
}))

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
