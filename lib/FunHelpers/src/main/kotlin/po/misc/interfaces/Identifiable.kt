package po.misc.interfaces



data class IdentifiableImplementation(
    override var sourceName: String,
    override val contextName: String
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
