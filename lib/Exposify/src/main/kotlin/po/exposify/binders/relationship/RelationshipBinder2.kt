package po.exposify.binders.relationship

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.binders.enums.Cardinality
import po.exposify.binders.relationship.models.PropertyInfo
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import kotlin.collections.set
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1



class RelationshipBinder2<DTO, DATA, ENTITY>(
   val dtoClass:  DTOClass2<DTO>
) where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity
{
    private var childBindings = mutableMapOf<BindingKeyBase2, BindingContainer2<DTO, DATA, ENTITY, *>>()

    private fun <CHILD_DTO: ModelDTO> attachBinding(
        key : BindingKeyBase2,
        container: BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>
    ){
        if (!childBindings.containsKey(key)) {
            childBindings[key] = container
        }
    }

    fun <CHILD_DTO: ModelDTO>single(
        childModel: DTOClass2<CHILD_DTO>,
        ownDataModel: KMutableProperty1<DATA, DataModel?>,
        ownEntity: KProperty1<ENTITY, *>,
        foreignEntity: KMutableProperty1<*, ENTITY>
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
        ownEntities: KProperty1<ENTITY, SizedIterable<*>>,
        foreignEntity: KMutableProperty1<*, ENTITY>,
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }

        val oneToMany = BindingContainer2.createOneToManyContainer<DTO, DATA, ENTITY, CHILD_DTO>(dtoClass, childModel)
        oneToMany.initProperties(ownDataModel, ownEntities, foreignEntity)
        attachBinding(oneToMany.thisKey, oneToMany)
    }

    fun trackedDataProperties(forDto : CommonDTO<DTO, DATA, ENTITY>): List<PropertyInfo<DTO, DATA>>{
        val result = mutableListOf<PropertyInfo<DTO, DATA>>()
        childBindings.values.forEach {
            when(it.type){
                Cardinality.ONE_TO_ONE ->{
                    result.add(PropertyInfo(
                        it.sourcePropertyWrapper.extract().name,
                        Cardinality.ONE_TO_ONE,
                        it.sourcePropertyWrapper.nullable,
                        it.thisKey,
                        it)
                    )
                }
                Cardinality.ONE_TO_MANY->{
                    result.add(
                        PropertyInfo(
                            it.ownDataModelsProperty.name,
                            Cardinality.ONE_TO_MANY,
                            false,
                            it.thisKey,
                            it
                        )
                    )
                }
                Cardinality.MANY_TO_MANY ->{

                }
            }
        }
        return result
    }

    fun createRepositories(parentDto: CommonDTO<DTO, DATA, ENTITY>){
         childBindings.forEach {
            when(it.key){
                is BindingKeyBase2.OneToOne<*>->{
                    val newRepo = it.value.createRepository(parentDto)
                    parentDto.repositories.put(it.key, newRepo)
                }

                is BindingKeyBase2.OneToMany<*>->{
                    val newRepo = it.value.createRepository(parentDto)
                    parentDto.repositories.put(it.key, newRepo)
                }
                else -> {}
            }
        }
        parentDto.dataContainer.setTrackedProperties(trackedDataProperties(parentDto))
    }
}