import { defineComponents } from "blume";
import TreeFile from "blume/components/content/TreeFile.astro";
import Tree from "./components/Tree.astro";
import TreeFolder from "./components/TreeFolder.astro";

export default defineComponents({
  mdx: {
    // Replace the built-in Tree with a variant whose folders accept an `icon` prop.
    Tree: Object.assign(Tree, { File: TreeFile, Folder: TreeFolder }),
  },
});
