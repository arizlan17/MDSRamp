plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.4")
    implementation("org.springframework.boot:spring-boot-starter-batch:3.2.4")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:3.2.4")
    implementation("org.springframework:spring-oxm:6.1.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.4")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.4")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.4")
    implementation("org.postgresql:postgresql:42.7.2")

    testImplementation("org.springframework.batch:spring-batch-test:5.1.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")
    testRuntimeOnly("com.h2database:h2:2.2.224")

}

tasks.test {
    useJUnitPlatform()
}