dependencies {
    implementation("org.springframework.boot:spring-boot-autoconfigure")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework:spring-web")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}

tasks.bootJar {
    enabled = false
}
tasks.jar {
    enabled = true
}
