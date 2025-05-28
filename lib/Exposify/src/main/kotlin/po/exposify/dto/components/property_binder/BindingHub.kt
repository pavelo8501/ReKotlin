package po.exposify.dto.components.property_binder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.DTOFactory
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.delegates.ComplexDelegate
import po.exposify.dto.components.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.components.relation_binder.delegates.RelationBindingDelegate
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.multipleRepository
import po.misc.interfaces.Identifiable
import po.misc.reflection.properties.PropertyRecord
import po.misc.reflection.properties.toPropertyRecordMap

class BindingHub<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    val dto : CommonDTO<DTO, DATA, ENTITY>,
): Identifiable  where  DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity,
                        F_DTO: ModelDTO, FD: DataModel, FE: LongEntity

{
    override val qualifiedName: String = "PropertyBinder[${dto.dtoName}]"

    private val complexDelegateMap : MutableMap<String,  ComplexDelegate<DTO, DATA, ENTITY, *, *, *, *, *>> = mutableMapOf()
    private val responsiveDelegates : MutableMap<String, ResponsiveDelegate<DTO, DATA, ENTITY, *>>  = mutableMapOf()
    private val relationDelegates : MutableMap<String, RelationBindingDelegate<DTO, DATA, ENTITY, out F_DTO, out FD, out FE, *>> = mutableMapOf()


    init {
        dto.dtoFactory.notificator.subscribe(this, DTOFactory.FactorySubscriptions.ON_INITIALIZED){
            val propertyMap = dto.dtoClass.config.propertyMap
            val typeRecord = dto.dtoClass.config.registry.getRecord<DTO, OperationsException>(ComponentType.DTO)
            val propertyRecordMap = responsiveDelegates.values.map { it.property }.toPropertyRecordMap(typeRecord)
            val casted = propertyRecordMap.castOrOperationsEx<Map<String, PropertyRecord<DTO, Any?>>>()
            propertyMap.provideMap<DTO>(ComponentType.DTO, casted)
        }
    }

    fun setBinding(
        binding: RelationBindingDelegate<DTO, DATA, ENTITY, out F_DTO , out FD, out FE, *>)
    : RelationBindingDelegate<DTO, DATA, ENTITY, out F_DTO, out FD, out FE, *>
    {
        relationDelegates[binding.qualifiedName] = binding
        return binding
    }

    fun <FE : LongEntity> setBinding(
        binding: ComplexDelegate<DTO, DATA, ENTITY,* , *, FE, *, *>)
    : ComplexDelegate<DTO, DATA, ENTITY, *, *, FE, *, *>
    {
        complexDelegateMap[binding.qualifiedName] = binding
        return binding
    }

    fun setBinding(
        binding : ResponsiveDelegate<DTO, DATA, ENTITY, *>
    ):ResponsiveDelegate<DTO, DATA, ENTITY, *>{
        responsiveDelegates[binding.qualifiedName] =  binding
        return  binding
    }

    fun  getForeignDataModels(cardinality: Cardinality, dataModel : DATA): List<FD>{
        val delegate = relationDelegates.values.firstOrNull { it.cardinality == cardinality }
        val dataModels =  delegate?.getForeignDataModels(dataModel)?:emptyList()
        return  dataModels
    }
    fun getForeignEntities(cardinality: Cardinality, entity : ENTITY):List<FE>{
        val delegate = relationDelegates.values.firstOrNull { it.cardinality == cardinality }
        val entities =  delegate?.getForeignEntities(entity)?:emptyList()
        return entities
    }

    fun attachToForeignEntity(cardinality: Cardinality,  container : EntityUpdateContainer<*, DTO, DATA, ENTITY>){

    }
    fun saveDataModel(dataModel : FD){

    }

    fun update(model:DATA, childClass: DTOClass<F_DTO , FD, FE>){
        responsiveDelegates.values.forEach { it.update(model) }
        relationDelegates.values.forEach { container->
            container.getForeignDataModels(model).forEach { childDataModel ->
                container.dto.multipleRepository(childClass)?.update(childDataModel)
            }
        }
    }

    fun update(model:DATA){
        responsiveDelegates.values.forEach { it.update(model) }
        relationDelegates.values.forEach { container->
            container.getForeignDataModels(model).forEach { childDataModel ->
                val repo = container.dto.multipleRepository(container.childModel)
                if(repo != null){
                   val casted = repo.castOrOperationsEx<MultipleRepository<DTO, DATA, ENTITY, F_DTO, FD, FE>>()
                   casted.update(childDataModel)
                }
            }
        }
    }

    fun <F_DTO: ModelDTO, FD: DataModel, FE: LongEntity> update(
        container : EntityUpdateContainer<ENTITY, F_DTO, FD, FE>
    ){

        if(container.updateMode == UpdateMode.ENTITY_TO_MODEL && container.inserted){
            val id = container.ownEntity.id.value
            dto.id =  id
            dto.dataModel.id = id
        }

        responsiveDelegates.values.forEach { it.update(container) }
        complexDelegateMap.values.forEach { it.beforeInsertedUpdate(container) }
    }

    fun <P_DTO: ModelDTO, PD: DataModel, FE: LongEntity> afterInsertUpdate(
        entityContainer : EntityUpdateContainer<ENTITY, P_DTO, PD, FE>)
    {
        complexDelegateMap.values.forEach { it.afterInsertedUpdate(entityContainer) }
    }

}

