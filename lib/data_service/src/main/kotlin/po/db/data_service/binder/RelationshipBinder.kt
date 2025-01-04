package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.EntityDTO
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


enum class OrdinanceType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_MANY,
}

sealed class BindingKeyBase(ordinanceType: OrdinanceType) {

    abstract class  ManyToMany<CHILD_ENTITY: LongEntity>(
        val childModel : DTOClass<*,CHILD_ENTITY>
    ):BindingKeyBase(OrdinanceType.MANY_TO_MANY)

    abstract class  OneToMany<CHILD_ENTITY: LongEntity>(
        val childModel :DTOClass<*,CHILD_ENTITY>
    ):BindingKeyBase(OrdinanceType.ONE_TO_MANY)

    abstract class  OneToOne<CHILD_ENTITY: LongEntity>(
        val childModel : DTOClass<*,CHILD_ENTITY>
    ):BindingKeyBase(OrdinanceType.ONE_TO_ONE)

    companion object{
        fun <CHILD_ENTITY: LongEntity> createKey(ordinanceType:OrdinanceType, childModel : DTOClass<*,CHILD_ENTITY>): BindingKeyBase{
          return  when(ordinanceType){
                OrdinanceType.ONE_TO_ONE -> {
                    object : OneToOne<CHILD_ENTITY>( childModel) {}
                }
                OrdinanceType.ONE_TO_MANY -> {
                    object : OneToMany<CHILD_ENTITY>( childModel) {}
                }
                OrdinanceType.MANY_TO_MANY -> {
                    object : OneToMany<CHILD_ENTITY>( childModel) {}
                }
            }
        }
    }
}

class ChildContainerNew<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    thisDTOModel: DTOClass<DATA,ENTITY>,
    val childDTOModel: DTOClass<CHILD_DATA,CHILD_ENTITY>,
    val byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
    val referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
) : BindingContainer<DATA, ENTITY, CHILD_DATA,CHILD_ENTITY>(thisDTOModel)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity {

    val dtoRepository = listOf<EntityDTO<CHILD_DATA,CHILD_ENTITY>>()

    fun doWithParent(parentDto: EntityDTO<DATA, ENTITY>, body: DTOClass<CHILD_DATA, CHILD_ENTITY>.(EntityDTO<DATA, ENTITY>) -> Unit) {
        childDTOModel.body(parentDto)
    }
}

class RelationshipBinderNew<DATA, ENTITY>(
    thisDTOModel: DTOClass<DATA, ENTITY>
): BindingContainer<DATA,ENTITY, DataModel, LongEntity>(thisDTOModel)
        where ENTITY : LongEntity, DATA: DataModel {

    private var childBindings = mapOf<BindingKeyBase, ChildContainerNew<DATA, ENTITY,* ,*>>()

    private fun <CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity> attachBinding(
        key : BindingKeyBase,
        container: ChildContainerNew<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
    ) {
        if (!childBindings.containsKey(key)) {
            childBindings = childBindings + (key to container)
        }
    }

    fun <CHILD_DATA, CHILD_ENTITY> addChildBinding(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
    ) where CHILD_ENTITY : LongEntity, CHILD_DATA : DataModel{
        if(!childModel.initialized){
            childModel.initialization()
        }
        ChildContainerNew<DATA, ENTITY,CHILD_DATA, CHILD_ENTITY>(thisDTOModel, childModel, byProperty, referencedOnProperty).also() {
            val key =  BindingKeyBase.createKey(OrdinanceType.ONE_TO_MANY, childModel)
            attachBinding(key,it)
        }
    }
}



sealed class BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   val thisDTOModel: DTOClass<DATA,ENTITY>
) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity {

}

class ChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    thisDTOModel: DTOClass<DATA,ENTITY>,
    val childDTOModel: DTOClass<CHILD_DATA,CHILD_ENTITY>,
    val byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
    val referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
    val type: OrdinanceType,
) : BindingContainer<DATA, ENTITY, CHILD_DATA,CHILD_ENTITY>(thisDTOModel)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity{

    val dtoRepository = listOf<CommonDTO<DATA>>()

    fun doStuff(parentDto: EntityDTO<DATA,ENTITY>, fn : DTOClass<DATA, ENTITY>.() -> Unit){
        childDTOModel.execute<CHILD_DATA,CHILD_ENTITY>{
            thisDTOModel.fn()
        }
        thisDTOModel.fn()
    }

    fun createChild(parent : CommonDTO<DATA>,  dataModel : CHILD_DATA):CommonDTO<DATA>?{
        childDTOModel.create(dataModel)?.let {

        }
//        return  childDTOModel.create(dataModel)?.let {dto->
//            daoFactory.new<CHILD,DATA>(childDTOModel){
//                referencedOnProperty.set(it, parent.entityDAO as PARENT)
//               // dto.updateDTO(it)
//            }
//            dto
//        }
        return null
    }

//    fun loadChild(entityDao : PARENT):List<CommonDTO<DATA>> {
//        val result = mutableListOf<CommonDTO<DATA>>()
//      //  val entityDao = parent.getEntityDAO<PARENT>()
//        val childEntities = byProperty.get(entityDao)
//        childEntities.forEach {childEntity ->
//            val childDto =  childDTOModel.create(childEntity)
//            result.add(childDto)
//        }
//        return result
//    }
}


class RelationshipBinder<DATA, ENTITY> (val thisDTOModel: DTOClass<DATA, ENTITY>)  where ENTITY : LongEntity, DATA: DataModel {

    private val childBindings = mutableMapOf<OrdinanceType, ChildContainer<DATA, ENTITY,*,*>>()

    fun  onBindings(dto : EntityDTO<DATA, ENTITY>, fn : DTOClass<DATA, ENTITY>.() -> Unit ){
        childBindings.values.forEach {binding->
            binding.childDTOModel
            binding.doStuff(dto,fn)
        }
    }

    fun bindings(): List<ChildContainer<DATA, ENTITY,*,*>> {
        return childBindings.values.toList()
    }

    fun <CHILD_DATA, CHILD_ENTITY> addChildBinding(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
    ) where CHILD_ENTITY : LongEntity, CHILD_DATA : DataModel{
        if(!childModel.initialized){
            childModel.initialization()
        }
        val type =  OrdinanceType.ONE_TO_MANY
        childBindings.putIfAbsent(
            type,
            ChildContainer<DATA, ENTITY,CHILD_DATA, CHILD_ENTITY>(thisDTOModel, childModel, byProperty, referencedOnProperty,  type)
        )
    }

    fun getDependantTables():List<IdTable<Long>>{
        val result = mutableListOf<IdTable<Long>>()
        childBindings.values.forEach {container ->
            result.add(container.childDTOModel.entityModel.table)
            container.childDTOModel.getAssociatedTables()
        }
        return result
    }
}
