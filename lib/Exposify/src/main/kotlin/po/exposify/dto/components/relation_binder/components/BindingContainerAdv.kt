package po.exposify.dto.components.relation_binder.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.components.relation_binder.delegates.OneToManyDelegate
import po.exposify.dto.components.relation_binder.delegates.OneToManyDelegateAdv
import po.exposify.dto.components.relation_binder.delegates.OneToOneDelegate
import po.exposify.dto.components.relation_binder.delegates.OneToOneDelegateAdv
import po.exposify.dto.components.relation_binder.delegates.RelationBindingDelegate
import po.exposify.dto.components.relation_binder.delegates.RelationBindingDelegateAdv
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.misc.collections.CompositeEnumKey
import po.misc.collections.CompositeKey
import po.misc.collections.generateKey
import po.misc.types.castOrThrow

private fun <DTO, DATA, ENTITY, F_DTO, FD, FE> DTOClass<F_DTO, FD, FE>.createOneToManyContainerAdv(
    dtoClass: DTOBase<DTO, DATA, ENTITY>,
    bindingDelegate : OneToManyDelegateAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>
): MultipleChildContainerAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO : ModelDTO,  FD: DataModel, FE : LongEntity
{

    val container =  MultipleChildContainerAdv(dtoClass, this, bindingDelegate)
    dtoClass.config.relationBinder.addContainerAdv(container)
    return   MultipleChildContainerAdv(dtoClass, this, bindingDelegate)
}

fun <DTO, DATA, ENTITY, F_DTO, FD, FE> DTOClass<F_DTO, FD, FE>.getOrCreateOneToManyContainer(
    dtoClass: DTOBase<DTO, DATA, ENTITY>,
    bindingDelegate : OneToManyDelegateAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>
): MultipleChildContainerAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO : ModelDTO,  FD: DataModel, FE : LongEntity
{
   val container = dtoClass.config.relationBinder.containers[this.generateKey(Cardinality.ONE_TO_MANY)]?:
   createOneToManyContainerAdv(dtoClass,bindingDelegate)
   val casted =   container.castOrInitEx<MultipleChildContainerAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>>("Unable to cast Binding Container")
    return casted
}

sealed interface BindingContainerAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>
     where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity, F_DTO : ModelDTO,  FD: DataModel, FE : LongEntity
{

    val dtoClass: DTOBase<DTO, DATA, ENTITY>
    val childClass: DTOClass<F_DTO, FD, FE>
    val bindingDelegate : RelationBindingDelegateAdv<DTO, DATA, ENTITY, F_DTO, FD, FE, *>

    val  childConfig: DTOConfig<F_DTO, FD, FE>
        get() = childClass.config.castOrOperationsEx<DTOConfig<F_DTO, FD, FE>>()

    fun getForeignEntity(id: Long): FE?
            = bindingDelegate.getForeignEntity(id)

    val cardinality  : Cardinality
    val thisKey : CompositeEnumKey<DTOClass<F_DTO, FD, FE>, Cardinality>

}



class SingleChildContainerAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>  (
    override val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    override val childClass: DTOClass<F_DTO, FD, FE>,
    override val bindingDelegate : OneToOneDelegateAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>
): BindingContainerAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO : ModelDTO,  FD: DataModel, FE : LongEntity
{
    override val thisKey = childClass.generateKey(Cardinality.ONE_TO_ONE)
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY

    fun getDataModel(dataModel: DATA): FD
            = bindingDelegate.getDataModel(dataModel)

    fun saveDataModel(dataModel:FD)
            = bindingDelegate.saveDataModel(dataModel)

    fun attachForeignEntity(container: EntityUpdateContainer<FE, DTO, DATA, ENTITY>): Unit
            = bindingDelegate.attachForeignEntity(container)


    fun getForeignEntity(entity: ENTITY): FE
            = bindingDelegate.getForeignEntity(entity)

    fun getDto(){
        bindingDelegate.getEffectiveValue()
    }
}


class MultipleChildContainerAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>(
    override val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    override val childClass: DTOClass<F_DTO, FD, FE>,
    override val bindingDelegate : OneToManyDelegateAdv<DTO, DATA, ENTITY, F_DTO, FD, FE>
): BindingContainerAdv<DTO,DATA, ENTITY, F_DTO, FD, FE>
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity,
              F_DTO : ModelDTO,  FD: DataModel, FE : LongEntity
{

    val delegateName get() = bindingDelegate.qualifiedName

    override val thisKey  = childClass.generateKey(Cardinality.ONE_TO_MANY)
    override val cardinality: Cardinality = Cardinality.ONE_TO_MANY


    fun getDataModels(dataModel: DATA): List<FD>
        = bindingDelegate.getDataModels(dataModel)

    fun saveDataModels(dataModels:List<FD>)
            = bindingDelegate.saveDataModels(dataModels)

    fun saveDataModel(dataModel:FD)
            = bindingDelegate.saveDataModels(listOf(dataModel))

    fun attachToForeignEntity(container: EntityUpdateContainer<FE, DTO, DATA, ENTITY>): Unit
        = bindingDelegate.attachToForeignEntity(container)

//    suspend fun processChildEntities(entity: ENTITY, processFn: suspend (List<FE>)-> List<CommonDTO<F_DTO, FD, FE>>)
//     =   bindingDelegate.processForeignEntities(entity, processFn)

    fun getForeignEntities(entity: ENTITY):List<FE>
        = bindingDelegate.getForeignEntities(entity)
}

