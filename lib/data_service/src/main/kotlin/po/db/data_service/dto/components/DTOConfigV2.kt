package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.binder.*
import po.db.data_service.dto.DTOClassV2
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KClass

class DTOConfigV2 {

    var dtoModelClass: KClass<out DTOModelV2>? = null
    var dataModelClass: KClass<out DataModel>? = null

    var daoModel:LongEntityClass<LongEntity>? = null

    var propertyBinder : PropertyBinderV2? = null
        private set

    var relationBinder  = RelationshipBinder()
        private set

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    fun <DM: DataModel, E: LongEntity>propertyBindings(vararg props: PropertyBindingV2<DM, E, *>) {
        PropertyBinderV2().let {
            it.setProperties(props.toList())
            propertyBinder = it
        }
    }

    fun updateProperties(dataModel: DataModel, daoEntity : LongEntity){
        propertyBinder?.updateProperties(dataModel, daoEntity, UpdateMode.ENTITY_TO_MODEL)
    }


    fun childBinding(childDtoModel : DTOClassV2, byProperty:SizedIterable<LongEntity>, type: OrdinanceType){
      //  childDtoModel.setup()
        RelationshipBinder().let {
            it.addChildBinding(childDtoModel,byProperty,  type)
            relationBinder = it
        }
    }

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

    fun <DTO: DTOModelV2>setClassData(dtoClass : KClass<DTO>, dataClass : KClass<out DataModel>, dao: LongEntityClass<LongEntity>){
        dtoModelClass = dtoClass
        dataModelClass = dataClass
        daoModel = dao
    }

}