
val kotlinVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val junitVersion:String by project
val coroutinesVersion:String by project
val restWraptorVersion:String by project
val okioVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "po.restwraptor"
version = restWraptorVersion

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {


    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:${ktorVersion}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("org.slf4j:slf4j-api:2.0.17")

    api(project(":lib:AuthCore"))
    api(project(":lib:LogNotify"))
    api(project(":lib:FunHelpers"))

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    testImplementation("com.squareup.okio:okio:${okioVersion}")
    testImplementation("io.ktor:ktor-server-call-logging:${ktorVersion}")
    testImplementation("org.slf4j:slf4j-api:2.0.17")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]) // This publishes the main Java/Kotlin component
            groupId = "po.restwraptor"
            artifactId = "restwraptor"
            version = restWraptorVersion
        }
    }
}

tasks.test {
    useJUnitPlatform()
}