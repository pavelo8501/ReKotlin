package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import po.db.data_service.binder.*
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class DTOConfigV2() {

    var dtoModelClass: KClass<out DTOModelV2>? = null
    var dataModelClass: KClass<out DataModel>? = null

    var daoModel:LongEntityClass<LongEntity>? = null

    var propertyBinder : PropertyBinderV2? = null
        private set

    var relationBinder  = RelationshipBinder(parent)

    var dataModelConstructor : (() -> DataModel)? = null
        private set

    private var _parent: DTOClass? = null
    val parent: DTOClass
        get(){return  _parent!!}

    fun setParent(parent : DTOClass){
        _parent = parent
    }

    fun <DM: DataModel, E: LongEntity>propertyBindings(vararg props: PropertyBindingV2<DM, E, *>) {
        PropertyBinderV2().let {
            it.setProperties(props.toList())
            propertyBinder = it
        }
    }

    fun updateProperties(dataModel: DataModel, daoEntity : LongEntity){
        propertyBinder?.updateProperties(dataModel, daoEntity, UpdateMode.ENTITY_TO_MODEL)
    }

    inline  fun <reified PARENT,  reified CHILD> DTOClass.childBinding(
        parentDTOModel: DTOClass,
        childDtoModel: DTOClass,
        byProperty: KProperty1<LongEntity, SizedIterable<CHILD>>, type: OrdinanceType
    ) where PARENT :  LongEntity, CHILD: LongEntity{
        if(!childDtoModel.initialized) {
            parent.onDtoInitializationCallback?.let { callback ->
                childDtoModel.initialization(callback)
            }
        }
        RelationshipBinder(parentDTOModel).let {
            it.addChildBinding<PARENT, CHILD>(parentDTOModel, childDtoModel, byProperty, type)
            relationBinder = it
        }
    }

    fun setDataModelConstructor(dataModelConstructor: () -> DataModel){
        this.dataModelConstructor = dataModelConstructor
    }

    fun <DTO>setClassData(
        dtoClass : KClass<DTO>,
        dataClass : KClass<out DataModel>,
        dao: LongEntityClass<LongEntity>
    ) where DTO: DTOModelV2 {
        dtoModelClass = dtoClass
        dataModelClass = dataClass
        daoModel = dao
    }


}