
val rekotlin : String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("com.google.devtools.ksp")
    id("maven-publish")
    signing
}


buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
    }
}

subprojects {
    //apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.google.devtools.ksp")
    apply(plugin = "maven-publish")

    plugins.withId("org.jetbrains.kotlin.jvm") {
        kotlin {
            jvmToolchain {
                languageVersion.set(JavaLanguageVersion.of(23))
            }
        }
    }
}


java {
    withSourcesJar()
    withJavadocJar()
}


