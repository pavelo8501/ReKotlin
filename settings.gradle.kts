
rootProject.name = "ReKotlin"

pluginManagement {

    plugins{
        kotlin("plugin.serialization")  version "2.0.21"
        id("com.google.devtools.ksp") version "2.0.21-1.0.25"
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }


    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}



includeBuild("build-logic")
include("app","lib", "lib:binder", "lib:function_processor", "lib:rest_service", "lib:ws_service", "lib:tg_components", "lib:data_service")

project(":lib:rest_service").also {
    it.name = "RestApiServerWrapper"
}

project(":lib:ws_service").also {
    it.name = "WSApiServerWrapper"
}

project(":lib:data_service").also {
    it.name = "ExposedDAOWrapper"
}

project(":lib:binder").also {
    it.name = "binderPlugin"
}

project(":lib:function_processor").also {
    it.name = "functionProcessorPlugin"
}
