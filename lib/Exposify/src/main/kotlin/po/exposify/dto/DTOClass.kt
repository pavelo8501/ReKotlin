package po.exposify.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.components.DTOConfig
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrInitEx
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.sequence.classes.RootSequenceHandler
import po.exposify.scope.sequence.classes.createHandler
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.models.RootSequencePack
import po.exposify.scope.service.ServiceClass
import po.exposify.scope.service.ServiceContext
import po.lognotify.TasksManaged
import po.lognotify.extensions.newTaskAsync
import po.misc.collections.Identifiable
import po.misc.collections.generateKey
import po.misc.types.castOrThrow
import po.misc.types.safeCast
import kotlin.reflect.KClass


abstract class RootDTO<DTO, DATA, ENTITY>()
    : DTOBase<DTO, DATA, ENTITY>(),  TasksManaged,  ClassDTO
        where DTO: ModelDTO, DATA: DataModel, ENTITY: LongEntity
{

    private var serviceContextOwned: ServiceContext<DTO, DATA, ENTITY>? = null
    val  serviceContext : ServiceContext<DTO, DATA, ENTITY> get()= serviceContextOwned.getOrInitEx("ServiceContext uninitialized",
        ExceptionCode.ABNORMAL_STATE)

    override val qualifiedName: String get() {
        return if(initialConfig != null){
            "RootDTO[${config.registry.dtoRootName}]"
        }else{
            "RootDTO[Uninitialized]"
        }
    }

    suspend fun initialization() {
        if (!initialized) setup()
    }

    fun setContextOwned(contextOwned: ServiceContext<DTO, DATA, ENTITY>){
        serviceContextOwned = contextOwned
    }

    fun getServiceClass(): ServiceClass<DTO, DATA, ENTITY>{
       return   serviceContextOwned?.serviceClass.getOrInitEx("ServiceClass not assigned for $qualifiedName")
    }

    inline fun <reified COMMON,  reified RD, reified RE> configuration(
        entityModel: ExposifyEntityClass<RE>,
        noinline block: suspend DTOConfig<COMMON, RD, RE>.() -> Unit
    ): Unit where COMMON : ModelDTO, RD : DataModel,   RE : LongEntity = newTaskAsync("DTO Configuration", qualifiedName) {

        val commonDTOClass =  COMMON::class.castOrInitEx<KClass<out CommonDTO<COMMON, RD, RE>>>(
            "KClass<out CommonDTO<DTO, DATA, ENTITY> cast failed")

        val derivedClass =  COMMON::class

        val newRegistryItem = DTORegistryItem(RD::class, RE::class, commonDTOClass, derivedClass)

        @Suppress("UNCHECKED_CAST") // Safe cast, to obtain reified class info
        val newConfiguration = DTOConfig(newRegistryItem, entityModel, this as DTOBase<COMMON, RD, RE>)
        newConfiguration.block()

        initialConfig = newConfiguration.castOrInitEx<DTOConfig<COMMON, RD, RE>>()
        initialized = true
    }.resultOrException()


    suspend fun runSequence(
        sequenceId: SequenceID,
        handlerBlock : suspend RootSequenceHandler<DTO, DATA, ENTITY>.()-> Unit
    ): List<DATA>{
        return withTransactionIfNone {
            val handler = createHandler(sequenceId)
            val serviceClass = getServiceClass()
            val pack = serviceClass.getSequencePack(generateKey(handler.sequenceId))
                .castOrThrow<RootSequencePack<DTO, DATA, ENTITY>, OperationsException>()

            val emitter = serviceClass.requestEmitter()
            emitter.dispatch<DTO, DATA, ENTITY, DTO, DATA, ENTITY, List<DATA>>(pack, handlerBlock, null)
        }
    }


    suspend fun <F_DTO: ModelDTO, FD : DataModel, FE: LongEntity> runSequence(
        sequenceId: SequenceID,
        childDtoClass: DTOClass<F_DTO, FD, FE>,
        handlerBlock : suspend RootSequenceHandler<DTO, DATA, ENTITY>.()-> Unit
    ): List<FD>{
        return withTransactionIfNone {
            val handler = createHandler(sequenceId)
            val serviceClass = getServiceClass()
            val pack = serviceClass.getSequencePack(generateKey(handler.sequenceId))
                .castOrThrow<RootSequencePack<DTO, DATA, ENTITY>, OperationsException>()
            val emitter = serviceClass.requestEmitter()
            emitter.dispatch<DTO, DATA, ENTITY, F_DTO, FD, FE, List<FD>>(pack, handlerBlock, childDtoClass)
        }
    }

}

abstract class DTOClass<DTO, DATA, ENTITY>(
    val  parentClass: DTOBase<*, *, *>,
): DTOBase<DTO, DATA, ENTITY>(), ClassDTO, TasksManaged where DTO: ModelDTO, DATA : DataModel, ENTITY: LongEntity {

    override val qualifiedName: String get() {
        return if(initialConfig != null){
            "DTOClass[${config.registry.dtoRootName}]"
        }else{
            "DTOClass[Uninitialized]"
        }
    }

    suspend fun initialization() {
        if (!initialized) setup()
    }


    inline fun <reified COMMON,  reified RD, reified RE> configuration(
        entityModel: ExposifyEntityClass<RE>,
        noinline block: suspend DTOConfig<COMMON, RD, RE>.() -> Unit
    ): Unit where COMMON : ModelDTO,  RE : LongEntity, RD : DataModel = newTaskAsync("DTO Configuration", qualifiedName) {

        val commonDTOClass =  COMMON::class.castOrInitEx<KClass<out CommonDTO<COMMON, RD, RE>>>(
            "KClass<out CommonDTO<DTO, DATA, ENTITY> cast failed")

        val derivedClass =  COMMON::class

        val newRegistryItem = DTORegistryItem(RD::class, RE::class, commonDTOClass, derivedClass)
        @Suppress("UNCHECKED_CAST") // Safe cast, to obtain reified class info
        val newConfiguration = DTOConfig(newRegistryItem, entityModel, this as DTOBase<COMMON, RD, RE>)
        newConfiguration.block()
        initialConfig =  newConfiguration.castOrInitEx<DTOConfig<COMMON, RD, RE>>()
        initialized = true
    }.resultOrException()

}


sealed class DTOBase<DTO, DATA, ENTITY>(): TasksManaged,  ClassDTO, Identifiable
        where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity{

   // abstract val config: DTOConfig<DTO, DATA, ENTITY>

    @PublishedApi
    internal var initialConfig: DTOConfig<DTO, DATA, ENTITY>? = null
    val config: DTOConfig<DTO, DATA, ENTITY>
        get() = initialConfig.getOrInitEx("DTOConfig uninitialized", ExceptionCode.LAZY_NOT_INITIALIZED)

    override var initialized: Boolean = false
    abstract override val qualifiedName: String

    protected abstract suspend fun  setup()

    fun getEntityModel(): ExposifyEntityClass<ENTITY> {
        return config.entityModel.castOrOperationsEx<ExposifyEntityClass<ENTITY>>()
    }

    fun <DTO: ModelDTO, D: DataModel, E: LongEntity> findHierarchyRoot(): RootDTO<DTO, D, E>{
        return when(this){
            is RootDTO<*, *, *>->{
                this.castOrInitEx()
            }

            is DTOClass<*, *, *>->{
                parentClass.findHierarchyRoot()
            }
        }
    }

    override fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>) {
        cumulativeList.add(config.entityModel.table)
        config.relationBinder.getChildClassList().forEach {
            it.getAssociatedTables(cumulativeList)
        }
    }

    internal fun <CHILD_DTO: ModelDTO>  lookupDTO(
        id: Long,
        childDtoClass: DTOBase<CHILD_DTO, *, *>
    ): CommonDTO<CHILD_DTO, DataModel, LongEntity>?
    {
        val dtos : MutableList<CommonDTO<DTO, DataModel, LongEntity>> = mutableListOf()
        if(this is RootDTO<*, *, *>){
            TODO("Not yet implemented")
           // dtos =  serviceContextOwned?.dtoMap?.values.toList()
        }
        return dtos.firstOrNull{ it.id == id }?.safeCast()
    }

}