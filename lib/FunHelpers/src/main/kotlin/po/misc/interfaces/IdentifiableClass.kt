package po.misc.interfaces

import po.misc.data.helpers.textIfNull
import kotlin.reflect.KClass

class ClassIdentity(val componentName: String, var sourceName: String) {
    private val hashCode: Int = this.hashCode()
    var id: Int? = null
        private set

    val completeName: String
        get() {
            return if (sourceName.isNotEmpty()) {
                "${componentName}[${sourceName}${id.textIfNull("") { "#${id.toString()}" }}]"
            } else {
                "$componentName[${id.textIfNull("") { "#${id.toString()}" }}]"
            }
        }

    fun provideId(newId: Int) {
        id = newId
    }

    fun updateSourceName(name: String) {
        sourceName = name
    }

    companion object {
        fun create(componentName: String, sourceObjectName: String): ClassIdentity {
            return ClassIdentity(componentName, sourceObjectName)
        }

        fun create(componentName: String, sourceClass: KClass<*>): ClassIdentity {
            return ClassIdentity(componentName, sourceClass.simpleName.toString())
        }
    }
}

fun asIdentifiableClass(componentName: String, sourceObjectName: String):ClassIdentity{
    val classImpl = ClassIdentity(componentName, sourceObjectName)
    return classImpl
}
