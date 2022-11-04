# Website NIAEFEUP - BackEnd
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
gradle bootRun
```

### Linting

We use [`ktlint`](https://ktlint.github.io/) to ensure a consistent coding style according to the community standards, through a [Gradle plugin](https://github.com/jlleitschuh/ktlint-gradle).

#### With IntelliJ
Although IntelliJ does not provide linting suggestions for Kotlin out of the box, you can use a [third-party plugin](https://plugins.jetbrains.com/plugin/15057-ktlint-unofficial-/) to run the linter at real time.

#### With the command line
You can fire up the analysis yourself by running in your shell:

```bash
gradle ktlintCheck
```

#### With a git hook

You can setup a local precommit [git hook](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks) for lint analysis running a Gradle task provided by the used linting plugin:

```bash
gradle addKtlintCheckGitPreCommitHook
```

Or even an auto-format hook, if that is your thing:

```bash
gradle addKtlintFormatGitPreCommitHook
```

### Testing

#### With IntelliJ

Run the test suite as usual, selecting the respective task for running.

#### With the command line

Run the following command in your shell:

```bash
gradle test
```

## Project Details

### Project Structure

- `src/main`
  - `controller/` - Methods that register endpoints for the app
  - `model/` - Database entity models (Spring Data JPA entities)
    - `dto/` - Data Transfer Objects for creating and modifying entities
  - `repository/` - Data access layer methods (Spring Data repositories)
  - `service/` - Business logic for the controllers
- `src/test/` - Self explanatory: unit tests, functional (end-to-end) tests, etc.

### REST API documentation

The api is documented using `openapi` with the `swagger` ui. When running the application, the ui it is accessible at 
`/swagger-ui.html` and the `JSON` file is available at `/v3/api-docs`.