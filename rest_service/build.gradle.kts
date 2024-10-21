val kotlinVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project

val restServerVersion: String by project


val logbackClassicVersion: String by project
val testCoroutinesVersion: String by project
val junitVersion: String by project


plugins {
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization")
    `java-library`
    `maven-publish`
}

group = "po.api.rest"
version = restServerVersion

repositories {
    mavenCentral()
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

    implementation(libs.guava)

    //testImplementation(libs.junit.jupiter)
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
    withSourcesJar()
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
            from(components["java"])
            groupId = "com.github.pavelo8501"
            artifactId = "rest-api-wrapper"
            version = restServerVersion
        }
    }
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

tasks.withType<PublishToMavenRepository> {
    dependsOn("test")
}
