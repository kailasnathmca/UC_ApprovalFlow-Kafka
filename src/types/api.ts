// src/types/api.ts // Centralized TypeScript types for API contracts

// ---------- Shared primitives ----------
export type UUID = string; // Represent IDs as strings, even if backend uses numbers
export type ISODateTime = string; // Represent date-time fields in ISO-8601 string format

// ---------- Proposal domain ----------
export type ProposalStatus = 'DRAFT' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED'; // Legal statuses
export type StepStatus = 'PENDING' | 'APPROVED' | 'REJECTED'; // Legal statuses for each review step

export interface ProposalStep { // One element in the workflow chain
	name: string; // Human-readable step name, e.g., 'PEER_REVIEW'
	status: StepStatus; // Current status of this step
	approvedBy?: string; // Optional username that approved the step
	approvedAt?: ISODateTime; // Optional timestamp of approval
	rejectionReason?: string; // Optional reason when REJECTED
}

export interface Proposal { // Aggregate representing a proposal
	id: number; // Unique identifier (assumed number per cheat sheet)
	title: string; // Proposal title
	applicantName: string; // Name of the applicant
	amount: number; // Amount requested
	description: string; // Description/details
	status: ProposalStatus; // Current overall status
	currentStepIndex: number; // Index of current step pointer
	steps: ProposalStep[]; // List of steps making up the workflow
	createdAt?: ISODateTime; // Optional timestamp when created
	updatedAt?: ISODateTime; // Optional timestamp when last updated
}

export interface CreateProposalRequest { // Payload to create new proposal
	title: string; // Title text
	applicantName: string; // Applicant name
	amount: number; // Amount value
	description: string; // Free-form description
}

export interface SubmitProposalRequest extends Array<string> {} // Optional custom chain body, e.g. ["TEAM_LEAD","RISK","CFO"]

export interface ApproveRejectRequest { // Shared payload for approve/reject
	approver: string; // Username who performs action
	comments?: string; // Optional comments
}

// ---------- Pagination ----------
export interface PageRequest { // Client-side representation of pagination parameters
	page?: number; // 0-based page index
	size?: number; // Page size
	sort?: string; // Sort expression, e.g., 'id,desc'
}

export interface PageResponse<TItem> { // Simplified page response envelope
	content: TItem[]; // Items on the current page
	totalElements: number; // Total item count across all pages
	totalPages: number; // Total number of pages
	page: number; // Current page index (0-based)
	size: number; // Current page size
}

// ---------- Audit domain ----------
export type AuditEventType = 'PROPOSAL_SUBMITTED' | 'STEP_APPROVED' | 'PROPOSAL_APPROVED' | 'PROPOSAL_REJECTED'; // Enumerated event kinds

export interface AuditRecord { // Single audit row
	id: number; // Unique row id
	proposalId: number; // Associated proposal id
	eventType: AuditEventType; // Event type string
	actor?: string; // Optional username who triggered event
	message?: string; // Optional message
	createdAt: ISODateTime; // Creation timestamp
}

// ---------- Notification domain ----------
export interface HealthResponse { // Result of GET /api/health
	status: string; // Arbitrary status string, e.g., 'OK'
}