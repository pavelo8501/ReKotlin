val kotlinVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val junitVersion: String by project
val logbackClassicVersion: String by project

plugins {

    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization")

    `java-library`
    `maven-publish`
}


repositories {
    maven {
        name = "PublicGitHubPackages"
        url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
    }
}

dependencies {

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    implementation("io.ktor:ktor-server-cors:$ktorVersion")

   // implementation(libs.guava)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

}

