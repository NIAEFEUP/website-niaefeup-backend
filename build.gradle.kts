import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.spring") version "1.8.10"
    kotlin("plugin.jpa") version "1.8.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.2.0"
    id("com.epages.restdocs-api-spec") version "0.17.1"

    jacoco
}

jacoco {
    toolVersion = "0.8.8"
}

group = "pt.up.fe.ni.website"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("ch.qos.logback:logback-core:1.4.5")
    implementation("org.slf4j:slf4j-api:2.0.6")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.0.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("ch.qos.logback:logback-classic:1.4.5")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc:3.0.0")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.17.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xemit-jvm-type-annotations")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
}

// Rest Docs API Spec tasks configuration
val apiSpecTitle = "NIAEFEUP Website - Backend API specification"
val apiSpecDescription =
    """This specification documents the available endpoints and possible operations on the website's backend.
        |For each of the operations, its purpose, security, requests and possible responses are documented.
        |
        |Postman collection also available <a href="postman-collection.json" download>here</a>.
    """.trimMargin()

configure<com.epages.restdocs.apispec.gradle.OpenApi3Extension> {
    setServer("http://localhost:8080")
    title = apiSpecTitle
    description = apiSpecDescription
    version = "${project.version}"
    format = "json"
    tagDescriptionsPropertiesFile =
        "src/test/kotlin/pt/up/fe/ni/website/backend/utils/documentation/tag-descriptions.yaml"
}

configure<com.epages.restdocs.apispec.gradle.PostmanExtension> {
    title = apiSpecTitle
    version = "${project.version}"
    baseUrl = "https://localhost:8080"
}

tasks.register<Copy>("generateDocs") {
    dependsOn(tasks.named("openapi3"))
    dependsOn(tasks.named("postman"))
    dependsOn(tasks.named("fixExamples"))

    from("${project.buildDir}/api-spec/openapi3.json")
    into(File("docs"))

    from("${project.buildDir}/api-spec/postman-collection.json")
    into(File("docs"))
}

tasks.register("fixExamples") {
    dependsOn(tasks.named("openapi3"))
    doLast {
        val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()

        val spec = objectMapper.readTree(File("${project.buildDir}/api-spec/openapi3.json"))
        (spec as com.fasterxml.jackson.databind.node.ObjectNode)
            .findValues("examples").forEach { examples ->
                examples.forEach { example ->
                    (example as com.fasterxml.jackson.databind.node.ObjectNode)
                        .replace("value", objectMapper.readTree(example.get("value").asText()))
                }
            }

        objectMapper.writer().withDefaultPrettyPrinter().writeValue(
            File("${project.buildDir}/api-spec/openapi3.json"),
            spec
        )
    }
}
