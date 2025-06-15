package po.misc.interfaces





interface Identifiable: IdentifiableContext{
    val sourceName: String
    val componentName: String
    override val contextName get()= componentName

    val completeName: String get()= "$componentName[$sourceName]"

    fun withIdentification(string: String): String{
        return "$string@$completeName"
    }
}

data class IdentifiableImplementation(
    override var sourceName: String,
    override val componentName: String
): Identifiable{

    fun updateName(name: String){
        sourceName = name
    }

    override fun toString(): String {
        return completeName
    }
}

fun asIdentifiable(sourceName: String, componentName: String):IdentifiableImplementation{
    return IdentifiableImplementation(sourceName, componentName)
}
