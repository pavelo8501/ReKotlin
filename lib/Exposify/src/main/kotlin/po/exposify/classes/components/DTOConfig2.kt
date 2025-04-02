package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.binders.PropertyBinder
import po.exposify.binders.PropertyBindingOption
import po.exposify.binders.UpdateMode
import po.exposify.binders.relationship.RelationshipBinder2
import po.exposify.binders.relationship.BindingContainer2
import po.exposify.binders.relationship.BindingKeyBase2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.components.DataModelContainer2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.entity.classes.ExposifyEntityBase


internal interface ConfigurableDTO<DTO: ModelDTO, DATA : DataModel, ENTITY: ExposifyEntityBase>{

    fun initFactoryRoutines()
    suspend  fun withFactory(block: suspend (DTOFactory<DTO, DATA, ENTITY>)-> Unit)
    suspend fun withDaoService(block: suspend (DAOService2<DTO, DATA,  ENTITY>)-> Unit)
    suspend fun withRelationshipBinder(block: suspend (RelationshipBinder2<DTO, DATA, ENTITY>)-> Unit)

}

internal class DTOConfig2<DTO, DATA, ENTITY>(
    val dtoRegItem : DTORegistryItem<DTO, DATA, ENTITY>,
    val entityModel:LongEntityClass<ENTITY>,
    private val parent : DTOClass2<DTO>
): ConfigurableDTO<DTO, DATA, ENTITY> where DTO: ModelDTO,  ENTITY : ExposifyEntityBase, DATA: DataModel{

    val dtoFactory: DTOFactory<DTO, DATA, ENTITY> =
        DTOFactory<DTO, DATA, ENTITY>(dtoRegItem.commonDTOKClass , dtoRegItem.dataKClass, this)

    val daoService: DAOService2<DTO, DATA,  ENTITY> = DAOService2<DTO, DATA, ENTITY>(false, entityModel)
    val propertyBinder : PropertyBinder<DATA,ENTITY> = PropertyBinder()

     val relationBinder: RelationshipBinder2<DTO, DATA, ENTITY> = RelationshipBinder2<DTO, DATA, ENTITY>(parent)

    val childBindings: MutableMap<BindingKeyBase2, BindingContainer2<DTO, DATA, ENTITY, ModelDTO>> =
        mutableMapOf<BindingKeyBase2, BindingContainer2<DTO, DATA, ENTITY, ModelDTO>>()

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    override  suspend fun withFactory(block: suspend (DTOFactory<DTO, DATA, ENTITY>)-> Unit){
        block.invoke(dtoFactory)
    }

    override suspend fun withDaoService(block: suspend (DAOService2<DTO, DATA,  ENTITY>)-> Unit){
        block.invoke(daoService)
    }

    override suspend fun withRelationshipBinder(block: suspend (RelationshipBinder2<DTO, DATA, ENTITY>)-> Unit){
        block.invoke(relationBinder)
    }

    override fun initFactoryRoutines(){
        dtoFactory.setPostCreationRoutine("dto_initialization") {

            val dataModelContainer = DataModelContainer2<DTO, DATA>(dataModel, dtoFactory.dataBlueprint)
            initialize(dtoRegItem, dataModelContainer, propertyBinder, daoService)
        }

        dtoFactory.setPostCreationRoutine("entity_initialization_for_root_dto") {entity->
            if(entity == null){
                if(id == 0L){
                    if(parent.serviceContextOwned!= null) { //Act as a hierarchy root
                        daoService.save(this)
                    }
                }else{
                    daoService.update(this)
                }
            }else{
                updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
            }
        }

        dtoFactory.setPostCreationRoutine("repository_bindings") {
            relationBinder.createRepositories(this)
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