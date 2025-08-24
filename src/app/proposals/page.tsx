// src/app/proposals/page.tsx // Proposals list page with create form and filters

'use client'; // Client-side page for interactive behavior

import React, { useEffect, useMemo, useState } from 'react'; // Import React hooks
import Link from 'next/link'; // Import Next.js Link for navigation
import { createProposal, listProposals } from '../../lib/proposals'; // Import client functions
import type { Proposal } from '../../types/api'; // Import types
import Pagination from '../../components/Pagination'; // Import pagination component
import StatusBadge from '../../components/StatusBadge'; // Import status badge component

export default function ProposalsPage(): JSX.Element { // Export default page component
	const [items, setItems] = useState<Proposal[]>([]); // State to hold current page items
	const [page, setPage] = useState(0); // Current page index
	const [size] = useState(10); // Fixed page size
	const [totalPages, setTotalPages] = useState(0); // Total pages count
	const [statusFilter, setStatusFilter] = useState<string>(''); // Optional status filter
	const [isLoading, setIsLoading] = useState(false); // Loading flag for list fetch
	const [error, setError] = useState<string | null>(null); // Error state

	const statusOptions = useMemo(() => ['', 'DRAFT', 'UNDER_REVIEW', 'APPROVED', 'REJECTED'], []); // Valid statuses

	async function load() { // Fetch list from API
		try { // Begin try block
			setIsLoading(true); // Set loading true
			setError(null); // Clear previous error
			const pageResp = await listProposals({ page, size, status: statusFilter || undefined, sort: 'id,desc' }); // Invoke API
			setItems(pageResp.content); // Update items
			setTotalPages(pageResp.totalPages); // Update total pages
		} catch (e) { // Catch errors
			setError((e as Error).message); // Store error message
		} finally { // Always execute
			setIsLoading(false); // Reset loading
		}
	} // End load

	useEffect(() => { load(); }, [page, size, statusFilter]); // Re-fetch when dependencies change

	async function handleCreate(e: React.FormEvent<HTMLFormElement>) { // Submit handler for create form
		e.preventDefault(); // Prevent default
		const form = e.currentTarget; // Get form element
		const data = new FormData(form); // Extract form data
		const title = String(data.get('title') || ''); // Read title
		const applicantName = String(data.get('applicantName') || ''); // Read applicant name
		const amount = Number(data.get('amount') || 0); // Read amount as number
		const description = String(data.get('description') || ''); // Read description
		if (!title || !applicantName || !amount || !description) return; // Basic validation
		await createProposal({ title, applicantName, amount, description }); // Call API to create
		form.reset(); // Reset form
		setPage(0); // Reset to first page to see newest
		load(); // Reload list
	} // End handleCreate

	return (
		<section> {/* Container */}
			<h2>Proposals</h2> {/* Heading */}

			{/* Filter Row */}
			<div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 12 }}> {/* Controls row */}
				<label>Status:</label> {/* Label */}
				<select value={statusFilter} onChange={(e) => { setPage(0); setStatusFilter(e.target.value); }}> {/* Select status */}
					{statusOptions.map((s) => (
						<option key={s} value={s}>{s || 'Any'}</option> // Render options
					))}
				</select>
				<button onClick={() => { setStatusFilter(''); setPage(0); }}>Clear</button> {/* Clear filter */}
			</div>

			{/* Create Form */}
			<details style={{ marginBottom: 16 }} open> {/* Collapsible section for create */}
				<summary>Create Proposal (DRAFT)</summary> {/* Summary text */}
				<form onSubmit={handleCreate}> {/* Form element */}
					<div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}> {/* Grid layout */}
						<label>Title<input name="title" placeholder="IPO Fund" required /></label> {/* Title field */}
						<label>Applicant<input name="applicantName" placeholder="Riya" required /></label> {/* Applicant field */}
						<label>Amount<input name="amount" type="number" placeholder="150000" min="1" required /></label> {/* Amount field */}
						<label>Description<input name="description" placeholder="Seed round investment" required /></label> {/* Description field */}
					</div>
					<button type="submit" style={{ marginTop: 8 }}>Create</button> {/* Submit button */}
				</form>
			</details>

			{/* List Table */}
			{isLoading ? (<p>Loading...</p>) : error ? (<p style={{ color: 'red' }}>{error}</p>) : (
				<table>
					<thead>
						<tr>
							<th>ID</th>
							<th>Title</th>
							<th>Applicant</th>
							<th>Amount</th>
							<th>Status</th>
							<th>Actions</th>
						</tr>
					</thead>
					<tbody>
						{items.map(p => (
							<tr key={p.id}>
								<td>{p.id}</td>
								<td>{p.title}</td>
								<td>{p.applicantName}</td>
								<td>{p.amount}</td>
								<td><StatusBadge value={p.status} /></td>
								<td><Link href={`/proposals/${p.id}`}>Open</Link></td>
							</tr>
						))}
					</tbody>
				</table>
			)}

			{/* Pagination */}
			<div style={{ marginTop: 12 }}> {/* Pagination container */}
				<Pagination page={page} size={size} totalPages={totalPages} onChange={setPage} /> {/* Pagination component */}
			</div>
		</section>
	); // Close return
} // End component