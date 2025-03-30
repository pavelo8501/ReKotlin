package po.exposify.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.binder.enums.OrdinanceType
import po.exposify.classes.DTOClass
import po.exposify.classes.components.MultipleRepository2
import po.exposify.classes.components.RepositoryBase2
import po.exposify.classes.components.SingleRepository2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO2
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.wrappers.NullablePropertyWrapper
import kotlin.collections.set
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


sealed class BindingKeyBase2(val ordinance: OrdinanceType) {
    open class  OneToMany<CHILD_DTO: ModelDTO>(
        val childModel :DTOClass2<CHILD_DTO>
    ):BindingKeyBase2(OrdinanceType.ONE_TO_MANY)

    open class  OneToOne<CHILD_DTO: ModelDTO>(
        val childModel : DTOClass2<CHILD_DTO>
    ):BindingKeyBase2(OrdinanceType.ONE_TO_ONE)

    open class  ManyToMany<CHILD_DTO: ModelDTO>(
        val childModel : DTOClass2<CHILD_DTO>
    ):BindingKeyBase2(OrdinanceType.MANY_TO_MANY)

    companion object{
        fun <CHILD_DTO: ModelDTO> createOneToManyKey(
            childModel : DTOClass2<CHILD_DTO>
        ): OneToMany<CHILD_DTO>{
            return  OneToMany<CHILD_DTO>( childModel)

        }

        fun <CHILD_DTO: ModelDTO> createOneToOneKey(
            childModel : DTOClass2<CHILD_DTO>
        ): OneToOne<CHILD_DTO>{
          return  object : OneToOne<CHILD_DTO>(childModel) {}
        }
    }
}


class MultipleChildContainer2<DTO, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>(
    parentClass: DTOClass2<DTO>,
    childClass: DTOClass2<CHILD_DTO>
): BindingContainer2<DTO, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>(parentClass, childClass, OrdinanceType.ONE_TO_MANY)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity
{
    override val thisKey  = BindingKeyBase2.createOneToManyKey(childModel)

    lateinit var byProperty : KProperty1<LongEntity, SizedIterable<CHILD_ENTITY>>
    lateinit var referencedOnProperty: KMutableProperty1<CHILD_ENTITY, LongEntity>
    lateinit var sourceProperty: KProperty1<DataModel, Iterable<CHILD_DATA>>

    fun initProperties(
        sourceProperty: KProperty1<DataModel, Iterable<CHILD_DATA>>,
        byProperty : KProperty1<LongEntity, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, LongEntity>){
        this.byProperty = byProperty
        this.referencedOnProperty = referencedOnProperty
        this.sourceProperty = sourceProperty
    }


    override fun createRepository(
        parentModel: CommonDTO2<DTO, *, *>,
    ): MultipleRepository2<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>{
        return MultipleRepository2(parentModel, childModel, this)
    }

}

class SingleChildContainer2<DTO, CHILD_DTO, CHILD_DATA,  CHILD_ENTITY>  (
    parentModel: DTOClass2<DTO>,
    childModel: DTOClass2<CHILD_DTO>
): BindingContainer2<DTO, CHILD_DTO,  CHILD_DATA,  CHILD_ENTITY>(parentModel, childModel, OrdinanceType.ONE_TO_ONE)
    where DTO: ModelDTO, CHILD_DTO: ModelDTO, CHILD_DATA :DataModel, CHILD_ENTITY: LongEntity
{
    override val thisKey = BindingKeyBase2.createOneToOneKey<CHILD_DTO>(childModel)

    val sourcePropertyWrapper: NullablePropertyWrapper<DataModel, CHILD_DATA> = NullablePropertyWrapper<DataModel, CHILD_DATA>()
    lateinit var byProperty: KProperty1<LongEntity, CHILD_ENTITY?>
    var referencedOnProperty: KMutableProperty1<CHILD_ENTITY, LongEntity>? = null


    fun  initProperties(
        sourceProperty: KMutableProperty1<DataModel, CHILD_DATA>,
        byProperty: KProperty1<LongEntity, CHILD_ENTITY?>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, LongEntity>)
    {
        this.sourcePropertyWrapper.inject(sourceProperty)
        this.byProperty = byProperty
        this.referencedOnProperty = referencedOnProperty
    }

    @JvmName("initPropertiesNullableChild")
    fun initProperties(
        sourceProperty: KMutableProperty1<DataModel, CHILD_DATA?>,
        byProperty: KProperty1<LongEntity, CHILD_ENTITY?>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, LongEntity>)
    {
        this.sourcePropertyWrapper.injectNullable(sourceProperty)
        this.byProperty = byProperty
        this.referencedOnProperty = referencedOnProperty
    }

    override fun createRepository(
        parentModel: CommonDTO2<DTO, *, *>
    ): SingleRepository2<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>{
        return SingleRepository2(parentModel, childModel, this)
    }
}

sealed class BindingContainer2<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    val parentModel: DTOClass2<DTO>,
    val childModel : DTOClass2<CHILD_DTO>,
    val type  : OrdinanceType,
) where DTO : ModelDTO, CHILD_DTO : ModelDTO,  CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity {

    abstract val thisKey : BindingKeyBase2
    companion object {

        fun <DTO: ModelDTO, CHILD_DTO: ModelDTO,  CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity>createOneToOneContainer(
            parent: DTOClass2<DTO>,
            childDtoClass: DTOClass2<CHILD_DTO>): SingleChildContainer2<DTO, CHILD_DTO,   CHILD_DATA, CHILD_ENTITY>{
            return SingleChildContainer2(parent, childDtoClass)
        }


        fun <DTO: ModelDTO, CHILD_DTO: ModelDTO, CHILD_DATA : DataModel, CHILD_ENTITY: LongEntity>createOneToManyContainer(
            parent: DTOClass2<DTO>,
            child: DTOClass2<CHILD_DTO>
        ): MultipleChildContainer2<DTO, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>{
            return MultipleChildContainer2(parent, child)
        }
    }

    abstract fun createRepository(
        parentDto: CommonDTO2<DTO, *, *>,
    ) : RepositoryBase2<DTO, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>
}

class RelationshipBinder2<DTO>(
   val dtoClass:  DTOClass2<DTO>
) where DTO: ModelDTO {

    private var childBindings = mutableMapOf<BindingKeyBase2, BindingContainer2<DTO, *, *, *>>()

    private fun <CHILD_DTO: ModelDTO, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> attachBinding(
        key : BindingKeyBase2,
        container: BindingContainer2<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>
    ){
        if (!childBindings.containsKey(key)) {
            childBindings[key] = container
        }
    }

    fun <CHILD_DTO: ModelDTO,  CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBinding(
        sourceProperty: KMutableProperty1<DataModel, CHILD_DATA>,
        childModel: DTOClass2<CHILD_DTO>,
        byProperty: KProperty1<LongEntity, CHILD_ENTITY>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, LongEntity>
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }
        val oneToOneContainer =  BindingContainer2.createOneToOneContainer<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(dtoClass, childModel)
        oneToOneContainer.initProperties(sourceProperty, byProperty, referencedOnProperty)
        attachBinding(oneToOneContainer.thisKey, oneToOneContainer)
    }

    fun <CHILD_DTO: ModelDTO, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBinding(
        childModel: DTOClass2<CHILD_DTO>,
        sourceProperty: KMutableProperty1<DataModel, CHILD_DATA?>,
        byProperty: KProperty1<LongEntity, CHILD_ENTITY?>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, LongEntity>
    ){
       if(!childModel.initialized){
           childModel.initialization()
       }
        val oneToOneContainerNullableData =  BindingContainer2.createOneToOneContainer<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(dtoClass, childModel)
        oneToOneContainerNullableData.initProperties(sourceProperty, byProperty, referencedOnProperty)
        attachBinding(oneToOneContainerNullableData.thisKey, oneToOneContainerNullableData)
    }

    fun <CHILD_DTO : ModelDTO, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>childBinding(
        childModel: DTOClass2<CHILD_DTO>,
        sourceProperty: KProperty1<DataModel, Iterable<CHILD_DATA>>,
        byProperty: KProperty1<LongEntity, SizedIterable<CHILD_ENTITY>>,
        referencedOnProperty: KMutableProperty1<CHILD_ENTITY, LongEntity>,
    ){
        if(!childModel.initialized){
            childModel.initialization()
        }

        val oneToMany = BindingContainer2.createOneToManyContainer<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(dtoClass, childModel)
        oneToMany.initProperties(sourceProperty, byProperty, referencedOnProperty)
        attachBinding(oneToMany.thisKey, oneToMany)
    }


    fun createRepositories(parentDto: CommonDTO2<DTO, *, *>){
         childBindings.forEach {
            when(it.key){
                is BindingKeyBase2.OneToOne<*>->{
                 //   val container = it.value as SingleChildContainer
                  //  val newSingleRepo = SingleRepository(parentDto, container)
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
    }
}