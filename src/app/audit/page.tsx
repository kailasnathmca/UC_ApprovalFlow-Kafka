// src/app/audit/page.tsx // Audit records listing page

'use client'; // Client page for interactivity

import React, { useEffect, useState } from 'react'; // Import React hooks
import { listAudit } from '../../lib/audit'; // Import audit client
import type { AuditRecord } from '../../types/api'; // Import types
import Pagination from '../../components/Pagination'; // Import pagination component

export default function AuditPage(): JSX.Element { // Export default page component
	const [items, setItems] = useState<AuditRecord[]>([]); // Items state
	const [page, setPage] = useState(0); // Page index
	const [size] = useState(10); // Page size
	const [totalPages, setTotalPages] = useState(0); // Total pages
	const [proposalId, setProposalId] = useState<string>(''); // Proposal filter input
	const [loading, setLoading] = useState(false); // Loading flag
	const [error, setError] = useState<string | null>(null); // Error state

	async function load() { // Load audit page
		try { // Try block
			setLoading(true); // Set loading true
			setError(null); // Clear error
			const resp = await listAudit({ page, size, sort: 'id,desc', proposalId: proposalId ? Number(proposalId) : undefined }); // Fetch data
			setItems(resp.content); // Update items
			setTotalPages(resp.totalPages); // Update total pages
		} catch (e) { // Catch
			setError((e as Error).message); // Store message
		} finally { // Finally
			setLoading(false); // Reset loading
		}
	} // End load

	useEffect(() => { load(); }, [page, size, proposalId]); // Re-run when dependencies change

	return (
		<section> {/* Container */}
			<h2>Audit</h2> {/* Heading */}
			<div style={{ display: 'flex', gap: 8, alignItems: 'center', marginBottom: 12 }}> {/* Controls row */}
				<label>Proposal ID:</label> {/* Label */}
				<input value={proposalId} onChange={(e) => { setPage(0); setProposalId(e.target.value); }} placeholder="e.g., 1" style={{ width: 100 }} /> {/* Filter input */}
				<button onClick={() => { setProposalId(''); setPage(0); }}>Clear</button> {/* Clear filter */}
			</div>

			{loading ? (<p>Loading...</p>) : error ? (<p style={{ color: 'red' }}>{error}</p>) : (
				<table>
					<thead>
						<tr>
							<th>ID</th>
							<th>Proposal ID</th>
							<th>Event</th>
							<th>Actor</th>
							<th>Message</th>
							<th>Created</th>
						</tr>
					</thead>
					<tbody>
						{items.map(r => (
							<tr key={r.id}>
								<td>{r.id}</td>
								<td>{r.proposalId}</td>
								<td>{r.eventType}</td>
								<td>{r.actor || '-'}</td>
								<td>{r.message || '-'}</td>
								<td>{r.createdAt}</td>
							</tr>
						))}
					</tbody>
				</table>
			)}

			<div style={{ marginTop: 12 }}> {/* Pagination container */}
				<Pagination page={page} size={size} totalPages={totalPages} onChange={setPage} /> {/* Pagination */}
			</div>
		</section>
	); // Close return
} // End component