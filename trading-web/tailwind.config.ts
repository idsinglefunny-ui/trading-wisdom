import type { Config } from "tailwindcss";

const config: Config = {
  darkMode: "class",
  content: [
    "./pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        // Material 3 Professional Dark Theme
        primary: {
          DEFAULT: "#3B82F6",
          foreground: "#FFFFFF",
        },
        background: "#0F172A",
        foreground: "#F8FAFC",
        surface: {
          DEFAULT: "#1E293B",
          variant: "#334155",
          container: "#1E293B",
          "container-low": "#1E293B",
          "container-high": "#334155",
        },
        border: "#334155",
        "border-strong": "#475569",
        destructive: {
          DEFAULT: "#DC2626",
          foreground: "#FFFFFF",
        },
        // Category Colors
        risk: {
          mgmt: "#0EA5E9",
        },
        mindset: {
          DEFAULT: "#F59E0B",
        },
        discipline: {
          DEFAULT: "#10B981",
        },
        technical: {
          DEFAULT: "#8B5CF6",
        },
        // Semantic Colors
        muted: {
          DEFAULT: "#334155",
          foreground: "#94A3B8",
        },
        accent: {
          DEFAULT: "#3B82F6",
          foreground: "#FFFFFF",
        },
        card: {
          DEFAULT: "#1E293B",
          foreground: "#F8FAFC",
        },
        input: "#1E293B",
        ring: "#3B82F6",
        secondary: {
          DEFAULT: "#475569",
          foreground: "#F8FAFC",
        },
        outline: {
          DEFAULT: "#475569",
          variant: "#334155",
        },
        success: "#10B981",
        warning: "#F59E0B",
        info: "#0EA5E9",
        error: "#DC2626",
      },
      borderRadius: {
        // Material 3 border radius
        "material-sm": "8px",
        "material-md": "12px",
        "material-lg": "16px",
        "material-xl": "20px",
      },
      fontFamily: {
        sans: [
          "system-ui",
          "-apple-system",
          "BlinkMacSystemFont",
          '"Segoe UI"',
          "Roboto",
          "sans-serif",
        ],
      },
      boxShadow: {
        // Material 3 elevation shadows
        "elevation-1": "0 1px 3px rgba(0, 0, 0, 0.3)",
        "elevation-2": "0 4px 6px -1px rgba(0, 0, 0, 0.3)",
        "elevation-3": "0 10px 15px -3px rgba(0, 0, 0, 0.3)",
        "elevation-4": "0 20px 25px -5px rgba(0, 0, 0, 0.3)",
      },
    },
  },
  plugins: [],
};

export default config;
