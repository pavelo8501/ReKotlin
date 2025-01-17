package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
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
    val childModel: DTOClass<CHILD_DATA,CHILD_ENTITY>,
    val byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
    val referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
    val sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>,
    val type: OrdinanceType,
    parentModel: DTOClass<DATA,ENTITY>,

): BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parentModel)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity {

    val thisKey: BindingKeyBase = BindingKeyBase.createKey(type, childModel)
    val repository = mutableListOf<CommonDTO<CHILD_DATA, CHILD_ENTITY>>()

    fun createFromDataModel(parentDto: CommonDTO<DATA, ENTITY>){
        val parentData = parentDto.injectedDataModel
        val extractedChildModels = childModel.factory.extractDataModel(sourceProperty, parentData)
        extractedChildModels.forEach { childDataModel ->
            val newDto = childModel.initDTO<DATA, ENTITY>(childDataModel) { childEntity ->
                referencedOnProperty.set(childEntity, parentDto.entityDAO)
            }
            if (newDto != null) {
                repository.add(newDto)
            }
        }
        parentDto.bindings[thisKey] = this
    }

    fun createFromEntity(parentDto: CommonDTO<DATA,ENTITY>){
        childModel.apply {
            byProperty.get(parentDto.entityDAO).forEach {
               val childDto = initDTO(it)
                if(childDto != null){
                    repository.add(childDto)
                }else{
                    TODO("Actions to be taken on child dto creation null")
                }
            }
        }
        parentDto.bindings[thisKey] = this.copy()
        repository.clear()
    }

    fun <DATA: DataModel, ENTITY: LongEntity>deleteChildren(parentDto: CommonDTO<DATA,ENTITY>){
        val binding =  parentDto.bindings[thisKey]
        if (binding != null){
            binding.repository.forEach {
               @Suppress("UNCHECKED_CAST")
               childModel.delete(it as CommonDTO<CHILD_DATA, CHILD_ENTITY>)
            }
        }
    }

    fun copy():ChildContainer<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>{
        return  ChildContainer<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>(
            this.childModel,
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
            parentModel)

        if(body!=null){
            container.body()
        }
        attachBinding(container.thisKey, container)
        parentModel.bindings[container.thisKey] = container
    }

    fun bindings(): List<ChildContainer<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>>{
        return childBindings.values.toList()
    }
}

sealed class BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    val parentModel: DTOClass<DATA,ENTITY>
) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity
