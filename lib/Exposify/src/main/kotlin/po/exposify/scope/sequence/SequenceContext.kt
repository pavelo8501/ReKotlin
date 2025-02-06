package po.exposify.scope.sequence

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.classes.DTOClass
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.dto.CommonDTO
import po.db.data_service.models.CrudResult

class SequenceContext<DATA, ENTITY>(
    val connection: Database,
    val hostDto : DTOClass<DATA,ENTITY>
) where  DATA : DataModel, ENTITY : LongEntity
{

    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    private var lastResult : CrudResult<DATA, ENTITY>? = null

    private fun dtos(): List<CommonDTO<DATA,ENTITY>>{
        val result =   mutableListOf<CommonDTO<DATA,ENTITY>>()
        lastResult?.rootDTOs?.forEach{  result.add(it) }
        return result
    }

    fun select(block: SequenceContext<DATA, ENTITY>.(dtos: List<CommonDTO<DATA,ENTITY>>)-> Unit){
        val result by lazy {
            dbQuery { lastResult = hostDto.select() }
            dtos()
        }
        this.block(result)
    }

    fun update(
        dataModels: List<DATA>,
        block: SequenceContext<DATA, ENTITY>.(dtos: List<CommonDTO<DATA,ENTITY>>)-> Unit)
    {
        val result by lazy {
            dbQuery { lastResult = hostDto.update<DATA, ENTITY>(dataModels) }
            dtos()
        }
        this.block(result)
    }

    @JvmName("UpdateDtos")
    fun update(
        dtos: List<CommonDTO<DATA,ENTITY>>,
        block: SequenceContext<DATA, ENTITY>.(dtos: List<CommonDTO<DATA,ENTITY>>)-> Unit)
    {
        val result by lazy {
            dbQuery { lastResult = hostDto.update<DATA, ENTITY>(dtos.map { it.getInjectedModel() }) }
            dtos()
        }
        this.block(result)
    }
}