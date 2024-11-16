package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.EntityDTO
import po.db.data_service.dto.EntityDTOClass
import po.db.data_service.dto.ModelDTOContext
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


enum class BindingType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}


class ChildClasses< T : ModelDTOContext ,  E: LongEntity, PT: ModelDTOContext, PE : LongEntity >(var hostEntityDTO : DTOClass<PT,PE>, vararg  bindings : OneToManyBinding<T,E,PT,PE>){
    var bindingList : List<OneToManyBinding<T,E,PT,PE>> = listOf()
   // var getParentDTOEntity : (()-> EntityDTO<T,E>)? = null

    init {
        bindingList = bindings.toList()
    }

    fun update(){
        bindingList.forEach{
            if(hostEntityDTO.daoEntity != null){
                if(it.childEntity.daoEntity != null  &&  hostEntityDTO.daoEntity != null){
                    it.parentDAOEntity?.set(it.childEntity.daoEntity!!, hostEntityDTO.daoEntity!!)
                    it.dtoHolder?.forEach { it.update() }
                }
            }
        }
    }

    fun getBindings():List<OneToManyBinding<T,E,PT,PE>>{
        return bindingList
    }
}

class ChildBindingContext{

//    var bindings : MutableList<ChildBinding<*,*,*>> = mutableListOf()
//    val childBindingClass = ChildBindingClass()

}

data class OneToManyBinding< T : ModelDTOContext, E: LongEntity, PT : ModelDTOContext, PE : LongEntity>(
    var childEntity :  DTOClass<T,E>,
    var childDAOEntities :  KProperty1<PE,SizedIterable<E>>?,
    var parentDAOEntity : KMutableProperty1<E,PE>?,
    var dtoHolder   :  List<EntityDTO<T,E>>?
){
    init {
        if(dtoHolder.isNullOrEmpty()){
            dtoHolder = emptyList()
        }
    }


}