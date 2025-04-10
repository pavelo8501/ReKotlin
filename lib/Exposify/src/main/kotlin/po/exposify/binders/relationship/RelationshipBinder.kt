package po.exposify.binders.relationship

import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.binders.enums.Cardinality
import po.exposify.binders.relationship.models.DataPropertyInfo
import po.exposify.binders.relationship.models.EntityPropertyInfo
import po.exposify.dto.components.RepositoryBase
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.safeCast
import po.lognotify.extensions.getOrThrowDefault
import kotlin.collections.set
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1



class RelationshipBinder<DTO, DATA, ENTITY>(
   val dtoClass:  DTOClass<DTO>
) where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntityBase
{
    internal var childBindings = mutableMapOf<BindingKeyBase, BindingContainer<DTO, DATA, ENTITY, ModelDTO>>()
        private set
    private fun <CHILD_DTO: ModelDTO> attachBinding(
        key : BindingKeyBase,
        container: BindingContainer<DTO, DATA, ENTITY, CHILD_DTO>
    ){
        if (!childBindings.containsKey(key)) {
            val containerCast = container.safeCast<BindingContainer<DTO, DATA, ENTITY, ModelDTO>>()
            if(containerCast != null){
                childBindings[key] = containerCast
            }
        }
    }

    fun <CHILD_DTO: ModelDTO>single(
        childModel: DTOClass<CHILD_DTO>,
        ownDataModel: KMutableProperty1<DATA, DataModel?>,
        ownEntity: KProperty1<ENTITY, ExposifyEntityBase>,
        foreignEntity: KMutableProperty1<ExposifyEntityBase, ENTITY>
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }

        val oneToOneContainerNullableData = BindingContainer.createOneToOneContainer<DTO, DATA, ENTITY, CHILD_DTO>(dtoClass, childModel)
        oneToOneContainerNullableData.initProperties(ownDataModel, ownEntity, foreignEntity)
        attachBinding(oneToOneContainerNullableData.thisKey, oneToOneContainerNullableData)
    }


    fun <CHILD_DTO : ModelDTO>many(
        childModel: DTOClass<CHILD_DTO>,
        ownDataModels: KProperty1<DATA,  MutableList<out DataModel>>,
        ownEntities: KProperty1<ENTITY, SizedIterable<ExposifyEntityBase>>,
        foreignEntity: KMutableProperty1<out ExposifyEntityBase, ENTITY>,
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }

        val oneToMany = BindingContainer.createOneToManyContainer<DTO, DATA, ENTITY, CHILD_DTO>(dtoClass, childModel)
        val foreignEntityCast = foreignEntity.safeCast<KMutableProperty1<ExposifyEntityBase, ENTITY>>()
            .getOrThrowDefault("ForeignEntity Property cast failure for ${dtoClass.personalName}")

        val castedOwnModelsProperty =  ownDataModels.safeCast<KProperty1<DATA,  MutableList<DataModel>>>()
            .getOrThrowDefault("Own DataModels Property cast failure for ${dtoClass.personalName}")

        oneToMany.initProperties(castedOwnModelsProperty, ownEntities, foreignEntityCast)
        attachBinding(oneToMany.thisKey, oneToMany)
    }

    fun trackedDataProperties(forDto : CommonDTO<DTO, DATA, ENTITY>): List<DataPropertyInfo<DTO,DATA, ENTITY, ModelDTO>>{
        val result = mutableListOf<DataPropertyInfo<DTO, DATA, ENTITY,  ModelDTO>>()
        childBindings.values.forEach {
            when(it){
                is SingleChildContainer ->{
                    val property = it.sourcePropertyWrapper
                    result.add(DataPropertyInfo(property.extract().name, Cardinality.ONE_TO_ONE, property.nullable, it.thisKey, it))
                }
                is MultipleChildContainer->{
                    val property = it.ownDataModelsProperty
                    result.add(DataPropertyInfo(property.name, Cardinality.ONE_TO_MANY, false, it.thisKey, it))
                }
            }
        }
        return result
    }

    fun trackedEntityProperties(forDto : CommonDTO<DTO, DATA, ENTITY>): List<EntityPropertyInfo<DTO,DATA, ENTITY, ModelDTO>>{
        val result = mutableListOf<EntityPropertyInfo<DTO, DATA, ENTITY,  ModelDTO>>()
        childBindings.values.forEach {
            when(it){
                is SingleChildContainer ->{
                    val property = it.ownEntityProperty
                    result.add(EntityPropertyInfo(property.name, Cardinality.ONE_TO_ONE, false, it.thisKey, it))
                }
                is MultipleChildContainer->{
                    val property = it.ownEntitiesProperty
                    result.add(EntityPropertyInfo(property.name, Cardinality.ONE_TO_MANY, false, it.thisKey, it))
                }
            }
        }
        return result
    }

    fun createRepositories(parentDto: CommonDTO<DTO, DATA, ENTITY>){
         childBindings.forEach {
            when(it.key){
                is BindingKeyBase.OneToOne<*>->{
                    val newRepo = it.value.createRepository(parentDto).safeCast<RepositoryBase<DTO, DATA, ENTITY, ModelDTO>>()
                    if(newRepo != null){
                        parentDto.repositories.put(it.key, newRepo)
                    }else{
                        throw OperationsException("CreateRepository failed for ${parentDto.personalName}",
                            ExceptionCode.CAST_FAILURE)
                    }
                }

                is BindingKeyBase.OneToMany<*>->{
                    val newRepo = it.value.createRepository(parentDto).safeCast<RepositoryBase<DTO, DATA, ENTITY, ModelDTO>>()
                    if(newRepo != null){
                        parentDto.repositories.put(it.key, newRepo)
                    }else{
                        throw OperationsException("CreateRepository failed for ${parentDto.personalName}",
                            ExceptionCode.CAST_FAILURE)
                    }
                }
                else -> {}
            }
        }
        val dataPropertiesCast = trackedDataProperties(parentDto).safeCast<List<DataPropertyInfo<DTO, DATA, ExposifyEntityBase, ModelDTO>>>()
        if(dataPropertiesCast != null){
            parentDto.dataContainer.setTrackedProperties(dataPropertiesCast)
          //  parentDto.daoService.setTrackedProperties(trackedEntityProperties(parentDto))
        }
    }
}