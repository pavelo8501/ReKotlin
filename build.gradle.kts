
//import org.jetbrains.kotlin

val kotlinVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val junitVersion: String by project
val logbackClassicVersion: String by project

plugins {
    kotlin("jvm")  version "2.0.21"
    id("com.google.devtools.ksp")
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

subprojects {

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.google.devtools.ksp")

    plugins.withId("org.jetbrains.kotlin.jvm") {
        kotlin {
            jvmToolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }
    }
}


