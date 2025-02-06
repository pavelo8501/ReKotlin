package po.exposify.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.classes.DTOClass
import po.db.data_service.classes.components.MultipleRepository
import po.db.data_service.classes.components.SingleRepository
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.dto.CommonDTO
import po.db.data_service.dto.DTOBase.Companion.copyAsHostingDTO
import po.db.data_service.dto.HostDTO
import po.db.data_service.wrappers.NullablePropertyWrapper
import kotlin.collections.set
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

enum class OrdinanceType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_MANY,
}

sealed class BindingKeyBase(val ordinance: OrdinanceType) {

    open class  ManyToMany<CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity>(
        val childModel : DTOClass<CHILD_DATA,CHILD_ENTITY>
    ):BindingKeyBase(OrdinanceType.MANY_TO_MANY)

    open class  OneToMany<CHILD_DATA: DataModel,CHILD_ENTITY: LongEntity>(
        val childModel :DTOClass<CHILD_DATA, CHILD_ENTITY>
    ):BindingKeyBase(OrdinanceType.ONE_TO_MANY)

    open class  OneToOne<CHILD_DATA: DataModel,CHILD_ENTITY: LongEntity>(
        val childModel : DTOClass<CHILD_DATA,CHILD_ENTITY>
    ):BindingKeyBase(OrdinanceType.ONE_TO_ONE)

    companion object{


        fun <CHILD_DATA: DataModel,CHILD_ENTITY: LongEntity> createOneToManyKey(
            childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>
        ): BindingKeyBase.OneToMany<CHILD_DATA, CHILD_ENTITY>{
            return  OneToMany<CHILD_DATA, CHILD_ENTITY>( childModel)

        }


        fun <CHILD_DATA: DataModel,CHILD_ENTITY: LongEntity> createOneToOneKey(
            childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>
        ): BindingKeyBase.OneToOne<CHILD_DATA, CHILD_ENTITY>{
          return  object : OneToOne<CHILD_DATA, CHILD_ENTITY>( childModel) {}
        }
    }
}


class MultipleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parentModel: DTOClass<DATA, ENTITY>,
    childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>
): BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parentModel, childModel, OrdinanceType.ONE_TO_MANY)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity
{
    override val thisKey  = BindingKeyBase.createOneToManyKey(childModel)

    lateinit var byProperty : KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>
    var referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>? = null
    lateinit var sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>

    fun initProperties(
        sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>,
        byProperty : KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>? = null){
        this.byProperty = byProperty
        this.referencedOnProperty = referencedOnProperty
        this.sourceProperty = sourceProperty
    }

    override fun setRepository(
        parent: HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
    ){
        parent.repositories[thisKey] =  MultipleRepository(parent, childModel, thisKey, this)
    }
}

class SingleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parentModel: DTOClass<DATA, ENTITY>,
    childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>
): BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parentModel, childModel, OrdinanceType.ONE_TO_ONE)
    where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity
{
    override val thisKey = BindingKeyBase.createOneToOneKey(childModel)

    val sourceProperty = NullablePropertyWrapper<DATA, CHILD_DATA>()
    lateinit var byProperty: KProperty1<ENTITY, CHILD_ENTITY?>
    var referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>? = null


    fun initProperties(
        sourceProperty: KMutableProperty1<DATA, CHILD_DATA>,
        byProperty: KProperty1<ENTITY, CHILD_ENTITY?>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>? = null)
    {
        this.sourceProperty.inject(sourceProperty)
        this.byProperty = byProperty
        this.referencedOnProperty = referencedOnProperty
    }

    @JvmName("initPropertiesNullableChild")
    fun initProperties(
        sourceProperty: KMutableProperty1<DATA, CHILD_DATA?>,
        byProperty: KProperty1<ENTITY, CHILD_ENTITY?>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>? = null)
    {
        this.sourceProperty.injectNullable(sourceProperty)
        this.byProperty = byProperty
        this.referencedOnProperty = referencedOnProperty
    }

    override fun setRepository(
        parent: HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
    ){
        parent.repositories[thisKey] = SingleRepository(parent, childModel, thisKey, this)
    }
}


sealed class BindingContainer<DATA, ENTITY,  CHILD_DATA,  CHILD_ENTITY>(
   val parentModel: DTOClass<DATA,ENTITY>,
   val childModel : DTOClass< CHILD_DATA,  CHILD_ENTITY>,
   val type  : OrdinanceType,
) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity{

    abstract val thisKey : BindingKeyBase

    abstract fun setRepository(
        parent: HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
    )

    fun applyBinding(parentDto: CommonDTO<DATA, ENTITY>): HostDTO<DATA, ENTITY, out CHILD_DATA, out CHILD_ENTITY> {
      val newHost =  parentDto.copyAsHostingDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>().let { host ->
            parentDto.hostDTO = host
            setRepository(host)
            host
        }
        return newHost
    }

    fun applyBindingToHost(parentDto: HostDTO<DATA, ENTITY, out DataModel, out LongEntity>){
        @Suppress("UNCHECKED_CAST")
        parentDto as  HostDTO<DATA, ENTITY,  CHILD_DATA, CHILD_ENTITY>
        setRepository(parentDto)
    }
}

class RelationshipBinder<DATA, ENTITY>(
   val parentModel: DTOClass<DATA, ENTITY>
) where DATA: DataModel, ENTITY : LongEntity{

    private var childBindings = mapOf<BindingKeyBase, BindingContainer<DATA, ENTITY, *, *>>()

    private fun <CHILD_DATA:DataModel, CHILD_ENTITY: LongEntity>attachBinding(
        key : BindingKeyBase, container: BindingContainer<DATA, ENTITY, *, *>
    ){
        if (!childBindings.containsKey(key)) {
            childBindings = (childBindings + (key to container))
        }
        parentModel.bindings[key] = container
    }

    private fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>createOneToOneContainer(
        parent: DTOClass<DATA, ENTITY>,
        child: DTOClass<CHILD_DATA, CHILD_ENTITY>
    ): SingleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>{
       return SingleChildContainer(parent, child)
    }

    private fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>createOneToManyContainer(
        parent: DTOClass<DATA, ENTITY>,
        child: DTOClass<CHILD_DATA, CHILD_ENTITY>
    ): MultipleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>{
        return MultipleChildContainer(parent, child)
    }


    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBinding(
        sourceProperty: KMutableProperty1<DATA, CHILD_DATA>,
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        byProperty: KProperty1<ENTITY, CHILD_ENTITY>
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }
        createOneToOneContainer(parentModel, childModel).let {
            it.initProperties(sourceProperty, byProperty, null)
            attachBinding<CHILD_DATA, CHILD_ENTITY>(it.thisKey, it)
        }
    }


    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBinding(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        sourceProperty: KMutableProperty1<DATA, CHILD_DATA?>,
        byProperty: KProperty1<ENTITY, CHILD_ENTITY?>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>
    ){
       if(!childModel.initialized){
           childModel.initialization()
       }
       createOneToOneContainer(parentModel, childModel).let {
           it.initProperties(sourceProperty, byProperty, referencedOnProperty)
           attachBinding<CHILD_DATA, CHILD_ENTITY>(it.thisKey, it)
       }
    }

    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBinding(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }
        createOneToManyContainer(parentModel, childModel).let {
            it.initProperties(sourceProperty, byProperty, referencedOnProperty)
            attachBinding<CHILD_DATA, CHILD_ENTITY>(it.thisKey, it)
        }
    }

    fun applyBindings(parentDto: CommonDTO<DATA, ENTITY>){
        val thisKeys = childBindings.keys
        if(thisKeys.count() > 0){
            if(parentDto.hostDTO == null){
                childBindings[thisKeys.first()]!!.applyBinding(parentDto).let {hosted->
                    if(thisKeys.count()>1){
                        thisKeys.drop(1).forEach {
                            childBindings[it]!!.applyBindingToHost(hosted)
                        }
                    }
                }
            }
        }
    }

}


