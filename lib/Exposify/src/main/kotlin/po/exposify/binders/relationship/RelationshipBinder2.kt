package po.exposify.binders.relationship

import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.binders.enums.Cardinality
import po.exposify.binders.relationship.models.DataPropertyInfo
import po.exposify.binders.relationship.models.EntityPropertyInfo
import po.exposify.classes.components.RepositoryBase2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.extensions.safeCast
import kotlin.collections.set
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1



class RelationshipBinder2<DTO, DATA, ENTITY>(
   val dtoClass:  DTOClass2<DTO>
) where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntityBase
{
    private var childBindings = mutableMapOf<BindingKeyBase2, BindingContainer2<DTO, DATA, ENTITY, ModelDTO>>()

    private fun <CHILD_DTO: ModelDTO> attachBinding(
        key : BindingKeyBase2,
        container: BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>
    ){
        if (!childBindings.containsKey(key)) {
            val containerCast = container.safeCast<BindingContainer2<DTO, DATA, ENTITY, ModelDTO>>()
            if(containerCast != null){
                childBindings[key] = containerCast
            }
        }
    }

    fun <CHILD_DTO: ModelDTO>single(
        childModel: DTOClass2<CHILD_DTO>,
        ownDataModel: KMutableProperty1<DATA, DataModel?>,
        ownEntity: KProperty1<ENTITY, ExposifyEntityBase>,
        foreignEntity: KMutableProperty1<ExposifyEntityBase, ENTITY>
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }

        val oneToOneContainerNullableData = BindingContainer2.createOneToOneContainer<DTO, DATA, ENTITY, CHILD_DTO>(dtoClass, childModel)
        oneToOneContainerNullableData.initProperties(ownDataModel, ownEntity, foreignEntity)
        attachBinding(oneToOneContainerNullableData.thisKey, oneToOneContainerNullableData)
    }


    fun <CHILD_DTO : ModelDTO>many(
        childModel: DTOClass2<CHILD_DTO>,
        ownDataModel: KProperty1<DATA, Iterable<DataModel>>,
        ownEntities: KProperty1<ENTITY, SizedIterable<ExposifyEntityBase>>,
        foreignEntity: KMutableProperty1<*, ENTITY>,
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }

        val oneToMany = BindingContainer2.createOneToManyContainer<DTO, DATA, ENTITY, CHILD_DTO>(dtoClass, childModel)
        val foreignEntityCast = foreignEntity.safeCast<KMutableProperty1<ExposifyEntityBase, ENTITY>>()
        if(foreignEntityCast != null){
            oneToMany.initProperties(ownDataModel, ownEntities, foreignEntityCast)
            attachBinding(oneToMany.thisKey, oneToMany)
        }else{
            throw OperationsException("ForeignEntity cast failure for ${dtoClass.personalName}", ExceptionCodes.CAST_FAILURE)
        }

    }

    fun trackedDataProperties(forDto : CommonDTO<DTO, DATA, ENTITY>): List<DataPropertyInfo<DTO,DATA, ENTITY, ModelDTO>>{
        val result = mutableListOf<DataPropertyInfo<DTO, DATA, ENTITY,  ModelDTO>>()
        childBindings.values.forEach {
            when(it){
                is SingleChildContainer2 ->{
                    val property = it.sourcePropertyWrapper
                    result.add(DataPropertyInfo(property.extract().name, Cardinality.ONE_TO_ONE, property.nullable, it.thisKey, it))
                }
                is MultipleChildContainer2->{
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
                is SingleChildContainer2 ->{
                    val property = it.ownEntityProperty
                    result.add(EntityPropertyInfo(property.name, Cardinality.ONE_TO_ONE, false, it.thisKey, it))
                }
                is MultipleChildContainer2->{
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
                is BindingKeyBase2.OneToOne<*>->{
                    val newRepo = it.value.createRepository(parentDto).safeCast<RepositoryBase2<DTO, DATA, ENTITY, ModelDTO>>()
                    if(newRepo != null){
                        parentDto.repositories.put(it.key, newRepo)
                    }else{
                        throw OperationsException("CreateRepository failed for ${parentDto.personalName}",
                            ExceptionCodes.CAST_FAILURE)
                    }
                }

                is BindingKeyBase2.OneToMany<*>->{
                    val newRepo = it.value.createRepository(parentDto).safeCast<RepositoryBase2<DTO, DATA, ENTITY, ModelDTO>>()
                    if(newRepo != null){
                        parentDto.repositories.put(it.key, newRepo)
                    }else{
                        throw OperationsException("CreateRepository failed for ${parentDto.personalName}",
                            ExceptionCodes.CAST_FAILURE)
                    }
                }
                else -> {}
            }
        }
        val dataPropertiesCast = trackedDataProperties(parentDto).safeCast<List<DataPropertyInfo<DTO, DATA, ExposifyEntityBase, ModelDTO>>>()
        if(dataPropertiesCast != null){
            parentDto.dataContainer.setTrackedProperties(dataPropertiesCast)
            parentDto.daoService.setTrackedProperties(trackedEntityProperties(parentDto))
        }
    }
}