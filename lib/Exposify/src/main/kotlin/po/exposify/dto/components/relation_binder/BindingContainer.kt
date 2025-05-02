package po.exposify.dto.components.relation_binder

import po.exposify.dto.DTOBase
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.dto.components.relation_binder.delegates.OneToManyDelegate
import po.exposify.dto.components.relation_binder.delegates.OneToOneDelegate
import po.exposify.extensions.castOrOperationsEx
import po.misc.collections.CompositeKey
import po.misc.collections.generateKey


fun <DTO, DATA, ENTITY, F_DTO, FD, FE> DTOBase<F_DTO, FD>.createOneToOneContainer(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    bindingDelegate : OneToOneDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>
): SingleChildContainer<DTO, DATA, ENTITY, F_DTO, FD, FE>

where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
       F_DTO : ModelDTO,  FD: DataModel, FE : ExposifyEntity
{
   return  SingleChildContainer(dto, this, bindingDelegate)
}


fun <DTO, DATA, ENTITY, F_DTO, FD, FE> DTOBase<F_DTO, FD>.createOneToManyContainer(
    dto: CommonDTO<DTO, DATA, ENTITY>,
    bindingDelegate : OneToManyDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>
): MultipleChildContainer<DTO, DATA, ENTITY, F_DTO, FD, FE>
        where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              F_DTO : ModelDTO,  FD: DataModel, FE : ExposifyEntity
{
    return  MultipleChildContainer(dto, this, bindingDelegate)
}



class SingleChildContainer<DTO, DATA, ENTITY, F_DTO, FD, FE>  (
    private  val dto: CommonDTO<DTO, DATA, ENTITY>,
    childClass: DTOBase<F_DTO, FD>,
    private val bindingDelegate : OneToOneDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>
): BindingContainer<DTO, DATA, ENTITY, F_DTO, FD, FE>(childClass)
        where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
            F_DTO : ModelDTO,  FD: DataModel, FE : ExposifyEntity
{
    override val thisKey = dto.dtoClass.generateKey(Cardinality.ONE_TO_ONE)
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY

    fun getDataModel(dataModel: DATA): FD
            = bindingDelegate.getDataModel(dataModel)

    fun saveDataModel(dataModel:FD)
            = bindingDelegate.saveDataModel(dataModel)

    fun attachForeignEntity(container: EntityUpdateContainer<FE, DTO, DATA, ENTITY>): Unit
            = bindingDelegate.attachForeignEntity(container)

    fun getChildEntity(entity: ENTITY): FE
            = bindingDelegate.getChildEntity(entity)

}


class MultipleChildContainer<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    private  val dto: CommonDTO<DTO, DATA, ENTITY>,
    childClass: DTOBase<F_DTO, FD>,
    private val bindingDelegate : OneToManyDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>
): BindingContainer<DTO,DATA, ENTITY, F_DTO, FD, FE>(childClass)
        where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity,
              F_DTO : ModelDTO,  FD: DataModel, FE : ExposifyEntity
{
    val delegateName get() = bindingDelegate.qualifiedName

    override val thisKey  = dto.dtoClass.generateKey(Cardinality.ONE_TO_MANY)
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY


    fun getDataModels(dataModel: DATA): List<FD>
        = bindingDelegate.getDataModels(dataModel)

    fun saveDataModels(dataModels:List<FD>)
            = bindingDelegate.saveDataModels(dataModels)

    fun attachForeignEntity(container: EntityUpdateContainer<FE, DTO, DATA, ENTITY>): Unit
        = bindingDelegate.attachForeignEntity(container)

    suspend fun processChildEntities(entity: ENTITY, processFn: suspend (List<FE>)-> List<CommonDTO<F_DTO, FD, FE>>)
     =   bindingDelegate.processForeignEntities(entity, processFn)

    fun getForeignEntities(entity: ENTITY):List<FE>
        = bindingDelegate.getForeignEntities(entity)

//    fun getChildEntities(entity: ENTITY): List<FE>
//        = bindingDelegate.getChildEntities(entity)
}


sealed class BindingContainer<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    val childClass: DTOBase<F_DTO, FD>,
) where DTO : ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity, F_DTO : ModelDTO,  FD: DataModel, FE : ExposifyEntity
{
    val  childConfig: DTOConfig<F_DTO, FD, FE>
        get() = childClass.config.castOrOperationsEx<DTOConfig<F_DTO, FD, FE>>()

    abstract val cardinality  : Cardinality
    abstract val thisKey : CompositeKey<DTOBase<DTO,*>, Cardinality>

}