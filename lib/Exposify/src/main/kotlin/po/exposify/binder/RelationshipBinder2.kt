package po.exposify.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.binder.enums.OrdinanceType
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


class MultipleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>(
    parentClass: DTOClass2<DTO>,
    override val  childClass: DTOClass2<CHILD_DTO>
): BindingContainer2<DTO,DATA, ENTITY, CHILD_DTO>(parentClass, OrdinanceType.ONE_TO_MANY)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity
{
    override val thisKey  = BindingKeyBase2.createOneToManyKey(childClass)

    override fun createRepository(
        parentModel: CommonDTO2<DTO, DATA, ENTITY>,
    ): MultipleRepository2<DTO, DATA, ENTITY, CHILD_DTO>{
        return MultipleRepository2(parentModel, childClass, this)
    }
}

class SingleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>  (
    parentModel: DTOClass2<DTO>,
    override val childClass: DTOClass2<CHILD_DTO>
): BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>(parentModel, OrdinanceType.ONE_TO_ONE)
    where DTO: ModelDTO, CHILD_DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity
{
    override val thisKey = BindingKeyBase2.createOneToOneKey<CHILD_DTO>(childClass)

    override fun createRepository(
        parentModel: CommonDTO2<DTO, DATA, ENTITY>
    ): SingleRepository2<DTO, DATA, ENTITY, CHILD_DTO>{
        return SingleRepository2(parentModel, childClass, this)
    }
}

sealed class BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>(
    val parentModel: DTOClass2<DTO>,
    val type  : OrdinanceType,
) where DTO : ModelDTO, CHILD_DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity{

    abstract val childClass : DTOClass2<CHILD_DTO>

    abstract val thisKey : BindingKeyBase2

    val sourcePropertyWrapper: NullablePropertyWrapper<DATA, Any?> = NullablePropertyWrapper<DATA, Any?>()
    lateinit var ownDataModelsProperty : KProperty1<DATA, Iterable<*>>
    lateinit var ownEntityProperty: KProperty1<*, *>
    lateinit var foreignEntityProperty: KMutableProperty1<*, *>


    fun initProperties(
        ownDataModel: KMutableProperty1<DATA, Any?>,
        ownEntity: KProperty1<ENTITY, *>,
        foreignEntity: KMutableProperty1<*, ENTITY>
    ) {
        sourcePropertyWrapper.inject(ownDataModel)
        ownEntityProperty = ownEntity
        foreignEntityProperty = foreignEntity
    }

    fun  initProperties(
        ownDataModels: KProperty1<DATA, Iterable<*>>,
        byProperty: KProperty1<ENTITY, *>,
        foreignEntity: KMutableProperty1<*, ENTITY>)
    {
        ownDataModelsProperty = ownDataModels
        ownEntityProperty = byProperty
        foreignEntityProperty = foreignEntity
    }

    companion object {
        fun <DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity, CHILD_DTO: ModelDTO>createOneToOneContainer(
            parent: DTOClass2<DTO>,
            childClass: DTOClass2<CHILD_DTO>): SingleChildContainer2<DTO,DATA, ENTITY, CHILD_DTO>{
            return SingleChildContainer2(parent, childClass)
        }


        fun <DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity, CHILD_DTO: ModelDTO>createOneToManyContainer(
            parent: DTOClass2<DTO>,
            childClass: DTOClass2<CHILD_DTO>
        ): MultipleChildContainer2<DTO,DATA, ENTITY, CHILD_DTO>{
            return MultipleChildContainer2(parent, childClass)
        }
    }

    abstract fun createRepository(
        parentDto: CommonDTO2<DTO, DATA, ENTITY>,
    ) : RepositoryBase2<DTO, DATA, ENTITY, CHILD_DTO>
}

class RelationshipBinder2<DTO, DATA, ENTITY>(
   val dtoClass:  DTOClass2<DTO>
) where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity {

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
        ownDataModel: KMutableProperty1<DATA, Any?>,
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
        ownDataModel: KProperty1<DATA, Iterable<*>>,
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

    fun createRepositories(parentDto: CommonDTO2<DTO, DATA, ENTITY>){
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
    }
}