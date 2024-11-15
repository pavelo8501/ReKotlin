val binderPluginVersion: String by project

plugins {
    kotlin("jvm")
}

group = "po.plugins"
version = binderPluginVersion

repositories {
    mavenCentral()
}

dependencies {

    implementation(kotlin("stdlib-jdk8"))

    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.21-1.0.25")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

    testImplementation(kotlin("test"))
}

kotlin {


    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}



tasks.test {
    useJUnitPlatform()
}