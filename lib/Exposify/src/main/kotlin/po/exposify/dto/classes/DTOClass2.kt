package po.exposify.dto.classes

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.components.CallbackEmitter2
import po.exposify.classes.components.DTOConfig2
import po.exposify.classes.components.DTOFactory2
import po.exposify.classes.interfaces.DTOInstance
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.classes.MapBuilder
import po.exposify.dto.CommonDTO2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem2
import po.exposify.exceptions.InitializationException
import po.exposify.exceptions.enums.InitErrorCodes
import po.exposify.scope.sequence.models.SequencePack2
import po.exposify.scope.service.ServiceContext2
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance


abstract class DTOClass2<DTO>(): DTOInstance where DTO : ModelDTO{

    private var _regItem: DTORegistryItem2<DTO, DataModel, LongEntity>? = null
    private val regItem : DTORegistryItem2<DTO, DataModel, LongEntity>
        get() = _regItem?: throw InitializationException(
                "DTOClass regItem not initialized",
                InitErrorCodes.KEY_PARAM_UNINITIALIZED)
    val registryItem : DTORegistryItem2<DTO, *, *> by lazy { regItem }

    override val personalName: String = "DTOClass:${ _regItem?.dtoKClass?.simpleName?:"not_yet_initialized"}"
    internal val emitter = CallbackEmitter2<DTO>()
    var initialized: Boolean = false
    lateinit var config : DTOConfig2<DTO, *, *>

    val dtoFactory : MapBuilder<String, DTOFactory2<DTO, DataModel, LongEntity>> = MapBuilder<String, DTOFactory2<DTO, DataModel, LongEntity>>()

    var serviceContextOwned: ServiceContext2<DTO, DataModel>? = null

   init {
       val stop = 10
   }

   protected abstract fun setup()

   internal fun <DATA : DataModel, ENTITY: LongEntity> applyConfig(
           initializedConfig : DTOConfig2<DTO, DATA, ENTITY>,
       ){
       config = initializedConfig
       this@DTOClass2._regItem = initializedConfig.dtoRegItem as DTORegistryItem2<DTO, DataModel, LongEntity>
       initialized = true
   }

    internal inline fun <reified DATA, reified ENTITY> configuration(
        dtoClass: KClass<out CommonDTO2<DTO, DATA, ENTITY>>,
        entityModel: LongEntityClass<ENTITY>,
        block: DTOConfig2<DTO, DATA, ENTITY>.() -> Unit
   ) where ENTITY: LongEntity, DATA: DataModel {
        val configuration = DTOConfig2(
            DTORegistryItem2<DTO, DATA, ENTITY>(
                dtoClass, DATA::class,  ENTITY::class,
                this@DTOClass2), entityModel, this)
        configuration.block()
        applyConfig(configuration)
    }

    fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>){
        cumulativeList.add(config.entityModel.table)
        config.childBindings.values.forEach {
            it.childModel.getAssociatedTables(cumulativeList)
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

    fun <DATA: DataModel>associateWithServiceContext(serviceContext: ServiceContext2<DTO, DATA>){
        if(serviceContextOwned == null){
            serviceContextOwned = serviceContext as  ServiceContext2<DTO, DataModel>
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