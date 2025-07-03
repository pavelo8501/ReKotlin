package po.misc.interfaces

import po.misc.exceptions.ManagedException

interface IdentifiableContext {
    val contextName: String
}

interface Identifiable: IdentifiableContext{
    var sourceName: String
    val completeName: String get()= "$contextName[$sourceName]"
}

interface IdentifiableClass : IdentifiableContext {
    val identity: ClassIdentity
    val completeName: String get()= identity.completeName
    override  val contextName: String get() = identity.componentName
}

interface ObservedContext: IdentifiableContext{
    val sourceName: String
    val completeName: String get()= "$contextName[$sourceName]"
    val exceptionOutput: ((ManagedException)-> Unit)?

}




