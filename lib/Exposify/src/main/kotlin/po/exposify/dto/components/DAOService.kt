package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.components.proFErty_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.DTORegistryItem
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.interfaces.ComponentType
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.anotations.LogOnFault
import po.lognotify.classes.task.result.resultOrNull
import po.lognotify.extensions.subTask


class DAOService<DTO, DATA, ENTITY>(
    val dtoClass: DTOBase<DTO, DATA, ENTITY>,
    private val registryRecord: DTORegistryItem<DTO, DATA, ENTITY>,
): TasksManaged, IdentifiableComponent where DTO: ModelDTO, DATA: DataModel, ENTITY : LongEntity{

    val entityModel: ExposifyEntityClass<ENTITY> get() = dtoClass.config.entityModel

    @LogOnFault()
    override val qualifiedName: String = "DAOService[${registryRecord.dtoName}]"
    override val type: ComponentType = ComponentType.DaoService

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }

    private  fun buildConditions(conditions: SimpleQuery): Op<Boolean> {
        val conditions = conditions.build()
        return conditions
    }

    internal suspend fun setActiveEntity(dto: CommonDTO<DTO, DATA, ENTITY>,  container : EntityUpdateContainer<ENTITY, ModelDTO, DataModel, LongEntity>){
        container.insertedEntity(true)
        dto.updateBindingsAfterInserted(container)
    }

   suspend fun  pick(conditions :  SimpleQuery): ENTITY? = subTask("Pick"){handler->
       val opConditions = buildConditions(conditions)
       val queryResult = entityModel.find(opConditions).firstOrNull()
        if(queryResult == null){
            handler.warn("Entity  not found")
        }
        queryResult
    }.resultOrNull()

    suspend fun pickById(id: Long): ENTITY?
        = subTask("PickById") {handler->
      val entity =  entityModel.findById(id)
      if(entity == null){
          handler.info("Entity with id: $id not found")
      }
      entity
    }.resultOrNull()

    suspend fun select(): List<ENTITY> = subTask("Select All"){
        entityModel.all().toList()
    }.resultOrException()

    suspend fun select(conditions:  SimpleQuery): List<ENTITY> =
        subTask("Select") {handler->
        val opConditions = buildConditions(conditions)
        val result = entityModel.find(opConditions).toList()
        handler.info("${result.count()} entities selected")
        result
    }.resultOrException()

    suspend fun save(dto: CommonDTO<DTO, DATA, ENTITY>): ENTITY =
        subTask("Save") {handler->
            val updateMode = UpdateMode.MODEL_TO_ENTITY
            val newEntity = entityModel.new {
            handler.withTaskContext(this){
                dto.dtoPropertyBinder.update(this.containerize(updateMode))
            }
        }
        setActiveEntity(dto, newEntity.containerize(updateMode) )
        handler.info("Dao entity created with id ${newEntity.id.value} for dto ${dto.dtoName}")
        newEntity
    }.resultOrException()

    suspend fun <P_DTO: ModelDTO, PD: DataModel, PE: LongEntity> saveWithParent(
        dto: CommonDTO<DTO, DATA, ENTITY>,
        parentDto: CommonDTO<P_DTO, PD, PE>,
        bindFn: (container: EntityUpdateContainer<ENTITY, P_DTO, PD, PE>)-> Unit)
            = subTask("Save") {handler->
            val updateMode = UpdateMode.MODEL_TO_ENTITY
            val newEntity = entityModel.new {
                handler.withTaskContext(this){
                    val container = this.containerize(updateMode, parentDto)
                    dto.dtoPropertyBinder.update(container)
                    bindFn.invoke(container)
                }
            }
            setActiveEntity(dto, newEntity.containerize(updateMode))
            handler.info("Entity created with id: ${newEntity.id.value} for parent entity id:")
            newEntity
    }.resultOrException()

    suspend fun update(dto: CommonDTO<DTO, DATA, ENTITY>): ENTITY = subTask("Update") {handler->
        val selectedEntity =  pickById(dto.id).getOrOperationsEx("Entity with id : ${dto.id} not found", ExceptionCode.DB_CRUD_FAILURE)
        val updateMode = UpdateMode.MODEL_TO_ENTITY
        dto.dtoPropertyBinder.update(selectedEntity.containerize(updateMode))
        setActiveEntity(dto, selectedEntity.containerize(updateMode))
        selectedEntity
    }.resultOrException()

}