import org.gradle.kotlin.dsl.dependencies

val kotlinVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project

val logbackClassicVersion: String by project
val coroutinesVersion: String by project
val junitVersion: String by project

val wsServerVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.gradleup.shadow") version "8.3.3"
    `maven-publish`
}

group = "po.api.ws"
version = wsServerVersion

repositories{
    mavenCentral()
    maven {
        name = "PublicGitHubPackages"
        url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
    }
}

dependencies {

    implementation(project(":lib:RestApiServerWrapper"))
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}


publishing {
    apply(plugin = "maven-publish")
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            artifact(tasks["shadowJar"])
            groupId = "com.github.pavelo8501"
            artifactId = "ws-api-wrapper"
            version = wsServerVersion
        }
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
       // configurations = listOf(project.configurations.runtimeClasspath.get())
        dependencies {
            include(project(":lib:RestApiServerWrapper"))
        }
    }
}

tasks.withType<PublishToMavenRepository> {
    dependsOn("test")
    doFirst{
    }
}







