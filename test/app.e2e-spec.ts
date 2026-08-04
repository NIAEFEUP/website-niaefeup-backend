import { INestApplication } from "@nestjs/common";
import { Test, TestingModule } from "@nestjs/testing";
import request from "supertest";
import { App } from "supertest/types";
import { AppModule } from "@/app.module";

describe("AppController (e2e)", () => {
  let app: INestApplication<App>;

  beforeEach(async () => {
    const moduleFixture: TestingModule = await Test.createTestingModule({
      imports: [AppModule],
    }).compile();

    app = moduleFixture.createNestApplication();
    await app.init();
  });

  it("/ (GET) - should return API info", () => {
    return request(app.getHttpServer())
      .get("/")
      .expect(200)
      .expect((res) => {
        expect(res.body).toHaveProperty("name", "NIAEFEUP Website API");
        expect(res.body).toHaveProperty("version", "1.0.0");
        expect(res.body).toHaveProperty("status", "running");
        expect(res.body).toHaveProperty("documentation", "/docs");
      });
  });

  it("/health (GET) - should return health status", () => {
    return request(app.getHttpServer())
      .get("/health")
      .expect(200)
      .expect((res) => {
        expect(res.body).toHaveProperty("status", "ok");
        expect(res.body).toHaveProperty("timestamp");
      });
  });

  afterEach(async () => {
    await app.close();
  });
});
