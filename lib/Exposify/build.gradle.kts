
val kotlinVersion: String by project
val ktorVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val logbackClassicVersion: String by project
val coroutinesVersion: String by project
val junitVersion: String by project
val kotlinSerializationVersion : String by project
val exposifyVersion: String by project
val postgresVersion: String by project
val testContainerVersion: String by project
val bcryptVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
   `maven-publish`
}

group = "po.exposify"
version = exposifyVersion

kotlin {
    compilerOptions{
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
    }
}

//detekt {
//    toolVersion = "1.23.7"
//   // config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
//    buildUponDefaultConfig = true // Merge with Detekt's default rules
//
//    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
//        reports {
//            html.required.set(true)
//            xml.required.set(true)
//            txt.required.set(false)
//        }
//    }
//}

repositories {
    mavenCentral()
    mavenLocal()
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
    api("org.jetbrains.exposed:exposed-core:$exposedVersion")
    api("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    api("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    api("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    api("com.zaxxer:HikariCP:$hikaricpVersion")

    implementation("at.favre.lib:bcrypt:$bcryptVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    api(project(":lib:AuthCore"))
    api(project(":lib:LogNotify"))

    testImplementation("org.testcontainers:testcontainers:$testContainerVersion")
    testImplementation("org.testcontainers:postgresql:$testContainerVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainerVersion")
    testImplementation("org.postgresql:postgresql:$postgresVersion")
    testImplementation("com.zaxxer:HikariCP:$hikaricpVersion")

    testImplementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    testImplementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion"){
        exclude(group = "junit", module = "junit")
    }
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("at.favre.lib:bcrypt:${bcryptVersion}")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]) // This publishes the main Java/Kotlin component
            groupId = "po.exposify"
            artifactId = "exposify"
            version = exposifyVersion
        }
    }
}

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version))
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}


tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<PublishToMavenRepository> {
    dependsOn("test")
}

