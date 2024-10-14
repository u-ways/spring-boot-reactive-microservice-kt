rootProject.name = "spring-boot-reactive-microservice-kt"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val flywayVersion = version("flyway", "10.10.0")
            val jacksonModuleVersion = version("jacksonModuleKotlin", "2.15.4")
            val jooqCodeGenerationVersion = version("jooqCodeGeneration", "9.0")
            val jooqVersion = version("jooq", "3.19.6")
            val kotest = version("kotest", "5.8.0")
            val kotlinVersion = version("kotlin", "1.9.23")
            val kotlinxCoroutinesReactorVersion = version("kotlinxCoroutinesReactor", "1.7.3")
            val logbackJsonVersion = version("logbackJson", "0.1.5")
            val logbackVersion = version("logback", "1.4.14")
            val micrometerRegistryPrometheusVersion = version("micrometerRegistryPrometheus", "1.12.4")
            val mockkVersion = version("mockk", "1.13.10")
            val postgresqlVersion = version("postgresql", "42.6.2")
            val r2dbcPostgresqlVersion = version("r2dbcPostgresql", "1.0.4.RELEASE")
            val reactorKotlinExtensionsVersion = version("reactorKotlinExtensions", "1.2.2")
            val reactorRabbitMQVersion = version("rabbitMQ", "1.5.6")
            val reactorVersion = version("reactorVersion", "3.6.4")
            val restAssured = version("restAssured", "5.4.0")
            val springAmqpRabbitTestVersion = version("springAmqpRabbitTest", "3.1.3")
            val springBootVersion = version("springBoot", "3.2.4")
            val springDependencyManagementVersion = version("springDependencyManagement", "1.1.4")
            val springRestDocsWebTestClientVersion = version("springRestDocsWebTestClient", "3.0.1")
            val testContainersVersion = version("testContainers", "1.19.7")
            val ktlintVersion = version("ktlint", "12.1.1")

            plugin("flyway", "org.flywaydb.flyway").versionRef(flywayVersion)
            plugin("jetbrains-kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlinVersion)
            plugin("jetbrains-kotlin-spring", "org.jetbrains.kotlin.plugin.spring").versionRef(kotlinVersion)
            plugin("jooq-code-generation", "nu.studer.jooq").versionRef(jooqCodeGenerationVersion)
            plugin("spring-boot", "org.springframework.boot").versionRef(springBootVersion)
            plugin("spring-dependency-management", "io.spring.dependency-management").versionRef(springDependencyManagementVersion)
            plugin("pinterest.ktlint", "org.jlleitschuh.gradle.ktlint").versionRef(ktlintVersion)

            library("flyway-core", "org.flywaydb", "flyway-core").versionRef(flywayVersion)
            library("flyway-postgresql", "org.flywaydb", "flyway-database-postgresql").versionRef(flywayVersion)
            library("jackson-module-jsr310", "com.fasterxml.jackson.datatype", "jackson-datatype-jsr310").versionRef(jacksonModuleVersion)
            library("jackson-module-kotlin", "com.fasterxml.jackson.module", "jackson-module-kotlin").versionRef(jacksonModuleVersion)
            library("jooq", "org.jooq", "jooq").versionRef(jooqVersion)
            library("jooq-kotlin", "org.jooq", "jooq-kotlin").versionRef(jooqVersion)
            library("jooq-kotlin-coroutines", "org.jooq", "jooq-kotlin-coroutines").versionRef(jooqVersion)
            library("jooq-meta", "org.jooq", "jooq-meta").versionRef(jooqVersion)
            library("jooq-meta-kotlin", "org.jooq", "jooq-meta-kotlin").versionRef(jooqVersion)
            library("kotest-assertions-core", "io.kotest", "kotest-assertions-core").versionRef(kotest)
            library("kotest-runner-junit5", "io.kotest", "kotest-runner-junit5").versionRef(kotest)
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef(kotlinVersion)
            library(
                "kotlinx-coroutines-reactor",
                "org.jetbrains.kotlinx",
                "kotlinx-coroutines-reactor",
            ).versionRef(kotlinxCoroutinesReactorVersion)
            library("logback-classic", "ch.qos.logback", "logback-classic").versionRef(logbackVersion)
            library("logback-jackson", "ch.qos.logback.contrib", "logback-jackson").versionRef(logbackJsonVersion)
            library("logback-json", "ch.qos.logback.contrib", "logback-json-classic").versionRef(logbackJsonVersion)
            library(
                "micrometer-registry-prometheus",
                "io.micrometer",
                "micrometer-registry-prometheus",
            ).versionRef(micrometerRegistryPrometheusVersion)
            library("mockk-core", "io.mockk", "mockk").versionRef(mockkVersion)
            library("postgresql", "org.postgresql", "postgresql").versionRef(postgresqlVersion)
            library("postgresql-r2dbc", "org.postgresql", "r2dbc-postgresql").versionRef(r2dbcPostgresqlVersion)
            library("reactor-core", "io.projectreactor", "reactor-core").versionRef(reactorVersion)
            library(
                "reactor-kotlin-extensions",
                "io.projectreactor.kotlin",
                "reactor-kotlin-extensions",
            ).versionRef(reactorKotlinExtensionsVersion)
            library("reactor-rabbitmq", "io.projectreactor.rabbitmq", "reactor-rabbitmq").versionRef(reactorRabbitMQVersion)
            library("reactor-test", "io.projectreactor", "reactor-test").versionRef(reactorVersion)
            library("rest-assured", "io.rest-assured", "rest-assured").versionRef(restAssured)
            library(
                "spring-boot-starter-actuator",
                "org.springframework.boot",
                "spring-boot-starter-actuator",
            ).versionRef(springBootVersion)
            library("spring-boot-starter-amqp", "org.springframework.boot", "spring-boot-starter-amqp").versionRef(springBootVersion)
            library(
                "spring-boot-starter-data-r2dbc",
                "org.springframework.boot",
                "spring-boot-starter-data-r2dbc",
            ).versionRef(springBootVersion)
            library("spring-boot-starter-jooq", "org.springframework.boot", "spring-boot-starter-jooq").versionRef(springBootVersion)
            library("spring-boot-starter-test", "org.springframework.boot", "spring-boot-starter-test").versionRef(springBootVersion)
            library("spring-boot-starter-webflux", "org.springframework.boot", "spring-boot-starter-webflux").versionRef(springBootVersion)
            library("spring-boot-testcontainers", "org.springframework.boot", "spring-boot-testcontainers").versionRef(springBootVersion)
            library("spring-rabbit-test", "org.springframework.amqp", "spring-rabbit-test").versionRef(springAmqpRabbitTestVersion)
            library(
                "spring-restdocs-webtestclient",
                "org.springframework.restdocs",
                "spring-restdocs-webtestclient",
            ).versionRef(springRestDocsWebTestClientVersion)
            library("testcontainers-junit", "org.testcontainers", "junit-jupiter").versionRef(testContainersVersion)
            library("testcontainers-postgresql", "org.testcontainers", "postgresql").versionRef(testContainersVersion)
            library("testcontainers-r2dbc", "org.testcontainers", "r2dbc").versionRef(testContainersVersion)
            library("testcontainers-rabbitmq", "org.testcontainers", "rabbitmq").versionRef(testContainersVersion)
        }
    }
}
