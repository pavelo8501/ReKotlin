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
    signing
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

//publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            from(components["java"]) // This publishes the main Java/Kotlin component
//            groupId = "po.misc"
//            artifactId = "funhelpers"
//            version = funHelpersVersion
//        }
//    }
//}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "io.github.pavelo8501"
            artifactId = "funhelpers"
            version = funHelpersVersion

            from(components["java"])

            pom {
                name.set("FunHelpers")
                description.set("Your library description")
                url.set("https://github.com/pavelo8501/ReKotlin")

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("pavelo8501")
                        name.set("Pavel Olshansky")
                        email.set("pavelo8501@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/pavelo8501/ReKotlin.git")
                    developerConnection.set("git@github.com:pavelo8501/ReKotlin.git")
                    url.set("https://github.com/pavelo8501/ReKotlin")
                }

            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            // S01
            setUrl("https://central.sonatype.com/api/v1/publisher/maven/releases")
            credentials {
                username = project.findProperty("sonatypeUsername")?.toString() ?: ""
                password = project.findProperty("sonatypeUsername")?.toString() ?: ""
            }
        }
    }
}

signing {
    val gpgExe = File("C:\\Program Files (x86)\\GnuPG\\bin\\gpg.exe")
    if (gpgExe.exists()) {
        useGpgCmd()
    } else {
        println("GPG not found at: $gpgExe")
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project

        if (signingKey != null && signingKeyId != null) {
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        }
        useGpgCmd()
        sign(publishing.publications["mavenJava"])
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
