package po.db.data_service.structure

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.*
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.models.NotificationEvent
import po.db.data_service.services.models.ServiceMetadata
import kotlin.reflect.KClass

class ServiceContext<DATA_MODEL : DataModel, ENTITY : LongEntity>(
    val name : String,
    private val rootDtoModel : DTOClass<DATA_MODEL, ENTITY>,
    private val dbConnection: Database,
    private val connectionContext : ConnectionContext,
) {
    companion object : ConstructorBuilder()

    var state : ContextState = ContextState.UNINITIALIZED
        set(value){
            if(field != value){
                field = value
                when(field){
                    ContextState.INITIALIZED->{
                        initialize(metaData)
                    }
                    else->{}
                }
            }
        }

    private var _metaData : ServiceMetadata<DATA_MODEL, ENTITY>? = null
    private val metaData : ServiceMetadata<DATA_MODEL, ENTITY>
        get(){
            return _metaData?: throw InitializationException("Service $name failed to initialize properly. MetaData missing", ExceptionCodes.NOT_INITIALIZED)
        }
    fun setServiceMetadata(meta : ServiceMetadata<DATA_MODEL, ENTITY>){
        _metaData  = meta
        state = ContextState.INITIALIZED
    }

    var dataModelClass: KClass<DATA_MODEL>? = null

    private var modelConfiguration = ModelDTOConfig<DATA_MODEL,ENTITY>()
    private  fun <T>configuration(conf: ModelDTOConfig<DATA_MODEL,ENTITY>, statement: ModelDTOConfig<DATA_MODEL, ENTITY>.() -> T): T =   statement.invoke(conf)
    fun  <T>config(serviceBody: ModelDTOConfig<DATA_MODEL, ENTITY>.() -> T): T = configuration(modelConfiguration) {
        serviceBody()
    }

    init {

    }

    private fun handleDtoInitialization(outer : DTOClassOuterContext<DATA_MODEL, ENTITY>, inner : DTOClassInnerContext<DATA_MODEL, ENTITY> ){
        val modelBlueprint = getConstructorBlueprint(outer.dataModelClass)
        metaData.addModelBlueprint(outer.dataModelClass, modelBlueprint)
        val dtoBlueprint =  getConstructorBlueprint(outer.dtoModelClass)
        metaData.addDtoBlueprint(outer.dtoModelClass, dtoBlueprint)
        inner.setBlueprints(dtoBlueprint, modelBlueprint)
    }


//    private fun initDTO(entityDTO : AbstractDTOModel<DATA_MODEL,ENTITY>){
//        if(entityDTO.id == 0L){
//           val newEntity = dbQuery {
//                return@dbQuery  entityDTO. .new {
//                   // modelConfiguration.propertyBinder.updateProperties(entityDTO.dataModel, this)
//                }
//            }
//            entityDTO.setEntityDAO(newEntity)
//        }
//    }

    fun initialize(metaData : ServiceMetadata<DATA_MODEL, ENTITY>){
        rootDtoModel.initializeDTO(this) {
            if(outerContext.state == ContextState.INITIALIZED){
                handleDtoInitialization(this.outerContext, this)
            }else{
                outerContext.notificator.subscribe("ServiceContext", NotificationEvent.ON_INITIALIZED){
                    handleDtoInitialization(this.outerContext, this)
                }
            }
        }
    }

//    fun <DATA_MODEL : DataModel, ENTITY : LongEntity>getDTOBlueprint(): ConstructorBlueprint<DATA_MODEL>{
//       // this.metaData.addBlueprint()
//    }

    fun <T : DTOClass<DATA_MODEL, ENTITY>> T.update(single:AbstractDTOModel<DATA_MODEL, ENTITY>, block: T.() -> Unit): Unit {
      // this@ServiceContext.initDTO(single)
        this.block()
    }

    fun <DTO : DTOClass<DATA_MODEL, ENTITY>> DTO.update(list : List<AbstractDTOModel<DATA_MODEL, ENTITY>>,   block: DTO.() -> Unit): Unit {
       // list.forEach { this@ServiceContext.initDTO(it) }
        this.block()
    }

    fun <DATA_MODEL : DataModel, ENTITY: LongEntity> DTOClass<DATA_MODEL, ENTITY>.select(block: DTOClass<DATA_MODEL, ENTITY>.(List<DATA_MODEL>) -> Unit): Unit {

        val result  = mutableListOf<CommonDTO<DATA_MODEL, ENTITY>>()
        dbQuery {
            this.daoEntityModel.all().forEach {
                result.add(this.create(it))
            }
        }

      //  return result
    }



//    private fun getDependantTables(forTable:IdTable<Long>): List<IdTable<Long>> {
//        val dependant = dtoCompanion.parentEntityDTO.registeredTables.filter { it.foreignKeys.map { keyMap->keyMap.targetTable.tableName }.contains(forTable.tableName) }
//        return dependant
//    }
//
//    fun dropTable(table : IdTable<Long>, allTables : List<IdTable<Long>> = emptyList() ): Boolean{
//        try {
//            return  dbQuery {
//                var result : Boolean = false
//                if(table.exists()) {
//                    val dependantTables = getDependantTables(table)
//                    if(dependantTables.isNotEmpty()){
//                        dependantTables.forEach{
//                            dropTable(it, allTables)
//                        }
//                        SchemaUtils.drop(table)
//                        result = true
//                    }else{
//                        SchemaUtils.drop(table)
//                        result = true
//                    }
//                }else{
//                    result = true
//                }
//                result
//            }
//        }catch (e: Exception){
//            return false
//        }
//    }
//    fun createTable(table : IdTable<Long>): Boolean{
//        return try {
//            dbQuery {
//                if(!table.exists()) {
//                    SchemaUtils.create(table)
//                    val dependantTables = getDependantTables(table)
//                    dependantTables.forEach{
//                        createTable(it)
//                    }
//                }
//                true
//            }
//        }catch (e: Exception){
//            false
//        }
//    }
//
   private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

}