// src/app/health/page.tsx // Notification service health check page

'use client'; // Client component to perform fetch on demand

import React, { useState } from 'react'; // Import React and state hook
import { getNotificationHealth } from '../../lib/notification'; // Import health client

export default function HealthPage(): JSX.Element { // Export default page component
	const [status, setStatus] = useState<string | null>(null); // State for health status
	const [error, setError] = useState<string | null>(null); // Error state
	const [loading, setLoading] = useState<boolean>(false); // Loading indicator

	async function check() { // Trigger health check
		try { // Try
			setLoading(true); // Set loading
			setError(null); // Reset error
			setStatus(null); // Reset status
			const res = await getNotificationHealth(); // Call API
			setStatus(res.status); // Store status string
		} catch (e) { // Catch
			setError((e as Error).message); // Store error
		} finally { // Finally
			setLoading(false); // Reset loading
		}
	} // End check

	return (
		<section> {/* Container */}
			<h2>Notification Health</h2> {/* Heading */}
			<button onClick={check} disabled={loading}>{loading ? 'Checking...' : 'Check Health'}</button> {/* Action button */}
			{status && <p style={{ color: 'green' }}>Service status: {status}</p>} {/* Success message */}
			{error && <p style={{ color: 'red' }}>{error}</p>} {/* Error message */}
		</section>
	); // Close return
} // End component