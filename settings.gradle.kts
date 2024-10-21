val restWrapperVersion: String by settings

pluginManagement {
   // includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "ReKotlin"
include("list",
        "app")

project(":data_service").also {
    it.name = "ExposedDAOWrapper"
    it.buildFileName = "data_service/build.gradle.kts"
}

project(":rest_service").also {
    it.name = "RestApiServerWrapper"
    it.buildFileName = "rest_service/build.gradle.kts"
}

project(":ws_service").also {
    it.name = "WsApiServerWrapper"
    it.buildFileName = "ws_service/build.gradle.kts"
}

project(":tg_components").also {
    it.name = "TelegramComponents"
    it.buildFileName = "tg_components/build.gradle.kts"
}


