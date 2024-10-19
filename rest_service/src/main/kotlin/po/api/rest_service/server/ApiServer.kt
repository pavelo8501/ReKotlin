package po.api.rest_service.server


import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.json.Json
import po.api.rest_service.common.ApiLoginRequestDataContext
import po.api.rest_service.models.DefaultLoginRequest

import po.api.rest_service.models.DeleteRequestData
import po.api.rest_service.models.LoginRequestData
import po.api.rest_service.models.RequestData
import po.api.rest_service.models.SelectRequestData
import po.api.rest_service.models.UpdateRequestData
import po.api.rest_service.plugins.PolymorphicJsonConverter


class ApiServer(
    private val configure: (Application.() -> Unit)? = null
) {
    companion object {
        fun create(configure: (Application.() -> Unit)? = null): ApiServer {
            return ApiServer(configure)
        }

        fun start(host: String, port: Int, configure: (Application.() -> Unit)? = null) {
            create(configure).configureHost(host, port).start()
        }

        @OptIn(ExperimentalSerializationApi::class)
        fun jsonDefault(builderAction: SerializersModuleBuilder.() -> Unit): Json{
            val json = Json {
                serializersModule = SerializersModule(builderAction)
                classDiscriminator = "type"
                ignoreUnknownKeys = true
                decodeEnumsCaseInsensitive = true
            }
            return json
        }
    }
    private var host: String = "0.0.0.0"
    private var port: Int = 8080

    fun configureHost(host: String, port: Int): ApiServer {
        this.host = host
        this.port = port
        return this
    }

    fun start(wait: Boolean = true) {

        embeddedServer(Netty, port, host) {
            configure?.invoke(this)
            if (this.pluginOrNull(ContentNegotiation) != null) {
                //Can do something here if ContentNegotiation is already installed
            } else {
                install(ContentNegotiation) {
                    //Register custom JSON converter, since the default one does not support polymorphic serialization
                    register(
                        ContentType.Application.Json,
                        PolymorphicJsonConverter(
                            jsonDefault() {
                                polymorphic(ApiLoginRequestDataContext::class) {
                                    subclass(DefaultLoginRequest::class, DefaultLoginRequest.serializer())
                                }
                                polymorphic(RequestData::class) {
                                   subclass(SelectRequestData::class, SelectRequestData.serializer())
                                   subclass(UpdateRequestData::class, UpdateRequestData.serializer())
                                   subclass(DeleteRequestData::class, DeleteRequestData.serializer())
                                   subclass(LoginRequestData::class, LoginRequestData.serializer())
                                }
                            }
                        )
                    )
                }
            }

            if (this.pluginOrNull(CORS) != null) {
                //Can do something here if CORS is already installed
            } else {
                install(CORS) {
                    run {
                        allowMethod(HttpMethod.Post)
                        allowHeader(HttpHeaders.ContentType)
                        allowCredentials = true
                        anyHost()
                    }
                }
            }
            routing {
                // Default routes
            }
        }.start(wait)
    }
}