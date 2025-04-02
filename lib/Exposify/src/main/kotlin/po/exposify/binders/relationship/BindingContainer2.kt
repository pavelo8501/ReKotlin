package po.exposify.binders.relationship

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.binders.enums.Cardinality
import po.exposify.classes.components.MultipleRepository2
import po.exposify.classes.components.RepositoryBase2
import po.exposify.classes.components.SingleRepository2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.wrappers.NullablePropertyWrapper
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


class SingleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>  (
    parentModel: DTOClass2<DTO>,
    override val childClass: DTOClass2<CHILD_DTO>
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
    ): SingleRepository2<DTO, DATA, ENTITY, CHILD_DTO>{
        return SingleRepository2(parentModel, childClass, this)
    }
}


class MultipleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>(
    parentClass: DTOClass2<DTO>,
    override val  childClass: DTOClass2<CHILD_DTO>
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
    ): MultipleRepository2<DTO, DATA, ENTITY, CHILD_DTO>{
        return MultipleRepository2(parentModel, childClass, this)
    }
}


sealed class BindingContainer2<DTO, DATA, ENTITY, CHILD_DTO>(
    val parentModel: DTOClass2<DTO>,
    val type  : Cardinality,
) where DTO : ModelDTO, CHILD_DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntityBase
{

    abstract val childClass : DTOClass2<CHILD_DTO>
    abstract val thisKey : BindingKeyBase2

    abstract val foreignEntityProperty: KMutableProperty1<ExposifyEntityBase, ENTITY>

    companion object {
        fun <DTO: ModelDTO, DATA : DataModel, ENTITY: ExposifyEntityBase, CHILD_DTO: ModelDTO>createOneToOneContainer(
            parent: DTOClass2<DTO>,
            childClass: DTOClass2<CHILD_DTO>): SingleChildContainer2<DTO,DATA, ENTITY, CHILD_DTO>{
            return SingleChildContainer2(parent, childClass)
        }


        fun <DTO: ModelDTO, DATA : DataModel, ENTITY: ExposifyEntityBase, CHILD_DTO: ModelDTO>createOneToManyContainer(
            parent: DTOClass2<DTO>,
            childClass: DTOClass2<CHILD_DTO>
        ): MultipleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>{
            return MultipleChildContainer2(parent, childClass)
        }
    }

    abstract fun createRepository(
        parentDto: CommonDTO<DTO, DATA, ENTITY>,
    ) : RepositoryBase2<DTO, DATA, ENTITY, CHILD_DTO>
}