package po.db.data_service.services.models

import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.annotations.BindProperty
import po.db.data_service.services.CoreService
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


class PropertyBindings{

}


abstract class ContainerModel() {

    companion object {
        // Cache for property mappings
        private val propertyMappings = mutableMapOf<KClass<*>, List<PropertyMapping>>()

        // Function to get property mappings for a class
        fun getPropertyMappings(kClass: KClass<*>): List<PropertyMapping> {
            return propertyMappings.getOrPut(kClass) {
                // Perform reflection only once per class
                kClass.memberProperties
                    .filter { it.findAnnotation<BindProperty>() != null }
                    .map { property ->
                        val annotation = property.findAnnotation<BindProperty>()!!
                        PropertyMapping(
                            property = property,
                            dbName = annotation.propName,
                            dbType = annotation.dbType
                        )
                    }
            }
        }
    }

    private var initialized = false

    abstract var id: Long

    var service : (CoreService<*>.()-> Unit)? = null
    var load : (CoreService<*>.()-> Unit)? = null


    abstract var dbEntity:CoreDbEntityContext

    abstract var entityClass: LongEntityClass<CoreDbEntity>


    private var bindings: PropertyBindings? = null

    private fun initNewBindings(bindings: PropertyBindings){
        this.bindings = bindings
    }

//    fun getPropertyValues(): Map<String, Any?> {
//        val mappings = getPropertyMappings(this::class)
//        return mappings.associate { mapping ->
//            val value = mapping.property.get(this)
//            mapping.dbName to value
//        }
//    }

    fun propertyBindings(block : PropertyBindings.()->Unit){
        val newBindings = PropertyBindings()
        newBindings.block()
        initNewBindings(newBindings)
    }


}