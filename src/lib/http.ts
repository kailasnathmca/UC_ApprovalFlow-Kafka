// src/lib/http.ts // Thin fetch-based HTTP helper

// Base URLs for the three backend services; consider moving to environment variables in production
export const PROPOSAL_BASE_URL = 'http://localhost:8081'; // Proposal service origin
export const AUDIT_BASE_URL = 'http://localhost:8082'; // Audit service origin
export const NOTIFICATION_BASE_URL = 'http://localhost:8083'; // Notification service origin

// Generic request options interface to enforce JSON semantics
interface RequestOptions { // Defines options the client can pass to the request helper
	method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'; // Allowed HTTP methods
	body?: unknown; // Arbitrary request body to be JSON-serialized
	signal?: AbortSignal; // Optional abort signal for cancellation
}

export async function httpJson<TResponse>( // Generic helper returning parsed JSON
	url: string, // Fully-qualified request URL
	options: RequestOptions = {} // Optional request options
): Promise<TResponse> { // Promise resolving to typed response
	const response = await fetch(url, { // Invoke native fetch
		method: options.method ?? 'GET', // Default method is GET
		headers: { 'Content-Type': 'application/json' }, // Always send JSON Content-Type
		body: options.body !== undefined ? JSON.stringify(options.body) : undefined, // Serialize body when present
		signal: options.signal // Forward abort signal
	}); // Close fetch call
	if (!response.ok) { // Check HTTP status
		const text = await response.text().catch(() => ''); // Try to read error text
		throw new Error(`HTTP ${response.status} ${response.statusText}: ${text}`); // Throw detailed error
	} // Close error check
	return (await response.json()) as TResponse; // Parse and return response as JSON
} // Close function