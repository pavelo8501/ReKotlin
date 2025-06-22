package po.misc.interfaces

import po.misc.data.helpers.ifNotEmpty

//
//interface IdentifiableModule: Identifiable {
//
//    val componentId: Int
//    val identifiable: Identifiable
//    val moduleName: Named
//
//    override var sourceName: String
//
//    override val completeName: String get()= "${moduleName}[${sourceName}" + sourceName.ifNotEmpty("[${sourceName}]")+ "]#${componentId}"
//
//    fun updateName(name: String)
//    fun setId(id: Int)
//
//}
//
//
//data class IdentifiableModuleInstance(
//    override val identifiable: IdentifiableImplementation,
//    override val moduleName: Named,
//    override var componentId: Int = 0
//):IdentifiableModule{
//
//
//    override fun updateName(name: String){
//        identifiable.updateName(name)
//    }
//
//    override fun setId(id: Int){
//        this.componentId = id
//    }
//
//    override fun toString(): String {
//        return  completeName
//    }
//}
//
//fun  asIdentifiableModule(identifiable: IdentifiableImplementation, moduleName: Named):IdentifiableModuleInstance{
//
//   return IdentifiableModuleInstance(identifiable, moduleName)
//}
//
//fun asIdentifiableModule(ownName: String, componentName: String, moduleName: Named):IdentifiableModuleInstance{
//    return IdentifiableModuleInstance(asIdentifiable(ownName, componentName), moduleName)
//}