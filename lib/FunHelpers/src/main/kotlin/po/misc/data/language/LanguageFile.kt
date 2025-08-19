package po.misc.data.language

import kotlin.reflect.KClass




interface LanguageFile {

    val notFoundError: (key: Any) -> String get() = defaultNotFoundError

    val castError: (kClass: KClass<*>) -> String get() = defaultCastError

    companion object {
        private val defaultNotFoundError: (Any) -> String = { "Element with key $it not found" }

        private val defaultCastError: (KClass<*>) -> String = { "Record exists but cast to $it failed" }
    }
}