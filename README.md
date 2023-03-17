### Running the demo

Start the Postgres database:

```
docker run -it --rm=true --name quarkus_test -e POSTGRES_USER=test -e POSTGRES_PASSWORD=test -e POSTGRES_DB=test -p 5432:5432 postgres:14.1
```

Run the application:

```
mvn quarkus:dev
```
