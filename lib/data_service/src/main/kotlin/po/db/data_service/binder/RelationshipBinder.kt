package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.common.enums.InitStatus
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.components.DTORepo
import po.db.data_service.dto.interfaces.DTOEntity
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO

import po.db.data_service.scope.service.models.DaoFactory
import javax.xml.crypto.Data
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


enum class OrdinanceType{
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}

data class BindingKey(
    val ordinanceType: OrdinanceType,
    val propertyName : String
)


open class BindingContainer<PARENT, CHILD, DM>(
    parentDTOModel: DTOClass<PARENT>,
    val childDTOModel: DTOClass<CHILD>,
    private val type: OrdinanceType,
    private val byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
    val thisKey :BindingKey
) : BindingContainerBase<PARENT, CHILD, DM>(parentDTOModel,childDTOModel,type) where PARENT:LongEntity, CHILD : LongEntity, DM:DataModel   {

    var dataSourceProperty : KProperty1<DM, Iterable<DataModel>>? = null

    fun createChild(parent : CommonDTO,  dataModel : DataModel):CommonDTO?{
//        return  childDTOModel.create(dataModel).let {dto->
//            dataSourceProperty?.let {
//
////                it.get(dataModel).forEach {
////                    childDTOModel.create(it)
////                }
//            }
//
////            daoFactory.new<CHILD>(dto,  childDTOModel)?.let{childEntity->
////                parent.getEntityDAO<PARENT>()?.let {parentEntity->
////                    referencedOnProperty.set(childEntity, parentEntity)
////                    dto.updateDAO(childEntity)
////                }
////            }
//            dto
//        }
        return null
    }

    fun loadChild(parentDTO : CommonDTO, daoFactory: DaoFactory):List<CommonDTO> {
        val result = mutableListOf<CommonDTO>()
        try {
            daoFactory.dbQuery {
//                parentDTO.getEntityDAO<PARENT>()?.let{parentEntity->
//                    val childEntities = this.byProperty.get(parentEntity)
//                    childEntities.forEach { childEntity ->
////                        val childDto = childDTOModel.create(childEntity)
////                        result.add(childDto)
//                    }
//                }
            }
        }catch (ex:Exception){
            println(ex.message)
        }
        return result
    }

    fun addDTORepository(dto:CommonDTO):CommonDTO{
        try {
            dto.also {
                it.addRepository(thisKey,DTORepo())
            }
        }catch (ex:Exception){
            dto.addError(ex.message?:"UnknownException")
        }
        return dto
    }

    fun getDataModel(parentModel:DataModel):List<DataModel>{
        try {
            dataSourceProperty?.let {
                @Suppress("UNCHECKED_CAST")
                parentModel as DM
                @Suppress("UNCHECKED_CAST")
                return it.get(parentModel) as List<DataModel>
            }
        }catch (ex:Exception){
            println(ex.message)
        }
        return emptyList()
    }

    fun withChildModel(parentDTO:CommonDTO, body: DTOClass<CHILD>.(CommonDTO)->Unit ){
        childDTOModel.body(parentDTO)
    }

    fun setDataSource(sourceProperty :  KProperty1<DM, Iterable<DataModel>>){
        dataSourceProperty = sourceProperty
    }

}

sealed class BindingContainerBase<PARENT : LongEntity, CHILD : LongEntity, DM:DataModel>(
    parentModel: DTOClass<PARENT>,
    childModel: DTOClass<CHILD>,
    type: OrdinanceType,
){
    private var  container : BindingContainer<PARENT,CHILD, DM>? = null

    fun <T, DATA: DataModel> DTOClass<PARENT>.createChildReceive(body: T.()->CommonDTO){
        val a = 10
    }
    companion object{
        fun <PARENT:LongEntity, CHILD:LongEntity, DM:DataModel> createContainer(
            parentDTOModel: DTOClass<PARENT>,
            childDtoModel: DTOClass<CHILD>,
            type: OrdinanceType,
            byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
            referencedOnProperty: KMutableProperty1<CHILD, PARENT>
        ): BindingContainer<PARENT, CHILD, DM>{
            val key  = BindingKey(type, byProperty.name)
            val newContainer =  object :  BindingContainer<PARENT, CHILD, DM>(parentDTOModel, childDtoModel,type, byProperty, key){}
            return newContainer
        }
    }
}

class RelationshipBinder<PARENT> (
    private val parentDTOModel: DTOClass<PARENT>
)  where PARENT : LongEntity {

    private val childBindings = mutableMapOf<BindingKey, BindingContainer<PARENT,*,*>>()

    val lastKeys = mutableListOf<BindingKey>()

    fun getBindingList():List<BindingContainer<PARENT, *,*>>{
        return childBindings.values.toList()
    }

    fun getBinding(key: BindingKey):BindingContainer<PARENT, *,*>?{
        return childBindings[key]
    }

    fun getKeys():List<BindingKey>{
        return childBindings.keys.toList()
    }

    fun <CHILD, DM> addBinding(
        childDtoModel: DTOClass<CHILD>,
        byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
        referencedOnProperty: KMutableProperty1<CHILD, PARENT>,
        context: (BindingContainer<PARENT, CHILD, DM>.() -> Unit)? = null
    ): BindingContainer<PARENT, CHILD, DM> where CHILD : LongEntity, DM: DataModel{
       val type = OrdinanceType.ONE_TO_MANY
       if(childDtoModel.initStatus == InitStatus.UNINITIALIZED){
           childDtoModel.initialization(parentDTOModel.dbConnection)
       }
        val key = BindingKey(type, byProperty.name)
        val container =  BindingContainerBase.createContainer<PARENT, CHILD, DM>(parentDTOModel, childDtoModel, type,  byProperty, referencedOnProperty)
        childBindings[key] = container
        lastKeys.add(key)
        if(context!= null){
            container.context()
        }
        return container
    }



    fun getDependantTables():List<IdTable<Long>>{
        val result = mutableListOf<IdTable<Long>>()
        childBindings.values.forEach {container ->
            result.add(container.childDTOModel.daoModel.table)
            container.childDTOModel.getAssociatedTables()
        }
        return result
    }
}
