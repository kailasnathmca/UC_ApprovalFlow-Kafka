# Proposal Portal (Next.js + TypeScript)

A minimal Next.js app that integrates with external services for Proposals, Audit, and Notification, plus a simple client-side login. All code files include detailed line-by-line comments (where the file format allows comments).

## Getting Started

- Copy your API services to run locally:
  - Proposal Service: http://localhost:8081
  - Audit Service: http://localhost:8082
  - Notification Service: http://localhost:8083

- Install and run:

```bash
npm install
npm run dev
```

- Build and start:

```bash
npm run build
npm start
```

## Notes
- JSON files (e.g., package.json, tsconfig.json) cannot include comments by spec.
- Approver name for approve/reject is the logged-in username.

