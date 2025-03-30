package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.binder.BindingContainer
import po.exposify.binder.BindingKeyBase
import po.exposify.binder.PropertyBinder
import po.exposify.binder.PropertyBindingOption
import po.exposify.binder.RelationshipBinder2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem2


class DTOConfig2<DTO, DATA, ENTITY>(
    val dtoRegItem : DTORegistryItem2<DTO, DATA, ENTITY>,
    val entityModel:LongEntityClass<ENTITY>,
    private val parent : DTOClass2<DTO>
) where DTO: ModelDTO,  ENTITY : LongEntity, DATA: DataModel{


    val dtoFactory: DTOFactory2<DTO, DATA, ENTITY> =
        DTOFactory2<DTO, DATA, ENTITY>(dtoRegItem.dtoKClass , dtoRegItem.dataKClass, parent)

    val daoService: DAOService2<DTO, ENTITY> = DAOService2<DTO, ENTITY>(entityModel, parent)
    val propertyBinder : PropertyBinder<DATA,ENTITY> = PropertyBinder()

    val childBindings: MutableMap<BindingKeyBase, BindingContainer<DATA, ENTITY, *,*>> =
        mutableMapOf<BindingKeyBase, BindingContainer<DATA, ENTITY, *,*>>()


     val relationBinder: RelationshipBinder2<DTO, DATA, ENTITY> = RelationshipBinder2<DTO, DATA, ENTITY>(parent)

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    fun propertyBindings(vararg props: PropertyBindingOption<DATA, ENTITY, *> ): Unit =  propertyBinder.setProperties(props.toList())

    inline fun childBindings(
        block: RelationshipBinder2<DTO, DATA, ENTITY>.()-> Unit
    ){
        relationBinder.block()
    }

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

}