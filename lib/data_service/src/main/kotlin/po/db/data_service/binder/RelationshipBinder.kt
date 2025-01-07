package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.EntityDTO
import kotlin.collections.set
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


enum class OrdinanceType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_MANY,
}

sealed class BindingKeyBase(val ordinance: OrdinanceType) {

    abstract class  ManyToMany<CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity>(
        val childModel : DTOClass<CHILD_DATA,CHILD_ENTITY>
    ):BindingKeyBase(OrdinanceType.MANY_TO_MANY)

    abstract class  OneToMany<CHILD_DATA: DataModel,CHILD_ENTITY: LongEntity>(
        val childModel :DTOClass<CHILD_DATA,CHILD_ENTITY>
    ):BindingKeyBase(OrdinanceType.ONE_TO_MANY)

    abstract class  OneToOne<CHILD_DATA: DataModel,CHILD_ENTITY: LongEntity>(
        val childModel : DTOClass<CHILD_DATA,CHILD_ENTITY>
    ):BindingKeyBase(OrdinanceType.ONE_TO_ONE)

    companion object{

        fun <CHILD_DATA: DataModel,CHILD_ENTITY: LongEntity> createKey(
            ordinanceType:OrdinanceType,
            childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>
        ): BindingKeyBase{
          return  when(ordinanceType){
                OrdinanceType.ONE_TO_ONE -> {
                    object : OneToOne<CHILD_DATA, CHILD_ENTITY>( childModel) {}
                }
                OrdinanceType.ONE_TO_MANY -> {
                    object : OneToMany<CHILD_DATA, CHILD_ENTITY>( childModel) {}
                }
                OrdinanceType.MANY_TO_MANY -> {
                    object : OneToMany<CHILD_DATA, CHILD_ENTITY>( childModel) {}
                }
            }
        }
    }
}

class ChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    val childDTOModel: DTOClass<CHILD_DATA,CHILD_ENTITY>,
    val byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
    val referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
    val sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>,
    val type: OrdinanceType,
    thisDTOModel: DTOClass<DATA,ENTITY>,
) : BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(thisDTOModel)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity {

    val repository = mutableListOf<EntityDTO<CHILD_DATA, CHILD_ENTITY>>()

    fun createFromDataModel(parentDto: EntityDTO<DATA,ENTITY>){
        childDTOModel.apply {
            val parentData = parentDto.injectedDataModel
            childDTOModel.factory.extractDataModel(sourceProperty, parentData).forEach {childData->
                create<DATA, ENTITY>(childData){
                    referencedOnProperty.set(it, parentDto.entityDAO )
                }?.let {childDto->
                    repository.add(childDto)
                }
            }
        }
    }

    fun createFromEntity(
        parentDto: EntityDTO<DATA,ENTITY>,
        key: BindingKeyBase){
        childDTOModel.apply {
            byProperty.get(parentDto.entityDAO).forEach {
               val childDto = create(it)
                if(childDto != null){
                    repository.add(childDto)
                }else{
                    TODO("Actions to be taken on child dto creation null")
                }
            }
        }
        parentDto.bindings[key] = this.copy()
        repository.clear()
    }


    fun copy():ChildContainer<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>{
        return  ChildContainer<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>(
            this.childDTOModel,
            this.byProperty,
            this.referencedOnProperty,
            this.sourceProperty,
            this.type,
            this.parentModel).also {
               it.repository.addAll(this.repository.toList())
        }

    }

}

class RelationshipBinder<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   val parentModel: DTOClass<DATA, ENTITY>
) where DATA: DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity {

    private var childBindings = mapOf<BindingKeyBase, ChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>>()

    private fun attachBinding(key : BindingKeyBase, container: ChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>){
        if (!childBindings.containsKey(key)) {
            childBindings = (childBindings + (key to container))
        }
    }

    fun getBinding(key: BindingKeyBase): ChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>?{
        if(childBindings.containsKey(key)){
            return childBindings[key]
        }
        return null
    }

    fun addChildBinding(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
        sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>,
        body: (ChildContainer<DATA,ENTITY,CHILD_DATA,CHILD_ENTITY>.()-> Unit)?
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }
        val container = ChildContainer<DATA, ENTITY,CHILD_DATA, CHILD_ENTITY>(
            childModel,
            byProperty,
            referencedOnProperty,
            sourceProperty,
            OrdinanceType.ONE_TO_MANY,
            parentModel
        ).also{
            val key = BindingKeyBase.createKey(OrdinanceType.ONE_TO_MANY, childModel)
            attachBinding(key, it)
            parentModel.bindings[key] = it
        }
        if(body!=null){
            container.body()
        }

    }

    fun bindings(): List<ChildContainer<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>>{
        return childBindings.values.toList()
    }
}


sealed class BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    val parentModel: DTOClass<DATA,ENTITY>
) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity{

}
