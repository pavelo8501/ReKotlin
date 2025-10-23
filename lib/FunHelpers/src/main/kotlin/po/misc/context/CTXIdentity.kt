package po.misc.context

import po.misc.context.models.IdentityData
import po.misc.types.token.TypeToken
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.KType

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
    val typeData: TypeToken<T>,
    private var userDefinedId: Long? = null,
    val parentContext: CTX? = null
) {

    val parentIdentity: CTXIdentity<*>? get() = parentContext?.identity
    val className: String = typeData.kClass.simpleName ?: "Unnamed"

    private var nameLockedByUserBacking: Boolean = false
    val uuid: UUID  = UUID.randomUUID()
    val baseName: String get() = userDefinedId?.let { "$className#$it" } ?: className

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

    val detailedDump: IdentityData  get(){
        return IdentityData(
            identifiedByName,
            uuid.toString(),
            typeData.typeName,
            numericId,
            isIdUsedDefined,
            hashCode()
        )
    }

    /**
     * Hierarchical identity string built from this context and its parents (if any).
     * Format: `Child/Parent/.../Root`
     */
    val completeName: String get() = buildString {
        append(baseName)
        parentIdentity?.let { parent ->
            append("/")
            if (parent.typeData.kClass != typeData.kClass || parent.userDefinedId != userDefinedId) {
                append(parent.completeName)
            }
        }
    }

    val classQualifiedName: String get() = typeData.kClass.qualifiedName?:"Unnamed"
    fun setId(id: Long){
        userDefinedId = id
    }

    fun setNamePattern(builder:(CTXIdentity<T>)-> String){
        if(!nameLockedByUserBacking){
            identifiedByNameBacking = builder.invoke(this)
            nameLockedByUserBacking = true
        }
    }

    fun strictComparison(identity: CTXIdentity<*>): Boolean{
        return identity.typeData.kClass == typeData.kClass && identity.numericId == numericId
    }

    override fun toString(): String = completeName

    override fun hashCode(): Int {
        var result = typeData.kClass.hashCode()
        result = 31 * result + typeData.kType.hashCode()
        result = 31 * result + (parentContext?.hashCode() ?: 0)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CTXIdentity<*>) return false
        return typeData.kClass == other.typeData.kClass && typeData.kType == other.typeData.kType
    }

}

inline fun <reified T> asSubIdentity(parentContext:CTX, withId: Long? = null): CTXIdentity<T> where T: CTX{
    return try {
        CTXIdentity(TypeToken.create<T>(), withId, parentContext)
    }catch (th: Throwable){
        throw th
    }
}

fun <T: CTX>  T.asSubIdentity(typeData: TypeToken<T>, parentContext:CTX,  withId: Long? = null): CTXIdentity<T>{
    return try {
        CTXIdentity(typeData, withId, parentContext)
    }catch (th: Throwable){
        throw th
    }
}

fun <T>  T.asIdentity(typeData: TypeToken<T>, withId: Long? = null): CTXIdentity<T> where T: CTX {
    return try {
        CTXIdentity(typeData, withId)
    }catch (th: Throwable){
        throw th
    }
}


inline fun <reified T>  T.asIdentity(withId: Long? = null): CTXIdentity<T> where T: CTX {
    return try {
        CTXIdentity(TypeToken.create<T>(), withId)
    }catch (th: Throwable){
        throw th
    }
}
