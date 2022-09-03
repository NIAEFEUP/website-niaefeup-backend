# Website NIAEFEUP - BackEnd
The online platform for NIAEFEUP.

## Development setup

### Prerequisites

- Java JDK 17+
- Gradle

### Running

#### With IntelliJ

Simply load the Gradle project and run the application.
For automatic restart to fire up every time a source file changes, make sure that `Build project automatically` under `File | Settings | Build, Execution, Deployment | Compiler` is checked.

#### With the command line

Run the following command in your shell:

```bash
gradle bootRun
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
  - `repository/` - Data access layer methods (Spring Data repositories)
  - `service/` - Business logic for the controllers
- `src/test/` - Self explanatory: unit tests, functional (end-to-end) tests, etc.
