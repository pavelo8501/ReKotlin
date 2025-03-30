package po.exposify.dto.classes

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.classes.components.CallbackEmitter2
import po.exposify.classes.components.DAOService2
import po.exposify.classes.components.DTOConfig2
import po.exposify.classes.components.DTOFactory2
import po.exposify.classes.interfaces.DTOInstance
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem2
import po.exposify.exceptions.InitializationException
import po.exposify.exceptions.enums.InitErrorCodes
import po.exposify.scope.sequence.models.SequencePack2
import kotlin.reflect.KClass


abstract class DTOClass2<DTO>(): DTOInstance where DTO : ModelDTO{

    private var _regItem: DTORegistryItem2<DTO, *, *>? = null
    private val regItem : DTORegistryItem2<DTO, *, *>
        get() = _regItem?: throw InitializationException(
                "DTOClass regItem not initialized",
                InitErrorCodes.KEY_PARAM_UNINITIALIZED)
    val registryItem : DTORegistryItem2<DTO, *, *> by lazy { regItem }

    override val personalName: String = "DTOClass:${ _regItem?.dtoKClass?.simpleName?:"not_yet_initialized"}"

    internal val emitter = CallbackEmitter2<DTO>()

    var initialized: Boolean = false
    lateinit var config : DTOConfig2<DTO, *, *>

    val dtoFactory : DTOFactory2<DTO, *, *>
        get() = config.dtoFactory

   init {
       val stop = 10
   }

   protected abstract fun setup()

   internal fun <DATA : DataModel, ENTITY: LongEntity> applyConfig(
           initializedConfig : DTOConfig2<DTO, DATA, ENTITY>,
       ){
       this@DTOClass2._regItem = initializedConfig.dtoRegItem
       config = initializedConfig
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
//        childBindings.values.forEach {
//            it.childModel.getAssociatedTables(cumulativeList)
//        }
    }

    fun initialization(onRequestFn: ((CallbackEmitter2<DTO>) -> Unit)? = null) {
        try {
            if(!initialized){
                setup()
                onRequestFn?.invoke(emitter)
            }
        }catch (ex: Exception){
            throw ex
        }
    }

    suspend fun <DATA: DataModel, ENTITY: LongEntity> withTypedConfig(block: suspend DTOConfig2<DTO, DATA, ENTITY>.() -> Unit) {
        val cfg = config
        if (cfg.dtoRegItem.typeKeyDataEntity == registryItem.typeKeyDataEntity) {
            @Suppress("UNCHECKED_CAST") // Safe cast: typeKeyDataModel match ensures type compatibility
            (cfg as? DTOConfig2<DTO, DATA, ENTITY>)?.block()
        }
    }


    /**
     * Selects a single entity from the database based on the provided conditions and maps it to a DTO.
     *
     * This function performs the following steps:
     * 1. Calls the `daoService.pick` method to retrieve a single entity that matches the given conditions.
     * 2. If an entity is found, a new DTO (`DTOFunctions<DATA, ENTITY>`) is created using the factory.
     * 3. The DTO is updated with the entity's data using `UpdateMode.ENTITY_TO_MODEL`.
     * 4. Relation bindings are applied to the DTO via `conf.relationBinder.applyBindings(it)`.
     * 5. Repository initialization is performed on the DTO via `it.initializeRepositories(it.entityDAO)`.
     * 6. Returns a `CrudResult` containing an list of DTO entities and any events recorded during the process.
     *
     * @param conditions A list of property-value pairs (`KProperty1<DATA, *>, Any?`)
     * representing the filtering conditions.
     * @return A `CrudResult<DATA, ENTITY>` containing the selected DTO (if found) and any triggered events.
     */
//    internal suspend fun <T: IdTable<Long>>pick(
//        conditions: QueryConditions<T>): CrudResult<DATA, ENTITY> {
//        val resultList = mutableListOf<CommonDTO<DATA, ENTITY>>()
//        val entity =  daoService.pick(conditions.build())
//        entity?.let {
//            factory.createEntityDto()?.let {newDto->
//                newDto.updateBinding(it, UpdateMode.ENTITY_TO_MODEL)
//                resultList.add(newDto)
//            }
//        }
//        resultList.forEach {
//            conf.relationBinder.applyBindings(it)
//            it.initializeRepositories(it.entityDAO)
//        }
//        return CrudResult(resultList)
//    }

    /**
     * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
     *
     * @return A [CrudResult] containing a list of initialized DTOs and associated events.
     */
//    internal suspend fun select(): CrudResult<DATA, ENTITY> {
//        val resultList = mutableListOf<CommonDTO<DATA, ENTITY>>()
//        val entities = daoService.selectAll()
//        entities.forEach {
//            factory.createEntityDto()?.let {newDto->
//                newDto.updateBinding(it, UpdateMode.ENTITY_TO_MODEL)
//                resultList.add(newDto)
//            }
//        }
//        resultList.forEach {
//            conf.relationBinder.applyBindings(it)
//            it.initializeRepositories(it.entityDAO)
//        }
//        return CrudResult(resultList.toList())
//    }
//
//    internal suspend fun <T: IdTable<Long>> select(conditions: QueryConditions<T>): CrudResult<DATA, ENTITY> {
//        val resultList = mutableListOf<CommonDTO<DATA, ENTITY>>()
//        val entities = daoService.select(conditions.build())
//        entities.forEach {
//            factory.createEntityDto()?.let {newDto->
//                newDto.updateBinding(it, UpdateMode.ENTITY_TO_MODEL)
//                resultList.add(newDto)
//            }
//        }
//        resultList.forEach {
//            conf.relationBinder.applyBindings(it)
//            it.initializeRepositories(it.entityDAO)
//        }
//        return CrudResult(resultList.toList())
//    }


    /**
     * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
     *
     * @return A [CrudResult] containing a list of initialized DTOs and associated events.
     */
//    internal suspend fun <PARENT_DATA: DataModel, PARENT_ENTITY: LongEntity>update(
//        dataModels: List<DATA>): CrudResult<DATA, ENTITY>{
//
//        val resultDTOs = mutableListOf<CommonDTO<DATA, ENTITY>>()
//
//        dataModels.forEach { dataModel ->
//            factory.createEntityDto(dataModel)?.let { newDto ->
//                resultDTOs.add(newDto)
//            }
//        }
//        resultDTOs.filter { !it.isSaved }.forEach {
//            conf.relationBinder.applyBindings(it)
//            it.initializeRepositories()
//            it.updateRepositories()
//        }
//        resultDTOs.filter { it.isSaved }.forEach {
//            conf.relationBinder.applyBindings(it)
//            it.initializeRepositories()
//            it.updateRepositories()
//        }
//
//        return CrudResult(resultDTOs.toList())
//    }

    /**
     * Deletes a given data model by first finding and initializing its DTO, then deleting it along with its bindings.
     *
     * @param dataModel The data model to delete.
     * @return A [CrudResult] containing a list of successfully deleted DTOs and associated events.
     */
//    internal suspend fun delete(dataModel: DATA): CrudResult<DATA, ENTITY>{
//        val resultDTOs = mutableListOf<CommonDTO<DATA, ENTITY>>()
//        factory.createEntityDto(dataModel)?.let { newDto ->
//            resultDTOs.add(newDto)
//        }
//        resultDTOs.forEach {
//            daoService.selectWhere(it.id).let { entity ->
//                it.updateBinding(entity, UpdateMode.ENTITY_TO_MODEL)
//                conf.relationBinder.applyBindings(it)
//                it.initializeRepositories(it.entityDAO)
//                it.deleteInRepositories()
//            }
//        }
//        return CrudResult(resultDTOs.toList())
//    }


    suspend fun triggerSequence(
        sequence: SequencePack2<DTO>): Deferred<List<DataModel>> = emitter.launchSequence(sequence)

}