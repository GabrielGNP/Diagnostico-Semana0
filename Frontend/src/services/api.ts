export class ApiClient {
    private baseUrl: string;

    constructor(baseUrl: string) {
        this.baseUrl = baseUrl;
    }

    async get<T>(endpoint: string): Promise<T> {
        const response = await fetch(`${this.baseUrl}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error(`Error en petición: ${response.statusText}`);
        }

        return response.json();
    }

    async post<T>(endpoint: string, payload: unknown): Promise<T> {
        const response = await fetch(`${this.baseUrl}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            throw new Error(`Error en petición: ${response.statusText}`);
        }

        return response.json();
    }
}

export const userApi = new ApiClient(import.meta.env.VITE_APIUSER);
export const orderApi = new ApiClient(import.meta.env.VITE_APIORDER);
