package po.exposify.dto.components.relation_binder

import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import kotlin.collections.set


class RelationshipBinder<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
   val dtoClass: DTOBase<DTO, DATA>
) where DTO: ModelDTO, DATA : DataModel, ENTITY : ExposifyEntity, CHILD_DTO: ModelDTO, CHILD_DATA: DataModel, CHILD_ENTITY: ExposifyEntity {

    private var childClassRegistry : MutableMap<String, ClassDTO> = mutableMapOf()

    internal var manyBindings =
        mutableMapOf<BindingKeyBase.OneToMany<DTO>, MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()
        private set

    internal var singeBindings =
        mutableMapOf<BindingKeyBase.OneToOne<DTO>, SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()
        private set

    suspend fun addChildClass(childClass: DTOClass<*>){
        if(!childClass.initialized){
            childClass.initialization()
        }
        val className = childClass::class.qualifiedName.toString()
        childClassRegistry[className] = childClass
    }

    fun getChildClassList(): List<ClassDTO>{
       return childClassRegistry.values.toList()
    }

    fun  attachBinding(
        key: BindingKeyBase.OneToOne<DTO>,
        container: SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    ) {
        singeBindings[key] = container
    }

    fun attachBinding(
        key: BindingKeyBase.OneToMany<DTO>,
        container: MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    ){
        manyBindings[key] = container
    }
}