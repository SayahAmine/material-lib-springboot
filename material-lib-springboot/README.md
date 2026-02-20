# material-lib (Spring Boot + SQLite)

This project mirrors your Python flow:

- Ship default CSVs in `src/main/resources/data/`
- On first run, seed them into a local SQLite database file
- Use repositories (or the sample REST API) to query materials, conditions, properties, curves, and curve points.

## Run

```bash
mvn spring-boot:run
```

SQLite DB will be created at:

- `${user.home}/material-lib/materials.db`

## Test endpoints

- `GET http://localhost:8080/api/materials?limit=20`
- `GET http://localhost:8080/api/materials?name=Aluminum&limit=20`
- `GET http://localhost:8080/api/materials/1`
- `GET http://localhost:8080/api/materials/1/default-condition`
- `GET http://localhost:8080/api/conditions/1/properties?limit=50`
- `GET http://localhost:8080/api/conditions/1/properties?key=composition_wt_percent`
- `GET http://localhost:8080/api/conditions/1/curves`
- `GET http://localhost:8080/api/curves/1/points`

## Desktop later

For a desktop app you can:

- Remove `spring-boot-starter-web`
- Set `spring.main.web-application-type=none`
- Build a JavaFX UI that calls the repositories/services directly.
