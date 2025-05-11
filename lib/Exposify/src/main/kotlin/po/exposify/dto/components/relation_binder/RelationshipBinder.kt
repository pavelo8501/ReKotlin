package po.exposify.dto.components.relation_binder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ClassDTO
import po.exposify.dto.interfaces.ModelDTO
import kotlin.collections.set


class RelationshipBinder<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
   val dtoClass: DTOBase<DTO, *, *>
) where DTO: ModelDTO, DATA : DataModel, ENTITY : LongEntity, CHILD_DTO: ModelDTO, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity {

    private var childClassRegistry : MutableMap<String, ClassDTO> = mutableMapOf()

    internal var manyBindings =
        mutableMapOf<Cardinality, MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()
        private set

    internal var singeBindings =
        mutableMapOf<Cardinality, SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()
        private set

    suspend fun addChildClass(childClass: DTOClass<*, *, *>){
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
        cardinality: Cardinality,
        container: SingleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    ) {
        singeBindings[cardinality] = container
    }

    fun attachBinding(
        cardinality: Cardinality,
        container: MultipleChildContainer<DTO, DATA, ENTITY, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>,
    ){
        manyBindings[cardinality] = container
    }
}