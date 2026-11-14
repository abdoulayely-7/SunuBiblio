import type { NextConfig } from "next";

// Adresse de l'API Spring Boot. Le navigateur appelle /api/... sur le serveur Next, qui relaie vers l'API :
// pas de CORS a configurer et une seule origine pour l'utilisateur.
const API_URL = process.env.API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${API_URL}/api/:path*` }];
  },
};

export default nextConfig;
