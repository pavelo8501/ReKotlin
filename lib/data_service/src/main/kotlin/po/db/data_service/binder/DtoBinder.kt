package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.ModelDTOContext
import kotlin.reflect.KMutableProperty1

//data class Model(
//    val name: String,
//    val legalName: String,
//    val regNr: String,
//    val updated: LocalDateTime,
//    val created: LocalDateTime,
//    )
//
//class Entity(
//    val name: String,
//    val legalName: String,
//    val regNr: String,
//    val updated: LocalDateTime,
//    val created: LocalDateTime,
//)

interface CommonBinder<T : ModelDTOContext, E : LongEntity>{
    var modelDTO: T?
    var entityDAO: E?
    fun getDTO(): T {
        if(modelDTO == null) throw Exception("Model is not set")
        return modelDTO!!
    }
    fun getDAO():E{
        if(entityDAO == null) throw Exception("Entity is not set")
        return entityDAO!!
    }
    var properties: List<BindPropertyClass<T, E, *>>
    fun updateProperties(entity : E, force: Boolean = false): E
}

class BindPropertyClass<T : ModelDTOContext, E : LongEntity, Y>(
     var name: String,
     var dtoProperty: KMutableProperty1<T, Y>,
     var entityProperty: KMutableProperty1<E, Y>,
){
     var binder :  CommonBinder<T, E>? = null

    private fun getDTO():T {
        if(binder == null) throw Exception("Parent is not set")
        return binder!!.getDTO()
    }

    private fun getDAO():E {
        if(binder == null) throw Exception("Parent is not set")
        return binder!!.getDAO()
    }

    fun update(force: Boolean = false): Boolean {

        val dtoValue = dtoProperty.get(getDTO())
        if(!force) {
            val daoValue = entityProperty.get(getDAO())
            if (dtoValue == daoValue) return false
        }
        entityProperty.set(getDAO(), dtoValue)

        return true
    }
}

class DTOBinderClass<T : ModelDTOContext, E : LongEntity>(vararg  props : BindPropertyClass<T, E, *>):  CommonBinder<T, E>{
    override var modelDTO   : T? = null
    override var entityDAO  : E? = null
    override var properties : List<BindPropertyClass<T, E, *>> = emptyList()
    init {
        this.properties = props.toList()
        properties.forEach{
            it.binder = this
        }
    }
    fun setModelObject(model : T){
        this.modelDTO = model
    }
    override fun updateProperties(entity: E, force: Boolean): E {
        entityDAO = entity
        properties.forEach {
            it.update(force)
        }
        return entity
    }
}