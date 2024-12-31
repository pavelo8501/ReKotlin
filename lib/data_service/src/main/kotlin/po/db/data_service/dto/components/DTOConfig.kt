package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.*
import po.db.data_service.constructors.ClassBlueprintContainer
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.scope.service.ServiceClass
import po.db.data_service.scope.service.ServiceClass.Companion.getConstructorBlueprint
import kotlin.reflect.KClass

class DTOConfig<ENTITY>(
    val parent: DTOClass<ENTITY>
) where  ENTITY : LongEntity{

    private var dataModelConstructor : (() -> DataModel)? = null
    private var dtoModelClass: KClass<out DTOEntity>? = null
    private var dataModelClass: KClass<out DataModel>? = null

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
        parent.dtoFactory.setDataModelConstructor(dataModelConstructor)
    }

    var daoModel:LongEntityClass<ENTITY>? = null

    var propertyBinder : PropertyBinder? = null
        private set
    var relationBinder  = RelationshipBinder<ENTITY>(parent)


    fun <DM: DataModel, E: LongEntity>propertyBindings(vararg props: PropertyBinding<DM, E, *>) {
        PropertyBinder().let {
            it.setProperties(props.toList())
            propertyBinder = it
        }
    }

    fun updateProperties(dataModel: DataModel, daoEntity : LongEntity){
        propertyBinder?.updateProperties(dataModel, daoEntity, UpdateMode.ENTITY_TO_MODEL)
    }

    fun <DTO>setClassData(dtoClass : KClass<out DTOEntity>, dataClass : KClass<out DataModel>, dao: LongEntityClass<ENTITY>)where DTO: DTOEntity {
        dtoModelClass = dtoClass
        dataModelClass = dataClass
        daoModel = dao
        parent.dtoFactory.dtoModelClass = dtoClass
        parent.dtoFactory.dataModelClass = dataClass

    }

}