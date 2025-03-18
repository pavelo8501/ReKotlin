package po.restwraptor.interfaces

import io.ktor.server.application.Application
import kotlinx.serialization.descriptors.StructureKind


interface StringHelper {

    fun toUrl(vararg pathParts: String): String

    companion object : StringHelper {
        override fun toUrl(vararg pathParts: String): String =
            pathParts
                .asSequence()
                .map { it.trim().trim('/') }
                .filter { it.isNotEmpty() }
                .joinToString("/")
    }
}



