package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.common.interfaces.AsClass
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.ComponentType
import po.exposify.dto.interfaces.IdentifiableComponent
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.service.enums.TableCreateMode
import po.lognotify.TasksManaged

class ServiceClass<DTO, DATA, ENTITY>(
    private val rootDTOModel: RootDTO<DTO, DATA, ENTITY>,
    private val connectionClass : ConnectionClass,
    private val serviceCreateOption: TableCreateMode = TableCreateMode.CREATE,
): IdentifiableComponent,  AsClass<DATA, ENTITY>, TasksManaged  where  DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity {

    private val serviceContext: ServiceContext<DTO, DATA, ENTITY> = ServiceContext(this, rootDTOModel)

    override val qualifiedName: String = "ServiceClass[${connectionClass.sourceName}]"
    override val type: ComponentType = ComponentType.ServiceClass

    internal val connection: Database get() = connectionClass.connection

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
    private fun prepareTables(serviceCreateOption: TableCreateMode) {
        val tableList = mutableListOf<IdTable<Long>>()
        rootDTOModel.getAssociatedTables(tableList)
        val dropStatement =  rootDTOModel.config.entityModel.sourceTable.dropStatement()
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

    internal fun initService(rootDTOModel: RootDTO<DTO, DATA, ENTITY>){

        transaction {
            rootDTOModel.initialization(serviceContext)
            prepareTables(serviceCreateOption)
        }
    }

    internal suspend fun runServiceContext(block: suspend ServiceContext<DTO, DATA, ENTITY>.()->Unit){
        println("Before   ServiceContext invoked (runServiceContext)")
        serviceContext.block()
    }

    internal suspend fun requestEmitter(session: AuthorizedSession): CoroutineEmitter
        = connectionClass.requestEmitter(session)

}