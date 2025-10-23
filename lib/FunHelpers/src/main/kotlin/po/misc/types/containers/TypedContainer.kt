package po.misc.types.containers

import po.misc.reflection.classes.ClassInfo
import po.misc.reflection.classes.ClassRole
import po.misc.reflection.classes.overallInfo
import po.misc.reflection.classes.overallInfoFromType
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KTypeParameter


interface TypedClass<T: Any>{
    val receiver:T
    val typeData: TypeToken<T>
}

open class TypedContainer<T: Any>(
    override val receiver:T,
    override val typeData: TypeToken<T>,
    val classInfo: ClassInfo<T>
): Single<T>(receiver), TypedClass<T>, Comparable<TypedContainer<*>>, ComplexContainers<T>{

    internal val typeName: String = typeData.kClass.java.typeName
    private val cachedHash: Int = typeName.hashCode()

    fun compareType(other: TypeToken<*>): Boolean{
        return typeData.kClass == other.kClass
    }

    override fun compareTo(other: TypedContainer<*>): Int {
        val thisClassifier = typeData.kType.classifier
        val otherClassifier = other.typeData.kType.classifier

        val thisName = when (thisClassifier) {
            is KClass<*> -> thisClassifier.qualifiedName ?: ""
            is KTypeParameter -> thisClassifier.name
            else -> thisClassifier?.toString() ?: ""
        }

        val otherName = when (otherClassifier) {
            is KClass<*> -> otherClassifier.qualifiedName ?: ""
            is KTypeParameter -> otherClassifier.name
            else -> otherClassifier?.toString() ?: ""
        }

        val nameComparison = thisName.compareTo(otherName)
        return if (nameComparison != 0) {
            nameComparison
        } else {
            receiver.hashCode().compareTo(other.receiver.hashCode())
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is TypeToken<*> &&
                this.typeName == other.typeName
    }
    override fun hashCode(): Int = cachedHash
}

inline fun <reified T: Any> T.toTypeContainer():TypedContainer<T>{
   return TypedContainer(this, TypeToken.create<T>(), overallInfo(ClassRole.Receiver))
}

fun <T: Any> T.toTypeContainer(container: TypeToken<T>):TypedContainer<T>{
    val info = overallInfoFromType<T>(ClassRole.Receiver, container.kType)
    return TypedContainer(this, container, info)
}