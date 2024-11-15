import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.sourceSets

val functionProcessorPluginVersion: String by project

plugins {
    kotlin("jvm")
}

group = "po.plugins"
version = functionProcessorPluginVersion

repositories {
    mavenCentral()
}


dependencies {

    implementation(kotlin("stdlib-jdk8"))

    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.25")
    testImplementation(kotlin("test"))
}



tasks.test {
    useJUnitPlatform()
}