package po.exposify.dto.components.relation_binder

import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.classes.DTOBase
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.components.MultipleRepository
import po.exposify.dto.components.RepositoryBase
import po.exposify.dto.components.SingleRepository
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.classes.DTOClass
import po.exposify.classes.components.DTOConfig
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.dto.components.relation_binder.classes.NullablePropertyWrapper
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


fun <DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY> createOneToOneContainer(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    childConfig: DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    childClass: DTOBase<CHILD_DTO, *>
): SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>

where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
       CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
   return  SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(dto,  childConfig,childClass)
}


fun <DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY> createOneToManyContainer(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    childConfig: DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    childClass: DTOBase<CHILD_DTO, *, >
): MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>

        where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
    return  MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(dto,  childConfig,childClass)
}



class SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>  (
    private  val dto: CommonDTO<DTO, DATA, ENTITY>,
    childConfig: DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    childClass: DTOBase<CHILD_DTO, *>
): BindingContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(childConfig, childClass)
        where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
            CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
    override val thisKey = BindingKeyBase.createOneToOneKey<DTO>(dto.dtoClass)
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY

    val sourcePropertyWrapper: NullablePropertyWrapper<DATA, CHILD_DATA?> = NullablePropertyWrapper<DATA, CHILD_DATA?>()
    lateinit var ownEntityProperty: KProperty1<ENTITY, CHILD_ENTITY>
    override lateinit var foreignEntityProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>

    fun initProperties(
        ownDataModel: KMutableProperty1<DATA, CHILD_DATA?>,
        ownEntity: KProperty1<ENTITY, CHILD_ENTITY>,
        foreignEntity: KMutableProperty1<CHILD_ENTITY, ENTITY>
    ) {
        sourcePropertyWrapper.inject(ownDataModel)
        ownEntityProperty = ownEntity
        foreignEntityProperty = foreignEntity
    }

}


class MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    private  val dto: CommonDTO<DTO, DATA, ENTITY>,
    childConfig: DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    childClass: DTOBase<CHILD_DTO, *>,
): BindingContainer<DTO,DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(childConfig, childClass)
        where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
    override val thisKey  = BindingKeyBase.createOneToManyKey(dto.dtoClass)
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY
    lateinit var ownDataModelsProperty : KProperty1<DATA, MutableList<CHILD_DATA>>
    lateinit var ownEntitiesProperty : KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>
    override lateinit var foreignEntityProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>

    fun  initProperties(
        ownDataModels: KProperty1<DATA, MutableList<CHILD_DATA>>,
        ownEntities: KProperty1<ENTITY, SizedIterable<CHILD_ENTITY>>,
        foreignEntity: KMutableProperty1<CHILD_ENTITY, ENTITY>
    )
    {
        ownDataModelsProperty = ownDataModels
        ownEntitiesProperty = ownEntities
        foreignEntityProperty = foreignEntity
    }
}


sealed class BindingContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    val childConfig: DTOConfig<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    val childClass: DTOBase<CHILD_DTO, *>,
) where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity, CHILD_DTO : ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY : ExposifyEntity
{
    abstract val cardinality  : Cardinality
    abstract val thisKey : BindingKeyBase
    abstract var foreignEntityProperty: KMutableProperty1<CHILD_ENTITY, ENTITY>
}