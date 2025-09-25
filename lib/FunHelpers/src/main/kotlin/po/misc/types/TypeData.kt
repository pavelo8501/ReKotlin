package po.misc.types

import po.misc.collections.ComparableType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf


open class TypeData<T: Any>(
    override val kClass: KClass<T>,
    val kType: KType
): ComparableType<T> {

    val simpleName : String get() = kClass.simpleName.toString()
    val qualifiedName: String get() = kClass.qualifiedName.toString()

    override val typeName: String
        get() = normalizedSimpleString()

    override fun hashCode(): Int {
        return kType.hashCode()
    }
    override fun equals(other: Any?): Boolean {
        return other is TypeData<*> &&
                this.kType == other.kType
    }
    override fun toString(): String = "TypeData<$typeName>"

    fun normalizedSimpleString(): String {
        val classifier = kType.classifier as? KClass<*> ?: return "Unknown"
        val typeArgs = kType.arguments.joinToString(", ") { arg ->
            val argType = arg.type
            val argClassifier = argType?.classifier as? KClass<*>
            val argName = argClassifier?.simpleName ?: "Unknown"
            if (argType?.isMarkedNullable == true) "$argName?" else argName
        }
        val baseName = classifier.simpleName ?: "Unknown"
        val nullableSuffix = if (kType.isMarkedNullable) "?" else ""
        return if (kType.arguments.isNotEmpty()) {
            "$baseName<$typeArgs>$nullableSuffix"
        } else {
            "$baseName$nullableSuffix"
        }
    }

    companion object{
        inline fun <reified T: Any> create():TypeData<T>{
            return TypeData(T::class, typeOf<T>())
        }
        @Deprecated("Highly unreliable", level = DeprecationLevel.WARNING)
        fun <T: Any> createByKClass(clazz: KClass<T>):TypeData<T>{
            val type =  clazz.createType()
            return TypeData(clazz, type)
        }
    }
}

