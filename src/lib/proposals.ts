// src/lib/proposals.ts // Typed client for the Proposal Service

import { httpJson, PROPOSAL_BASE_URL } from './http'; // Import HTTP helper and base URL
import type { PageRequest, PageResponse, Proposal, CreateProposalRequest, SubmitProposalRequest, ApproveRejectRequest } from '../types/api'; // Import shared types

// Helper to build query string for pagination and optional status filter
function buildQuery(params: Record<string, string | number | undefined>): string { // Serialize query params
	const search = new URLSearchParams(); // Instantiate URLSearchParams
	Object.entries(params).forEach(([key, value]) => { // Iterate over provided entries
		if (value !== undefined) search.set(key, String(value)); // Add only defined values
	}); // Close loop
	const qs = search.toString(); // Convert to query string
	return qs ? `?${qs}` : ''; // Prefix with '?' when non-empty
} // Close helper

export async function createProposal(request: CreateProposalRequest): Promise<Proposal> { // Create a draft proposal
	return httpJson<Proposal>(`${PROPOSAL_BASE_URL}/api/proposals`, { method: 'POST', body: request }); // POST body
} // Close function

export async function getProposal(id: number): Promise<Proposal> { // Fetch one proposal by id
	return httpJson<Proposal>(`${PROPOSAL_BASE_URL}/api/proposals/${id}`); // GET request
} // Close function

export async function listProposals(options: PageRequest & { status?: string } = {}): Promise<PageResponse<Proposal>> { // List proposals with paging
	const qs = buildQuery({ status: options.status, page: options.page, size: options.size, sort: options.sort }); // Build query string
	return httpJson<PageResponse<Proposal>>(`${PROPOSAL_BASE_URL}/api/proposals${qs}`); // GET with query
} // Close function

export async function submitProposal(id: number, customChain?: SubmitProposalRequest): Promise<Proposal> { // Submit a draft for review
	const url = `${PROPOSAL_BASE_URL}/api/proposals/${id}/submit`; // Build endpoint URL
	return httpJson<Proposal>(url, { method: 'POST', body: customChain }); // POST optional chain body
} // Close function

export async function approveStep(id: number, payload: ApproveRejectRequest): Promise<Proposal> { // Approve current step
	const url = `${PROPOSAL_BASE_URL}/api/proposals/${id}/approve`; // Build endpoint URL
	return httpJson<Proposal>(url, { method: 'POST', body: payload }); // POST body
} // Close function

export async function rejectProposal(id: number, payload: ApproveRejectRequest): Promise<Proposal> { // Reject current step and finalize proposal
	const url = `${PROPOSAL_BASE_URL}/api/proposals/${id}/reject`; // Build endpoint URL
	return httpJson<Proposal>(url, { method: 'POST', body: payload }); // POST body
} // Close function