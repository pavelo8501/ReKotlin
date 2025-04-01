package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.binders.PropertyBinder
import po.exposify.binders.PropertyBindingOption
import po.exposify.binders.relationship.RelationshipBinder2
import po.exposify.binders.UpdateMode
import po.exposify.binders.relationship.BindingContainer2
import po.exposify.binders.relationship.BindingKeyBase2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.components.DataModelContainer2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem


class DTOConfig2<DTO, DATA, ENTITY>(
    val dtoRegItem : DTORegistryItem<DTO, DATA, ENTITY>,
    val entityModel:LongEntityClass<ENTITY>,
    private val parent : DTOClass2<DTO>
) where DTO: ModelDTO,  ENTITY : LongEntity, DATA: DataModel{

    val dtoFactory: DTOFactory2<DTO, DATA, ENTITY> =
        DTOFactory2<DTO, DATA, ENTITY>(dtoRegItem.commonDTOKClass , dtoRegItem.dataKClass, this)

    val daoService: DAOService2<DTO, ENTITY> = DAOService2<DTO, ENTITY>(entityModel, parent)
    val propertyBinder : PropertyBinder<DATA,ENTITY> = PropertyBinder()

    val childBindings: MutableMap<BindingKeyBase2, BindingContainer2<DTO, DATA, ENTITY, *>> =
        mutableMapOf<BindingKeyBase2, BindingContainer2<DTO, DATA, ENTITY, *>>()

     val relationBinder: RelationshipBinder2<DTO, DATA, ENTITY> = RelationshipBinder2<DTO, DATA, ENTITY>(parent)

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    init {
        dtoFactory.setPostCreationRoutine("dto_initialization") {
            val dataModelContainer = DataModelContainer2<DTO, DATA>(it.dataModel, dtoFactory.dataBlueprint)
            it.initialize(dtoRegItem, dataModelContainer, propertyBinder, daoService)
        }

        if(parent.serviceContextOwned!= null){ //Act as a hierarchy root
            dtoFactory.setPostCreationRoutine("entity_initialization") {
                if(it.id != 0L){
                    val entity =  daoService.selectById(it.id)
                    if(entity != null){
                        it.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
                    }
                }
            }
        }

        dtoFactory.setPostCreationRoutine("repository_bindings") {
            relationBinder.createRepositories(it)
        }
    }

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