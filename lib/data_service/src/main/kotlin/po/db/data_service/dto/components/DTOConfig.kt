package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.binder.*
import po.db.data_service.binder.RelationshipBinder
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


class DTOConfig<DATA, ENTITY>(
    val  parent : DTOClass<DATA,ENTITY>,
    var relationBinder : RelationshipBinder<ENTITY,DATA>,
    withConfig: (RelationshipBinder<ENTITY,DATA>.()-> Unit)? = null
) where  ENTITY : LongEntity, DATA: DataModel  {

    init {

    }

    var dtoModelClass: KClass<out CommonDTO<DATA>>? = null
    var dataModelClass: KClass<out DataModel>? = null

    var daoModel:LongEntityClass<ENTITY>? = null

    var propertyBinder : PropertyBinder<DATA,ENTITY,*>? = null
        private set

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    fun propertyBindings(vararg props: PropertyBinding<DATA, ENTITY, *>) {
        PropertyBinder<DATA,ENTITY, Any>().let {
            it.setProperties(props.toList() as List<PropertyBinding<DATA, ENTITY, Any>>)
            propertyBinder = it
        }
    }

    fun updateProperties(dataModel: DATA, daoEntity : ENTITY){
        propertyBinder?.updateProperties(dataModel, daoEntity, UpdateMode.ENTNTITY_TO_MODEL)
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

