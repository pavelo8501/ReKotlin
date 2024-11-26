package po.db.data_service.binder

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.CommonDTO
import po.db.data_service.dto.CompleteDto
import po.db.data_service.dto.DTOClass
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

interface CommonBinder<DTO, ENTITY, TYPE>{
    var dtoProperty: KMutableProperty1<DTO, TYPE>
    var entityProperty: KMutableProperty1<ENTITY,TYPE>
//    fun getDTO(): CompleteDto {
//        return modelDTO
//    }
//    fun getDAO():ENTITY{
//        return entityDAO
//    }
var properties: List<PropertyBinding<DTO, ENTITY, TYPE>>
    fun updateProperties(entity : ENTITY, force: Boolean = false): ENTITY
}



class PropertyBinding<DTO, ENTITY, TYPE>(
    var name: String,
    override var dtoProperty :  KMutableProperty1<DTO, TYPE>,
    override var entityProperty : KMutableProperty1<ENTITY,TYPE>,
    var binder  :  DTOBinder<DTO,ENTITY> ) : CommonBinder<ENTITY, TYPE>
{

    override var properties: List<PropertyBinding<ENTITY, TYPE>>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun updateProperties(entity: ENTITY, force: Boolean): ENTITY {
        properties.forEach {
            it.update(force)
        }
        return entity
    }


    fun update(force: Boolean = false): Boolean {

        val dtoValue = dtoProperty.get(binder.modelDTO)
        if(!force) {
            val daoValue = entityProperty.get(binder.entityDAO)
            if (dtoValue == daoValue) return false
        }
        entityProperty.set(binder.entityDAO, dtoValue)
        return true
    }
}

class DTOBinder<DTO, ENTITY> (props : List<PropertyBinding<DTO, ENTITY,*>>) {


    var  entityDTO : DTO? = null
    var  entityDAO : ENTITY? = null

    fun bind(dto: DTO, entity: ENTITY) {
        this.entityDAO = entity
        this.entityDTO= dto
        //println("Binding DTO with entity ID ${entity.id}")
    }


    var properties : List<PropertyBinding<DTO, ENTITY, *>> = emptyList()
    init {
        this.properties = props.toList()
        properties.forEach{
            it.binder = this
        }
    }
    fun setModelObject(model : DTO){
        this.entityDTO = model
    }
    fun updateProperties(entity: ENTITY, force: Boolean): ENTITY {
        entityDAO = entity
        properties.forEach {
            it.update(force)
        }
        return entity
    }
}


//class DTOBinderClass<T : ModelDTOContext, E : LongEntity>(vararg  props : BindPropertyClass<T, E, *>):  CommonBinder<T, E>{
//    override var modelDTO   : T? = null
//    override var entityDAO  : E? = null
//    override var properties : List<BindPropertyClass<T, E, *>> = emptyList()
//    init {
//        this.properties = props.toList()
//        properties.forEach{
//            it.binder = this
//        }
//    }
//    fun setModelObject(model : T){
//        this.modelDTO = model
//    }
//    override fun updateProperties(entity: E, force: Boolean): E {
//        entityDAO = entity
//        properties.forEach {
//            it.update(force)
//        }
//        return entity
//    }
//}