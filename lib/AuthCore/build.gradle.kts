
val kotlinVersion: String by project
val authCoreVersion: String by project
val coroutinesVersion: String by project
val ktorVersion: String by project
val kotlinxIOVersion:String by project
val okioVersion:String by project
val kotlinSerializationVersion: String by project
val bcryptVersion : String by project

val junitVersion: String by project


plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

group = "po.auth"
version = authCoreVersion

repositories {
    mavenCentral()
    mavenLocal()
}


dependencies {

   // implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
   // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
   // implementation("org.jetbrains.kotlinx:kotlinx-io-core:$kotlinxIOVersion")
    //implementation("com.squareup.okio:okio:$okioVersion")
   // implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
   // implementation("at.favre.lib:bcrypt:$bcryptVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-io-core:${kotlinxIOVersion}")
    implementation("io.ktor:ktor-server-auth-jwt:${ktorVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinSerializationVersion}")
    api("com.squareup.okio:okio:${okioVersion}")
    api("at.favre.lib:bcrypt:$bcryptVersion")

    api(project(":lib:LogNotify"))
    api(project(":lib:FunHelpers"))

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${coroutinesVersion}")
    testImplementation("com.squareup.okio:okio:${okioVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]) // This publishes the main Java/Kotlin component
            groupId = "po.auth"
            artifactId = "authcore"
            version = authCoreVersion
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
