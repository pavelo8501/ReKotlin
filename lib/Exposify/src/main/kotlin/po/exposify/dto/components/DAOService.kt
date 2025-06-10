package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.models.ExposifyModule
import po.exposify.dto.models.ModuleType
import po.lognotify.TasksManaged
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.classes.task.result.resultOrNull
import po.lognotify.extensions.subTask
import po.misc.interfaces.IdentifiableModule
import po.misc.registries.type.TypeRegistry


class DAOService<DTO, DATA, ENTITY>(
    val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    private val registry: TypeRegistry,
    private val moduleType : ExposifyModule = ExposifyModule(ModuleType.ServiceClass, dtoClass.component)
): IdentifiableModule by moduleType, TasksManaged   where DTO: ModelDTO, DATA: DataModel, ENTITY : LongEntity{

    val entityModel: ExposifyEntityClass<ENTITY> get() = dtoClass.config.entityModel

    val taskConfig : TaskConfig get() {
       return TaskConfig(actor = dtoClass.component.componentName)
    }

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }

    private  fun buildConditions(conditions: SimpleQuery): Op<Boolean> {
        val conditions = conditions.build()
        return conditions
    }

   fun  pick(conditions :  SimpleQuery): ENTITY?
    = subTask("Pick", taskConfig){handler->
       val opConditions = buildConditions(conditions)
       val queryResult = entityModel.find(opConditions).firstOrNull()
        queryResult
    }.resultOrNull()

   fun pickById(id: Long): ENTITY?
        = subTask("PickById", taskConfig) {handler->
      val entity =  entityModel.findById(id)
      if(entity == null){
          handler.info("Entity with id: $id not found")
      }
      entity
    }.resultOrNull()

    fun select(): List<ENTITY> = subTask("Select All", taskConfig){
        entityModel.all().toList()
    }.resultOrException()


    fun select(conditions:  SimpleQuery): List<ENTITY>
        = subTask("Select", taskConfig) {handler->
        val opConditions = buildConditions(conditions)
        val result = entityModel.find(opConditions).toList()
        handler.info("${result.count()} entities selected")
        result
    }.resultOrException()


    fun save(block: (entity: ENTITY)-> Unit ): ENTITY =
        subTask("Save", taskConfig) {handler->
            val newEntity = entityModel.new {
                block.invoke(this)
        }
        handler.info("Dao entity created with id ${newEntity.id.value} for ${dtoClass.component.completeName}")
        newEntity
    }.onFail {
        val a = it
    }.resultOrException()


    fun saveWithParent(bindFn: (newEntity:ENTITY)-> Unit)
            = subTask("SaveWithParent", taskConfig) {handler->
            val newEntity = entityModel.new {
                bindFn(this)
            }
            handler.info("Entity created with id: ${newEntity.id.value} for ${dtoClass.component.completeName}")
            newEntity
    }.onFail{
        val dtoClass = dtoClass
        val a = it
    }.resultOrException()

    fun update(entityId: Long, updateFn: (newEntity:ENTITY)-> Unit): ENTITY?
            = subTask("Update", taskConfig) {handler->
        val selectedEntity =  pickById(entityId)
        if(selectedEntity != null){
            updateFn.invoke(selectedEntity)
            handler.info("Updated entity with id: ${selectedEntity.id.value} for ${dtoClass.component.completeName}")
        }else{
            handler.warn("Update failed. Entity with id: ${entityId} for ${dtoClass.component.completeName} can not be found")
        }
        selectedEntity
    }.resultOrException()

}