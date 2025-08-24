// src/app/login/page.tsx // Login page to set username in session

'use client'; // Client component for form handling

import React, { useState } from 'react'; // Import React and state hook
import { useRouter } from 'next/navigation'; // Import router for navigation after login
import { useAuth } from '../../context/AuthContext'; // Import auth hook

export default function LoginPage(): JSX.Element { // Export default page component
	const { login } = useAuth(); // Get login action from context
	const router = useRouter(); // Get router instance
	const [username, setUsername] = useState(''); // Local state for input field

	function handleSubmit(e: React.FormEvent) { // Handle form submit
		e.preventDefault(); // Prevent default browser submission
		if (!username.trim()) return; // Guard against empty username
		login(username.trim()); // Persist username in context/localStorage
		router.push('/proposals'); // Navigate to proposals as landing
	} // End submit handler

	return (
		<section style={{ maxWidth: 420 }}> {/* Constrain form width */}
			<h2>Login</h2> {/* Heading */}
			<form onSubmit={handleSubmit}> {/* Controlled form */}
				<label htmlFor="username">Username</label> {/* Input label */}
				<input id="username" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="e.g., john.doe" required style={{ display: 'block', width: '100%', margin: '8px 0' }} /> {/* Text input */}
				<button type="submit">Sign in</button> {/* Submit button */}
			</form>
			<p style={{ marginTop: 12, color: '#666' }}>Your username will be used as the approver for approve/reject actions.</p> {/* Help text */}
		</section>
	); // Close return
} // End component