
val authCoreVersion: String by project
val coroutinesVersion: String by project

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "po.auth"
version = authCoreVersion

repositories {
    mavenCentral()
    mavenLocal()
}


dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
