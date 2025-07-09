package po.misc.collections

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf


interface ComparableType<T: Any>{
    val kClass: KClass<T>

    val typeName: String

    override fun equals(other: Any?): Boolean
}

class StaticTypeKey<T: Any> @PublishedApi internal constructor(
    override val kClass: KClass<T>
):ComparableType<T> {

    internal val javaName: String = kClass.java.typeName
    private val cachedHash: Int = typeName.hashCode()

    override val typeName: String
        get() = javaName

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
