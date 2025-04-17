package po.restwraptor.plugins

import io.ktor.http.ContentType
import io.ktor.serialization.ContentConverter
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.call
import io.ktor.server.request.contentType
import io.ktor.server.request.httpMethod
import io.ktor.util.AttributeKey

class CustomContentNegotiation private constructor(config: Configuration) {
    private val converters = config.converters.toList()
    class Configuration {
        internal val converters = mutableListOf<Pair<ContentType, ContentConverter>>()
        fun register(contentType: ContentType, converter: ContentConverter) {
            converters.add(contentType to converter)
        }
    }
    fun intercept(call: ApplicationCall): ContentConverter? {
        val contentType = call.request.contentType()
        val method = call.request.httpMethod // Example additional parameter

        // Find the matching converter
        val converter = converters.firstOrNull { (type: ContentType, _) ->
            contentType.match(type)
        }?.second
        if (converter != null) {
            println("HTTP Method: $method, Content-Type: $contentType")
        }
        return converter
    }

    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, CustomContentNegotiation> {
        override val key = AttributeKey<CustomContentNegotiation>("CustomContentNegotiation")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): CustomContentNegotiation {
            val config = Configuration().apply(configure)
            val plugin = CustomContentNegotiation(config)

            pipeline.intercept(ApplicationCallPipeline.Plugins) {
                val converter = plugin.intercept(call)
                proceed()
            }
            return plugin
        }
    }
}