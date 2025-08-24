// src/app/proposals/[id]/page.tsx // Proposal detail page with workflow actions

'use client'; // Mark as client for interactive actions

import React, { useEffect, useState } from 'react'; // Import React hooks
import { useParams, useRouter } from 'next/navigation'; // Import routing hooks
import { approveStep, getProposal, rejectProposal, submitProposal } from '../../../lib/proposals'; // Import API client functions
import type { Proposal, SubmitProposalRequest } from '../../../types/api'; // Import types
import StatusBadge from '../../../components/StatusBadge'; // Import status badge
import { useAuth } from '../../../context/AuthContext'; // Import auth context

export default function ProposalDetailPage(): JSX.Element { // Export default page component
	const params = useParams<{ id: string }>(); // Read dynamic route params
	const id = Number(params.id); // Convert id param to number
	const router = useRouter(); // Router for navigation
	const { username } = useAuth(); // Get current username for approve/reject
	const [proposal, setProposal] = useState<Proposal | null>(null); // Local state with proposal data
	const [error, setError] = useState<string | null>(null); // Error state
	const [loading, setLoading] = useState<boolean>(false); // Loading state
	const [customChainText, setCustomChainText] = useState<string>(''); // Optional custom chain input

	async function load() { // Load proposal from API
		try { // Begin try block
			setLoading(true); // Set loading flag
			setError(null); // Reset error
			const data = await getProposal(id); // Fetch proposal
			setProposal(data); // Store result
		} catch (e) { // Catch any error
			setError((e as Error).message); // Store message
		} finally { // Always execute
			setLoading(false); // Reset loading
		}
	} // End load

	useEffect(() => { if (Number.isFinite(id)) load(); }, [id]); // Load on id change

	async function onSubmitForReview() { // Submit proposal for review
		try { // Try block
			const body: SubmitProposalRequest | undefined = customChainText.trim() ? (customChainText.split(',').map(s => s.trim()).filter(Boolean) as SubmitProposalRequest) : undefined; // Parse custom chain CSV to array
			await submitProposal(id, body); // Call API to submit
			await load(); // Refresh proposal
		} catch (e) { // Catch
			alert((e as Error).message); // Display error
		}
	} // End submit handler

	async function onApprove() { // Approve current step
		if (!username) { alert('Please login first.'); router.push('/login'); return; } // Require login
		try { // Try block
			await approveStep(id, { approver: username, comments: 'Looks good' }); // Call API
			await load(); // Reload state
		} catch (e) { // Catch
			alert((e as Error).message); // Show error
		}
	} // End approve

	async function onReject() { // Reject current step
		if (!username) { alert('Please login first.'); router.push('/login'); return; } // Require login
		const reason = prompt('Enter rejection reason/comments:') || 'Insufficient documentation'; // Prompt for reason
		try { // Try block
			await rejectProposal(id, { approver: username, comments: reason }); // Call API
			await load(); // Refresh
		} catch (e) { // Catch
			alert((e as Error).message); // Show error
		}
	} // End reject

	if (loading) return <p>Loading...</p>; // Render loading state
	if (error) return <p style={{ color: 'red' }}>{error}</p>; // Render error
	if (!proposal) return <p>Not found</p>; // Render when proposal missing

	return (
		<section> {/* Container */}
			<h2>Proposal #{proposal.id} <StatusBadge value={proposal.status} /></h2> {/* Heading with status */}
			<p><strong>Title:</strong> {proposal.title}</p> {/* Title row */}
			<p><strong>Applicant:</strong> {proposal.applicantName}</p> {/* Applicant row */}
			<p><strong>Amount:</strong> {proposal.amount}</p> {/* Amount row */}
			<p><strong>Description:</strong> {proposal.description}</p> {/* Description row */}

			{/* Actions */}
			<div style={{ display: 'flex', gap: 8, alignItems: 'center', margin: '12px 0' }}> {/* Actions row */}
				{proposal.status === 'DRAFT' && (
					<>
						<input value={customChainText} onChange={(e) => setCustomChainText(e.target.value)} placeholder="Custom chain CSV (e.g., TEAM_LEAD,RISK,CFO)" style={{ minWidth: 360 }} /> {/* Custom chain input */}
						<button onClick={onSubmitForReview}>Submit for Review</button> {/* Submit button */}
					</>
				)}
				{proposal.status === 'UNDER_REVIEW' && (
					<>
						<button onClick={onApprove}>Approve Current Step</button> {/* Approve button */}
						<button onClick={onReject}>Reject Proposal</button> {/* Reject button */}
					</>
				)}
			</div>

			{/* Steps Table */}
			<h3>Workflow Steps</h3> {/* Steps heading */}
			<table>
				<thead>
					<tr>
						<th>#</th>
						<th>Name</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					{proposal.steps.map((s, idx) => (
						<tr key={idx} style={{ background: proposal.currentStepIndex === idx ? '#f5f5f5' : 'transparent' }}> {/* Highlight current step */}
							<td>{idx + 1}</td>
							<td>{s.name}</td>
							<td><StatusBadge value={s.status} /></td>
						</tr>
					))}
				</tbody>
			</table>
		</section>
	); // Close return
} // End component