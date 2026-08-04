import { Test, TestingModule } from "@nestjs/testing";
import { AppController } from "./app.controller";
import { AppService } from "./app.service";

describe("AppController", () => {
  let appController: AppController;
  let appService: AppService;

  beforeEach(async () => {
    const app: TestingModule = await Test.createTestingModule({
      controllers: [AppController],
      providers: [AppService],
    }).compile();

    appController = app.get<AppController>(AppController);
    appService = app.get<AppService>(AppService);
  });

  describe("root", () => {
    it("should return API information", () => {
      const result = {
        name: "NIAEFEUP Website API",
        version: "1.0.0",
        status: "running",
        documentation: "/docs",
      };
      jest.spyOn(appService, "getApiInfo").mockReturnValue(result);
      expect(appController.getApiInfo()).toEqual(result);
    });
  });

  describe("health", () => {
    it("should return health status", () => {
      expect(appController.healthCheck()).toMatchObject({
        status: "ok",
        timestamp: expect.any(String),
      });
    });
  });
});
