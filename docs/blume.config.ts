import { defineConfig } from "blume";

export default defineConfig({
  title: "Arciphant",
  description: "User documentation for the Arciphant Gradle plugin.",
  logo: { image: "/icon.svg" },
  content: {
    root: "content",
  },
  github: {
    owner: "ergon",
    repo: "arciphant",
    dir: "docs",
  },
});
