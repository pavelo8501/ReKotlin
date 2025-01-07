
val kotlinVersion: String by project
val ktorVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project
val logbackClassicVersion: String by project
val testCoroutinesVersion: String by project
val junitVersion: String by project
val kotlinSerializationVersion : String by project

val daoServiceVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
   `maven-publish`
}

version = daoServiceVersion

repositories {
    mavenCentral()

    maven {
        name = "PublicGitHubPackages"
        url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("com.zaxxer:HikariCP:$hikaricpVersion")
    implementation("mysql:mysql-connector-java:$mysqlVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$testCoroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    testImplementation("io.mockk:mockk:1.13.13")


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
            from(components["java"])
            groupId = "com.github.pavelo8501"
            artifactId = "exposed-dao-wrapper"
            version = daoServiceVersion
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

