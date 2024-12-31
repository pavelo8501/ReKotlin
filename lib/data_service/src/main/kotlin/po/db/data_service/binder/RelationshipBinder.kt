package po.db.data_service.binder

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.common.enums.InitStatus
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.components.HostableRepo
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.HostableDTO

import po.db.data_service.scope.service.models.DaoFactory
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


open class BindingContainer<PARENT, CHILD, DM>(
    val parentDTOModel: DTOClass<PARENT>,
    val childDTOModel: DTOClass<CHILD>,
    val type: OrdinanceType,
    val byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
    val referencedOnProperty : KMutableProperty1<CHILD, PARENT>,
    val thisKey :BindingKey
) : BindingContainerBase<PARENT, CHILD, DM>(parentDTOModel, childDTOModel,type) where PARENT:LongEntity, CHILD : LongEntity, DM:DataModel   {

    var dataSourceProperty : KProperty1<DM, Iterable<DataModel>>? = null

    fun attachToParent(parentEntity : PARENT, parentDTO :  HostableDTO<PARENT> ){
        withChildModel {
            parentDTO.repos[thisKey]?.getAll()?.forEach {it as HostableDTO<CHILD>
                childDTOModel.daoModel.new {
                    it.updateDTO(this, UpdateMode.MODEL_TO_ENTITY)
                    referencedOnProperty.set(this , parentEntity)
                }
            }
        }
        parentEntity.flush()
    }

    fun loadChild(parentDTO : HostableDTO<PARENT>, daoFactory:DaoFactory):List<CommonDTO> {
        val result = mutableListOf<CommonDTO>()
        try {
            daoFactory.dbQuery {
                withChildModel(parentDTO){
                    parentDTO.entityDAO?.let {
                        val childEntities = this@BindingContainer.byProperty.get(it)
                        childEntities.forEach { childEntity ->
                            childDTOModel.create(childEntity)?.let {childDto->
                                //parentDTO.addChildDTO(childDto,this@BindingContainer.thisKey )
                            }
                        }
                    }
                }
            }
        }catch (ex:Exception){
            println(ex.message)
        }
        return result
    }


    fun createChildEntities(dto : HostableDTO<PARENT>): HostableDTO<PARENT> {
        withChildModel{
            //val newRepo = HostableRepo(this, thisKey,dto.getId())
            val newRepo =  this.createRepository(thisKey,0).also {repository->
                val childModels = getDataModel(dto.dataModel)
                childModels.forEach {childDataModel->
                    val hostable = this.copyAsHostableDTO(this.dtoFactory.constructDtoEntity(childDataModel)!!)
                    hostable.initialize(this.conf)
                    repository.add(hostable)
                }
                dto.addRepo(thisKey, repository as HostableRepo<PARENT>)
            }
        }
        return dto
    }

    fun getDataModel(parentModel:DataModel):List<DataModel>{
        try {
            dataSourceProperty?.let {
                @Suppress("UNCHECKED_CAST")
                parentModel as DM
                return it.get(parentModel) as List<DataModel>
            }
        }catch (ex:Exception){
            println(ex.message)
        }
        return emptyList()
    }

    fun withChildModel(dto: HostableDTO<PARENT>, body: DTOClass<CHILD>.(HostableDTO<PARENT>)->Unit ){
        childDTOModel.body(dto)
    }

    fun withChildModel(body: DTOClass<CHILD>.()->Unit ){
        childDTOModel.body()
    }

    fun executeOnChildModel(parentDTO:CommonDTO, body: DTOClass<CHILD>.(CommonDTO)->  DTOClass<CHILD> ){
        executeOnChildModel(parentDTO, body)
    }

    fun setDataSource(sourceProperty :  KProperty1<DM, Iterable<DataModel>>){
        dataSourceProperty = sourceProperty
    }
}


sealed class BindingContainerBase<PARENT : LongEntity, CHILD : LongEntity, DM: DataModel>(
    parentModel: DTOClass<PARENT>,
    childModel: DTOClass<CHILD>,
    type: OrdinanceType,
){

    companion object{
        fun <PARENT:LongEntity, CHILD:LongEntity, DM:DataModel> createContainer(
            parentDTOModel: DTOClass<PARENT>,
            childDtoModel: DTOClass<CHILD>,
            type: OrdinanceType,
            byProperty: KProperty1<PARENT, SizedIterable<CHILD>>,
            referencedOnProperty: KMutableProperty1<CHILD, PARENT>
        ): BindingContainer<PARENT, CHILD, DM>{
            val key  = BindingKey(type, byProperty.name)
            val newContainer =  object :  BindingContainer<PARENT, CHILD, DM>(parentDTOModel, childDtoModel,type, byProperty,referencedOnProperty, key){}
            return newContainer
        }
    }
}

class RelationshipBinder<PARENT> (
    private val parentDTOModel: DTOClass<PARENT>
)  where PARENT : LongEntity {

    private val childBindings = mutableMapOf<BindingKey, BindingContainer<PARENT,*,*>>()

    val lastKeys = mutableListOf<BindingKey>()

    fun bindings():List<BindingContainer<PARENT, *,*>>{
        return childBindings.values.toList()
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
        BindingContainerBase.createContainer<PARENT, CHILD, DM>(parentDTOModel, childDtoModel, type,  byProperty, referencedOnProperty).let {
            childBindings[key] = it
            lastKeys.add(key)
            context?.invoke(it)
            return it
        }
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
