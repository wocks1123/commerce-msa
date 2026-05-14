import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.4"
    id("net.ltgt.errorprone") version "4.1.0"
    id("jacoco")
    id("jacoco-report-aggregation")
}

allprojects {
    group = "dev.labs.ecommerce"
    version = "0.0.1-SNAPSHOT"
    description = "ecommerce-msa"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("io.freefair.lombok")
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("net.ltgt.errorprone")
    }
    dependencyManagement {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.0.1")
        }
    }
    dependencies {
        compileOnly("org.jspecify:jspecify:1.0.0")
        errorprone("com.google.errorprone:error_prone_core:2.37.0")
        errorprone("com.uber.nullaway:nullaway:0.12.6")

        // Observability: traceId 자동 생성 + 전파
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("io.micrometer:micrometer-tracing-bridge-otel")
        implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile>().configureEach {
        options.errorprone {
            disableAllChecks.set(true)
            option("NullAway:OnlyNullMarked", "true")
            error("NullAway")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

val coverageExcludes = listOf(
    "**/Q*.class",
    "**/*Application.class",
    "**/config/**",
    "**/*Request.class",
    "**/*Response.class",
    "**/*Event.class",
    "**/*Command.class",
    "**/*Result.class",
    "**/*Exception.class",
)

configure(subprojects.filter { it.path.startsWith(":service:") }) {
    apply(plugin = "jacoco")

    tasks.withType<Test>().configureEach {
        finalizedBy(tasks.withType<JacocoReport>())
    }

    tasks.withType<JacocoReport>().configureEach {
        dependsOn(tasks.withType<Test>())
    }
}

dependencies {
    jacocoAggregation(project(":service:inventory-service"))
    jacocoAggregation(project(":service:order-service"))
    jacocoAggregation(project(":service:payment-service"))
    jacocoAggregation(project(":service:product-service"))
}

// 모든 프로젝트(루트 집계 + service 모듈)의 JacocoReport 공통 설정
allprojects {
    tasks.withType<JacocoReport>().configureEach {
        reports {
            html.required.set(true)
            xml.required.set(true)
            csv.required.set(false)
        }
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) { exclude(coverageExcludes) }
            })
        )
    }
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = false
}
