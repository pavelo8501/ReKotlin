
val authCoreVersion: String by project
val coroutinesVersion: String by project
val ktorVersion: String by project
val kotlinxIOVersion:String by project
val okioVersion:String by project


plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "po.auth"
version = authCoreVersion

repositories {
    mavenCentral()
    mavenLocal()
}


dependencies {

    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:$kotlinxIOVersion")
    implementation("com.squareup.okio:okio:$okioVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
