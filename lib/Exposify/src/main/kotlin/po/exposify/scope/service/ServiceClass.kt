package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import po.exposify.classes.interfaces.DataModel
import po.exposify.common.interfaces.AsClass
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.safeCast
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.extensions.getOrThrowDefault
import kotlin.collections.set

class ServiceClass<DTO, DATA, ENTITY>(
    private val connectionClass : ConnectionClass,
    private val rootDTOModel : DTOClass<DTO>,
    private val serviceCreateOption: TableCreateMode = TableCreateMode.CREATE,
) :  AsClass<DATA, ENTITY>  where  DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity {

    internal val connection : Database = connectionClass.connection

    val name : String = "ServiceClass"
    var personalName : String = "[$name|${rootDTOModel.personalName}]"
    var serviceContext : ServiceContext<DTO, DATA>? = null

    private val sequences = mutableMapOf<String, SequencePack<DTO, DATA>>()

    init {
        try {
            start()
        }catch (initException : InitException){
            println(initException.message)
        }
    }

    private fun createTable(table : IdTable<Long>): Boolean{
        return try {
            if(!table.exists()) {
                SchemaUtils.create(table)
                return true
            }
            return false
        }catch (e: Exception){
            false
        }
    }

    private fun dropTables(tables : List<IdTable<Long>>): Boolean{
        val backwards = tables.reversed()
        return try {
            SchemaUtils.drop(*backwards.toTypedArray<IdTable<Long>>(), inBatch = true)
            tables.forEach {
                if(!createTable(it)){
                    throw InitException(
                        "Table ${it.schemaName} creation after drop failed",
                        ExceptionCode.DB_TABLE_CREATION_FAILURE)
                }
            }
            true
        }catch (ex:Exception) {
            println(ex.message)
            throw ex
        }
    }

    fun addSequencePack(pack: SequencePack<DTO,DATA>){
        sequences[pack.sequenceName()] = pack
    }

//    suspend fun runSequence(sequenceName: String, inputList: List<DATA> = emptyList()): List<DATA> {
//       val foundSequence =  sequences[sequenceName].getOrThrowDefault("Sequence name: $sequenceName not found")
//        foundSequence.saveInputList(inputList)
//       return connectionClass.launchSequence(foundSequence)
//    }

    suspend fun runSequence(sequenceKey: String): List<DATA> {
        val foundSequence =  sequences[sequenceKey].getOrThrowDefault("Sequence with key : $sequenceKey not found")
        return connectionClass.launchSequence(foundSequence)
    }

    suspend fun getSequenceHandler(sequenceName: String): SequenceHandler<DTO,DATA>{
        val foundSequence =  sequences[sequenceName].getOrThrowDefault("Sequence name: $sequenceName not found")
        val handler = foundSequence.getSequenceHandler()
        return handler
    }


    private fun initializeDTOs(context: ServiceClass<DTO, DATA, ENTITY>.() -> Unit ) {
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
            rootDTOModel.initialization()
        }
        prepareTables(serviceCreateOption)
    }

    fun launch(receiver: ServiceContext<DTO, DATA>.() -> Unit){
        val casted = safeCast<ServiceClass<DTO,DATA, ExposifyEntityBase>>()
            .getOrThrowDefault("Cast to ServiceClass<DTO,DATA, ExposifyEntityBase> failed")
        ServiceContext(casted, rootDTOModel).let { context->
            context.receiver()
            serviceContext = context
        }
    }
}