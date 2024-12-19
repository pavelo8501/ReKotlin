package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.PropertyBinderV2
import po.db.data_service.binder.PropertyBindingV2
import po.db.data_service.binder.RelationshipBinder
import po.db.data_service.dto.DTOClassV2
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KClass

class DTOConfigV2 {

    var dtoModelClass: KClass<out DTOModelV2>? = null
    var dataModelClass: KClass<out DataModel>? = null

    var daoModel:LongEntityClass<LongEntity>? = null

    private var propertyBinder : PropertyBinderV2? = null
    private var relationBinder : RelationshipBinder? = null

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    fun <DM: DataModel, E: LongEntity>propertyBindings(vararg props: PropertyBindingV2<DM, E, *>) {
        PropertyBinderV2().let {
            it.setProperties(props.toList())
            propertyBinder = it
        }
    }

    fun childBinding(childDtoModel : DTOClassV2, type: OrdinanceType){
      //  childDtoModel.setup()
        RelationshipBinder().let {
            it.addChildBinding(childDtoModel,type)
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