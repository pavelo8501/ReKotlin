package po.db.data_service.services.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.annotations.BindProperty
import po.db.data_service.services.BaseService
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties


class PropertyBindings{

}


abstract class ContainerModel<E>(val dbEntityClazz: KClass<E>) where E : CoreDbEntityContext, E : LongEntity {

    companion object {
        private val propertyMappings = mutableMapOf<KClass<*>, List<PropertyMapping>>()

        fun getPropertyMappings(containerModelClass: KClass<*>, dbEntityClazz: KClass<*>): List<PropertyMapping> {
            return propertyMappings.getOrPut(containerModelClass) {
                containerModelClass.memberProperties
                    .filter { it.findAnnotation<BindProperty>() != null }
                    .map { property ->
                        val annotation = property.findAnnotation<BindProperty>()!!

                        val dbEntityProperty = dbEntityClazz.memberProperties.firstOrNull { it.name == annotation.propName }
                            ?: throw IllegalStateException("Property ${annotation.propName} not found in ${dbEntityClazz.simpleName}")

                        val dbEntityMutableProperty = dbEntityClazz.memberProperties
                            .firstOrNull { it.name == annotation.propName } as? KMutableProperty1<Any, Any?>
                            ?: throw IllegalStateException("Mutable property ${annotation.propName} not found or not mutable in ${dbEntityClazz.simpleName}")

                        PropertyMapping(
                            property = property as KProperty1<ContainerModel<*>, Any?>,
                            dbPropName = annotation.propName,
                            dbProperty = dbEntityMutableProperty
                        )
                    }
            }
        }
    }

    val entityClass: LongEntityClass<E> = dbEntityClazz.companionObjectInstance as? LongEntityClass<E>
        ?: throw IllegalStateException("Companion object is not LongEntityClass for $dbEntityClazz")


    init {
        dbEntityClazz.companionObject
    }

    private var initialized = false

    private var hostService : BaseService? = null

    fun setService(service: BaseService){
        hostService = service
        initialized = true
    }


    abstract var id: Long

  //  var service : (CoreService<*>.()-> Unit)? = null
  //  var load : (CoreService<*>.()-> Unit)? = null


    var dbEntity:E? = null

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


   private fun getProperties(): List<PropertyMapping>{
       return getPropertyMappings(this::class,dbEntityClazz)
   }


    fun create(){
        val newEntity = this.hostService?.create {
            entityClass.new {
                val mappings = getProperties()
                for (mapping in mappings) {
                    val modelValue = mapping.property.get(this@ContainerModel)
                    mapping.dbProperty.set(this, modelValue)
                }
            }
        }
        dbEntity = newEntity
    }


}