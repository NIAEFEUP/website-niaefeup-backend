import { Controller, Get } from "@nestjs/common";
import { ApiOperation, ApiResponse, ApiTags } from "@nestjs/swagger";
import { AppService } from "./app.service";

@ApiTags("App")
@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  @ApiOperation({ summary: "Get API information" })
  @ApiResponse({
    status: 200,
    description: "API metadata and status",
    schema: {
      type: "object",
      properties: {
        name: { type: "string", example: "NIAEFEUP Website API" },
        version: { type: "string", example: "1.0.0" },
        status: { type: "string", example: "running" },
        documentation: { type: "string", example: "/docs" },
      },
    },
  })
  getApiInfo() {
    return this.appService.getApiInfo();
  }

  @Get("health")
  @ApiOperation({ summary: "Health check for monitoring" })
  @ApiResponse({
    status: 200,
    description: "Service health status",
    schema: {
      type: "object",
      properties: {
        status: { type: "string", example: "ok" },
        timestamp: { type: "string", example: "2024-01-01T00:00:00.000Z" },
      },
    },
  })
  healthCheck() {
    return this.appService.getHealthCheck();
  }
}
