# Website NIAEFEUP - BackEnd
[![codecov](https://codecov.io/gh/NIAEFEUP/website-niaefeup-backend/branch/develop/graph/badge.svg?token=4OPGXYESGP)](https://codecov.io/gh/NIAEFEUP/website-niaefeup-backend)

The online platform for NIAEFEUP.

Below, you can find a quickstart guide with development setup and project structure. For additional information about any implementation or usage details, please refer to our [Wiki Page](https://github.com/NIAEFEUP/website-niaefeup-backend/wiki).

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


## API Documentation
API documentation is generated through the use of the [Spring REST Docs API specification Integration (aka restdocs-api-spec)](https://github.com/ePages-de/restdocs-api-spec), a [Spring Rest Docs](https://spring.io/projects/spring-restdocs) extension that builds an [OpenAPI specification](https://www.openapis.org/) or a [Postman collection](https://learning.postman.com/docs/sending-requests/intro-to-collections/) from its description, included in the controller tests. To see examples of how to document the API, hop to one of the controller tests and read the [API documentation wiki page](https://github.com/NIAEFEUP/website-niaefeup-backend/wiki/API-documentation).

Find the current version of the API documentation [here](https://develop--niaefeup-backend-docs.netlify.app/).

The Postman collection is also available [here](https://develop--niaefeup-backend-docs.netlify.app/postman-collection.json).

##### With IntelliJ
Run the `generateDocs` gradle task to generate the OpenAPI specification or the Postman collection.

##### With the command line
Run the following command in your shell:

```bash
./gradlew generateDocs
```

###### Results
Find the OpenAPI specification and Postman collection under `docs/` after running the task.


## Project Structure

- `src/main`
  - `backend/` - Contains all the source code (excluding tests and resources)
    - `config/` - Configuration classes used at boot
    - `controller/` - Methods that register endpoints for the app
    - `model/` - Database entity models (Spring Data JPA entities)
    - `dto/` - Data Transfer Objects for creating and modifying entities
    - `repository/` - Data access layer methods (Spring Data repositories)
    - `service/` - Business logic for the controllers
    - `utils/` - Auxiliary packages used in the project
      - `extensions/` - [Extension functions](https://kotlinlang.org/docs/extensions.html) used throughout the project
      - `validation/` - Custom validations used across the different models
  - `resources/` - All assets and static files needed, including static configurations
- `src/test/` - Self explanatory: unit tests, functional (end-to-end) tests, etc.
