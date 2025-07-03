package po.misc.collections

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


class StaticTypeKey<T : Any> @PublishedApi internal constructor(
    private val clazz: KClass<T>
) {

    internal val typeName: String = clazz.java.typeName
    private val cachedHash: Int = typeName.hashCode()

    override fun equals(other: Any?): Boolean {
        return other is StaticTypeKey<*> &&
                this.typeName == other.typeName
    }
    override fun hashCode(): Int = cachedHash
    override fun toString(): String = "StaticTypeKey(type=$typeName)"
    fun getHash(): String{
        return "StaticTypeKey(cachedHash=$cachedHash)"
    }

    fun <I: Any> isInstanceOfType(instance: I): Boolean{
       val instanceName = instance::class.java.typeName
       return typeName == instanceName
    }

    companion object {
        inline fun <reified T : Any> createTypeKey(): StaticTypeKey<T> {
            return StaticTypeKey(T::class)
        }
        fun <T : Any> createTypeKey(clazz : KClass<T>): StaticTypeKey<T> {
            return StaticTypeKey(clazz)
        }
    }
}
