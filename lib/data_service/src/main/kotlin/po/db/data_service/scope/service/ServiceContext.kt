package po.db.data_service.scope.service

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.classes.DTOClass
import po.db.data_service.scope.dto.DTOContext
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.dto.CommonDTO
import po.db.data_service.scope.sequence.SequenceContext
import po.db.data_service.scope.sequence.models.SequencePack
import po.db.data_service.scope.service.enums.WriteMode
import kotlin.reflect.KProperty1

class ServiceContext<DATA,ENTITY>(
    private val dbConnection: Database,
    internal val rootDtoModel : DTOClass<DATA,ENTITY>,
) where  ENTITY : LongEntity,DATA: DataModel{

    val name : String = "${rootDtoModel.className}|Service"

    internal val sequences = mutableMapOf<String, MutableList<(DATA) -> DATA>>()
    internal val sequences2 =
        mutableMapOf<String, SequencePack<DATA, ENTITY>>()

    private fun  <T>dbQuery(body : () -> T): T = transaction(dbConnection) {
        body()
    }

    private fun <T> service(statement: ServiceContext<DATA, ENTITY>.() -> T): T = statement.invoke(this)
    fun <T> context(serviceBody: ServiceContext<DATA, ENTITY>.() -> T): T = service{
        serviceBody()
    }

    fun DTOClass<DATA, ENTITY>.pick(
        vararg conditions: Pair<KProperty1<DATA, *>, Any?>, block: DTOContext<DATA, ENTITY>.() -> Unit
    ): DTOClass<DATA, ENTITY>?
    {
        val selectedDTOs = dbQuery {
            pick(conditions.toList())
        }
        val context  = DTOContext(selectedDTOs)
        context.block()
        return null
    }

    fun DTOClass<DATA, ENTITY>.select(block: DTOContext<DATA, ENTITY>.() -> Unit){
        val selectedDTOs = dbQuery {
           select()
        }
        val context  = DTOContext(selectedDTOs)
        context.block()
    }

    @JvmName("updateFromDataModels")
    fun DTOClass<DATA, ENTITY>.update(
        dataModels : List<DATA>,
        writeMode: WriteMode = WriteMode.STRICT,
        block: DTOContext<DATA, ENTITY>.() -> Unit){
        val createdDTOs =  dbQuery {
            update<DATA, ENTITY>(dataModels)
        }
        val context = DTOContext(createdDTOs)
        context.block()
    }

    fun DTOClass<DATA, ENTITY>.update(
        dtoList : List<CommonDTO<DATA, ENTITY>>,
        block: DTOClass<DATA, ENTITY>.() -> Unit
    ){
        TODO("To implement update variance if CommonDTO list is supplied")
    }

    fun DTOClass<DATA, ENTITY>.delete(toDelete: DATA, block: DTOContext<DATA, ENTITY>.() -> Unit){
        val selectedDTOs = dbQuery {
            delete(toDelete)
        }
        val context  = DTOContext(selectedDTOs)
        context.block()
    }

    fun DTOClass<DATA, ENTITY>.sequence(
        name:String,
        block: suspend SequenceContext<DATA, ENTITY>.() -> Unit): DTOClass<DATA, ENTITY>
    {
        sequences2[name] = SequencePack(name, SequenceContext<DATA, ENTITY>(dbConnection, rootDtoModel), block)

        return this
    }

}