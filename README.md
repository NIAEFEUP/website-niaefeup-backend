# Website NIAEFEUP - BackEnd

[![Checks](https://github.com/NIAEFEUP/website-niaefeup-backend/actions/workflows/checks.yml/badge.svg)](https://github.com/NIAEFEUP/website-niaefeup-backend/actions/workflows/checks.yml)
[![codecov](https://codecov.io/gh/NIAEFEUP/website-niaefeup-backend/branch/main/graph/badge.svg)](https://codecov.io/gh/NIAEFEUP/website-niaefeup-backend)
[![Node.js](https://img.shields.io/badge/Node.js-22+-339933?logo=node.js&logoColor=white)](https://nodejs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Biome](https://img.shields.io/badge/Code_style-Biome-60A5FA)](https://biomejs.dev/)
[![License](https://img.shields.io/badge/License-GPL--3.0-blue)](LICENSE)

The backend API for the NIAEFEUP website, built with NestJS.

## Technologies used

- [NestJS](https://nestjs.com/) - Backend framework
- [TypeORM](https://typeorm.io/) - ORM for database interactions
- [PostgreSQL](https://www.postgresql.org/) - Database
- [Swagger](https://swagger.io/) - API documentation
- [Biome](https://biomejs.dev/) - Linter and formatter

## Development setup

### Prerequisites

- [Node.js 22+](https://nodejs.org/)
- [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/)

### Running

#### 1. Environment variables

Copy the example environment file:

```bash
cp .env.example .env
```

#### 2. Start the database

The project uses PostgreSQL as its database. Start it with Docker:

```bash
docker compose up -d
```

To stop the database:

```bash
docker compose down
```

To reset the database (wipe volume):

```bash
docker compose down -v
```

#### 3. Install dependencies and run

```bash
# Install project dependencies
npm install

# Start the development server
npm run start:dev
```

The API will be available at `http://localhost:3000/api`.

### Database setup

To create the database schema:

```bash
npm run schema:create
```

To seed the database with test data:

```bash
npm run seed
```

### Linting

We use [Biome](https://biomejs.dev/) for linting and formatting to ensure a consistent coding style.

```bash
# Check for issues
npm run check

# Auto-fix issues
npm run check:fix

# Format only
npm run format

# Lint only
npm run lint
```

We recommend installing the [Biome VS Code extension](https://marketplace.visualstudio.com/items?itemName=biomejs.biome).

### Testing

```bash
# Run unit tests
npm run test

# Run tests with coverage
npm run test:cov

# Run e2e tests
npm run test:e2e
```

## API Documentation

API documentation is available through Swagger UI. After starting the application, visit:

```txt
http://localhost:3000/api/docs
```

The OpenAPI JSON specification will be available at:

```txt
http://localhost:3000/api/docs-json
```

## Building

To create a production build:

```bash
npm run build
```

To run the production build:

```bash
npm run start:prod
```

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.
