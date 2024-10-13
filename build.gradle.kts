import nu.studer.gradle.jooq.JooqGenerate
import org.flywaydb.gradle.task.FlywayMigrateTask
import org.gradle.api.JavaVersion.VERSION_21
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.hasProperty
import org.jooq.meta.jaxb.Logging
import java.util.Properties

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.spring)
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq.code.generation)
    alias(libs.plugins.pinterest.ktlint)
}

group = "com.katanox.api"
version = System.getenv("VERSION") ?: "DEV-SNAPSHOT"
description = "A modern reactive Spring Boot Kotlin application based on the Katanox API."

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.data.r2dbc)
    implementation(libs.spring.boot.starter.jooq)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.jooq)
    implementation(libs.jooq.kotlin)
    implementation(libs.jooq.kotlin.coroutines)
    implementation(libs.flyway.core)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.reactor)
    implementation(libs.reactor.kotlin.extensions)
    implementation(libs.reactor.rabbitmq)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.logback.classic)
    implementation(libs.logback.json)
    implementation(libs.logback.jackson)

    // NOTE: RabbitMQ Reactor API relies on the reactor-core library.
    //       See: https://projectreactor.io/docs/rabbitmq/snapshot/reference/#_reactor_rabbitmq_api
    compileOnly(libs.reactor.core)

    runtimeOnly(libs.postgresql.r2dbc)
    runtimeOnly(libs.flyway.postgresql)
    // NOTE: Since Flyway doesn't yet support the R2DBC driver,
    //       we'll need to add the standard JDBC driver as well.
    //       See: https://github.com/flyway/flyway/issues/2502
    runtimeOnly(libs.postgresql)
    // NOTE: This is also the case for jOOQ, which doesn't yet
    //       support R2DBC drivers.
    jooqGenerator(libs.postgresql)
    // NOTE: We are explicitly setting the jOOQ version here.
    //       As latest stable Spring Boot defines an older version
    //       which is not compatible with the latest jOOQ plugin.
    jooqGenerator(libs.jooq)
    jooqGenerator(libs.jooq.meta)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.spring.rabbit.test)
    testImplementation(libs.spring.restdocs.webtestclient)
    testImplementation(libs.mockk.core)
    testImplementation(libs.rest.assured)
    testImplementation(libs.reactor.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.r2dbc)
    testImplementation(libs.testcontainers.rabbitmq)
    testImplementation(libs.jackson.module.jsr310)
}

buildscript {
    dependencies {
        /**
         * Flyway team has recently pulled the Postgres artifact out of core and into a separate module.
         * see: https://github.com/flyway/flyway/issues/3722#issuecomment-1871178785
         */
        classpath(libs.flyway.postgresql)
    }
}

/**
 * Instead of duplicating the configuration as hardcoded values, we're going to use the application
 * properties values to configure the Flyway and jOOQ plugins. This way, we can have a single source
 * of truth for the configuration values.
 *
 * I usually don't follow such approach, but I am using this project as an opportunity to experiment
 * with different ways to streamline configurations of flyway and jooq plugins.
 */
val applicationProperties: Properties by lazy {
    project
        .mainSourceSetConfigs()
        .resources.sourceDirectories.asPath
        .let(project::file)
        .resolve("application.properties")
        .takeIf(File::exists)
        ?.inputStream()
        ?.let { stream -> Properties().apply { stream.use(::load) } }
        ?: error("Could not find the main source set's resources directory.")
}

java {
    sourceCompatibility = VERSION_21
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

flyway {
    url = "spring.flyway.url" from applicationProperties
    user = "spring.r2dbc.username" from applicationProperties
    password = "spring.r2dbc.password" from applicationProperties
    driver = "spring.flyway.driver-class-name" from applicationProperties
    schemas = ("spring.flyway.schemas" from applicationProperties).split(",").toTypedArray()
    locations = ("spring.flyway.locations" from applicationProperties).split(",").toTypedArray()
}

jooq {
    version = libs.versions.jooq.get()
    configurations {
        create(MAIN_SOURCE_SET_NAME) {
            generateSchemaSourceOnCompilation = false
            jooqConfiguration.apply {
                logging = Logging.INFO
                jdbc.apply {
                    driver = "spring.flyway.driver-class-name" from applicationProperties
                    url = "spring.flyway.url" from applicationProperties
                    user = "spring.r2dbc.username" from applicationProperties
                    password = "spring.r2dbc.password" from applicationProperties
                }
                generator.apply {
                    database.apply {
                        name = org.jooq.meta.postgres.PostgresDatabase::class.qualifiedName
                        inputSchema = "spring.flyway.schemas" from applicationProperties
                        excludes = "flyway_schema_history"
                    }
                    generate.apply {
                        name = org.jooq.codegen.KotlinGenerator::class.qualifiedName
                        isDeprecated = false
                        isFluentSetters = true
                        isDaos = true
                        isKotlinNotNullPojoAttributes = true
                        isImmutablePojos = true
                        isRecords = false
                    }
                    target.apply {
                        directory = project.mainSourceSetConfigs().kotlin.srcDirs.first().parentFile.resolve("generated").absolutePath
                        packageName = project.group.toString().plus(".sql")
                    }
                }
            }
        }
    }
}

tasks {
    withType<FlywayMigrateTask> {
        /** To ensure access to the classpath resources, we need to run the Flyway task after the resources are processed. */
        dependsOn("processResources")
    }

    withType<JooqGenerate> {
        /** Since we're using the Flyway plugin, we need to tell jOOQ to generate the code after the migrations are done. */
        dependsOn("flywayMigrate")
        /** On top of that, we can point jOOQ task inputs to the migration files. */
        inputs
            .files(fileTree("src/main/resources/db/migration"))
            .withPropertyName("migrationFiles")
            .withPathSensitivity(RELATIVE)
        /** Thus, enabling incremental build support. (i.e. caching) */
        allInputsDeclared = true
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xjsr305=strict"
            jvmTarget = VERSION_21.toString()
        }
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging { showStandardStreams = true }
        systemProperty("org.jooq.no-logo", "true")
        systemProperty("org.jooq.no-tips", "true")
        /**
         * The JDK team's plan (post JDK-21) is to disable dynamic agent loading by default.
         * @see [JDK 21 - Dynamic Loading of Agent (byte-buddy-agent-1.14.4.jar) #3037](https://github.com/mockito/mockito/issues/3037)
         * @see [JEP 451: Prepare to Disallow the Dynamic Loading of Agents](https://openjdk.org/jeps/451)
         */
        jvmArgs("-XX:+EnableDynamicAgentLoading")
    }
}

/** We collect the main source set configurations instead of path guessing. */
private fun Project.mainSourceSetConfigs(): SourceSet =
    extensions
        .getByName("sourceSets")
        .let { ext -> ext as SourceSetContainer }
        .getByName(MAIN_SOURCE_SET_NAME)

/** A safer way to collect properties from the application.properties file. */
private infix fun String.from(properties: Properties): String =
    if (properties.hasProperty(this)) {
        properties.getProperty(this)
    } else {
        error("Property '$this' not found.")
    }
