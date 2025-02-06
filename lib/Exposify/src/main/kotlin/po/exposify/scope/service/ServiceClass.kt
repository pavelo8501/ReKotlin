package po.exposify.scope.service

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.components.logger.LoggingService
import po.db.data_service.components.logger.enums.LogLevel
import po.db.data_service.classes.DTOClass
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.InitializationException
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.scope.connection.ConnectionClass
import kotlin.Long

enum  class TableCreateMode{
    CREATE,
    FORCE_RECREATE
}

class ServiceClass<DATA, ENTITY>(
    private val connectionClass : ConnectionClass,
    private val rootDTOModel : DTOClass<DATA, ENTITY>,
    private val serviceCreateOption: TableCreateMode? = null
)  where  DATA: DataModel, ENTITY : LongEntity{

   val connection : Database = connectionClass.connection

   var name : String = "undefined"
   val logger = LoggingService()
   var serviceContext : ServiceContext<DATA, ENTITY>? = null

   init {
        try {
            runBlocking {
                logger.registerLogFunction(LogLevel.MESSAGE){msg, level, time, throwable->
                    println("Service${name} Logger|${msg}|${time}")
                }
            }

            start()
        }catch (initException : InitializationException){
            println(initException.message)
        }
    }

    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    private fun launchSequence(name: String){

        println("Launch Sequence on ServiceClass with name :${name}")

        serviceContext?.sequences2?.values?.firstOrNull{ it.name ==  name}?.let{pack->
            println("Found Pack  :${pack.name}")
            connectionClass.launchSequence<DATA,ENTITY>(pack)

        }?:run {
            throw OperationsException("Sequence not found", ExceptionCodes.NOT_INITIALIZED)
        }
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
                       throw InitializationException(
                           "Table ${it.schemaName} creation after drop failed",
                           ExceptionCodes.DB_TABLE_CREATION_FAILURE)
                   }
                }
            }
            true
        }catch (ex:Exception) {
            println(ex.message)
            throw ex
        }
    }

    private fun initializeDTOs(context: ServiceClass<DATA,ENTITY>.() -> Unit ) {
        context.invoke(this)
    }

    private fun prepareTables(serviceCreateOption : TableCreateMode){
        val tableList = mutableListOf<IdTable<Long>>()
        rootDTOModel.getAssociatedTables(tableList)
        when(serviceCreateOption){
            TableCreateMode.CREATE->{
                tableList.forEach {
                    createTable(it)
                }
            }
            TableCreateMode.FORCE_RECREATE->{
                dropTables(tableList)
            }
        }
    }

    private fun start(){
        initializeDTOs{
            rootDTOModel.initialization(){
                emitter.onSequenceLaunch ={
                    launchSequence(it)
                }
            }
            name =  ("${rootDTOModel.className}|Service").trim()
        }
        if(serviceCreateOption!=null){
            prepareTables(serviceCreateOption)
        }
    }

    fun <DATA: DataModel, ENTITY: LongEntity> attachToContext(
        dtoModel : DTOClass<DATA, ENTITY>,
        context:  ServiceContext<DATA, ENTITY>.() -> Unit
    ): Boolean {
        serviceContext?.let {serviceCtx->
            if (rootDTOModel::class.isInstance(dtoModel)) {
                serviceCtx.also {
                    @Suppress("UNCHECKED_CAST")
                    context.invoke(serviceCtx as ServiceContext<DATA, ENTITY>)
                    return true
                }
            }
        }
        return false
    }

    fun launch(receiver: ServiceContext<DATA, ENTITY>.() -> Unit){
       ServiceContext(connection, rootDTOModel).let {context->
           context.receiver()
           serviceContext = context
       }
    }
}