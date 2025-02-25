package po.exposify.classes

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.binder.BindingContainer
import po.exposify.binder.BindingKeyBase
import po.exposify.binder.UpdateMode
import po.exposify.classes.components.CallbackEmitter
import po.exposify.classes.components.DAOService
import po.exposify.classes.components.DTOConfig
import po.exposify.classes.components.Factory
import po.exposify.classes.interfaces.DTOInstance
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException
import po.exposify.models.CrudResult
import po.exposify.dto.CommonDTO
import po.exposify.scope.sequence.classes.SequenceHandler
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

abstract class DTOClass<DATA, ENTITY>(
    val sourceClass: KClass<out CommonDTO<DATA, ENTITY>>
): DTOInstance, CanNotify  where DATA : DataModel, ENTITY : LongEntity{

    override val qualifiedName  = sourceClass.qualifiedName.toString()
    override val className  = sourceClass.simpleName.toString()

    override val eventHandler = RootEventHandler(className)

    internal val emitter = CallbackEmitter<DATA>()

    var initialized: Boolean = false
    val conf = DTOConfig<DATA, ENTITY>(this)

    val factory = Factory(this, sourceClass)

    val entityModel: LongEntityClass<ENTITY>
        get(){
            return  conf.entityModel?: throw OperationsException(
                "Unable read daoModel property on $className",
                ExceptionCodes.LAZY_NOT_INITIALIZED)
        }

    val daoService  =  DAOService<DATA, ENTITY>(this)

    val bindings = mutableMapOf<BindingKeyBase, BindingContainer<DATA, ENTITY, *, *>>()

    protected abstract fun setup()

    fun getAssociatedTables(cumulativeList: MutableList<IdTable<Long>>){
       cumulativeList.add(this.entityModel.table)
       bindings.values.forEach {
           it.childModel.getAssociatedTables(cumulativeList)
       }
    }

    fun initialization(onRequestFn: ((CallbackEmitter<DATA>) -> Unit)? = null) {
        setup()
        initialized = true
        onRequestFn?.invoke(emitter)
    }

    inline fun <reified DATA, reified ENTITY> DTOClass<DATA, ENTITY>.dtoSettings(
        entityModel: LongEntityClass<ENTITY>,
        block: DTOConfig<DATA,ENTITY>.() -> Unit) where ENTITY: LongEntity, DATA: DataModel
    {
        factory.initializeBlueprints(DATA::class, ENTITY::class)
        conf.dataModelClass = DATA::class
        conf.entityClass = ENTITY::class
        conf.entityModel = entityModel
        conf.block()
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
    internal suspend fun pick(
        conditions: List<Pair<KProperty1<DATA, *>, Any?>>): CrudResult<DATA, ENTITY> {
        val resultList = mutableListOf<CommonDTO<DATA, ENTITY>>()

        val entity =  daoService.pick(conditions, conf.propertyBinder.propertyList)
        entity?.let {
            factory.createEntityDto()?.let {newDto->
                newDto.update(it, UpdateMode.ENTITY_TO_MODEL)
                resultList.add(newDto)
            }
        }
        resultList.forEach {
            conf.relationBinder.applyBindings(it)
            it.initializeRepositories(it.entityDAO)
        }

        return CrudResult(resultList)
    }

    /**
     * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
     *
     * @return A [CrudResult] containing a list of initialized DTOs and associated events.
     */
    internal suspend fun select(): CrudResult<DATA, ENTITY> {
       val resultList = mutableListOf<CommonDTO<DATA, ENTITY>>()
       val entities = daoService.selectAll()
       entities.forEach {
           factory.createEntityDto()?.let {newDto->
               newDto.update(it, UpdateMode.ENTITY_TO_MODEL)
               resultList.add(newDto)
           }
       }
       resultList.forEach {
           conf.relationBinder.applyBindings(it)
           it.initializeRepositories(it.entityDAO)
       }

       return CrudResult(resultList.toList())
    }

    /**
     * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
     *
     * @return A [CrudResult] containing a list of initialized DTOs and associated events.
     */
    internal suspend fun <PARENT_DATA: DataModel, PARENT_ENTITY: LongEntity>update(
        dataModels: List<DATA>): CrudResult<DATA, ENTITY>{

        val resultDTOs = mutableListOf<CommonDTO<DATA, ENTITY>>()

        dataModels.forEach { dataModel ->
            factory.createEntityDto(dataModel)?.let { newDto ->
                resultDTOs.add(newDto)
            }
        }
        resultDTOs.filter { !it.isSaved }.forEach {
            conf.relationBinder.applyBindings(it)
            it.initializeRepositories()
            it.updateRepositories()
        }
        resultDTOs.filter { it.isSaved }.forEach {
            conf.relationBinder.applyBindings(it)
            it.initializeRepositories()
            it.updateRepositories()
        }

        return CrudResult(resultDTOs.toList())
    }

    /**
     * Deletes a given data model by first finding and initializing its DTO, then deleting it along with its bindings.
     *
     * @param dataModel The data model to delete.
     * @return A [CrudResult] containing a list of successfully deleted DTOs and associated events.
     */
    internal suspend fun delete(dataModel: DATA): CrudResult<DATA, ENTITY>{
        val resultDTOs = mutableListOf<CommonDTO<DATA, ENTITY>>()
        factory.createEntityDto(dataModel)?.let { newDto ->
            resultDTOs.add(newDto)
        }
        resultDTOs.forEach {
            daoService.selectWhere(it.id).let { entity ->
                it.update(entity, UpdateMode.ENTITY_TO_MODEL)
                conf.relationBinder.applyBindings(it)
                it.initializeRepositories(it.entityDAO)
                it.deleteInRepositories()
            }
        }
        return CrudResult(resultDTOs.toList())
    }

    suspend fun triggerSequence(handler: SequenceHandler<DATA>, list: List<DATA>): Deferred<List<DATA>> {
        return emitter.launchSequence(handler, list)
    }

}
