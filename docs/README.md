# Development of user documentation

The a Arciphant user documentation is created with [Blume](https://useblume.dev/).

## Start live preview server

* `npm run dev`
* open http://localhost:4321 in the browser

## Diagnose config and content problems
* `npm run dictor`

## Build docs

* `npm run build`

## Publish to GitHub Pages

* Go
  to [Publish documentation](https://github.com/ergon/arciphant/actions/workflows/publish-docs.yml)
  workflow under `Actions` tab of repository → *Run workflow*
* Open published page: https://ergon.github.io/arciphant

## Edit Diagrams (with `draw.io`)

The diagrams in `content/images` are editable draw.io PNGs (`*.drawio.png`). To create and edit them with Claude
Code, the [draw.io plugin](https://www.drawio.com/docs/manual/generate/drawio-mcp-server/) is used.

The plugin is enabled for this project in `.claude/settings.json` (`enabledPlugins` +
`extraKnownMarketplaces`), so Claude Code offers to install it automatically once you trust the project settings.

### Manual installation

To install it manually instead:

#### Claude Code

```
/plugin marketplace add jgraph/drawio-mcp
/plugin install drawio@drawio
```

#### Claude Desktop

* Settings → Plugins → Add marketplace
* `jgraph/drawio-mcp` → Synchronize
* Install `Drawio` (for project)

### Further Links

* https://www.drawio.com/docs/manual/generate/drawio-mcp-server/
* https://github.com/jgraph/drawio-mcp/blob/main/plugins/README.md
