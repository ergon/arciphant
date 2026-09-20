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
  // GitHub Pages serves the site under /arciphant; local dev stays at the root.
  // `site` gives sitemap, canonical URLs and Open Graph images an absolute origin.
  deployment:
    process.env.DEPLOY_TARGET === "github-pages"
      ? { site: "https://ergon.github.io", base: "/arciphant" }
      : undefined,
});
