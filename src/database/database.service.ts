import { Injectable } from "@nestjs/common";
import { ConfigService } from "@nestjs/config";
import { TypeOrmModuleOptions, TypeOrmOptionsFactory } from "@nestjs/typeorm";
import { getDatabaseSynchronize } from "./synchronize";

@Injectable()
export class DatabaseService implements TypeOrmOptionsFactory {
  constructor(private configService: ConfigService) {}

  createTypeOrmOptions(): TypeOrmModuleOptions {
    const isTest = this.configService.get<string>("NODE_ENV") === "test";
    if (isTest) {
      return {
        type: "better-sqlite3",
        database: ":memory:",
        dropSchema: true,
        autoLoadEntities: true,
        synchronize: true,
      };
    }

    const host = this.configService.getOrThrow<string>("DATABASE_MASTER");
    const port = this.configService.get<number>("DATABASE_PORT", 5432);
    const username = this.configService.getOrThrow<string>("DATABASE_USER");
    const password = this.configService.getOrThrow<string>("DATABASE_PASSWORD");
    const database = this.configService.getOrThrow<string>("DATABASE_NAME");
    const slaveHost = this.configService.get<string>("DATABASE_SLAVE");

    const connection = { host, port, username, password, database };

    return {
      type: "postgres",
      ...(slaveHost
        ? {
            replication: {
              master: {
                ...connection,
              },
              slaves: [
                {
                  ...connection,
                  host: slaveHost,
                },
              ],
            },
          }
        : connection),
      autoLoadEntities: true,
      synchronize: getDatabaseSynchronize(this.configService),
    };
  }
}
