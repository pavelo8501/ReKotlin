
val kotlinVersion: String by project
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
}

dependencies {


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}


tasks.test {
    useJUnitPlatform()
}