package po.exposify.dto.models

import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.ValueBased


//class ExposifyModule(val type: ModuleType, val identifiable: Identifiable):IdentifiableModule{
//    override val moduleName: String get() = type.moduleName
//    override val sourceName: String
//        get() = identifiable.sourceName
//}
//
//sealed class ModuleType(override val value: Int, val source: ComponentType): ValueBased{
//
//    abstract  val moduleName: String
//
//    object DTOFactory:  ModuleType(1, ComponentType.DTOClass){
//        override val moduleName: String = "DTOFactory"
//    }
//
//    object DAOService:  ModuleType(2, ComponentType.DTOClass){
//        override val moduleName: String = "DAOService"
//    }
//
//    object SequenceContext:  ModuleType(3, ComponentType.DTOClass){
//        override val moduleName: String = "SequenceContext"
//    }
//
//    object ServiceClass:  ModuleType(4, ComponentType.DTOClass){
//        override val moduleName: String = "ServiceClass"
//    }
//
//    object ExecutionProvider:  ModuleType(5, ComponentType.DTOClass){
//        override val moduleName: String = "ExecutionProvider"
//    }
//
//    object BindingHub:  ModuleType(6, ComponentType.CommonDTO){
//        override val moduleName: String = "BindingHub"
//    }
//
//}