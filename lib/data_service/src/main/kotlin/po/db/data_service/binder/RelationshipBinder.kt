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

data class DataSource<PARENT, CHILD>(
    val dataSourceProperty : KProperty1<PARENT, Iterable<CHILD>>,
) where PARENT : DataModel,  CHILD: DataModel{
   private val sourceItems = mutableListOf<DataModel>()

    fun setSourceItems(items : List<DataModel>){
        sourceItems.addAll(items)
    }
}

open class BindingContainer<PARENT, CHILD>(
    parentDTOModel: DTOClass<PARENT>,
    val childDTOModel: DTOClass<CHILD>,
    val type: OrdinanceType,
    val byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
) : BindingContainerBase<PARENT, CHILD>(parentDTOModel,childDTOModel,type) where PARENT:LongEntity, CHILD : LongEntity   {

    var dataSourceProperty : KProperty1<out DataModel, Iterable<DataModel>>? = null

    val dataSources = mutableListOf<DataSource<*,*>>()

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
                parentDTO.getEntityDAO<PARENT>()?.let{parentEntity->
                    val childEntities = this.byProperty.get(parentEntity)
                    childEntities.forEach { childEntity ->
//                        val childDto = childDTOModel.create(childEntity)
//                        result.add(childDto)
                    }
                }
            }
        }catch (ex:Exception){
            println(ex.message)
        }
        return result
    }

    fun addDTORepository(dto:CommonDTO):CommonDTO{
        try {
            dto.also {
                it.addRepository(DTORepo(byProperty, childDTOModel))
            }
        }catch (ex:Exception){
            dto.addError(ex.message?:"UnknownException")
        }
        return dto
    }

    inline fun <reified PARENT: DataModel, CHILD:DataModel>  setDataSource(
        source: KProperty1<PARENT, Iterable<CHILD>>,
        context: DataSource<PARENT, CHILD>.()->Unit
    ){
       val dataSource = DataSource(source)
       dataSource.context()
    }
}

sealed class BindingContainerBase<PARENT : LongEntity, CHILD : LongEntity>(
    parentModel: DTOClass<PARENT>,
    childModel: DTOClass<CHILD>,
    type: OrdinanceType,
){
    private var  container : BindingContainer<PARENT,CHILD>? = null

    fun <T, DATA: DataModel> DTOClass<PARENT>.createChildReceive(body: T.()->CommonDTO){
        val a = 10
    }
    companion object{
        fun <ENTITY:LongEntity, CHILD:LongEntity> createContainer(
            parentDTOModel: DTOClass<ENTITY>,
            childDtoModel: DTOClass<CHILD>,
            type: OrdinanceType,
            byProperty: KProperty1<ENTITY, SizedIterable<CHILD>>,
            referencedOnProperty: KMutableProperty1<CHILD, ENTITY>,
        ): BindingContainer<ENTITY, CHILD>{
            val newContainer =  object :  BindingContainer<ENTITY, CHILD>(parentDTOModel, childDtoModel,type, byProperty){}
            return newContainer
        }
    }
}

class RelationshipBinder<ENTITY> (
    private val parentDTOModel: DTOClass<ENTITY>
)  where ENTITY : LongEntity {

    private val childBindings = mutableMapOf<BindingKey, BindingContainer<ENTITY, *>>()

    val lastKeys = mutableListOf<BindingKey>()

    fun getBindingList():List<BindingContainer<ENTITY, *>>{
        return childBindings.values.toList()
    }

    fun getBinding(key: BindingKey):BindingContainer<ENTITY, *>?{
        return childBindings[key]
    }

    fun getKeys():List<BindingKey>{
        return childBindings.keys.toList()
    }

    fun <CHILD> addBinding(
        childDtoModel: DTOClass<CHILD>,
        byProperty: KProperty1<ENTITY, SizedIterable<CHILD>>,
        referencedOnProperty: KMutableProperty1<CHILD, ENTITY>,
        context: (BindingContainer<ENTITY, CHILD>.() -> Unit)? = null
    ): BindingContainer<ENTITY, CHILD> where CHILD : LongEntity{
       val type = OrdinanceType.ONE_TO_MANY
       if(childDtoModel.initStatus == InitStatus.UNINITIALIZED){
           childDtoModel.initialization(parentDTOModel.dbConnection)
       }
       val container =  BindingContainerBase.createContainer<ENTITY, CHILD>(parentDTOModel, childDtoModel, type,  byProperty, referencedOnProperty)
       val key = BindingKey(type, byProperty.name)
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
