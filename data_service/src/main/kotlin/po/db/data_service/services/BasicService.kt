package po.db.data_service.services

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.EntityID
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


abstract class BasicDataService<T : ServiceDataModel<E>, E:ServiceDBEntity>(private val modelClass: ServiceDataModelClass<T,E>) {

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

    protected open var childServices: List<BasicDataService<*,*>> = listOf()

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
        modelClass.initDataService(this)
    }

    private fun initModelAnySubClass(model:ServiceDataModel<E>):ServiceDataModel<E>{
        if(model.id != 0L){
            val result = getModel(model.id)
            if(result == null) {
                return model.also { it.initialized = true }
            }else{
                return copyModel(model as T, result as T).also { it.initialized = true }
            }
        }else{
            return model.also { it.initialized = true }
        }
    }

    private fun initModel(model:T):T{
        if(model.id != 0L){
            val result = getModel(model.id)
            if(result == null) {
               return model.also { it.initialized = true }
            }else{
                return copyModel(model,result).also { it.initialized = true }
            }
        }else{
            return model.also { it.initialized = true }
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

    fun saveModelAnySubClass(model: ServiceDataModel<E>){

        val initializedModel = if(model.initialized == false){
            initModelAnySubClass(model)
        }else {
            model
        }

        val result = dbQuery {

        }
    }

    fun saveModel(model:T):T{
        val initializedModel = if(model.initialized == false){
            initModel(model)
        }else {
            model
        }
        if(initializedModel.hasEntity) {
            return updateModel(initializedModel)
        }
        val result = dbQuery {
           modelClass.entityClass.new {
               initializedModel.saveToEntity(this)
           }
        }
        initializedModel.childMapping.forEach { mapping ->
            mapping.modelClass.saveChildMapping(mapping, mapping.parentEntityId)
        }
        initializedModel.setEntity(result)
        return initializedModel
    }
    fun saveModelForParent(model:T, parentEntityId : EntityID<Long> ):T{
        val initializedModel = if(model.initialized == false){
            initModel(model)
        }else {
            model
        }
        if(initializedModel.hasEntity) {
            return updateModel(initializedModel)
        }
        val result = dbQuery {
            modelClass.entityClass.new {
                initializedModel.parentEntityId = parentEntityId
                initializedModel.saveToEntity(this)
            }
        }
        initializedModel.childMapping.forEach { mapping ->
            mapping.modelClass.saveChildMapping(mapping, parentEntityId)
        }
        initializedModel.setEntity(result)
        return initializedModel
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
            modelClass.entityClass.findById(id)
        }
    }
    private fun selectAll(): List<E>{
        return dbQuery {
            modelClass.entityClass.all().toList()
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

    fun  <T>dbQuery(body : () -> T): T = transaction(dbManager.getConnection()) {
        body()
    }

    suspend fun <T> dbQueryAsync(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) {
        block()
    }

}

