plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

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

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.4")

}



tasks.test {
    useJUnitPlatform()
}