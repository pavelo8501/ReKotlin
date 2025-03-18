rootProject.name = "ReKotlin"


pluginManagement {
    plugins{
        id("com.google.devtools.ksp") version(providers.gradleProperty("kspVersion").get()) apply false
        id("org.gradle.toolchains.foojay-resolver-convention") version(
            providers.gradleProperty("fooJayResolverVersion")
        )
        id("io.gitlab.arturbosch.detekt") version(providers.gradleProperty("detektVersion").get())
        kotlin("plugin.serialization")  version(providers.gradleProperty("kotlinVersion").get()) apply false
        kotlin("jvm") version (providers.gradleProperty("kotlinVersion").get()) apply false
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
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
