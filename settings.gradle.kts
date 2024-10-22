
rootProject.name = "ReKotlin"

pluginManagement {
    plugins{
        id("org.jetbrains.kotlin.jvm") version "2.1.0-Beta2"
        id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}



includeBuild("build-logic")
include("app","lib", "lib:rest_service", "lib:ws_service", "lib:tg_components", "lib:data_service")

project(":lib:rest_service").also {
    it.name = "RestApiServerWrapper"
}

project(":lib:ws_service").also {
    it.name = "WSApiServerWrapper"
}

project(":lib:data_service").also {
    it.name = "ExposedDAOWrapper"
}


