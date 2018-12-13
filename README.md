# Campsite reservation API

## How to run

### Environment

The API uses a MySQL database. A docker-compose file is provided to run the database and create the schema. To start it, run:

``docker-compose -f environment/docker-compose.yml up``

### Run the application

You can run the project using gradle, executing:

``./gradlew bootRun``

or

``./gradlew bootJar`` and then executing the jar in build/libs directory

####Configuration

The configuration file is src/main/resources/application.yml. There is no need to change the values there.

The server is configured to listen in port 8080 and the database port is 13306 (the same port exposed in the docker-compose).

####Try the app

In doc/postman there is a Postman collection with all the requests supported with examples (it is for Postman 2.1).

### Tests

To execute the unit tests, run:

``./gradlew tests -i``

A report is generated in build/reports/tests/test/index.html

### Documentation

In the doc folder, there is raml with the API specification and an html with its visual representation.



