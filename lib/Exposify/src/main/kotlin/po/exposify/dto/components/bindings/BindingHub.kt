package po.exposify.dto.components.bindings

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.components.DAOService
import po.exposify.dto.components.bindings.property_binder.delegates.AttachedForeign
import po.exposify.dto.components.bindings.property_binder.delegates.ComplexDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ParentDelegate
import po.exposify.dto.components.bindings.property_binder.delegates.ResponsiveDelegate
import po.exposify.dto.components.bindings.relation_binder.delegates.RelationDelegate
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.ModuleType
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableModule


class BindingHub<DTO, D, E, F_DTO, FD, FE>(
    val hostingDTO: CommonDTO<DTO, D, E>,
    val moduleType : ModuleType.BindingHub = ModuleType.BindingHub
): IdentifiableModule by moduleType
        where  DTO : ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    val qualifiedName: String = "BindingHub[${hostingDTO.type.componentName}]"
    val dtoClass : DTOBase<DTO, D, E>  get() = hostingDTO.dtoClass
    val daoService: DAOService<DTO, D, E> get() = hostingDTO.daoService


    private val complexDelegateMap : MutableMap<String, ComplexDelegate<DTO, D, E, F_DTO, FD, FE>> = mutableMapOf()
    private val responsiveDelegates : MutableMap<String, ResponsiveDelegate<DTO, D, E, *>>  = mutableMapOf()
    private val relationDelegates : MutableMap<String, RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>> = mutableMapOf()

    fun setRelationBinding(
        binding: RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>
    ): RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>
    {
        relationDelegates[binding.componentType.completeName] = binding
        return binding
    }

    fun setComplexBinding(
        binding: ComplexDelegate<DTO, D, E, F_DTO,  FD,  FE>
    )
    : ComplexDelegate<DTO, D, E, F_DTO,  FD,  FE>
    {
        complexDelegateMap[binding.componentType.completeName] = binding
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

    fun getAttachedForeignDelegates():List<AttachedForeign<DTO, D, E, F_DTO, FD, FE>>{
        return complexDelegateMap.values.filterIsInstance<AttachedForeign<DTO, D, E, F_DTO, FD, FE>>()
    }

    fun getParentDelegates():List<ParentDelegate<DTO, D, E, F_DTO, FD, FE>>{
        return complexDelegateMap.values.filterIsInstance<ParentDelegate<DTO, D, E, F_DTO, FD, FE>>()
    }

    /***
        INSERT Statement
        1) Updating DTO properties from data
        2) Creating Entity (entity in this case should be complete)
        3) Creating DTO without data
        4) Launching update on child binding hub together with its data
    ***/
//    internal fun createFromData(data: D){
//        if(dtoClass is RootDTO){
//           val insertedEntity = daoService.save { entity->
//                responsiveDelegates.values.forEach { it.updateDTOProperty(data, entity) }
//            }
//            hostingDTO.provideInsertedEntity(insertedEntity)
//        }else{
//            responsiveDelegates.values.forEach { it.updateDTOProperty(data, null) }
//        }
//        relationDelegates.values.forEach { it.createFromData(data) }
//    }


    internal fun createByData(){
        val insertedEntity = daoService.save { entity ->
            responsiveDelegates.values.forEach {responsiveDelegate->
                responsiveDelegate.updateDTOProperty(hostingDTO.dataModel, entity)
            }
        }
        hostingDTO.provideInsertedEntity(insertedEntity)
        getRelationDelegates(hostingDTO.cardinality).forEach {relationDelegate->
            relationDelegate.createByData()
        }
    }

    internal fun <F_DTO2: ModelDTO, FD2: DataModel, FE2: LongEntity> createByData(
        childDTO: CommonDTO<F_DTO2, FD2, FE2>,
        bindFn: (FE2) -> Unit
    ){
        val insertedEntity = childDTO.daoService.saveWithParent { entity->
            childDTO.bindingHub.responsiveDelegates.values.forEach {responsiveDelegate->
                responsiveDelegate.updateDTOProperty(childDTO.dataModel, entity)
            }
            bindFn.invoke(entity)
            childDTO.bindingHub.getRelationDelegates().forEach { relationDelegate ->
                relationDelegate.createByData()
            }
        }
        childDTO.provideInsertedEntity(insertedEntity)
    }

    fun createByEntity(){
        responsiveDelegates.values.forEach {responsiveDelegate->
            responsiveDelegate.updateDTOProperty(hostingDTO.entity)
        }
        getRelationDelegates(hostingDTO.cardinality).forEach {relationDelegate->
            relationDelegate.createByEntity()
        }
    }

    fun updateFromData(data:D){
        responsiveDelegates.values.forEach {
            it.updateDTOProperty(data, hostingDTO.entity)
        }
        getRelationDelegates(hostingDTO.cardinality).forEach {relationDelegate->
            relationDelegate.updateFromData(data)
        }
    }

    fun updateEntity2(entity: E):E{
        responsiveDelegates.values.forEach {
            it.updateDTOProperty(entity)
        }
        return entity
    }

}