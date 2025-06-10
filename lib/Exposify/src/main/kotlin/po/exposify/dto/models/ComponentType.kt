package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableModule
import po.misc.interfaces.ValueBased
import po.misc.interfaces.ValueBasedClass
import po.misc.interfaces.asIdentifiable
import po.misc.types.TypeRecord


fun <DTO: ModelDTO, D: DataModel, E: LongEntity> componentInstance(type: ComponentType, dto: CommonDTO<DTO, D, E>):Component<DTO>{
    return object : Component<DTO>(type,dto) {}
}

abstract class Component<DTO: ModelDTO>(val type: ComponentType, val source: CommonDTO<DTO, *, *>): Identifiable {
    override val componentName: String get() = type.componentName
    override val sourceName: String get() = source.sourceName

    val value: Int =  type.value
}

sealed class ComponentType(override val value: Int): ValueBased{

   abstract val componentName: String

    object CommonDTO:  ComponentType(1){
        override val componentName: String = "CommonDTO"
    }

    object DTOClass: ComponentType(2){
        override val componentName: String = "DTOClass"
    }

    object RootClass: ComponentType(8){
        override val componentName: String = "RootClass"
    }

    object ResponsiveDelegate:  ComponentType(3){
        override val componentName: String = "ResponsiveDelegate"
    }

    object AttachedForeignDelegate:  ComponentType(4){
        override val componentName: String = "AttachedForeignDelegate"
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