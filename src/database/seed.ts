import { config } from "dotenv";
import { DataSource, DataSourceOptions } from "typeorm";
import { runSeeders, SeederOptions } from "typeorm-extension";

if (process.env.NODE_ENV !== "test") {
  config({ path: ".env" });
}

export const seed = async () => {
  const options: DataSourceOptions & SeederOptions = {
    type: "postgres",
    host: process.env.DATABASE_MASTER,
    port: parseInt(process.env.DATABASE_PORT || "5432", 10),
    username: process.env.DATABASE_USER,
    password: process.env.DATABASE_PASSWORD,
    database: process.env.DATABASE_NAME,
    synchronize: true,
    dropSchema: true,
    schema: "public",
    entities: ["src/**/*.entity{.ts,.js}"],
    seeds: ["src/database/seeds/*.seeder.{ts,js}"],
    factories: ["src/database/factories/*.factory.{ts,js}"],
  };

  const dataSource = new DataSource(options);
  await dataSource.initialize();

  await runSeeders(dataSource);

  await dataSource.destroy();
};

export const handleMain = (
  moduleRef: NodeJS.Module,
  mainModule: NodeJS.Module | undefined = require.main,
) => {
  if (mainModule === moduleRef) {
    seed().catch((err) => {
      console.error("Seeding failed:", err);
      process.exit(1);
    });
  }
};

handleMain(module);
