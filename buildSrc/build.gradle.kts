plugins {
    `kotlin-dsl` // <1>
}

repositories {
    gradlePluginPortal() // <2>
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
}
