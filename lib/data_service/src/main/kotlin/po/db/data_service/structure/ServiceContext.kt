package po.db.data_service.structure

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.dto.*

class ServiceContext<DATA_MODEL, ENTITY>(
    val name : String,
    private val connection: Database,
    private val dtoModel : DTOClass<DATA_MODEL, ENTITY>,
    private val entityModel : LongEntityClass<ENTITY>,
   // val dtoModelFactory: (ServiceContext<DATA_MODEL, ENTITY>) -> AbstractDTOModel<DATA_MODEL>
)  where DATA_MODEL : DTOMarker, ENTITY : LongEntity
{
//    fun createModel(): AbstractDTOModel<DATA_MODEL> {
//        return dtoModelFactory(this) // Pass the context during creation
//    }

    private var modelConfiguration = ModelDTOConfig(dtoModel, entityModel)

    private  fun <T>configuration(conf: ModelDTOConfig<DATA_MODEL,ENTITY>, statement: ModelDTOConfig<DATA_MODEL, ENTITY>.() -> T): T =   statement.invoke(conf)

    fun  <T>config(serviceBody: ModelDTOConfig<DATA_MODEL, ENTITY>.() -> T): T = configuration(modelConfiguration) {
        serviceBody()
    }

    init {
          dtoModel.passContext(this)
    }

//    fun <T> T.configure(block: T.() -> Unit): T {
//        block() // `this` is the calling context (an instance of `T`)
//        return this
//    }


    fun saveDtoEntity(dtoEntity: AbstractDTOModel<DATA_MODEL, ENTITY>){

        val a = dtoEntity
        dbQuery{
         //   dtoModel.initChild()
         //   dtoModel.update()
        }
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