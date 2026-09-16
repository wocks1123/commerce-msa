dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")

    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.10")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.bootJar {
    enabled = true
}
tasks.jar {
    enabled = false
}
