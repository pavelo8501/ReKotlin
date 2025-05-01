package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.EntityUpdateContainer
import po.exposify.dto.components.property_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.WhereCondition
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.extensions.resultOrNull
import po.lognotify.extensions.subTask


class DAOService<DTO, ENTITY>(
    private val hostingDTO: CommonDTO<DTO, *, ENTITY>,
    val entityModel: LongEntityClass<ENTITY>,
): TasksManaged where DTO: ModelDTO, ENTITY : ExposifyEntity{

    private val personalName = "DAOService[${hostingDTO.personalName}]"
    var entity : ENTITY? = null
        private set

    internal fun getLastEntity():ENTITY{
        return entity.getOrOperationsEx("Entity is null", ExceptionCode.NOT_INITIALIZED)
    }

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }

    private  fun  <T:IdTable<Long>> buildConditions(conditions: WhereCondition<T>): Op<Boolean> {
        val conditions = conditions.build()
        return conditions
    }

    internal suspend fun setActiveEntity(updateMode : UpdateMode, insertedEntity:ENTITY){
        entity = insertedEntity
        hostingDTO.dataModel.id = insertedEntity.id.value
        hostingDTO.updateBindingAfterInserted(updateMode, insertedEntity.containerize())
    }

   suspend fun  <T:IdTable<Long>> pick(conditions :  WhereCondition<T>): ENTITY? = subTask("Pick", personalName){handler->
       val opConditions = buildConditions(conditions)
       val queryResult = entityModel.find(opConditions).firstOrNull()
        if(queryResult == null){
            handler.warn("Entity  not found")
        }
        queryResult
    }.resultOrNull()

    suspend fun pickById(id: Long): ENTITY? =  subTask("PickById", personalName) {handler->
      val entity =  entityModel.findById(id)
      if(entity == null){
          handler.warn("Entity with id: $id not found")
      }
      entity
    }.resultOrNull()


    suspend fun select(): List<ENTITY> = subTask("Select All", personalName){
        entityModel.all().toList()
    }.resultOrException()

    suspend fun <T:IdTable<Long>> select(conditions:  WhereCondition<T>): List<ENTITY> =
        subTask("Select", personalName) {handler->
        val opConditions = buildConditions(conditions)
        val result = entityModel.find(opConditions).toList()
        handler.info("${result.count()} entities selected")
        result
    }.resultOrException()

    suspend fun save(): ENTITY =
        subTask("Save", "DAOService") {handler->
            val updateMode = UpdateMode.MODEL_TO_ENTITY

        val newEntity = entityModel.new {
            handler.withTaskContext(this){
                hostingDTO.updateBinding(this, updateMode, this.containerize())
            }
        }
        setActiveEntity(updateMode, newEntity)
        handler.info("Dao entity created with id ${newEntity.id.value} for dto ${hostingDTO.personalName}")
        newEntity
    }.resultOrException()

    suspend fun <P_DTO: ModelDTO, PD: DataModel, PE: ExposifyEntity> saveWithParent(
        parent: PE,
        parentDto: CommonDTO<P_DTO, PD, PE>,
        bindFn: (newEntity:ENTITY)-> Unit):ENTITY
            = subTask("Save", personalName) {handler->

            val updateMode = UpdateMode.MODEL_TO_ENTITY
            val newEntity = entityModel.new {
                handler.withTaskContext(this){
                    bindFn.invoke(this)
                    val container =  EntityUpdateContainer<ENTITY, P_DTO, PD, PE>(this).setParentData(parent, parentDto)
                    hostingDTO.updateBinding(this, updateMode,  container)
                }
            }
            setActiveEntity(updateMode, newEntity)
            handler.info("Entity created with id: ${newEntity.id.value} for parent entity id:")
            newEntity
    }.resultOrException()

    suspend fun update(): ENTITY = subTask("Update", "DAOService") {handler->
        val selectedEntity =  pickById(hostingDTO.id).getOrOperationsEx("Entity with id : ${hostingDTO.id} not found", ExceptionCode.DB_CRUD_FAILURE)
        val updateMode = UpdateMode.MODEL_TO_ENTITY
        hostingDTO.updateBinding(selectedEntity, updateMode, selectedEntity.containerize())
        setActiveEntity(updateMode, selectedEntity)
        selectedEntity
    }.resultOrException()

}