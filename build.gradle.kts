
plugins {
    kotlin("jvm") version "2.1.0"
    id("com.google.devtools.ksp")
    id("maven-publish")
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
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
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.google.devtools.ksp")
    apply(plugin = "maven-publish")

    plugins.withId("org.jetbrains.kotlin.jvm") {
        kotlin {
            jvmToolchain {
                languageVersion.set(JavaLanguageVersion.of(23))
            }
            compilerOptions {
                freeCompilerArgs.add("-Xcontext-parameters")
            }
        }
    }
}




