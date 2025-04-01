package po.exposify.dto.classes

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.components.CallbackEmitter2
import po.exposify.classes.components.DTOConfig2
import po.exposify.classes.components.safeCast
import po.exposify.classes.interfaces.DTOInstance
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.exceptions.InitializationException
import po.exposify.exceptions.enums.InitErrorCodes
import po.exposify.scope.sequence.models.SequencePack2
import po.exposify.scope.service.ServiceContext2
import kotlin.reflect.KClass

abstract class DTOClass2<DTO>(): DTOInstance where DTO : ModelDTO{

    private var _regItem: DTORegistryItem<DTO, DataModel, LongEntity>? = null
    private val regItem : DTORegistryItem<DTO, DataModel, LongEntity>
        get() = _regItem?: throw InitializationException(
                "DTOClass regItem not initialized",
                InitErrorCodes.KEY_PARAM_UNINITIALIZED)
    val registryItem : DTORegistryItem<DTO, DataModel, LongEntity> by lazy { regItem }

    override val personalName: String = "DTOClass:${ _regItem?.commonDTOKClass?.simpleName?:"not_yet_initialized"}"
    internal val emitter = CallbackEmitter2<DTO>()
    var initialized: Boolean = false
    lateinit var config : DTOConfig2<DTO, DataModel, LongEntity>

    var serviceContextOwned: ServiceContext2<DTO, DataModel>? = null

   init {
       val stop = 10
   }

   protected abstract fun setup()

   internal fun <DATA : DataModel, ENTITY: LongEntity> applyConfig(initializedConfig : DTOConfig2<DTO, DATA, ENTITY>) {
        initializedConfig.safeCast<DTOConfig2<DTO, DataModel, LongEntity>>()?.let {
            config = it
        }?: throw InitializationException("Safe cast failed for DTOConfig2", InitErrorCodes.CAST_FAILURE)

       initializedConfig.dtoRegItem.safeCast<DTORegistryItem<DTO, DataModel, LongEntity>>()?.let {
           _regItem = it
       }?: throw InitializationException("Safe cast failed for DTORegistryItem", InitErrorCodes.CAST_FAILURE)
       initialized = true
   }

    internal inline fun <reified DATA, reified ENTITY> configuration(
        dtoClass: KClass<out CommonDTO<DTO, DATA, ENTITY>>,
        entityModel: LongEntityClass<ENTITY>,
        block: DTOConfig2<DTO, DATA, ENTITY>.() -> Unit
   ) where ENTITY: LongEntity, DATA: DataModel {

       val newRegistryItem = DTORegistryItem<DTO, DATA, ENTITY>(dtoClass, DATA::class,  ENTITY::class, this@DTOClass2)
       val configuration = DTOConfig2(newRegistryItem, entityModel, this)
       configuration.block()
       applyConfig(configuration)
    }

    fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>){
        cumulativeList.add(config.entityModel.table)
        config.childBindings.values.forEach {
            it.childClass.getAssociatedTables(cumulativeList)
        }
    }

    fun initialization(onRequestFn: ((CallbackEmitter2<DTO>) -> Unit)? = null) {
        runCatching {
            if(!initialized){
                setup()
                onRequestFn?.invoke(emitter)
            }
        }.onFailure {
            println(it.message)
        }
    }


    fun <DATA: DataModel>asHierarchyRoot(serviceContext: ServiceContext2<DTO, DATA>){
        if(serviceContextOwned == null){
            serviceContext.safeCast<ServiceContext2<DTO, DataModel>>()?.let {
                serviceContextOwned = it

            }?: throw InitializationException("Cast for ServiceContext2 failed", InitErrorCodes.CAST_FAILURE)
        }
    }

    suspend fun <DATA: DataModel, ENTITY: LongEntity> withTypedConfig(block: suspend DTOConfig2<DTO, DATA, ENTITY>.() -> Unit) {

        val cfg = config
        if (cfg.dtoRegItem.typeKeyDataEntity == registryItem.typeKeyDataEntity) {
            @Suppress("UNCHECKED_CAST") // Safe cast: typeKeyDataModel match ensures type compatibility
            (cfg as? DTOConfig2<DTO, DATA, ENTITY>)?.block()
        }
    }

    suspend fun triggerSequence(
        sequence: SequencePack2<DTO>): Deferred<List<DataModel>> = emitter.launchSequence(sequence)

}