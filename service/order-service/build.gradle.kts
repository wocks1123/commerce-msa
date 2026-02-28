dependencies {
    project(":shared:common")

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:6.1:jakarta")
    implementation("io.github.openfeign.querydsl:querydsl-jpa:6.1")

    implementation("org.postgresql:postgresql")

    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.bootJar {
    enabled = true
}
tasks.jar {
    enabled = false
}
