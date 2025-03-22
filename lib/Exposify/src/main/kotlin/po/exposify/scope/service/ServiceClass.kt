package po.exposify.scope.service

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.classes.DTOClass
import po.exposify.classes.components.CallbackEmitter
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.InitializationException
import po.exposify.exceptions.OperationsException
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.sequence.models.SequencePack
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import kotlin.Long

enum  class TableCreateMode{
    CREATE,
    FORCE_RECREATE
}

class ServiceClass<DATA, ENTITY>(
    private val connectionClass : ConnectionClass,
    private val rootDTOModel : DTOClass<DATA, ENTITY>,
    private val serviceCreateOption: TableCreateMode? = null,
) : CanNotify  where  DATA: DataModel, ENTITY : LongEntity {

   internal val connection : Database = connectionClass.connection

   var name : String = "undefined"
   var serviceContext : ServiceContext<DATA, ENTITY>? = null

   override val eventHandler = RootEventHandler(name)

    init {
        eventHandler.registerPropagateException<OperationsException> {
            OperationsException("Operations Exception", ExceptionCodes.REFLECTION_ERROR)
        }

        try {
            start()
        }catch (initException : InitializationException){
            println(initException.message)
        }
    }

    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    internal suspend fun launchSequence(
        pack : SequencePack<DATA, ENTITY>): Deferred<List<DATA>> {

       val result = task("Launch Sequence on ServiceClass with name :${name}") {
            connectionClass.launchSequence<DATA, ENTITY>(pack, eventHandler)
        }
        return result ?: CompletableDeferred(emptyList())
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


    private fun  emitterSubscriptions(callbackEmitter : CallbackEmitter<DATA, ENTITY>){
        callbackEmitter.subscribeSequenceExecute{
            launchSequence(it)
        }
    }


    private fun start(){
        initializeDTOs{
            rootDTOModel.initialization(::emitterSubscriptions)
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

   suspend fun launch(receiver: suspend ServiceContext<DATA, ENTITY>.() -> Unit){
       ServiceContext(this, rootDTOModel).let {context->
           context.receiver()
           serviceContext = context
       }
    }
}