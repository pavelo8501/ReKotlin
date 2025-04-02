package po.exposify.scope.service

import kotlinx.coroutines.Deferred
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import po.exposify.classes.components.CallbackEmitter2
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.interfaces.AsClass
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.InitializationException
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.InitErrorCodes
import po.exposify.scope.connection.ConnectionClass2
import po.exposify.scope.sequence.models.SequencePack2
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.eventhandler.RootEventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import kotlin.collections.forEach

class ServiceClass2<DTO, DATA, ENTITY>(
    private val connectionClass : ConnectionClass2,
    private val rootDTOModel : DTOClass2<DTO>,
    private val serviceCreateOption: TableCreateMode? = null,
) : CanNotify, AsClass<DATA, ENTITY>  where  DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity {

    internal val connection : Database = connectionClass.connection

    var name : String = "undefined"
    var serviceContext : ServiceContext2<DTO, DATA>? = null

    override val eventHandler = RootEventHandler(name){
        echo(it, "ServiceClass: RootEventHandler")
    }

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
        pack : SequencePack2<DTO>): Deferred<List<DataModel>> {
        return  connectionClass.launchSequence<DTO>(pack)
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
                            InitErrorCodes.DB_TABLE_CREATION_FAILURE)
                    }
                }
            }
            true
        }catch (ex:Exception) {
            println(ex.message)
            throw ex
        }
    }

    private fun initializeDTOs(context: ServiceClass2<DTO, DATA, ENTITY>.() -> Unit ) {
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


    private fun  emitterSubscriptions(callbackEmitter : CallbackEmitter2<DTO>){
        callbackEmitter.subscribeSequenceExecute{
            launchSequence(it)
        }
    }


    private fun start(){
        initializeDTOs{
            rootDTOModel.initialization(::emitterSubscriptions)
            name =  ("${rootDTOModel.personalName}|Service").trim()
        }
        if(serviceCreateOption!=null){
            prepareTables(serviceCreateOption)
        }
    }


    fun launch(receiver: ServiceContext2<DTO, DATA>.() -> Unit){
        ServiceContext2(this, rootDTOModel).let {context->
            context.receiver()
            serviceContext = context
        }
    }
}