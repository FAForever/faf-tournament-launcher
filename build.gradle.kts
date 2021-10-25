import com.google.cloud.tools.jib.gradle.JibExtension
import com.google.cloud.tools.jib.gradle.JibPlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.5.31"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "2.5.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.google.cloud.tools.jib") version "3.1.4"

    // /****** Additional tooling *****/
    // Code formatting
    id("com.diffplug.spotless") version "5.12.5"
}

group = "com.faforever"
version = "SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

extra["springCloudVersion"] = "2020.0.4"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
//    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-rabbit")
    implementation("org.springframework.cloud:spring-cloud-function-kotlin")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    val ktlintVersion = "0.42.1"
    kotlin {
        ktlint(ktlintVersion)
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint(ktlintVersion)
    }
}

plugins.withType<JibPlugin> {
    configure<JibExtension> {

        from.image = "eclipse-temurin:17-jdk"

        to {
            image = "faforever/faf-tournament-launcher"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Summarize test results
    addTestListener(object : TestListener {
        val ANSI_BOLD_WHITE = "\u001B[0;1m"
        val ANSI_RESET = "\u001B[0m"
        val ANSI_BLACK = "\u001B[30m"
        val ANSI_RED = "\u001B[31m"
        val ANSI_GREEN = "\u001B[32m"
        val ANSI_YELLOW = "\u001B[33m"
        val ANSI_BLUE = "\u001B[34m"
        val ANSI_PURPLE = "\u001B[35m"
        val ANSI_CYAN = "\u001B[36m"
        val ANSI_WHITE = "\u001B[37m"
        val BALLOT_CHECKED = "\uD83D\uDDF9"
        val BALLOT_UNCHECKED = "\u2610"
        val BALLOT_CROSS = "\uD83D\uDDF7"

        override fun beforeSuite(suite: TestDescriptor) {
            if (suite.name.startsWith("Test Run") || suite.name.startsWith("Gradle Worker")) {
                return
            }

            if (suite.parent != null && suite.className != null) {
                println(ANSI_BOLD_WHITE + suite.name + ANSI_RESET)
            }
        }

        override fun beforeTest(testDescriptor: TestDescriptor?) {
        }

        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
            val indicator = when {
                result.failedTestCount > 0 -> ANSI_RED + BALLOT_CROSS
                result.skippedTestCount > 0 -> ANSI_YELLOW + BALLOT_UNCHECKED
                else -> ANSI_GREEN + BALLOT_CHECKED
            }

            println("    $indicator$ANSI_RESET ${testDescriptor.name}")
        }

        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent != null && suite.className != null) {
                println("")
            }

            if (suite.parent == null) { // will match the outermost suite
                val successStyle = ANSI_GREEN
                val skipStyle = ANSI_YELLOW
                val failStyle = ANSI_RED
                val summaryStyle = when (result.resultType) {
                    TestResult.ResultType.SUCCESS -> successStyle
                    TestResult.ResultType.SKIPPED -> skipStyle
                    TestResult.ResultType.FAILURE -> failStyle
                }

                println(
                    """
                        --------------------------------------------------------------------------
                        Results: $summaryStyle${result.resultType}$ANSI_RESET (${result.testCount} tests, $successStyle${result.successfulTestCount} passed$ANSI_RESET, $failStyle${result.failedTestCount} failed$ANSI_RESET, $skipStyle${result.skippedTestCount} skipped$ANSI_RESET)
                        --------------------------------------------------------------------------
                    """.trimIndent()
                )
            }
        }
    })
}
