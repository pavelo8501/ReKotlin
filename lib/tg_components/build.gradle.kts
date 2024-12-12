
val kotlinVersion: String by project
val ktorVersion: String by project
val kotlinSerializationVersion: String by project
val exposedVersion: String by project
val hikaricpVersion: String by project
val mysqlVersion: String by project

val tgUiVersion: String by project


plugins{
    kotlin("jvm")
}
group = "po.tg.ui"
version = tgUiVersion

repositories {
    mavenCentral()
}



dependencies {


}
