# Development of user documentation

The a Arciphant user documentation is created with [Zensical](https://zensical.org/). 

To run Zensical, a dockerized setup is provided.

## Start live preview server

* `docker compose up --build zensical_serve -d`
* `docker compose watch zensical_serve`
* open http://localhost:8000 in the browser

## Stop live preview server

* Press `Ctrl+C` to stop watch mode
* `docker compose down zensical_serve --rmi all`

## Build docs
* `docker compose run --build --rm zensical_build`

To run a clean build (no usage of caches):
* `docker compose run --build --rm zensical_build --clean`

## Publish to GitHub Pages
* Go to [Publish Arciphant user documentation](https://github.com/ergon/arciphant/actions/workflows/publish-user-docs.yml) workflow under `Actions` tab of repository → *Run workflow*
* Open published page: https://ergon.github.io/arciphant
