import { ConfigService } from "@nestjs/config";

export const getDatabaseSynchronize = (configService?: ConfigService) => {
  const nodeEnv = configService
    ? configService.get<string>("NODE_ENV")
    : process.env.NODE_ENV;
  const synchronizeOverride = configService
    ? configService.get<string>("DATABASE_SYNCHRONIZE")?.toLowerCase()
    : process.env.DATABASE_SYNCHRONIZE?.toLowerCase();

  if (nodeEnv === "test") {
    return true;
  }

  if (synchronizeOverride === "true") {
    return true;
  }

  if (synchronizeOverride === "false") {
    return false;
  }

  return nodeEnv !== "production";
};
