# Database for local development

## Start: 
```
docker compose -p artifact-sample-project -f .\db\docker-compose.yml up -d
```

Or use IntelliJ Run configuration `Dev DB Up`.

## Stop

```
docker compose -p artifact-sample-project down
```

## Setup IntelliJ Datasource

* Copy the content of [intellij-datasource.xml](./intellij-datasource.xml) into clipboard
* Click the '+' icon in the IntelliJ Database Tool Window and choose *Import from Clipboard*
* Add the password (see [docker-compose.yml](./docker-compose.yml)) to the password field