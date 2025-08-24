// src/components/Pagination.tsx // Reusable pagination control

'use client'; // Mark as client component for event handlers

import React from 'react'; // Import React

interface PaginationProps { // Props required by the pagination component
	page: number; // Current 0-based page index
	size: number; // Page size count
	totalPages: number; // Total number of pages
	onChange: (page: number) => void; // Callback when page changes
}

export default function Pagination({ page, size, totalPages, onChange }: PaginationProps): JSX.Element { // Export component
	const canPrev = page > 0; // Determine if previous page exists
	const canNext = page < totalPages - 1; // Determine if next page exists

	return (
		<div style={{ display: 'flex', alignItems: 'center', gap: 8 }}> {/* Container for controls */}
			<button onClick={() => onChange(0)} disabled={!canPrev}>⏮ First</button> {/* Go to first page */}
			<button onClick={() => onChange(page - 1)} disabled={!canPrev}>◀ Prev</button> {/* Go to previous page */}
			<span>Page {page + 1} / {Math.max(totalPages, 1)} • Size {size}</span> {/* Current page info */}
			<button onClick={() => onChange(page + 1)} disabled={!canNext}>Next ▶</button> {/* Go to next page */}
			<button onClick={() => onChange(totalPages - 1)} disabled={!canNext}>Last ⏭</button> {/* Go to last page */}
		</div>
	); // Close return
} // End component