
rootProject.name = "ReKotlin"


pluginManagement {
    plugins{
        id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
        kotlin("plugin.serialization")  version "2.0.21" apply false
        kotlin("jvm") version "2.0.21" apply false

    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}



//includeBuild("build-logic")
include("app", "lib:rest_service", "lib:ws_service", "lib:data_service", "lib:tg_components", "lib:function_processor", "lib:binder")

project(":lib:rest_service").also {
    it.name = "RestApiServerWrapper"
}

project(":lib:ws_service").also {
    it.name = "WSApiServerWrapper"
}

project(":lib:data_service").also {
    it.name = "ExposedDAOWrapper"
}

project(":lib:tg_components").also {
    it.name = "TelegramComponents"
}

project(":lib:binder").also {
    it.name = "binderPlugin"
}

project(":lib:function_processor").also {
    it.name = "functionProcessorPlugin"
}
