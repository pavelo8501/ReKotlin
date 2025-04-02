package po.exposify.binders.relationship

import po.exposify.binders.enums.Cardinality
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.RepositoryBase
import po.exposify.dto.components.SingleRepository
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.wrappers.NullablePropertyWrapper
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


class SingleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>  (
    parentModel: DTOClass<DTO>,
    override val childClass: DTOClass<CHILD_DTO>
): BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>(parentModel, Cardinality.ONE_TO_ONE)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntityBase
{
    override val thisKey = BindingKeyBase2.createOneToOneKey<CHILD_DTO>(childClass)

    val sourcePropertyWrapper: NullablePropertyWrapper<DATA, DataModel?> = NullablePropertyWrapper<DATA, DataModel?>()
    lateinit var ownEntityProperty: KProperty1<ENTITY, ExposifyEntityBase>
    override lateinit var foreignEntityProperty: KMutableProperty1<ExposifyEntityBase, ENTITY>

    fun initProperties(
        ownDataModel: KMutableProperty1<DATA, DataModel?>,
        ownEntity: KProperty1<ENTITY, ExposifyEntityBase>,
        foreignEntity: KMutableProperty1<ExposifyEntityBase, ENTITY>
    ) {
        sourcePropertyWrapper.inject(ownDataModel)
        ownEntityProperty = ownEntity
        foreignEntityProperty = foreignEntity
    }

    override fun createRepository(
        parentModel: CommonDTO<DTO, DATA, ENTITY>
    ): SingleRepository<DTO, DATA, ENTITY, CHILD_DTO>{
        return SingleRepository(parentModel, childClass, this)
    }
}


class MultipleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>(
    parentClass: DTOClass<DTO>,
    override val  childClass: DTOClass<CHILD_DTO>
): BindingContainer2<DTO,DATA, ENTITY, CHILD_DTO>(parentClass, Cardinality.ONE_TO_MANY)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO, DATA: DataModel, ENTITY: ExposifyEntityBase
{
    override val thisKey  = BindingKeyBase2.createOneToManyKey(childClass)
    lateinit var ownDataModelsProperty : KProperty1<DATA, Iterable<DataModel>>
    lateinit var ownEntitiesProperty : KProperty1<ENTITY, Iterable<ExposifyEntityBase>>
    override lateinit var foreignEntityProperty: KMutableProperty1<ExposifyEntityBase, ENTITY>

    fun  initProperties(
        ownDataModels: KProperty1<DATA, Iterable<DataModel>>,
        ownEntities: KProperty1<ENTITY, Iterable<ExposifyEntityBase>>,
        foreignEntity: KMutableProperty1<ExposifyEntityBase, ENTITY>)
    {
        ownDataModelsProperty = ownDataModels
        ownEntitiesProperty = ownEntities
        foreignEntityProperty = foreignEntity
    }

    override fun createRepository(
        parentModel: CommonDTO<DTO, DATA, ENTITY>,
    ): MultipleRepository<DTO, DATA, ENTITY, CHILD_DTO>{
        return MultipleRepository(parentModel, childClass, this)
    }
}


sealed class BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>(
    val parentModel: DTOClass<DTO>,
    val type  : Cardinality,
) where DTO : ModelDTO, CHILD_DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntityBase
{

    abstract val childClass : DTOClass<CHILD_DTO>
    abstract val thisKey : BindingKeyBase2

    abstract val foreignEntityProperty: KMutableProperty1<ExposifyEntityBase, ENTITY>

    companion object {
        fun <DTO: ModelDTO, DATA : DataModel, ENTITY: ExposifyEntityBase, CHILD_DTO: ModelDTO>createOneToOneContainer(
            parent: DTOClass<DTO>,
            childClass: DTOClass<CHILD_DTO>): SingleChildContainer2<DTO,DATA, ENTITY, CHILD_DTO>{
            return SingleChildContainer2(parent, childClass)
        }


        fun <DTO: ModelDTO, DATA : DataModel, ENTITY: ExposifyEntityBase, CHILD_DTO: ModelDTO>createOneToManyContainer(
            parent: DTOClass<DTO>,
            childClass: DTOClass<CHILD_DTO>
        ): MultipleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>{
            return MultipleChildContainer2(parent, childClass)
        }
    }

    abstract fun createRepository(
        parentDto: CommonDTO<DTO, DATA, ENTITY>,
    ) : RepositoryBase<DTO, DATA, ENTITY, CHILD_DTO>
}