import { Injectable } from "@nestjs/common";

@Injectable()
export class AppService {
  getApiInfo() {
    return {
      name: "NIAEFEUP Website API",
      version: "1.0.0",
      status: "running",
      documentation: "/docs",
    };
  }

  getHealthCheck() {
    return {
      status: "ok",
      timestamp: new Date().toISOString(),
    };
  }
}
