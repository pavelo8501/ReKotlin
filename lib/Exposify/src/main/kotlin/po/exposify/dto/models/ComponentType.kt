package po.exposify.dto.models

import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.ValueBased
import po.misc.interfaces.ValueBasedClass

sealed class ComponentType(override val value: Int, protected val source: SourceObject<*>): Identifiable, ValueBased{




    protected val sourceName: String get() = source.name

    object CommonDTO:  ComponentType(1, SourceObject.DTO){
        override val componentName: String = "CommonDTO"
        override val completeName: String  get() = "$componentName[${sourceName}]"
    }

    object DTOClass: ComponentType(2, SourceObject.DTO){
        override val componentName: String = "DTOClass"
        override val completeName: String get() = "${componentName}[${sourceName}]"
    }

    object ResponsiveDelegate:  ComponentType(3, SourceObject.DTO){
        override val componentName: String = "ResponsiveDelegate"
        override val completeName: String  get() = "${componentName}[${sourceName}]"
    }

    object AttachedDelegate:  ComponentType(4, SourceObject.DTO){
        override val componentName: String = "AttachedDelegate"
        override val completeName: String  get() = "${componentName}[${sourceName}]"
    }

    object ParentDelegate:  ComponentType(5, SourceObject.DTO){
        override val componentName: String = "ParentDelegate"
        override val completeName: String  get() = "${componentName}[${sourceName}]"
    }

    object RelationDelegate:  ComponentType(6, SourceObject.DTO){
        override val componentName: String = "RelationDelegate"
        override val completeName: String  get() = "${componentName}[${sourceName}]"
    }

    object Tracker: ComponentType(7, SourceObject.DTO){
        override val componentName: String = "Tracker"
        override val completeName: String  get() = "${componentName}[${sourceName}]"
    }
}


sealed class ModuleType(override val value: Int, val source: ComponentType): ValueBased, IdentifiableModule{


    object DTOFactory:  ModuleType(1, ComponentType.DTOClass){
        override val moduleName: String = "DTOFactory"
        override val completeName: String = "$moduleName[${source.completeName}]"
    }

    object DAOService:  ModuleType(2, ComponentType.DTOClass){
        override val moduleName: String = "DAOService"
        override val completeName: String = "$moduleName[${source.completeName}]"
    }

    object SequenceContext:  ModuleType(3, ComponentType.DTOClass){
        override val moduleName: String = "SequenceContext"
        override val completeName: String = "$moduleName[${source.completeName}]"
    }

    object ServiceClass:  ModuleType(4, ComponentType.DTOClass){
        override val moduleName: String = "ServiceClass"
        override val completeName: String = "$moduleName[${source.completeName}]"
    }

    object ExecutionProvider:  ModuleType(5, ComponentType.DTOClass){
        override val moduleName: String = "ExecutionProvider"
        override val completeName: String = "$moduleName[${source.completeName}]"
    }

    object BindingHub:  ModuleType(6, ComponentType.CommonDTO){
        override val moduleName: String = "BindingHub"
        override val completeName: String = "$moduleName[${source.completeName}]"
    }

}