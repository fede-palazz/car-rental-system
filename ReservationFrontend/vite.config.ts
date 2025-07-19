import path from "path";
import tailwindcss from "@tailwindcss/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vite.dev/config/
export default defineConfig(({ command }) => {
  const isProduction = command === "build";

  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    base: isProduction ? "/ui" : "/ui", // Only set base to /ui/ in production
    build: {
      emptyOutDir: true,
      //outDir: "./dist",
      //assetsDir: ".",
    },
  };
});
