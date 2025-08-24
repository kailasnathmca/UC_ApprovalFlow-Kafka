// src/lib/notification.ts // Typed client for the Notification Service

import { httpJson, NOTIFICATION_BASE_URL } from './http'; // Import helper and base URL
import type { HealthResponse } from '../types/api'; // Import response type

export async function getNotificationHealth(): Promise<HealthResponse> { // Fetch health from notification service
	return httpJson<HealthResponse>(`${NOTIFICATION_BASE_URL}/api/health`); // GET /api/health
} // End function