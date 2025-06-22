package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.lognotify.classes.action.InlineAction
import po.lognotify.classes.action.runInlineAction
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.extensions.subTask
import po.misc.registries.type.TypeRegistry


class DAOService<DTO, DATA, ENTITY>(
    val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    private val registry: TypeRegistry,
): InlineAction   where DTO: ModelDTO, DATA: DataModel, ENTITY : LongEntity{

    override val contextName: String = "DAOService"

    val entityModel: ExposifyEntityClass<ENTITY> get() = dtoClass.config.entityModel

    val taskConfig : TaskConfig get() {
       return TaskConfig(actor = dtoClass.completeName)
    }

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }

    private  fun buildConditions(conditions: SimpleQuery): Op<Boolean> {
        val conditions = conditions.build()
        return conditions
    }

   fun  pick(conditions :  SimpleQuery): ENTITY?
    = runInlineAction("Pick"){handler->
       val opConditions = buildConditions(conditions)
       val queryResult = entityModel.find(opConditions).firstOrNull()
        queryResult
    }

   fun pickById(id: Long): ENTITY?
        = runInlineAction("PickById") {handler->
      val entity =  entityModel.findById(id)
      if(entity == null){
          handler.info("Entity with id: $id not found")
      }
      entity
    }

    fun select(): List<ENTITY> = runInlineAction("Select All"){
        entityModel.all().toList()
    }

    fun select(conditions:  SimpleQuery): List<ENTITY>
        = runInlineAction("Select") { handler ->
        val opConditions = buildConditions(conditions)
        val result = entityModel.find(opConditions).toList()
        handler.info("${result.count()} entities selected")
        result
    }


    fun save(block: (entity: ENTITY)-> Unit ): ENTITY =
        runInlineAction("Save") {handler->
            val newEntity = entityModel.new {
                block.invoke(this)
        }
        handler.info("Dao entity created with id ${newEntity.id.value} for ${dtoClass.completeName}")
        newEntity
    }


    fun saveWithParent(bindFn: (newEntity:ENTITY)-> Unit)
            = runInlineAction("SaveWithParent") {handler->
            val newEntity = entityModel.new {
                bindFn(this)
            }
            handler.info("Entity created with id: ${newEntity.id.value} for ${dtoClass.completeName}")
            newEntity
    }

    fun update(entityId: Long, updateFn: (newEntity:ENTITY)-> Unit): ENTITY?
            = subTask("Update", taskConfig) {handler->
        val selectedEntity =  pickById(entityId)
        if(selectedEntity != null){
            updateFn.invoke(selectedEntity)
            handler.info("Updated entity with id: ${selectedEntity.id.value} for ${dtoClass.completeName}")
        }else{
            handler.warn("Update failed. Entity with id: ${entityId} for ${dtoClass.completeName} can not be found")
        }
        selectedEntity
    }.resultOrException()

}