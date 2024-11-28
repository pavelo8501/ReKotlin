package po.db.data_service.structure

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

import po.db.data_service.dto.ModelDTOContext

class ServiceContext<T: ModelDTOContext, E: LongEntity>(
    val name : String,
    val connection: Database,
   // val dtoCompanion : DTOClass<Any,Any>,
) {



    private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
        body()
    }

    fun testUsability(){
        dbQuery {

        }
    }

    //val entityCompanion : EntityDTOClass<T,E> = dtoCompanion.parentEntityDTO

//    fun saveDTO(dto: EntityDTO<out ModelDTOContext, out LongEntity>){
//        dbQuery{
//            dto.initChild()
//            dto.update()
//        }
//    }

//    private fun getDependantTables(forTable:IdTable<Long>): List<IdTable<Long>> {
//        val dependant = dtoCompanion.parentEntityDTO.registeredTables.filter { it.foreignKeys.map { keyMap->keyMap.targetTable.tableName }.contains(forTable.tableName) }
//        return dependant
//    }
//
//    fun dropTable(table : IdTable<Long>, allTables : List<IdTable<Long>> = emptyList() ): Boolean{
//        try {
//            return  dbQuery {
//                var result : Boolean = false
//                if(table.exists()) {
//                    val dependantTables = getDependantTables(table)
//                    if(dependantTables.isNotEmpty()){
//                        dependantTables.forEach{
//                            dropTable(it, allTables)
//                        }
//                        SchemaUtils.drop(table)
//                        result = true
//                    }else{
//                        SchemaUtils.drop(table)
//                        result = true
//                    }
//                }else{
//                    result = true
//                }
//                result
//            }
//        }catch (e: Exception){
//            return false
//        }
//    }
//    fun createTable(table : IdTable<Long>): Boolean{
//        return try {
//            dbQuery {
//                if(!table.exists()) {
//                    SchemaUtils.create(table)
//                    val dependantTables = getDependantTables(table)
//                    dependantTables.forEach{
//                        createTable(it)
//                    }
//                }
//                true
//            }
//        }catch (e: Exception){
//            false
//        }
//    }
//
//   private fun  <T>dbQuery(body : () -> T): T = transaction(connection) {
//        body()
//    }

}