package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
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
import po.exposify.scope.sequence.models.SequenceKey
import po.exposify.scope.sequence.models.SequencePack
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.subTask
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

class ServiceClass<DTO, DATA, ENTITY>(
    private val connectionClass : ConnectionClass,
    private val rootDTOModel : DTOClass<DTO>,
    private val serviceCreateOption: TableCreateMode = TableCreateMode.CREATE,
):  AsClass<DATA, ENTITY>, TasksManaged  where  DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity {

    internal val connection: Database = connectionClass.connection

    var personalName: String = "ServiceClass[${rootDTOModel.personalName}]"
    var serviceContext: ServiceContext<DTO, DATA>? = null

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

    private val sequences = ConcurrentHashMap<SequenceKey, SequencePack<DTO, DATA>>()

    fun addSequencePack(pack: SequencePack<DTO, DATA>) {
        sequences[pack.getSequenceHandler().thisKey] = pack
    }

    suspend fun runSequence(sequenceKey: SequenceKey): List<DATA> {
        val foundSequence = sequences[sequenceKey].getOrThrowDefault("Sequence with key : $sequenceKey not found")
        return connectionClass.launchSequence(foundSequence)
    }

    fun getSequenceHandler(sequenceId: Int, dtoClass: DTOClass<DTO>): SequenceHandler<DTO, DATA> {
        val lookupKey =
            sequences.keys.firstOrNull { it.sequenceId == sequenceId && it.dtoClassName == dtoClass.personalName }
                .getOrThrowDefault(
                    "Sequence key with sequenceId: $sequenceId and className : ${dtoClass.personalName} not found. Available keys: ${
                    sequences.keys.joinToString(", ") { "${it.hashCode()}" }
                }")

        val handler = sequences[lookupKey]!!.getSequenceHandler()
        return handler
    }

    private fun initializeDTOs(context: ServiceClass<DTO, DATA, ENTITY>.() -> Unit) {
        context.invoke(this)
    }

    private fun prepareTables(serviceCreateOption: TableCreateMode) {
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

    private suspend fun start() = subTask("Initializing", "ServiceClass") {
        suspendedTransactionAsync {
            initializeDTOs {
                rootDTOModel.initialization()
            }
            prepareTables(serviceCreateOption)
        }.await()
    }.resultOrException()

    suspend fun launch(receiver: ServiceContext<DTO, DATA>.() -> Unit) {
        start()
        val casted = this@ServiceClass.safeCast<ServiceClass<DTO, DATA, ExposifyEntityBase>>()
            .getOrThrowDefault("Cast to ServiceClass<DTO,DATA, ExposifyEntityBase> failed")
        ServiceContext(casted, rootDTOModel).let { context ->
            context.receiver()
            serviceContext = context
        }
    }
}