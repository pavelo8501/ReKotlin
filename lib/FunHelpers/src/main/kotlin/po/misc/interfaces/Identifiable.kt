package po.misc.interfaces

import po.misc.types.TypeRecord


interface Identifiable{

    val sourceName: String
    val componentName: String
    val completeName: String get()= "$componentName[$sourceName]"

    fun withIdentification(string: String): String{
        return "$string@$completeName"
    }
}


interface IdentifiableModule: Identifiable {
    val moduleName: String
    val identifiable : Identifiable

    override val sourceName: String get()= identifiable.sourceName
    override val componentName: String get()= identifiable.componentName

    override val completeName: String get()= "$moduleName[${identifiable.completeName}]"

    override fun withIdentification(string: String): String {
        return "$string@$moduleName(${identifiable.completeName})"
    }
}


fun asIdentifiable(sourceName: String, componentName: String):Identifiable{
    return IdentifiableImplementation(sourceName, componentName)

}

data class IdentifiableImplementation(
    override val sourceName: String,
    override val componentName: String
): Identifiable{
    override fun toString(): String {
        return componentName
    }
}
