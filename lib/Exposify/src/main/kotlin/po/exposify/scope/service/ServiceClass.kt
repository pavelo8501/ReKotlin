package po.exposify.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import po.auth.sessions.models.AuthorizedSession
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.RootDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.withTransactionIfNone
import po.exposify.scope.connection.ConnectionClass
import po.exposify.scope.connection.controls.CoroutineEmitter
import po.exposify.scope.service.models.TableCreateMode
import po.lognotify.TasksManaged
import po.lognotify.tasks.TaskHandler
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity

class ServiceClass<DTO, DATA, ENTITY>(
    private val rootDTOModel: RootDTO<DTO, DATA, ENTITY>,
    @PublishedApi internal val connectionClass : ConnectionClass
):  TasksManaged  where  DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity {


    override val identity: CTXIdentity<ServiceClass<DTO, DATA, ENTITY>> = asIdentity()

    private var tableRecreationList: List<IdTable<Long>>? = null

    internal val connection: Database get() = connectionClass.connection
    val serviceContext: ServiceContext<DTO, DATA, ENTITY> = ServiceContext(this, rootDTOModel)

    val logger : TaskHandler<*> get()= taskHandler

    private var running: Boolean = true

    private fun prepareTables(createOption: TableCreateMode) {
        when (createOption) {
            TableCreateMode.Create -> {
                val tableList = mutableListOf<IdTable<Long>>()
                rootDTOModel.getAssociatedTables(tableList)
                notify("Creating tables TableCreateMode.CREATE")
                tableList.forEach {table->
                    if (!table.exists()) {
                        notify("Creating table ${table.tableName}")
                        SchemaUtils.create(table)
                        notify("${table.tableName} created")
                    }else{
                        notify("Table ${table.tableName} skip. Already exists")
                    }
                }
            }
            TableCreateMode.ForceRecreate -> {
                val tableList = TableCreateMode.ForceRecreate.tables.ifEmpty {
                    val newList = mutableListOf<IdTable<Long>>()
                    rootDTOModel.getAssociatedTables(newList)
                    newList.reversed()
                }
                notify("Dropping tables  TableCreateMode.FORCE_RECREATE  ${tableList.joinToString(", "){it.tableName} }")
                SchemaUtils.drop(*tableList.toTypedArray<IdTable<Long>>(), inBatch = true)
                notify("Dropped. Recreating")
                tableList.forEach {table->
                    notify("Creating table ${table.tableName}")
                    SchemaUtils.create(table)
                    notify("${table.tableName} created")
                }
            }
        }
    }

    internal fun recreateUsing(tables: List<IdTable<Long>>){
        tableRecreationList = tables
    }

    internal fun deinitializeService(){
        running = false
        rootDTOModel.finalize()
    }

    internal fun initService(
        rootDTOModel: RootDTO<DTO, DATA, ENTITY>,
        serviceCreateOption: TableCreateMode = TableCreateMode.Create,
        block:  ServiceContext<DTO, DATA, ENTITY>.()->Unit
    ): ServiceContext<DTO, DATA, ENTITY>{
        withTransactionIfNone(serviceContext.debugger, false){
            if(running){
                rootDTOModel.initialization(serviceContext)
                prepareTables(serviceCreateOption)
                serviceContext.block()
            }
        }
        return serviceContext
    }
    internal suspend fun requestEmitter(session: AuthorizedSession): CoroutineEmitter =
        connectionClass.requestEmitter(session)

}