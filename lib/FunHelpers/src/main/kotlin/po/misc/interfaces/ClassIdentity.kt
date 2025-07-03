package po.misc.interfaces

import po.misc.data.helpers.textIfNull
import kotlin.reflect.KClass

class ClassIdentity(val componentName: String, var sourceName: String) {
    private val hashCode: Int = this.hashCode()
    var id: Long? = null
        private set

    val completeName: String
        get() {
            return if (sourceName.isNotEmpty()) {
                "${componentName}[${sourceName}${id.textIfNull("") { "#${id.toString()}" }}]"
            } else {
                "$componentName[${id.textIfNull("") { "#${id.toString()}" }}]"
            }
        }

    fun provideId(newId: Long) {
        id = newId
    }

    fun updateSourceName(name: String) {
        sourceName = name
    }

    companion object {
        fun create(componentName: String, sourceObjectName: String, id: Long? = null): ClassIdentity {
            val identity = ClassIdentity(componentName, sourceObjectName)
            if(id != null){
                identity.provideId(id)
            }
            return identity
        }

        fun create(componentName: String, sourceClass: KClass<*>, id: Long? = null): ClassIdentity {
            val identity = ClassIdentity(componentName, sourceClass.simpleName.toString())
            if(id != null){
                identity.provideId(id)
            }
            return ClassIdentity(componentName, sourceClass.simpleName.toString())
        }
    }
}

fun asIdentifiableClass(componentName: String, sourceObjectName: String):ClassIdentity{
    val classImpl = ClassIdentity(componentName, sourceObjectName)
    return classImpl
}
