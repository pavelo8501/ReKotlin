package po.misc.types.info

import po.misc.context.CTX
import po.misc.types.helpers.normalizedName
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class TypeInfo(private val kType: KType) {

    private val normalizedName by lazy { kType.normalizedName() }
    override fun toString(): String {
        return normalizedName
    }
    companion object {
        inline fun <reified T> create(): TypeInfo {
            val type = typeOf<T>()
            return TypeInfo(type)
        }
    }
}