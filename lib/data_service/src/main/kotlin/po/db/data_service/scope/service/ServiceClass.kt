package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.constructors.ClassBlueprintContainer
import po.db.data_service.constructors.ConstructorBuilder
import po.db.data_service.dto.DTOClass
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.scope.service.models.SequenceTag
import kotlin.reflect.KClass

enum  class TableCreateMode{
    CREATE,
    FORCE_RECREATE
}

class ServiceClass<ENTITY>(
    private val connection :Database,
    private val rootDTOModel : DTOClass<ENTITY>,
    private val serviceCreateOption: TableCreateMode? = null
)  where  ENTITY : LongEntity{

   companion object :  ConstructorBuilder()

   var name : String = "undefined"

    init {
        try {
            start()
        }catch (initException : InitializationException){
            println(initException.message)
        }
    }

    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }



    private fun createTable(table : IdTable<Long>): Boolean{
        return try {
            dbQuery {
                if(!table.exists()) {
                    SchemaUtils.create(table)
                    return@dbQuery true
                }
                return@dbQuery false
            }
        }catch (e: Exception){
            false
        }
    }

    private fun dropTables(tables : List<IdTable<Long>>): Boolean{
        val backwards = tables.reversed()
        return try {
            dbQuery {
                SchemaUtils.drop(*backwards.toTypedArray<IdTable<Long>>(), inBatch = true)
                tables.forEach {
                   if(!createTable(it)){
                       throw InitializationException("Table ${it.schemaName} creation after drop failed", ExceptionCodes.DB_TABLE_CREATION_FAILURE)
                   }
                }
            }
            true
        }catch (ex:Exception) {
            false
        }
    }

    private fun initializeDTO(context: ServiceClass<ENTITY>.() -> Unit ) {
        context.invoke(this)
    }

    private fun prepareTables(serviceCreateOption : TableCreateMode){
        val tables = rootDTOModel.getAssociatedTables()
        when(serviceCreateOption){
            TableCreateMode.CREATE->{
                tables.forEach {
                    createTable(it)
                }
            }
            TableCreateMode.FORCE_RECREATE->{
                dropTables(tables)
            }
        }
    }

    private fun start(){
        initializeDTO{
            rootDTOModel.initialization(connection)
//            rootDTOModel.i(){
//                it.conf.getBlueprinting(this)
//            }
            name = " ${rootDTOModel.className}|Service"
        }
        if(serviceCreateOption!=null){
            prepareTables(serviceCreateOption)
        }
    }

    fun launch(receiver: ServiceContext<ENTITY>.()->Unit ){
        if(connection!=null){
            val serviceContext = ServiceContext(connection, rootDTOModel)
            serviceContext.receiver()
        }
    }

}