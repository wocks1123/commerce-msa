dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-web")
    compileOnly("jakarta.servlet:jakarta.servlet-api")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.10")

    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:6.1:jakarta")
    implementation("io.github.openfeign.querydsl:querydsl-jpa:6.1")

    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}
