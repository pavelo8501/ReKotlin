import org.gradle.api.tasks.Exec


val exposedVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project
val serializationVersion : String by project


plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "po.playground"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":data_service"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikaricpVersion")
    implementation("mysql:mysql-connector-java:$mysqlVersion")

    implementation("io.github.cdimascio:dotenv-kotlin:6.3.1")

    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.12")
}

application {
    mainClass.set("po.playground.MainKt") // Set your main class
}


kotlin {
    jvmToolchain(21)
}

tasks.register<Exec>("dockerComposeUp") {
    commandLine("docker-compose", "up", "-d")
}

tasks.named("run") {
    dependsOn("dockerComposeUp")
}
