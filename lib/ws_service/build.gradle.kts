
//WsApiServerWrapper

val kotlinVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project


val logbackClassicVersion: String by project
val testCoroutinesVersion: String by project
val junitVersion: String by project

val restWrapperVersion: String by project
val wsWrapperVersion: String by project

plugins {
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization")
//    id("org.gradle.kotlin.kotlin-dsl") version "5.1.2"
//    id("com.diffplug.spotless") version "7.0.0.BETA3"
//    id("com.gradleup.shadow") version "8.3.3"
    `java-library`
    `maven-publish`
}

group = "po.api.ws"
version = wsWrapperVersion


repositories {
    mavenCentral()

    maven {
        name = "PublicGitHubPackages"
        url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
    }
}

dependencies {
    implementation(project(":RestApiServerWrapper"))
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$testCoroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

publishing {
    apply(plugin = "maven-publish")
    repositories {
        maven {
            name = "PublicGitHubPackages"
            url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            groupId = "com.github.pavelo8501"
            artifactId = "ws-api-wrapper"
            version = this.version
        }
    }
}


//spotless {
//    kotlinGradle {
//        ktlint()
//        target("**/*.kts")
//        targetExclude("build-logic/build/**")
//    }
//}


tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<PublishToMavenRepository> {
    dependsOn("test")
//    doFirst {
//        configurations["publishOnly"].resolve()
//    }
}







