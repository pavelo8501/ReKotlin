package po.misc.interfaces

import kotlin.reflect.KClass

interface IdentifiableClass : IdentifiableContext {
    val identity: ClassIdentity<*>
    val sourceName: String get() = identity.sourceName

    override val contextName: String get() = identity.componentName
    val completeName: String get() = "$contextName[$sourceName]"
}

class ClassIdentity<C>(val componentName: String, internal val receiver: C) where  C: IdentifiableClass{

    var sourceNameProvider : (()->  KClass<*>)? = null
    private var sourceNameProvided : String? = null
    val sourceName: String
        get() {
            sourceNameProvided = sourceNameProvider?.invoke()?.simpleName
            return sourceNameProvided ?: "Undefined"
        }
    fun updateSourceName(name: String){
        sourceNameProvided = name
    }

    fun updateSourceName(sourceProvider : ()-> KClass<*>){
        sourceNameProvider = sourceProvider
    }
}

fun <C: IdentifiableClass> C.asIdentifiableClass(sourceName: String, clasName: String? = null):ClassIdentity<C> {
    val name = clasName ?: this::class.simpleName.toString()
    val classImpl = ClassIdentity(name, this)
    classImpl.updateSourceName(sourceName)
    return classImpl
}

fun <C: IdentifiableClass, S: Any> C.asIdentifiableClass(
    sourceProvider: ()-> KClass<S>
):ClassIdentity<C>{
    val clasName = this::class.simpleName.toString()
    val classImpl = ClassIdentity(clasName, this)
    classImpl.updateSourceName(sourceProvider)
    return classImpl
}