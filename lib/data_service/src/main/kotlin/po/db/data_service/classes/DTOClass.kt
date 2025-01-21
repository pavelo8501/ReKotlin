package po.db.data_service.classes

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import po.db.data_service.binder.BindingContainer
import po.db.data_service.binder.BindingKeyBase
import po.db.data_service.binder.UpdateMode
import po.db.data_service.components.eventhandler.RootEventHandler
import po.db.data_service.components.eventhandler.interfaces.CanNotify
import po.db.data_service.classes.components.DAOService
import po.db.data_service.classes.components.DTOConfig
import po.db.data_service.classes.components.Factory
import po.db.data_service.classes.interfaces.DTOInstance
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.CrudResult
import po.db.data_service.dto.CommonDTO
import kotlin.reflect.KClass

abstract class DTOClass<DATA, ENTITY>(
    val sourceClass: KClass<out CommonDTO<DATA, ENTITY>>
): DTOInstance, CanNotify  where DATA : DataModel, ENTITY : LongEntity{

    override val qualifiedName  = sourceClass.qualifiedName.toString()
    override val className  = sourceClass.simpleName.toString()
    override val eventHandler = RootEventHandler(className)

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

    fun initialization(
        beforeInit: ((DTOClass<DATA,ENTITY>) -> Unit)? = null,
        onDtoAfter: (DTOClass<DATA,ENTITY>.() -> Unit)?= null
    ) {
        beforeInit?.let {
            it(this)
        }
        setup()
        initialized = true
        onDtoAfter?.let {
            this.it()
        }
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
     * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
     *
     * @return A [CrudResult] containing a list of initialized DTOs and associated events.
     */
    fun select(): CrudResult<DATA, ENTITY> {
       val resultList = mutableListOf<CommonDTO<DATA, ENTITY>>()
       notify("select()"){
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
       }
       return CrudResult(resultList.toList(), eventHandler.getEvent())
    }

    /**
     * Selects all entities from the database, initializes DTOs for them, and returns a result containing these DTOs.
     *
     * @return A [CrudResult] containing a list of initialized DTOs and associated events.
     */
    fun <PARENT_DATA: DataModel, PARENT_ENTITY: LongEntity>update(dataModels: List<DATA>): CrudResult<DATA, ENTITY>{
        val resultDTOs = mutableListOf<CommonDTO<DATA, ENTITY>>()
        notify("create() count=${dataModels.count()}") {

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
        }
        return CrudResult(resultDTOs.toList(), eventHandler.getEvent())
    }

    /**
     * Deletes a given data model by first finding and initializing its DTO, then deleting it along with its bindings.
     *
     * @param dataModel The data model to delete.
     * @return A [CrudResult] containing a list of successfully deleted DTOs and associated events.
     */
    fun delete(dataModel: DATA): CrudResult<DATA, ENTITY>{
        val resultDTOs = mutableListOf<CommonDTO<DATA, ENTITY>>()
        notify("delete(dataModel.id = ${dataModel.id})") {
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

        }
        return CrudResult(resultDTOs.toList(), eventHandler.getEvent())
    }

}
