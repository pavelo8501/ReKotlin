package po.db.data_service.structure

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.constructors.ConstructorBlueprint
import po.db.data_service.dto.*
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.services.models.ServiceMetadata
import kotlin.reflect.KClass

class ServiceContext<DATA_MODEL, ENTITY>(
    val name : String,
    private val connection: Database,
    private val rootDtoModel : DTOClass<DATA_MODEL, ENTITY>,
    private val entityModel : LongEntityClass<ENTITY>,
)  where DATA_MODEL : DataModel, ENTITY : LongEntity {

    private var _metaData : ServiceMetadata<DATA_MODEL, ENTITY>? = null
    private val metaData : ServiceMetadata<DATA_MODEL, ENTITY>
        get(){
            if(_metaData == null){
                throw InitializationException("Service $name failed to initialize properly. MetaData missing", ExceptionCodes.NOT_INITIALIZED)
            }
            return this._metaData!!
        }

    var dataModelClass: KClass<DATA_MODEL>? = null

    private var modelConfiguration = ModelDTOConfig<DATA_MODEL,ENTITY>()
    private  fun <T>configuration(conf: ModelDTOConfig<DATA_MODEL,ENTITY>, statement: ModelDTOConfig<DATA_MODEL, ENTITY>.() -> T): T =   statement.invoke(conf)
    fun  <T>config(serviceBody: ModelDTOConfig<DATA_MODEL, ENTITY>.() -> T): T = configuration(modelConfiguration) {
        serviceBody()
    }

    private fun initializeDTO(entityDTO : AbstractDTOModel<DATA_MODEL,ENTITY>){
        if(entityDTO.id == 0L){
           val newEntity = dbQuery {
                return@dbQuery  entityModel.new {
                   // modelConfiguration.propertyBinder.updateProperties(entityDTO.dataModel, this)
                }
            }
            entityDTO.entityDAO = newEntity
        }
    }

    fun initialize(metaData : ServiceMetadata<DATA_MODEL, ENTITY>){
        this._metaData = metaData
        rootDtoModel.configuration()
        metaData.getBlueprint<DATA_MODEL>(rootDtoModel.dtoModelClassName)?.let {
            rootDtoModel.initialize(it, this)
        }?: throw  InitializationException("Service $name failed to initialize DTO Model Class", ExceptionCodes.NOT_INITIALIZED )
    }

//    fun <DATA_MODEL : DataModel, ENTITY : LongEntity>getDTOBlueprint(): ConstructorBlueprint<DATA_MODEL>{
//       // this.metaData.addBlueprint()
//    }

    fun <T : DTOClass<DATA_MODEL, ENTITY>> T.update(single:AbstractDTOModel<DATA_MODEL, ENTITY>, block: T.() -> Unit): Unit {
        this@ServiceContext.initializeDTO(single)
        this.block()
    }

    fun <DTO : DTOClass<DATA_MODEL, ENTITY>> DTO.update(list : List<AbstractDTOModel<DATA_MODEL, ENTITY>>,   block: DTO.() -> Unit): Unit {
        list.forEach { this@ServiceContext.initializeDTO(it) }
        this.block()
    }

    fun <DTO : DTOClass<DATA_MODEL, ENTITY>> DTO.select(block: DTO.(List<DATA_MODEL>) -> Unit): Unit {
        val result  = mutableListOf<DATA_MODEL>()
        dbQuery {
            entityModel.all().forEach {
                result.add(this.create(it))
            }
        }
        this.block(result)
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
   private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

}