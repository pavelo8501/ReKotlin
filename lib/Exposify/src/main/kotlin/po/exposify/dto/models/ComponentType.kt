package po.exposify.dto.models

import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.ValueBased
import po.misc.interfaces.ValueBasedClass
import po.misc.types.TypeRecord

fun <T:Component> T.provideType(source: TypeRecord<*>):T{
    sourceType = source
    return this
}


fun componentInstance(type: ComponentType, sourceType: TypeRecord<*>? = null):Component{
    return object : Component(type, sourceType) {}
}

abstract class Component(val type: ComponentType, var sourceType: TypeRecord<*>? = null): ValueBased{
    val componentName: String get() = type.componentName
    val sourceName: String get() = sourceType?.simpleName?:"N/A"
    val completeName: String  get() = "${componentName}[${sourceName}]"
    override val value: Int = type.value
}


sealed class ComponentType(override val value: Int): ValueBased{

   abstract val componentName: String

    object CommonDTO:  ComponentType(1){
        override val componentName: String = "CommonDTO"
    }

    object DTOClass: ComponentType(2){
        override val componentName: String = "DTOClass"
    }

    object ResponsiveDelegate:  ComponentType(3){
        override val componentName: String = "ResponsiveDelegate"

    }

    object AttachedDelegate:  ComponentType(4){
        override val componentName: String = "AttachedDelegate"
    }

    object ParentDelegate:  ComponentType(5){
        override val componentName: String = "ParentDelegate"
    }

    object RelationDelegate:  ComponentType(6){
        override val componentName: String = "RelationDelegate"
    }

    object Tracker: ComponentType(7){
        override val componentName: String = "Tracker"
    }
}

abstract class Module(val type: ModuleType){

}

sealed class ModuleType(override val value: Int, val source: ComponentType): ValueBased, IdentifiableModule{


    object DTOFactory:  ModuleType(1, ComponentType.DTOClass){
        override val moduleName: String = "DTOFactory"
        override val completeName: String = "$moduleName[${source.componentName}]"
    }

    object DAOService:  ModuleType(2, ComponentType.DTOClass){
        override val moduleName: String = "DAOService"
        override val completeName: String = "$moduleName[${source.componentName}]"
    }

    object SequenceContext:  ModuleType(3, ComponentType.DTOClass){
        override val moduleName: String = "SequenceContext"
        override val completeName: String = "$moduleName[${source.componentName}]"
    }

    object ServiceClass:  ModuleType(4, ComponentType.DTOClass){
        override val moduleName: String = "ServiceClass"
        override val completeName: String = "$moduleName[${source.componentName}]"
    }

    object ExecutionProvider:  ModuleType(5, ComponentType.DTOClass){
        override val moduleName: String = "ExecutionProvider"
        override val completeName: String = "$moduleName[${source.componentName}]"
    }

    object BindingHub:  ModuleType(6, ComponentType.CommonDTO){
        override val moduleName: String = "BindingHub"
        override val completeName: String = "$moduleName[${source.componentName}]"
    }

}