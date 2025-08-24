// src/app/layout.tsx // Root layout for the App Router

import './globals.css'; // Import global styles for base resets
import type { Metadata } from 'next'; // Import metadata type for static SEO
import React from 'react'; // Import React
import Navbar from '../components/Navbar'; // Import top navigation bar
import { AuthProvider } from '../context/AuthContext'; // Import auth provider

export const metadata: Metadata = { // Export site-wide metadata
	title: 'Proposal Portal', // Document title shown in browser tab
	description: 'Submit and review investment proposals', // Short description
}; // End metadata

export default function RootLayout({ children }: { children: React.ReactNode }): JSX.Element { // Export default layout component
	return (
		<html lang="en"> {/* Set document language */}
			<body> {/* Body container required by Next.js */}
				<AuthProvider> {/* Provide auth context to entire app */}
					<Navbar /> {/* Render top navigation */}
					<main style={{ padding: 16 }}> {/* Main content area with padding */}
						{children} {/* Render current page */}
					</main>
				</AuthProvider>
			</body>
		</html>
	); // Close return
} // End component