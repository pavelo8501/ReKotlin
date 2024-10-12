plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "po.playground"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":data_service"))
}

application {
    mainClass.set("po.playground.MainKt") // Set your main class
}


kotlin {
    jvmToolchain(21)
}