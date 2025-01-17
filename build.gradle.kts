
plugins {
    kotlin("jvm")  version "2.0.21"
    id("com.google.devtools.ksp")
}

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.7")
    }
}


allprojects {
    repositories {
        mavenCentral()
        google()
    }
}

subprojects {

    apply(plugin = "io.gitlab.arturbosch.detekt")
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




