import net.ltgt.gradle.errorprone.errorprone

plugins {
    java
    id("org.springframework.boot") version "3.5.9"
    id("io.spring.dependency-management") version "1.1.7"
    id("io.freefair.lombok") version "8.4"
    id("net.ltgt.errorprone") version "4.1.0"
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

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = false
}
