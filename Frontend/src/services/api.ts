export class ApiClient {
    private baseUrl: string;

    constructor(baseUrl: string) {
        this.baseUrl = baseUrl;
    }

    private async buildError(response: Response, endpoint: string): Promise<Error> {
        const text = await response.text().catch(() => "");
        const detail = text ? ` - ${text}` : "";
        return new Error(
            `HTTP ${response.status} ${response.statusText} (${endpoint})${detail}`,
        );
    }

    async get<T>(endpoint: string): Promise<T> {
        const response = await fetch(`${this.baseUrl}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw await this.buildError(response, endpoint);
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
            throw await this.buildError(response, endpoint);
        }

        return response.json();
    }
}

export const userApi = new ApiClient(import.meta.env.VITE_APIUSER);
export const orderApi = new ApiClient(import.meta.env.VITE_APIORDER);
