// src/context/AuthContext.tsx // Lightweight client-side authentication context

'use client'; // Mark this module as client-side for Next.js App Router

import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'; // Import React hooks and helpers

interface AuthState { // Encapsulates the authentication state
	username: string | null; // Current logged-in username or null when not authenticated
}

interface AuthContextValue extends AuthState { // Context value extended with actions
	login: (username: string) => void; // Log in with a username
	logout: () => void; // Log out and clear session
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined); // Create context with undefined default

const STORAGE_KEY = 'proposal-portal.username'; // localStorage key for persisted username

export function AuthProvider({ children }: { children: React.ReactNode }): JSX.Element { // Provider component wrapping the app
	const [username, setUsername] = useState<string | null>(null); // React state for username

	useEffect(() => { // On initial mount, load session from localStorage
		const saved = typeof window !== 'undefined' ? localStorage.getItem(STORAGE_KEY) : null; // Read persisted username
		if (saved) setUsername(saved); // If present, apply to state
	}, []); // Empty dependency array ensures this runs once

	const login = useCallback((name: string) => { // Login action to set username
		setUsername(name); // Update state
		if (typeof window !== 'undefined') localStorage.setItem(STORAGE_KEY, name); // Persist to localStorage
	}, []); // Stable reference

	const logout = useCallback(() => { // Logout action to clear session
		setUsername(null); // Clear state
		if (typeof window !== 'undefined') localStorage.removeItem(STORAGE_KEY); // Remove from localStorage
	}, []); // Stable reference

	const value = useMemo<AuthContextValue>(() => ({ username, login, logout }), [username, login, logout]); // Memoize context value

	return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>; // Render provider with children
} // End provider

export function useAuth(): AuthContextValue { // Custom hook to access auth context
	const ctx = useContext(AuthContext); // Read context value
	if (!ctx) throw new Error('useAuth must be used within AuthProvider'); // Enforce provider presence
	return ctx; // Return context to caller
} // End hook