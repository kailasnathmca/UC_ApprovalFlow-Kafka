// src/components/Navbar.tsx // Top navigation bar component

'use client'; // Client component to access auth context

import Link from 'next/link'; // Import Next.js Link for client-side navigation
import React from 'react'; // Import React
import { useAuth } from '../context/AuthContext'; // Import auth hook

export default function Navbar(): JSX.Element { // Export default Navbar component
	const { username, logout } = useAuth(); // Get current username and logout action

	return (
		<nav style={{ display: 'flex', gap: 12, padding: 12, borderBottom: '1px solid #eee' }}> {/* Simple inline-styled nav */}
			<Link href="/">Home</Link> {/* Link to home page */}
			<Link href="/proposals">Proposals</Link> {/* Link to proposals page */}
			<Link href="/audit">Audit</Link> {/* Link to audit page */}
			<Link href="/health">Notification Health</Link> {/* Link to notification health page */}
			<div style={{ marginLeft: 'auto' }}> {/* Right-aligned auth area */}
				{username ? ( // Conditional rendering when logged in
					<span>
						Signed in as <strong>{username}</strong>{' '} {/* Show current username */}
						<button onClick={logout} style={{ marginLeft: 8 }}>Logout</button> {/* Logout button */}
					</span>
				) : (
					<Link href="/login">Login</Link> // Link to login page when not authenticated
				)}
			</div>
		</nav>
	); // Close return
} // End component