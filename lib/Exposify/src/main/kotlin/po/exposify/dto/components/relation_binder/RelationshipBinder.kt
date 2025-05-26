package po.exposify.dto.components.relation_binder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.components.relation_binder.components.BindingContainerAdv
import po.exposify.dto.components.relation_binder.components.MultipleChildContainer
import po.exposify.dto.components.relation_binder.components.SingleChildContainer
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.collections.CompositeEnumKey
import po.misc.collections.CompositeKey
import kotlin.collections.set


class RelationshipBinder<DTO, D, E, F_DTO, FD, CE>(
   val dtoClass: DTOBase<DTO, D, E>
) where DTO: ModelDTO, D : DataModel, E : LongEntity, F_DTO: ModelDTO, FD: DataModel, CE: LongEntity {

    private var childClassRegistry : MutableMap<String, DTOClass<*,*,*>> = mutableMapOf()

    internal var manyBindings =
        mutableMapOf<Cardinality, MultipleChildContainer<DTO, D, E, F_DTO, FD, CE>>()
        private set
    
    internal var containers = mutableMapOf<CompositeEnumKey<DTOClass<*, *, *>, Cardinality>, BindingContainerAdv<DTO, D, E, out F_DTO, out FD, out CE>>()

    internal var singeBindings =
        mutableMapOf<Cardinality, SingleChildContainer<DTO, D, E, F_DTO, FD, CE>>()
        private set

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

    fun  attachBinding(
        cardinality: Cardinality,
        container: SingleChildContainer<DTO, D, E, F_DTO, FD, CE>,
    ) {
        singeBindings[cardinality] = container
    }
    
    fun addContainerAdv(
        container: BindingContainerAdv<DTO, D, E, out F_DTO, out FD, out CE>,
    ){
        val key =  container.thisKey as CompositeEnumKey<DTOClass<*,*,*>, Cardinality>
        containers[key] = container
    }

    fun attachBinding(
        cardinality: Cardinality,
        container: MultipleChildContainer<DTO, D, E, F_DTO, FD, CE>,
    ){
        manyBindings[cardinality] = container
    }
}