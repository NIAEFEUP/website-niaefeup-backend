# Website NIAEFEUP - BackEnd
[![codecov](https://codecov.io/gh/NIAEFEUP/website-niaefeup-backend/branch/develop/graph/badge.svg?token=4OPGXYESGP)](https://codecov.io/gh/NIAEFEUP/website-niaefeup-backend)
The online platform for NIAEFEUP.

## Development setup

### Prerequisites

- [Java JDK 17+](https://www.java.com/)
- [Gradle 7.5+](https://gradle.org/)

### Running

#### With IntelliJ

Simply load the Gradle project and run the application.
For automatic restart to fire up every time a source file changes, make sure that `Build project automatically` under `File | Settings | Build, Execution, Deployment | Compiler` is checked.

#### With the command line

Run the following command in your shell:

```bash
./gradlew bootRun
```

### Linting

We use [`ktlint`](https://ktlint.github.io/) to ensure a consistent coding style according to the community standards, through a [Gradle plugin](https://github.com/jlleitschuh/ktlint-gradle).

#### With IntelliJ
Although IntelliJ does not provide linting suggestions for Kotlin out of the box, you can use a [third-party plugin](https://plugins.jetbrains.com/plugin/15057-ktlint-unofficial-/) to run the linter at real time.

#### With the command line
You can fire up the analysis yourself by running in your shell:

```bash
./gradlew ktlintCheck
```

You can fix the lint automatically by running in your shell:

```bash
./gradlew ktlintFormat
```

#### With a git hook

You can setup a local precommit [git hook](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks) for lint analysis running a Gradle task provided by the used linting plugin:

```bash
./gradlew addKtlintCheckGitPreCommitHook
```

Or even an auto-format hook, if that is your thing:

```bash
./gradlew addKtlintFormatGitPreCommitHook
```

### Testing

#### With IntelliJ

Run the test suite as usual, selecting the respective task for running.

#### With the command line

Run the following command in your shell:

```bash
./gradlew test
```


### API Documentation
API documentation is generated through the use of the [Spring REST Docs API specification Integration (aka restdocs-api-spec)](https://github.com/ePages-de/restdocs-api-spec), a [Spring Rest Docs](https://spring.io/projects/spring-restdocs) extension that builds an [OpenAPI](https://www.openapis.org/) or a [Postman collection](https://learning.postman.com/docs/sending-requests/intro-to-collections/) specification from its description, included in the controller tests. To see examples of how to document the API hop to one of the controller tests and read the [API documentation wiki page](https://github.com/NIAEFEUP/website-niaefeup-backend/wiki/API-documentation).

##### With IntelliJ
Run the `openapi3` or `postman` gradle task to get either the OpenAPI specification or the Postman collection respectively.

##### With the command line
Run the following command in your shell:

```bash
./gradlew openapi3
```

```bash
./gradlew postman
```

###### Results
Find the OpenAPI specification under `build/api-spec/openapi3.json` after running the task or the resulting Postman collection under `build/api-spec/postman-collection.json`.


## Project Details

### Project Structure

- `src/main`
  - `controller/` - Methods that register endpoints for the app
  - `model/` - Database entity models (Spring Data JPA entities)
    - `dto/` - Data Transfer Objects for creating and modifying entities
  - `repository/` - Data access layer methods (Spring Data repositories)
  - `service/` - Business logic for the controllers
  - `annotations/` - Custom annotations used in the project
    - `validation/` - Custom validations used across the different models
- `src/test/` - Self explanatory: unit tests, functional (end-to-end) tests, etc.
