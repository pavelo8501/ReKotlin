package po.exposify.dto.components.relation_binder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass

import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.collections.CompositeEnumKey
import kotlin.collections.set


class RelationshipBinder<DTO, D, E, F_DTO, FD, CE>(
   val dtoClass: DTOBase<DTO, D, E>
) where DTO: ModelDTO, D : DataModel, E : LongEntity, F_DTO: ModelDTO, FD: DataModel, CE: LongEntity {

    private var childClassRegistry : MutableMap<String, DTOClass<*,*,*>> = mutableMapOf()

    fun addChildClass(childClass: DTOClass<out F_DTO, out FD, out CE>){
        if(!childClass.initialized){
            childClass.initialization()
        }
        val className = childClass::class.qualifiedName.toString()
        childClassRegistry[className] = childClass
    }

    fun isDtoClassInHierarchy(childClass : DTOClass<*,*,*>): Boolean{
       val found =  childClassRegistry[childClass::class.qualifiedName.toString()]
       return found != null
    }

    fun getChildClassList(): List<DTOClass<*,*,*>>{
       return childClassRegistry.values.toList()
    }
}