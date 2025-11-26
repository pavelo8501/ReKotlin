package po.misc.types

import po.misc.data.helpers.orDefault
import po.misc.data.helpers.replaceIfNull
import po.misc.data.tags.EnumTag
import po.misc.data.tags.Tagged
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KType


/**
 * Represents a strongly typed and tagged type descriptor that couples a Kotlin type [T] with an enum-based tag [E].
 *
 * This class is primarily used to uniquely identify types by both their Kotlin class and a specific tag enum,
 * enabling advanced type tagging, dispatching, or classification scenarios.
 *
 * @param T the Kotlin type being described
 * @param E the enum type used for tagging
 *
 * @property kClass The Kotlin class reference of type [T]
 * @property kType The Kotlin reflective type, potentially with generic arguments
 * @property enumTag The associated enum tag containing the enum value and an optional alias
 * @property tag The enum constant used for tagging this type
 * @property alias An alias string that defaults to [typeName] unless explicitly provided
 * @property simpleName The simple name of the class (without package)
 * @property qualifiedName The fully qualified name of the class
 * @property typeName A human-readable version of the type, including generics and nullability
 *
 * @constructor Creates a [TaggedType] by combining type and tag metadata
 */
data class TaggedType<T: Any, E: Enum<E>>(
    override val typeToken: TypeToken<T>,
    override val enumTag: EnumTag<E>
): Tokenized<T>, Tagged<E>{

    val kClass: KClass<T> get() = typeToken.kClass
    val kType: KType   get() = typeToken.kType

    val simpleName : String get() = kClass.simpleName.toString()
    val qualifiedName: String get() = kClass.qualifiedName.toString()

    override fun hashCode(): Int {
        return 31 * kType.hashCode() + enumTag.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is TaggedType<*, *> &&
                this.kType == other.kType &&
                this.enumTag == other.enumTag
    }


    override val alias: String = enumTag.alias.orDefault(typeToken.typeName)

    override fun toString(): String {
        return "TaggedType<$typeToken.typeName, ${enumTag.value.name}>"
    }

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
        /**
         * Creates a [TaggedType] from the reified type [T] and enum tag [E], optionally providing an alias.
         *
         * This method uses reified generics and is recommended when both the type and enum tag are known at compile time.
         *
         * Example usage:
         * ```
         * val tag = TaggedType.create<MyDTO, MyEnum>(MyEnum.UPDATE)
         * ```
         *
         * @param enumTag The enum constant to tag this type with
         * @param alias Optional alias string; defaults to the computed [typeName] if not provided
         */
        inline fun <reified T: Any, reified E: Enum<E>> create(enumTag:E, alias: String? = null):TaggedType<T, E>{
            val tagRecord = EnumTag(enumTag, E::class.java)
            alias?.let { tagRecord.alias = it }
            val typeData =  TypeToken.create<T>()
            return TaggedType(typeData, tagRecord)
        }
    }
}