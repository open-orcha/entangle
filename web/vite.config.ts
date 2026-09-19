import { defineConfig } from "vite";

export default defineConfig({
  server: {
    // The dev server must be allowed to read ../content/lessons.json,
    // the content contract shared with the iOS and Android apps.
    fs: { allow: [".."] },
  },
});
