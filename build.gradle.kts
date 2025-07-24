val ktorVersion = "2.3.7"
val mysqlVersion = "8.0.33"
val exposedVersion = "0.45.0"
val hikaricpVersion = "5.0.1"

plugins {
    kotlin("jvm") version "2.1.21"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-config-yaml-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-call-logging:${ktorVersion}")


    implementation("org.jetbrains.exposed:exposed-core:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-dao:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${exposedVersion}")
    implementation("org.jetbrains.exposed:exposed-java-time:${exposedVersion}")
    implementation("mysql:mysql-connector-java:${mysqlVersion}")

    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    implementation("com.zaxxer:HikariCP:${hikaricpVersion}")

    implementation("io.ktor:ktor-server-content-negotiation:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-jackson:${ktorVersion}")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}