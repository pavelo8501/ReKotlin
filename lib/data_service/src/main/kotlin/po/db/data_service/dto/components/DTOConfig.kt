package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.binder.*
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

class DTOConfig<ENTITY>(
    val parent: DTOClass<ENTITY>
) where  ENTITY : LongEntity{

    var dtoModelClass: KClass<out DTOEntity>? = null
    var dataModelClass: KClass<out DataModel>? = null

    var daoModel:LongEntityClass<ENTITY>? = null

    var propertyBinder : PropertyBinder? = null
        private set

    var relationBinder  = RelationshipBinder<ENTITY>(parent)

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    fun <DM: DataModel, E: LongEntity>propertyBindings(vararg props: PropertyBinding<DM, E, *>) {
        PropertyBinder().let {
            it.setProperties(props.toList())
            propertyBinder = it
        }
    }

    fun updateProperties(dataModel: DataModel, daoEntity : LongEntity){
        propertyBinder?.updateProperties(dataModel, daoEntity, UpdateMode.ENTITY_TO_MODEL)
    }

    inline  fun <reified CHILD> DTOClass<ENTITY>.childBinding(
        childDtoModel: DTOClass<CHILD>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD>>,
        referencedOnProperty: KMutableProperty1<CHILD, ENTITY>,
        type: OrdinanceType,
        binderContext: ChildContainer<ENTITY, CHILD>.()->Unit,
       // childDataProperty:  KMutableProperty1<Any, List<DataModel>>? = null
    ) where CHILD: LongEntity{
       val  parentDTOModel  = this
       if(!childDtoModel.initialized) {
            parent.onDtoInitializationCallback?.let { callback ->
                childDtoModel.initialization(callback)
            }
        }
        RelationshipBinder(parentDTOModel).let {
            it.addChildBinding<CHILD>(childDtoModel, byProperty, referencedOnProperty, type)
            relationBinder = it
        }
    }

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

    fun <DTO>setClassData(
        dtoClass : KClass<DTO>,
        dataClass : KClass<out DataModel>,
        dao: LongEntityClass<ENTITY>
    ) where DTO: DTOEntity {
        dtoModelClass = dtoClass
        dataModelClass = dataClass
        daoModel = dao
    }


}