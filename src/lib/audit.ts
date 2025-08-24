// src/lib/audit.ts // Typed client for the Audit Service

import { httpJson, AUDIT_BASE_URL } from './http'; // Import HTTP helper and base URL
import type { PageRequest, PageResponse, AuditRecord } from '../types/api'; // Import relevant types

// Helper to build query string similar to proposals client
function buildQuery(params: Record<string, string | number | undefined>): string { // Serialize query params
	const search = new URLSearchParams(); // Create params container
	Object.entries(params).forEach(([key, value]) => { // Iterate entries
		if (value !== undefined) search.set(key, String(value)); // Include only defined values
	}); // End iteration
	const qs = search.toString(); // Convert to string
	return qs ? `?${qs}` : ''; // Return with leading '?'
} // End helper

export async function listAudit(options: PageRequest & { proposalId?: number } = {}): Promise<PageResponse<AuditRecord>> { // List audit records
	const qs = buildQuery({ proposalId: options.proposalId, page: options.page, size: options.size, sort: options.sort }); // Build query
	return httpJson<PageResponse<AuditRecord>>(`${AUDIT_BASE_URL}/api/audit${qs}`); // GET request to audit endpoint
} // End function