
plugins {
    kotlin("jvm") version "2.0.21"
    `java-library`
}


version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


    api(libs.commons.math3)

    implementation(libs.guava)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version))
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
