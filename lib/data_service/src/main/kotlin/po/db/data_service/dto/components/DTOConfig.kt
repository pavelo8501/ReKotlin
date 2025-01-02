package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.*
import po.db.data_service.binder.RelationshipBinder
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import kotlin.reflect.KClass


class DTOConfig<DATA, ENTITY>(
    val  parent : DTOClass<DATA,ENTITY>,
    var relationBinder : RelationshipBinder<ENTITY,DATA>
) where  ENTITY : LongEntity, DATA: DataModel  {

    var dtoModelClass: KClass<out CommonDTO<DATA>>? = null
    var dataModelClass: KClass<out DataModel>? = null

    var daoModel:LongEntityClass<ENTITY>? = null

    val propertyBinder : PropertyBinder<DATA,ENTITY> = PropertyBinder()


    var dataModelConstructor : (() -> DataModel)? = null
        private set


    fun propertyBindings(vararg props: PropertyBinding<DATA, ENTITY, *>) =  propertyBinder.setProperties(props.toList())

//    fun <T>propertyBindings(vararg props: PropertyBinding<DATA, ENTITY, T>) {
//
//
//        propertyBinder.setProperties<T>(props.toList())
//
//    }

    fun updateProperties(dto: CommonDTO<DATA>, daoEntity : ENTITY){
        propertyBinder.update(dto.injectedDataModel,daoEntity, UpdateMode.ENTITY_TO_MODEL)
    }

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

    fun setClassData(
        dtoClass : KClass<out CommonDTO<DATA>>,
        dataClass : KClass<out DataModel>,
        dao: LongEntityClass<ENTITY>
    )  {
        dtoModelClass = dtoClass
        dataModelClass = dataClass
        daoModel = dao
    }
    
}

