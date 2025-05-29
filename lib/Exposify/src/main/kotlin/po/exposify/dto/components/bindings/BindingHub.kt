package po.exposify.dto.components.bindings

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.bindings.helpers.createEntity
import po.exposify.dto.components.bindings.property_binder.delegates.ComplexDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.components.proFErty_binder.EntityUpdateContainer
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Identifiable


class BindingHub<DTO, D, E, F_DTO, FD, FE>(
    val dtoClass : DTOBase<DTO, D, E>
): Identifiable
        where  DTO : ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{

    
    override val qualifiedName: String = "BindingHub[${dtoClass.qualifiedName}]"

    internal val complexDelegateMap : MutableMap<String, ComplexDelegate<DTO, D, E, *, *, *, *, *>> = mutableMapOf()
    private val responsiveDelegates : MutableMap<String, ResponsiveDelegate<DTO, D, E, *>>  = mutableMapOf()
    private val relationDelegates : MutableMap<String, RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>> = mutableMapOf()

    fun setRelationBinding(
        binding: RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>
    ): RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>
    {
        relationDelegates[binding.qualifiedName] = binding
        return binding
    }

    fun <FE : LongEntity> setBinding(
        binding: ComplexDelegate<DTO, D, E, *, *, FE, *, *>
    )
    : ComplexDelegate<DTO, D, E, *, *, FE, *, *>
    {
        complexDelegateMap[binding.qualifiedName] = binding
        return binding
    }

    fun setBinding(
        binding : ResponsiveDelegate<DTO, D, E, *>
    ): ResponsiveDelegate<DTO, D, E, *> {
        responsiveDelegates[binding.propertyName] =  binding
        return  binding
    }

    fun getResponsiveDelegates(): List<ResponsiveDelegate<DTO, D, E, *>>{
        return this.responsiveDelegates.values.toList()
    }

    fun getRelationDelegates(cardinality: Cardinality = Cardinality.ONE_TO_MANY): List<RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>>{
        return this.relationDelegates.values.filter { it.cardinality == cardinality }.toList()
    }

//    fun  getForeignDataModels(cardinality: Cardinality, dataModel : D): List<FD>{
//        val delegate = relationDelegates.values.firstOrNull { it.cardinality == cardinality }
//        val dataModels =  delegate?.getForeignDataModels(dataModel)?:emptyList()
//        return  dataModels
//    }
//    fun getForeignEntities(cardinality: Cardinality, entity : E):List<FE>{
//        val delegate = relationDelegates.values.firstOrNull { it.cardinality == cardinality }
//        val entities =  delegate?.getForeignEntities(entity)?:emptyList()
//        return entities
//    }

    /***
        INSERT Statement
        1) Updating DTO properties from data
        2) Creating Entity (entity in this case should be complete)
        3) Creating DTO without data
        4) Launching update on child binding hub together with its data
    ***/
    fun createFromData(dto: CommonDTO<DTO, D, E>){
        responsiveDelegates.values.forEach { it.updateDTOProperty(dto.dataModel) }
        if(dto.dtoClass is RootDTO){
            createEntity(dto)
        }
        relationDelegates.values.forEach { it.createFromData(dto.dataModel) }
    }

    fun createFromEntity(dto: CommonDTO<DTO, D, E>):CommonDTO<DTO, D, E>{
        responsiveDelegates.values.forEach { it.updateDTOProperty(dto.entity) }
        getRelationDelegates(dto.cardinality).forEach {
            it.createFromEntity(dto.entity)
        }
        return dto
    }


    fun updateEntity(entity: E):E{
        responsiveDelegates.values.forEach { it.updateEntityProperty(entity) }
        return entity
    }

    /**
     * Recursive insert from child perspective
     */
//    fun createFromData(data: D, parentDTO: CommonDTO<F_DTO, FD, FE> ){
//        responsiveDelegates.values.forEach { it.updateDTOProperty(data) }
//        parentDTO.createEntityWithParent()
//        relationDelegates.values.forEach { delegate ->
//            delegate.getForeignDataModels(data).forEach { childData ->
//                val childDto =  delegate.childModel.createDTO(childData)
//                childDto.bindingHub.createFromData(
//                    childData,
//                    childDto.castOrOperationsEx("Cast of childDto in createFromData(data: D, parentDTO: CommonDTO<F_DTO, FD, FE>) failed")
//                )
//            }
//        }
//    }



}