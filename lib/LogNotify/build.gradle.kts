
val kotlinVersion: String by project
val kotlinReflectVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project
val junitVersion:String by project
val coroutinesVersion:String by project
val logNotifyVersion:String by project

plugins{
    kotlin("jvm")
}

group = "po.lognotify"
version = logNotifyVersion

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinReflectVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"]) // This publishes the main Java/Kotlin component
            groupId = "po.lognotify"
            artifactId = "lognotify"
            version = logNotifyVersion
        }
    }
}


tasks.withType<Test> {
    useJUnitPlatform()
}