package po.exposify.scope.service

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
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.getOrOperationsEx
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.sequence.classes.SequenceHandler
import po.exposify.scope.sequence.models.SequenceKey
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.extensions.subTask
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class ServiceClass<DTO, DATA, ENTITY>(
    private val connectionClass : ConnectionClass,
    private val serviceCreateOption: TableCreateMode = TableCreateMode.CREATE,
):  AsClass<DATA, ENTITY>, TasksManaged  where  DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity {

    private lateinit var serviceContext: ServiceContext<DTO, DATA, ENTITY>

    var personalName: String = "ServiceClass[Uninitialized]"
        private set

    internal val connection: Database = connectionClass.connection
    private val sequences = ConcurrentHashMap<SequenceKey, SequencePack<DTO, DATA>>()

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
    private fun prepareTables(serviceCreateOption: TableCreateMode, rootDTOModel: RootDTO<DTO, DATA>) {
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
        rootDTOModel: RootDTO<DTO, DATA>,
        block:  ServiceContext<DTO, DATA, ExposifyEntity>.()->Unit)
            = subTask("Initializing", "ServiceClass") {

        rootDTOModel.initialization()
        suspendedTransactionAsync {
            prepareTables(serviceCreateOption, rootDTOModel)
        }.await()

        serviceContext = ServiceContext(this, rootDTOModel)
        val castedContext = serviceContext.castOrInitEx<ServiceContext<DTO, DATA, ExposifyEntity>>("StartService. Cast failed")
        rootDTOModel.setContextOwned(castedContext)
        castedContext.block()
    }.resultOrException()

    internal fun addSequencePack(pack: SequencePack<DTO, DATA>) {
        sequences[pack.getSequenceHandler().thisKey] = pack
    }

    internal suspend fun runSequence(sequenceKey: SequenceKey): List<DATA> {
        val foundSequence = sequences[sequenceKey].getOrOperationsEx(
            "Sequence with key : $sequenceKey not found",
            ExceptionCode.VALUE_NOT_FOUND)
        return connectionClass.launchSequence(foundSequence)
    }

    fun getSequenceHandler(sequenceId: Int, dtoClass: DTOBase<DTO, *>): SequenceHandler<DTO, DATA> {
        val lookupKey =
            sequences.keys.firstOrNull { it.sequenceId == sequenceId && it.dtoClassName == dtoClass.personalName }
                .getOrOperationsEx(
                    "Sequence key with sequenceId: $sequenceId and className : ${dtoClass.personalName} not found. Available keys: ${
                    sequences.keys.joinToString(", ") { "${it.hashCode()}"} }",
                    ExceptionCode.VALUE_NOT_FOUND)

        val handler = sequences[lookupKey]!!.getSequenceHandler()
        return handler
    }

}