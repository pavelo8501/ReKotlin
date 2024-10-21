
import  org.gradle.kotlin.dsl.*
import org.gradle.api.artifacts.*

val restWrapperVersion: String by settings

plugins {
    kotlin("plugin.serialization") version "2.0.21" apply false
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "ReKotlin"
include("data_service")
include("rest_service")
include("ws_service")
include("tg_ui")
include("playground")

project(":data_service").name = "ExposedDAOWrapper"
project(":rest_service").name = "RestApiServerWrapper"
project(":ws_service").name = "WsApiServerWrapper"






