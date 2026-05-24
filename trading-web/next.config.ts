import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: 'export',
  // Remove trailing slash from URLs for consistency
  trailingSlash: false,
  // Disable image optimization for static export
  images: {
    unoptimized: true,
  },
};

export default nextConfig;
