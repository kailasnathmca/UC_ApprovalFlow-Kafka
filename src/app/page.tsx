// src/app/page.tsx // Home page with quick guidance

import React from 'react'; // Import React
import Link from 'next/link'; // Import Link for navigation

export default function HomePage(): JSX.Element { // Export default page component
	return (
		<section> {/* Container for content */}
			<h1>Proposal Portal</h1> {/* Main heading */}
			<p>Use this app to create, submit, and review proposals using existing backend services.</p> {/* Subtitle */}
			<ul> {/* List of quick links */}
				<li><Link href="/login">Login</Link> to start acting as an approver.</li> {/* Link to login */}
				<li>Go to <Link href="/proposals">Proposals</Link> to create and manage proposals.</li> {/* Link to proposals */}
				<li>Check <Link href="/audit">Audit</Link> for the audit trail.</li> {/* Link to audit */}
				<li>Ping <Link href="/health">Notification Health</Link> to verify service availability.</li> {/* Link to health */}
			</ul>
		</section>
	); // Close return
} // End component