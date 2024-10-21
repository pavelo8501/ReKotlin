import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
    kotlin("jvm").also{
        println("kotlin jvm plugin version $it")
    }
    kotlin("plugin.serialization")
    id("org.gradle.kotlin.kotlin-dsl")
    id("com.diffplug.spotless") version "7.0.0.BETA3"
    id("com.gradleup.shadow") version "8.3.3"

    `java-library`
    `maven-publish`
}

group = "po.api"
version = wsWrapperVersion


spotless {
    kotlinGradle {
      //  ktlint()
        target("**/*.kts")
        targetExclude("build-logic/build/**")
    }
}


repositories {
    mavenCentral()

    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
    }
}

val publishOnly by configurations.creating
val developmentOnly = configurations.create("developmentOnly")
configurations.runtimeClasspath.get().extendsFrom(developmentOnly)


dependencies {

    implementation("com.github.pavelo8501:rest-api-wrapper:$restWrapperVersion")
    implementation(project(":RestApiServerWrapper"))
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")


    // compileOnly(localGroovy())
   // developmentOnly(project(":RestApiServerWrapper"))
   // publishOnly("com.github.pavelo8501:rest-api-wrapper:$restWrapperVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$testCoroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackClassicVersion")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

}

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            substitute(module("com.github.pavelo8501:rest-api-wrapper"))
                .using(project(":RestApiServerWrapper"))
        }
    }
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
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/pavelo8501/ReKotlin")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "com.github.pavelo8501"
            artifactId = "ws-api-wrapper"
            version = wsWrapperVersion
        }

    }
}

spotless {
    kotlinGradle {
        ktlint()
        target("**/*.kts")
        targetExclude("build-logic/build/**")
    }
}


tasks.withType<ShadowJar> {
    archiveClassifier.set("all")
    mergeServiceFiles()
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version))
    }
}


tasks.withType<JavaExec> {
    // Already included via runtimeClasspath, no need to manipulate classpath manually
}


tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<PublishToMavenRepository> {
    dependsOn("test")
    doFirst {
        configurations["publishOnly"].resolve()
    }
}

tasks.register("release") {

    dependencies {
        implementation("com.github.pavelo8501:rest-api-wrapper:$restWrapperVersion")
    }

    dependsOn(
        dependsOn("test"),
        tasks.withType<ShadowJar>(),
        tasks.publish,
       // tasks.publishPlugins,
      //  tasks.gitPublishPush,
    )
}







