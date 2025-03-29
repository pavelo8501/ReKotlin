package po.auth.sessions.models

import po.auth.sessions.enumerators.SessionDataType
import kotlin.reflect.KClass

class ExternalKey<T>(key: String, clazz: KClass<T & Any>) : StorageKey<T>(SessionDataType.EXTERNAL,key, clazz)
class RoundTripKey<T>(key: String, clazz: KClass<T & Any>) : StorageKey<T>(SessionDataType.ROUND_TRIP, key, clazz)
class SessionKey<T>(key: String, clazz: KClass<T & Any>) : StorageKey<T>(SessionDataType.SESSION, key, clazz)

sealed class StorageKey<T>(
    val keyType: SessionDataType,
    val name: String,
    val clazz: KClass<T & Any>
)