package po.db.data_service.services

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.DatabaseManager
import po.db.data_service.exceptions.DataServiceException
import po.db.data_service.exceptions.ErrorCodes
import po.db.data_service.services.models.ServiceDBEntity
import po.db.data_service.services.models.ServiceDataModel
import po.db.data_service.services.models.ServiceDataModelClass


interface DataServiceDataContext<T>{

    val dbManager: DatabaseManager
    val autoload : Boolean
    val table: LongIdTable
    var itemList : MutableList<T>

    var onItemsUpdated: ((List<T>) -> Unit)?

    var initComplete: Boolean
    var onInitComplete: ((Boolean) -> Unit)?

}

abstract class BasicDataService<T: ServiceDataModel<E>, E: ServiceDBEntity>(private val dataClass: ServiceDataModelClass<T,E>, private val entityClass: LongEntityClass<E>) {

    abstract  val dbManager: DatabaseManager

    abstract  val autoload : Boolean

    var onInitComplete: ((Boolean) -> Unit)? = null
    var initComplete: Boolean = false
        set(value) {
            field = value
            if(value){
                onInitComplete?.invoke(field)
            }
        }

    var onItemsUpdated: ((List<T>) -> Unit)? = null
    var itemList : MutableList<T> = mutableListOf()
    // protected abstract fun saveRecord(record: T):T

   // abstract fun newItem():T

    abstract val table: LongIdTable

    protected open fun initializeService(){
        dbQuery {
            if(!table.exists()){
                SchemaUtils.create(table)
            }
            if(autoload){
                selectAll().forEach {createNewModel(it)}
                onItemsUpdated?.invoke(itemList)
                initComplete = true
            }
        }
        dataClass.initDataService(this)
    }

    fun initModel(model:T):T{
        if(model.id != 0L){
            val result = getModel(model.id)
            if(result == null) {
               return model
            }else{
                return copyModel(model,result)
                return result
            }
        }else{
            return model
        }
    }

    protected abstract fun newDataModel(entity: E):T
    protected abstract fun copyModel(source:T, target: T):T
    private fun createNewModel(entity : E): T{
        val model = newDataModel(entity)
        model.setEntity(entity)
        addItem(model)
        return model
    }
    fun saveModel(model:T):T{
       val result = dbQuery {
            entityClass.new {
              val savedEntity=  model.saveToEntity(this)
            }
        }
        model.setEntity(result)
        return model
    }
    fun updateModel(model:T):T{
        if(!model.hasEntity){
            return saveModel(model)
        }

        dbQuery {
            val entity = model.getEntity(true)
            entity.flush()
            model.setEntity(entity)
        }
        return model
    }
    protected fun deleteItem(model:T): Boolean{
        if(!model.hasEntity){
            throw DataServiceException("Cannot delete model without entity.", ErrorCodes.NOT_INITIALIZED)
        }
        val itemToDelete = itemList.firstOrNull { it.id == model.id }
        if (itemToDelete != null){
            itemList.remove(itemToDelete)
            onItemsUpdated?.invoke(itemList)
        }
        dbQuery {
            model.getEntity(false).delete()
        }
        return true
    }


    private fun pick(id: Long): E?{
        return dbQuery {
            entityClass.findById(id)
        }
    }
    private fun selectAll(): List<E>{
        return dbQuery {
            entityClass.all().toList()
        }
    }

    private fun addItem(item: T) {
        itemList.add(item)
        onItemsUpdated?.invoke(itemList)
    }

    private fun getModel(id: Long): T?{
        val result = itemList.firstOrNull { it.id == id }
        if(result == null){
            val entity = pick(id)
            if(entity != null){
               return createNewModel(entity)
            }
            return null
        }else{
            return result
        }
    }

//    open fun itemsCount(): Int{
//        return itemList.count()
//    }
//
//    open fun getItem(id: Long):T?{
//        val foundItem = this.itemList.firstOrNull { it.id == id }
//        return foundItem
//    }
//    open fun getItems(reload: Boolean = false): List<T>{
//        if(reload){
//          //  itemList =  selectAll().map {  this.  createItem(it).apply { setEntity(it) } }.toMutableList()
//        }
//        return itemList.toList()
//    }
//    open fun getItemsCount(): Int{
//        return itemList.toList().size
//    }

    fun  <T>dbQuery(body : () -> T): T = transaction(dbManager.getConnection()) {
        body()
    }

    suspend fun <T> dbQueryAsync(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }

}

