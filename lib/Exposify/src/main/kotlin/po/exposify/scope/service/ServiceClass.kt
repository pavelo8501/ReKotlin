package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.interfaces.AsClass
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.sequence.enums.SequenceID
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import po.misc.collections.CompositeKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class ServiceClass<DTO, DATA, ENTITY>(
    private val connectionClass : ConnectionClass,
    private val serviceCreateOption: TableCreateMode = TableCreateMode.CREATE,
):  AsClass<DATA, ENTITY>, TasksManaged  where  DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity {

    private lateinit var serviceContext: ServiceContext<DTO, DATA, ENTITY>

    var personalName: String = "ServiceClass[Uninitialized]"
        private set

    internal val connection: Database = connectionClass.connection
    private val sequences = ConcurrentHashMap<CompositeKey<RootDTO<*, *, *>, SequenceID>, SequencePack<*, *, *>>()

    private fun createTable(table: IdTable<Long>): Boolean {
        return try {
            if (!table.exists()) {
                SchemaUtils.create(table)
                return true
            }
            return false
        } catch (e: Exception) {
            false
        }
    }

    private fun dropTables(tables: List<IdTable<Long>>): Boolean {
        val backwards = tables.reversed()
        return try {
            SchemaUtils.drop(*backwards.toTypedArray<IdTable<Long>>(), inBatch = true)
            tables.forEach {
                if (!createTable(it)) {
                    throw InitException(
                        "Table ${it.schemaName} creation after drop failed",
                        ExceptionCode.DB_TABLE_CREATION_FAILURE
                    )
                }
            }
            true
        } catch (ex: Exception) {
            println(ex.message)
            throw ex
        }
    }
    private fun prepareTables(serviceCreateOption: TableCreateMode, rootDTOModel: RootDTO<DTO, DATA, ENTITY>) {
        val tableList = mutableListOf<IdTable<Long>>()
        rootDTOModel.getAssociatedTables(tableList)
        when (serviceCreateOption) {
            TableCreateMode.CREATE -> {
                tableList.forEach {
                    createTable(it)
                }
            }
            TableCreateMode.FORCE_RECREATE -> {
                dropTables(tableList)
            }
        }
    }

    internal suspend fun startService(
        rootDTOModel: RootDTO<DTO, DATA, ENTITY>,
        block: suspend  ServiceContext<DTO, DATA, ENTITY>.()->Unit)
            = subTask("Initializing", "ServiceClass") {

        rootDTOModel.initialization()
        suspendedTransactionAsync {
            prepareTables(serviceCreateOption, rootDTOModel)
        }.await()

        serviceContext = ServiceContext(this, rootDTOModel)
        val castedContext = serviceContext.castOrInitEx<ServiceContext<DTO, DATA, ENTITY>>("StartService. Cast failed")
        rootDTOModel.setContextOwned(castedContext)
        castedContext.block()
    }.resultOrException()

    internal fun addSequencePack(key: CompositeKey<RootDTO<*, *, *>, SequenceID>,  pack: SequencePack<*, *, *>) {
        sequences[key] = pack
    }

    internal suspend fun requestEmitter(): CoroutineEmitter = connectionClass.requestEmitter()


    internal fun getSequencePack(key: CompositeKey<RootDTO<*, *, *>,SequenceID>):SequencePack<*, *, *> {
        return sequences[key].getOrOperationsEx(
            "Sequence with key : $key not found",
            ExceptionCode.VALUE_NOT_FOUND)
    }

//    fun getSequenceHandler(sequenceId: Int, dtoClass: DTOBase<DTO, *>): SequenceHandler<DTO, DATA> {
//        val lookupKey =
//            sequences.keys.firstOrNull { it.sequenceId == sequenceId && it.dtoClassName == dtoClass.personalName }
//                .getOrOperationsEx(
//                    "Sequence key with sequenceId: $sequenceId and className : ${dtoClass.personalName} not found. Available keys: ${
//                    sequences.keys.joinToString(", ") { "${it.hashCode()}"} }",
//                    ExceptionCode.VALUE_NOT_FOUND)
//
//        val handler = sequences[lookupKey]!!.getSequenceHandler()
//        return handler
//    }

}