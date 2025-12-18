val kotlinVersion: String by project
val kotlinReflectVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project
val junitVersion: String by project
val coroutinesVersion: String by project
val logNotifyVersion: String by project
val funHelpersVersion: String by project
val typesafeVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
    `maven-publish`
}

group = "io.github.pavelo8501"
version = funHelpersVersion

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "sonatype"
        val releasesRepoUrl = uri("https://central.sonatype.com/api/v1/publisher/maven/releases")
        val snapshotsRepoUrl = uri("https://central.sonatype.com/api/v1/publisher/maven/snapshots")
        url = if (version.toString().endsWith("SNAPSHOT", ignoreCase = true)) {
            snapshotsRepoUrl
        } else {
            releasesRepoUrl
        }
        credentials {
            username = project.findProperty("sonatypeUsername")?.toString()
            password = project.findProperty("sonatypeUsername")?.toString()
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinReflectVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("com.typesafe:config:$typesafeVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


kotlin {
    compilerOptions {
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

tasks.withType<Javadoc> {
    isFailOnError = false
    (options as StandardJavadocDocletOptions).addBooleanOption("Xdoclint:none", true)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<PublishToMavenRepository> {
    dependsOn("test")
    doFirst{
    }
}
