package po.exposify.dto.components.relation_binder

import po.exposify.classes.interfaces.DataModel
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import kotlin.collections.set



class RelationshipBinder<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
   val dtoClass:  DTOClass<DTO>
) where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity, CHILD_DTO: ModelDTO, CHILD_DATA: DataModel, CHILD_ENTITY: ExposifyEntity {
    internal var manyBindings =
        mutableMapOf<BindingKeyBase.OneToMany<DTO>, MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()
        private set

    internal var singeBindings =
        mutableMapOf<BindingKeyBase.OneToOne<DTO>, SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()
        private set

    fun attachBinding(
        key: BindingKeyBase.OneToOne<DTO>,
        container: SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
    ) {
        singeBindings[key] = container
    }

    fun attachBinding(
        key: BindingKeyBase.OneToMany<DTO>,
        container: MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
    ) {
        manyBindings[key] = container
    }
}

 //   fun trackedDataProperties(forDto : CommonDTO<DTO, DATA, ENTITY>)
 //   :List<DataPropertyInfo<DTO,DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>{
//        val result = mutableListOf<DataPropertyInfo<DTO, DATA, ENTITY,  CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()
//        childBindings.values.forEach {
//            when(it){
//                is SingleChildContainer ->{
//                    val property = it.sourcePropertyWrapper
//                    result.add(DataPropertyInfo(property.extract().name, Cardinality.ONE_TO_ONE, property.nullable, it.thisKey, it))
//                }
//                is MultipleChildContainer->{
//                    val property = it.ownDataModelsProperty
//                    result.add(DataPropertyInfo(property.name, Cardinality.ONE_TO_MANY, false, it.thisKey, it))
//                }
//            }
//        }
    //    return result
   // }
//
//    fun trackedEntityProperties(forDto : CommonDTO<DTO, DATA, ENTITY>)
//    : List<EntityPropertyInfo<DTO,DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>{
//        val result = mutableListOf<EntityPropertyInfo<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()
//        childBindings.values.forEach {
//            when(it){
//                is SingleChildContainer ->{
//                    val property = it.ownEntityProperty
//                    result.add(EntityPropertyInfo(property.name, Cardinality.ONE_TO_ONE, false, it.thisKey, it))
//                }
//                is MultipleChildContainer->{
//                    val property = it.ownEntitiesProperty
//                    result.add(EntityPropertyInfo(property.name, Cardinality.ONE_TO_MANY, false, it.thisKey, it))
//                }
//            }
//        }
     //   return result
    //}
