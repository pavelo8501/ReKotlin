package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.components.MultipleRepository
import po.db.data_service.dto.components.RepositoryBase
import po.db.data_service.dto.components.SingleRepository
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.DTOContainerBase.Companion.copyAsHostingDTO
import po.db.data_service.models.HostDTO
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
        val childModel :DTOClass<CHILD_DATA, CHILD_ENTITY>
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


class MultipleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parentModel: DTOClass<DATA, ENTITY>,
    childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>
): BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parentModel, childModel, OrdinanceType.ONE_TO_MANY)
        where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity
{
    lateinit var byProperty : KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>
    lateinit var referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>
    lateinit var sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>

    fun initProperties(
        byProperty : KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
        sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>){
        this.byProperty = byProperty
        this.referencedOnProperty = referencedOnProperty
        this.sourceProperty = sourceProperty
    }

    override fun createRepository(
        parent : HostDTO<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>
    ): MultipleRepository<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>{
        return  MultipleRepository(parent, childModel, thisKey,this)
    }
}

class SingleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
    parentModel: DTOClass<DATA, ENTITY>,
    childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>
): BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parentModel, childModel, OrdinanceType.ONE_TO_ONE)
    where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity
{

    lateinit var byProperty: KProperty1<ENTITY, CHILD_ENTITY?>
    lateinit var referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>
    lateinit var sourceProperty: KProperty1<DATA, CHILD_DATA?>

    fun initProperties(
        byProperty: KProperty1<ENTITY, CHILD_ENTITY?>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
        sourceProperty: KProperty1<DATA, CHILD_DATA?>)
    {
        this.byProperty = byProperty
        this.referencedOnProperty = referencedOnProperty
        this.sourceProperty = sourceProperty
    }

    override fun createRepository(
        parent : HostDTO<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>
    ): SingleRepository<DATA,ENTITY,CHILD_DATA, CHILD_ENTITY>{
        return  SingleRepository(parent,childModel, thisKey, this)
    }
}


sealed class BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
   val parentModel: DTOClass<DATA,ENTITY>,
   val childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
   val type  : OrdinanceType,
) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity{
    val thisKey: BindingKeyBase = BindingKeyBase.createKey(type, childModel)

    val repository = mutableListOf<CommonDTO<CHILD_DATA, CHILD_ENTITY>>()

    abstract fun createRepository(
        parent : HostDTO<DATA,ENTITY, CHILD_DATA, CHILD_ENTITY>
    ): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>

    fun applyBindings(parentDto: CommonDTO<DATA, ENTITY>){

        parentDto.hostDTO?.let {
            @Suppress("UNCHECKED_CAST")
            val repo =  createRepository(it as HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>)
            it.addRepository(thisKey, repo)
        }?:run {
            parentDto.copyAsHostingDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>().let { host ->
                parentDto.hostDTO = host
                host.addRepository(thisKey, createRepository(host))
            }
        }
    }

    fun deleteChildren(parentDto: CommonDTO<DATA, ENTITY>){
        val binding =  parentDto.bindings[thisKey]
        if (binding != null){
            binding.repository.forEach {
              //  @Suppress("UNCHECKED_CAST")
              //  childModel.delete(it as CommonDTO<DATA, ENTITY>)
            }
        }
    }
}

class RelationshipBinder<DATA, ENTITY>(
   val parentModel: DTOClass<DATA, ENTITY>
) where DATA: DataModel, ENTITY : LongEntity {

    private var childBindings = mapOf<BindingKeyBase, BindingContainer<DATA, ENTITY, *, *>>()

    private fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>attachBinding(
        key : BindingKeyBase, container: BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
    ){
        if (!childBindings.containsKey(key)) {
            childBindings = (childBindings + (key to container))
        }
        parentModel.bindings[container.thisKey] = container
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

    @JvmName("childBindingOneToOne")
    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBinding(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        byProperty: KProperty1<ENTITY, CHILD_ENTITY?>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
        sourceProperty: KProperty1<DATA, CHILD_DATA?>
    ){
       if(!childModel.initialized){
           childModel.initialization()
       }
       createOneToOneContainer(parentModel, childModel).let {
           it.initProperties(byProperty, referencedOnProperty, sourceProperty)
           attachBinding<CHILD_DATA, CHILD_ENTITY>(it.thisKey, it)
       }
    }

    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBinding(
        childModel: DTOClass<CHILD_DATA, CHILD_ENTITY>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>,
        sourceProperty: KProperty1<DATA, Iterable<CHILD_DATA>>,
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }
        createOneToManyContainer(parentModel, childModel).let {
            it.initProperties(byProperty, referencedOnProperty, sourceProperty)
            attachBinding<CHILD_DATA, CHILD_ENTITY>(it.thisKey, it)
        }
    }
}


