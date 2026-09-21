# Development of user documentation

The Arciphant user documentation is created with [Blume](https://useblume.dev/).

## Start live preview server

* `npm run dev`
* open http://localhost:4321 in the browser

## Diagnose config and content problems
* `npm run doctor`

## Build docs

* `npm run build`

## Publish to GitHub Pages

* Go
  to [Publish documentation](https://github.com/ergon/arciphant/actions/workflows/publish-docs.yml)
  workflow under `Actions` tab of repository → *Run workflow*
* Open published page: https://ergon.github.io/arciphant

## Edit Diagrams (with `draw.io`)

The diagrams exist in two forms:

* **Editable sources**: `diagrams/*.drawio` — edit these with the draw.io app (or with Claude Code via the draw.io
  plugin, see below).
* **Published exports**: `content/images/*.svg` — generated SVG exports referenced by the docs pages. Never edit these
  by hand; re-export them after changing a source.

The SVGs are exported with the *automatic* appearance, so all colors use CSS `light-dark(…)` and the diagrams adapt to
the light/dark theme of the docs site. Keep diagram colors either at their draw.io defaults or as explicit
`light-dark(<light>,<dark>)` pairs (never a plain hex color) so this keeps working. 

### Color palette
The diagrams use a palette derived from the Arciphant logo blue `#2078A4`: stroke `light-dark(#2078A4,#7FB5D1)`, fills from outer to inner
`light-dark(#EAF3F8,#12303E)`, `light-dark(#CEE4F0,#1B4358)`, `light-dark(#A9D2E6,#2A6A8C)`, title text
`light-dark(#19607F,#93C3DB)`.

### Export a diagram

After editing a source, re-export it with the draw.io CLI (run from the `docs` directory; requires
[draw.io Desktop](https://get.diagrams.net/) 26+):

```bash
"C:\Program Files\draw.io\draw.io.exe" -x -f svg -t -o content/images/<name>.svg diagrams/<name>.drawio
```

Or manually in the draw.io app: *File → Export as → SVG* with *Transparent Background* checked, *Appearance:
Automatic*, and **_Include a copy of my diagram_ unchecked***.

***Important:** Do not export with an embedded copy of the diagram (`-e` / *Include a copy of my diagram*). The embedded
XML ends up in a huge `content` attribute on the SVG root tag, which breaks Astro's SVG metadata parser and fails the
docs build. The `.drawio` sources are the single editable truth instead.

### Claude Code `draw.io` plugin

To create and edit the diagrams with Claude Code,
the [draw.io plugin](https://www.drawio.com/docs/manual/generate/drawio-mcp-server/) is used.

The plugin is enabled for this project in `.claude/settings.json` (`enabledPlugins` +
`extraKnownMarketplaces`), so Claude Code offers to install it automatically once you trust the project settings.

#### Manual installation

To install it manually instead:

##### Claude Code

```
/plugin marketplace add jgraph/drawio-mcp
/plugin install drawio@drawio
```

##### Claude Desktop

* Settings → Plugins → Add marketplace
* `jgraph/drawio-mcp` → Synchronize
* Install `Drawio` (for project)

#### Further Links

* https://www.drawio.com/docs/manual/generate/drawio-mcp-server/
* https://github.com/jgraph/drawio-mcp/blob/main/plugins/README.md
