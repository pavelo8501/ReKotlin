val kotlinVersion: String by project
val kotlinReflectVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project
val junitVersion:String by project
val coroutinesVersion:String by project
val logNotifyVersion:String by project
val funHelpersVersion: String by project


plugins {
    kotlin("jvm")
}

group = "po.misc"
version = funHelpersVersion

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinSerializationVersion}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinReflectVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions{
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]) // This publishes the main Java/Kotlin component
            groupId = "po.misc"
            artifactId = "funhelpers"
            version = funHelpersVersion
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}