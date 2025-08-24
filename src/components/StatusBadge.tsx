// src/components/StatusBadge.tsx // Visual label for status values

'use client'; // Client component for consistency

import React from 'react'; // Import React

interface BadgeProps { // Props accepted by the badge
	value: string; // Status string to display
}

export default function StatusBadge({ value }: BadgeProps): JSX.Element { // Export component
	const color = (() => { // Compute color based on value
		if (value === 'DRAFT') return '#999'; // Grey for DRAFT
		if (value === 'UNDER_REVIEW' || value === 'PENDING') return '#f0ad4e'; // Amber for in-progress
		if (value === 'APPROVED') return '#5cb85c'; // Green for success
		if (value === 'REJECTED') return '#d9534f'; // Red for failure
		return '#777'; // Fallback color
	})(); // Immediately-invoked function to pick color

	return (
		<span style={{ background: color, color: '#fff', padding: '2px 8px', borderRadius: 12, fontSize: 12 }}> {/* Styled badge */}
			{value}
		</span>
	); // Close return
} // End component