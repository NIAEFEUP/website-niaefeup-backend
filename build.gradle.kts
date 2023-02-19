import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.0"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("jvm") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
    kotlin("plugin.jpa") version "1.7.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
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
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-validation:3.0.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
    """.trimMargin()

configure<com.epages.restdocs.apispec.gradle.OpenApi3Extension> {
    setServer("http://localhost:8080")
    title = apiSpecTitle
    description = apiSpecDescription
    version = "${project.version}"
    format = "json"
    tagDescriptionsPropertiesFile = "src/docs/tag-descriptions.yaml"
}

configure<com.epages.restdocs.apispec.gradle.PostmanExtension> {
    title = apiSpecTitle
    version = "${project.version}"
    baseUrl = "https://localhost:8080"
}

tasks.register<Copy>("copyToDocs") {
    dependsOn(tasks.named("openapi3"))

    from("${project.buildDir}/api-spec/openapi3.json")
    into(File("docs"))
}
