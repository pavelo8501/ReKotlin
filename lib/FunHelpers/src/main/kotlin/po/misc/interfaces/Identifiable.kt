package po.misc.interfaces

import po.misc.types.TypeRecord


interface Identifiable{
    val personalName: String
    val componentName: String

    val completeName: String get()= "$componentName[$personalName]"

    fun withIdentification(string: String): String{
        return "$string@$completeName"
    }

}


interface IdentifiableModule : Identifiable {
    val moduleName: String
    override val componentName: String get()= moduleName

}


fun asIdentifiable(personalName: String, componentName: String):Identifiable{
    return IdentifiableImplementation(personalName, componentName)

}

data class IdentifiableImplementation(
    override val personalName: String,
    override val componentName: String,
): Identifiable
