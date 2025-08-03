package po.misc.context

import po.misc.types.TypeData
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Represents a structural identity for a given [CTX] type.
 *
 * Combines runtime type information ([KClass] and [KType]) with optional hierarchical context resolution
 * through [parentContext]. This allows building composite identity strings for traceability or matching.
 *
 * @param T The type of the context being identified.
 * @property kClass The runtime class reference of the context.
 * @property kType The full type information including generics.
 * @property parentContext Optional parent context used for hierarchical resolution.
 */
class CTXIdentity<T: CTX> @PublishedApi internal constructor(
    @PublishedApi
    internal val kClass: KClass<T>,
    @PublishedApi
    internal val kType: KType,
    private var userDefinedId: Long? = null,
    val parentContext: CTX? = null
) {

    val typeData: TypeData<T> = TypeData(kClass, kType)

    val parentIdentity: CTXIdentity<*>? get() = parentContext?.identity
    val className: String = kClass.simpleName ?: "Unnamed"

    private var nameLockedByUserBacking: Boolean = false
    private val uuid: UUID  = UUID.randomUUID()
    private val baseName: String get() = userDefinedId?.let { "$className#$it" } ?: className

    private var identifiedByNameBacking: String = ""

    val nameLockedByUser: Boolean get() = nameLockedByUserBacking
    val numericId: Long  by lazy { userDefinedId?: run { uuid.mostSignificantBits xor uuid.leastSignificantBits } }
    val isIdUsedDefined: Boolean get() = userDefinedId != null

    val identifiedByName: String get () {
        return if(nameLockedByUserBacking){
             identifiedByNameBacking
        }else   {
            baseName
        }
    }

    val detailedDump: String  get(){
        return buildString {
            appendLine(identifiedByName)
            appendLine("Type Parameters: ${typeData.typeName}")
            appendLine("numericId : $numericId")
            appendLine("Id User Defined : $isIdUsedDefined")
            appendLine("Hash Code : ${hashCode()}")
        }
    }

    /**
     * Hierarchical identity string built from this context and its parents (if any).
     * Format: `Child/Parent/.../Root`
     */
    val completeName: String get() = buildString {
        append(baseName)
        parentIdentity?.let { parent ->
            append("/")
            if (parent.kClass != kClass || parent.userDefinedId != userDefinedId) {
                append(parent.completeName)
            }
        }
    }

    val classQualifiedName: String get() = kClass.qualifiedName?:"Unnamed"
    fun setId(id: Long){
        userDefinedId = id
    }

    fun setNamePattern(builder:(CTXIdentity<T>)-> String){
        if(!nameLockedByUserBacking){
            identifiedByNameBacking = builder.invoke(this)
            nameLockedByUserBacking = true
        }
    }

    override fun toString(): String = completeName

    override fun hashCode(): Int {
        var result = kClass.hashCode()
        result = 31 * result + kType.hashCode()
        result = 31 * result + (parentContext?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CTXIdentity<*>) return false
        return kClass == other.kClass && kType == other.kType
    }

}

inline fun <reified T>  asSubIdentity(thisContext: T,  parentContext:CTX, withId: Long? = null): CTXIdentity<T> where T: CTX{
    return try {
        CTXIdentity(thisContext::class as KClass<T>, typeOf<T>(), withId, parentContext)
    }catch (th: Throwable){
        throw th
    }
}

fun <T> createIdentity(kClass: KClass<T>, kType: KType, withId: Long? = null): CTXIdentity<out T> where T: CTX {
    return try {
        CTXIdentity(kClass, kType, withId)
    }catch (th: Throwable){
        throw th
    }
}

inline fun <reified T>  T.asIdentity(withId: Long? = null): CTXIdentity<T> where T: CTX {
    return try {
        CTXIdentity(T::class, typeOf<T>(), withId)
    }catch (th: Throwable){
        throw th
    }
}
