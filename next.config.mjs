// next.config.mjs // Configure Next.js build and runtime options
/** @type {import('next').NextConfig} */ // Type hint for IntelliSense
const nextConfig = { // Exported Next.js configuration object
  reactStrictMode: true, // Enable additional React warnings in development
  experimental: { // Enable experimental features
    typedRoutes: true // Use typed routes with TS for safer navigation
  }, // Close experimental
  output: 'standalone' // Produce a standalone build suitable for Docker or server
}; // Close config object
export default nextConfig; // Export the configuration as default