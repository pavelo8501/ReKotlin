package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.EntityDTO
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


enum class OrdinanceType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_MANY,
}

sealed class BindingKeyBase(ordinanceType: OrdinanceType) {

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
        fun <CHILD_DATA: DataModel,CHILD_ENTITY: LongEntity> createKey(ordinanceType:OrdinanceType, childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>): BindingKeyBase{
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
    thisDTOModel: DTOClass<DATA,ENTITY>,
    val childDTOModel: DTOClass<CHILD_DATA,CHILD_ENTITY>,
    val byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
    val referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
    val type: OrdinanceType
) : BindingContainer<DATA, ENTITY, CHILD_DATA,CHILD_ENTITY>(thisDTOModel)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity {

         val dtoRepository = listOf<EntityDTO<CHILD_DATA,CHILD_ENTITY>>()

        fun doWithParent(
            parentDto: EntityDTO<DATA, ENTITY>,
            body: DTOClass<CHILD_DATA, CHILD_ENTITY>.() -> DTOClass<CHILD_DATA, CHILD_ENTITY>) {

            childDTOModel.body()
        }

        fun getChild():DTOClass<CHILD_DATA, CHILD_ENTITY>{
            return childDTOModel
        }


}

class RelationshipBinder<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   thisDTOModel: DTOClass<DATA, ENTITY>
): BindingBase<DATA,ENTITY, CHILD_DATA, CHILD_ENTITY>(thisDTOModel)
        where ENTITY : LongEntity, DATA: DataModel, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity {

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
        body: ChildContainer<DATA,ENTITY,CHILD_DATA,CHILD_ENTITY>.()-> Unit
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }
        val container = ChildContainer<DATA, ENTITY,CHILD_DATA, CHILD_ENTITY>(thisDTOModel, childModel, byProperty, referencedOnProperty,OrdinanceType.ONE_TO_MANY).also() {
            attachBinding(BindingKeyBase.createKey(OrdinanceType.ONE_TO_MANY, childModel),it)
        }
        container.body()
    }

    fun bindings(): List<ChildContainer<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>>{
        return childBindings.values.toList()
    }
}


sealed class BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   thisDTOModel: DTOClass<DATA,ENTITY>
): BindingBase<DATA,ENTITY,CHILD_DATA,CHILD_ENTITY>(thisDTOModel)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity{

    protected  var sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>? = null
    fun dataSource(source:KProperty1<DATA, Iterable<CHILD_DATA>>){
        sourceProperty = source
    }

    @JvmName("getSourcePropertyFun")
    fun getSourceProperty(): KProperty1<DATA, Iterable<CHILD_DATA>>?{
        return sourceProperty
    }
}

sealed class BindingBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(val thisDTOModel: DTOClass<DATA,ENTITY>)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity
