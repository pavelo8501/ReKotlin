
val kotlinVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project


plugins{
   // kotlin("plugin.serialization")
    kotlin("jvm")
}

version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {

   // testImplementation(libs.junit.jupiter)
  //  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    //implementation(libs.guava)
}
