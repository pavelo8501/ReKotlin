
plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    kotlin("jvm") version "2.0.21" apply false
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}





rootProject.name = "ReKotlin"
include("data_service")
include("rest_service")
include("ws_service")
include("tg_ui")
include("playground")
