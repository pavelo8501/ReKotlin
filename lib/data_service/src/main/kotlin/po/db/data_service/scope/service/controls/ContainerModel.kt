package po.db.data_service.scope.service.controls

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SizedIterable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.findAnnotation
import po.db.data_service.annotations.ChildMapping
import kotlin.reflect.full.declaredFunctions


class PropertyBindings{

}


class ChildHolder<T: ContainerDatContext, E : Any>(
    val childClass : KClass<T>,
    val dbEntityProperty : KProperty1<E, SizedIterable<*>>,
    val single: Boolean = false){

    val containerName : String = dbEntityProperty.name

    init {

    }
}


class ChildBindingClass(val parentContainer: ContainerModel<*>){

    private val childBindings = mutableListOf<ChildHolder<*, *>>()

    private fun <T: ContainerDatContext>initialInitialization(childClass : KClass<T>){

    }

    fun addNewBinding(binding : ChildHolder<*, *>){
        childBindings.add(binding)
    }

    fun <T: ContainerDatContext,E : Any>childContainer(childContainerClass: KClass<T>, dbEntityProperty : KProperty1<E, SizedIterable<*>>, single: Boolean = false) {
        initialInitialization(childContainerClass)
        addNewBinding(ChildHolder(childContainerClass, dbEntityProperty ,single))
    }

//    fun processChildEntities(parentEntity: PartnerEntity) {
//        val departments = dbEntityProperty.get(parentEntity)
//        // 'departments' is a SizedIterable<DepartmentEntity>
//        for (department in departments) {
//            // Process each department...
//        }
//    }

//    fun someFunction(partnerEntity: PartnerEntity) {
//        val departments = dbEntityProperty.get(partnerEntity)
//        // Now 'departments' is a SizedIterable<DepartmentEntity>
//    }

}


open class ContainerDatContextClass

interface ContainerDatContext{
    companion object {

    }
}

abstract class ContainerModel<E:  LongEntityClass<LongEntity>>(val dbEntityClazz: E) : ContainerDatContext {

    companion object {
     //   private val propertyMappings = mutableMapOf<KClass<*>, List<PropertyMapping>>()

//        fun getPropertyMappings(containerModelClass: KClass<T>, dbEntityClazz: KClass<E>): List<PropertyMapping> {
//            return propertyMappings.getOrPut(containerModelClass) {
//                containerModelClass.memberProperties
//                    .filter { it.findAnnotation<BindProperty>() != null }
//                    .map { property ->
//                        val annotation = property.findAnnotation<BindProperty>()!!
//
//                        val dbEntityProperty = dbEntityClazz.memberProperties.firstOrNull { it.name == annotation.propName }
//                            ?: throw IllegalStateException("Property ${annotation.propName} not found in ${dbEntityClazz.simpleName}")
//
//                        val dbEntityMutableProperty = dbEntityClazz.memberProperties
//                            .firstOrNull { it.name == annotation.propName } as? KMutableProperty1<Any, Any?>
//                            ?: throw IllegalStateException("Mutable property ${annotation.propName} not found or not mutable in ${dbEntityClazz.simpleName}")
//
//                        PropertyMapping(
//                            property = property as KProperty1<ContainerModel<*>, Any?>,
//                            dbPropName = annotation.propName,
//                            dbProperty = dbEntityMutableProperty
//                        )
//                    }
//            }
//        }

        private val childMappings = mutableMapOf<KClass<*>, List<ChildMapping>>()
        fun getChildMappings(containerModelClass: KClass<*>): List<ChildMapping> {

            return childMappings.getOrPut(containerModelClass) {
                containerModelClass.declaredFunctions.filter { function ->
                    function.findAnnotation<ChildMapping>() != null
                }.map { kFunc ->
                    kFunc.name

                    val annotation = kFunc.findAnnotation<ChildMapping>()!!
                    // annotation.mappingName

                    ChildMapping(
                        annotation.mappingName

                    )

                }
            }


//            return childMappings.getOrPut(containerModelClass) {
//                containerModelClass.memberProperties.filter { it.findAnnotation<ChildMapping>() != null}
//                    .map { function  ->
//                        val annotation = function.findAnnotation<ChildMapping>()!!
//                        ChildMapping(
//                            annotation.mappingName
//                        )
//                    }
//            }
        }

        private val comulutiveTableMap  = mutableListOf<IdTable<*>>()
        fun addTableMap(map:  IdTable<*>){
            comulutiveTableMap.add(map)
        }

        fun getTableList(): List<IdTable<*>>{
            return comulutiveTableMap.toList()
        }

        private val extLongEntityClassCache = mutableMapOf<String, LongEntityClass<LongEntity>>()
        fun saveExtLongEntityClass(name : String, extLongEntityClass: LongEntityClass<LongEntity>) {
            extLongEntityClassCache.putIfAbsent(name, extLongEntityClass).let {
                if (it == null) {
                    addTableMap(extLongEntityClass.table)
                }
            }
        }

        val dbEntityClassCache = mutableMapOf<String, KClass<*>>()

        fun <E:LongEntity>saveExposedEntityClass(dbEntityClazz : KClass<E>){
            val className : String = dbEntityClazz.simpleName?:"unknown"
            dbEntityClassCache.putIfAbsent(className, dbEntityClazz).let {
                   if(it == null){
                       if(dbEntityClazz.companionObjectInstance is LongEntityClass<LongEntity>  ){
                           saveExtLongEntityClass("$className Companion", dbEntityClazz.companionObjectInstance as LongEntityClass<LongEntity> )
                       }
                   }
                   val a = 10
               }
        }
    }

    private var initialized = false
        get(){
            return  field
        }
        set(value) {
            if(value != field){
                field = value
                if(value){
                    initialize()
                }
            }
        }

//    saveExposedEntityClass(dbEntityClazz)

    init {

    }


  //  fun serviceContext(serviceHandler: ServiceInterface<*,E>.()-> Unit) {




//        fun create(){
//            val newEntity = this.hostService?.create {
//                entityClass.new {
//                    val mappings = getProperties()
//                    for (mapping in mappings) {
//                        val modelValue = mapping.property.get(this@ContainerModel)
//                        mapping.dbProperty.set(this, modelValue)
//                    }
//                }
//            }
//            dbEntity = newEntity
//        }


   // }

    abstract var id: Long

    abstract fun initialize()

  //  var service : (CoreService<*>.()-> Unit)? = null
  //  var load : (CoreService<*>.()-> Unit)? = null

    private var dbEntity:E? = null

    private var bindings: PropertyBindings? = null

    private fun initNewBindings(bindings: PropertyBindings){
        this.bindings = bindings
    }

    private var childBindingReceiver: (ChildBindingClass.()->Unit)? = null
    private var childBindings: ChildBindingClass? = null
    fun childBindings(receiver : ChildBindingClass.()->Unit){
        receiver(ChildBindingClass(this))
    }

    private fun invokeBindings(): Boolean{
        if(childBindingReceiver != null){
            val newBindings = ChildBindingClass(this)
            childBindingReceiver!!.invoke(newBindings)
            childBindings = newBindings
            return true
        }
        return false
    }



//    fun getPropertyValues(): Map<String, Any?> {
//        val mappings = getPropertyMappings(this::class)
//        return mappings.associate { mapping ->
//            val value = mapping.property.get(this)
//            mapping.dbName to value
//        }
//    }


//    private fun getProperties(): List<PropertyMapping>{
//       return getPropertyMappings(this::class,dbEntityClazz)
//    }

    fun getMappings(): List<ChildMapping> {
        return getChildMappings(this::class)
    }


//    fun createTable(forceRecreate: Boolean = false){
//        if(forceRecreate){
//            this.hostService?.dropTable(entityClass.table, getTableList()).let { if(it == true){
//                    this.hostService?.createTable(entityClass.table)
//                }
//            }
//        }else{
//            this.hostService?.createTable(entityClass.table)
//        }
//    }



    fun update(){
//        this.hostService?.dbQuery{
//            val mappings = getProperties()
//            for (mapping in mappings) {
//                val modelValue = mapping.property.get(this@ContainerModel)
//                mapping.dbProperty.set(dbEntity as Any, modelValue)
//            }
//            dbEntity!!.flush()
//        }
    }

//    fun load(){
//      val found =  this.hostService?.dbQuery{
//          if(childBindings != null){
//              entityClass.findById(id)
//          }else{
//              entityClass.findById(id)
//          }
//        }
//       if(found==null){
//           this.initialized = false
//           throw IllegalStateException("Entity not found")
//       }
//       dbEntity = found
//    }

}