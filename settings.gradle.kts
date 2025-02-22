
rootProject.name = "ReKotlin"

pluginManagement {
    plugins{
        id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
        id("io.gitlab.arturbosch.detekt") version "1.23.7"
        kotlin("plugin.serialization")  version "2.1.10" apply false
        kotlin("jvm") version "2.1.10" apply false

    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(
    "app",
    "lib:rest_service",
    "lib:ws_service",
    "lib:Exposify",
    "lib:LogNotify",
    "lib:RestWraptor",
    "lib:WebSocketWraptor",
    "lib:function_processor",
    "lib:binder")

project(":lib:rest_service").also {
    it.name = "RestApiServerWrapper"
}

project(":lib:ws_service").also {
    it.name = "WSApiServerWrapper"
}

project(":lib:Exposify").also {
    it.name = "Exposify"
}

project(":lib:binder").also {
    it.name = "binderPlugin"
}

project(":lib:function_processor").also {
    it.name = "functionProcessorPlugin"
}

project(":lib:RestWraptor").also {
    it.name = "RestWraptor"
}

project(":lib:WebSocketWraptor").also {
    it.name = "WebSocketWraptor"
}

project(":lib:LogNotify").also {
    it.name = "LogNotify"
}
include("MedTest")
