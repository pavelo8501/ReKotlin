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
import po.exposify.dto.models.SourceObject
import po.misc.interfaces.Identifiable
import po.misc.interfaces.IdentifiableModule


class BindingHub<DTO, D, E, F_DTO, FD, FE>(
    val hostingDTO: CommonDTO<DTO, D, E>,
    val moduleType : ModuleType.BindingHub = ModuleType.BindingHub
): IdentifiableModule by moduleType
        where  DTO : ModelDTO, D: DataModel, E: LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity
{
    val qualifiedName: String = "BindingHub[${hostingDTO.componentName}]"
    val dtoClass : DTOBase<DTO, D, E>  get() = hostingDTO.dtoClass
    val daoService: DAOService<DTO, D, E> get() = hostingDTO.daoService


    private val complexDelegateMap : MutableMap<String, ComplexDelegate<DTO, D, E, F_DTO, FD, FE>> = mutableMapOf()
    private val responsiveDelegates : MutableMap<String, ResponsiveDelegate<DTO, D, E, *>>  = mutableMapOf()
    private val relationDelegates : MutableMap<String, RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>> = mutableMapOf()

    fun setRelationBinding(
        binding: RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>
    ): RelationDelegate<DTO, D, E,  F_DTO,  FD,  FE, *>
    {
        relationDelegates[binding.completeName] = binding
        return binding
    }

    fun setComplexBinding(
        binding: ComplexDelegate<DTO, D, E, F_DTO,  FD,  FE>
    )
    : ComplexDelegate<DTO, D, E, F_DTO,  FD,  FE>
    {
        complexDelegateMap[binding.completeName] = binding
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
     * updateEntity update entity properties
     * relations are not yet assigned
     */
    internal fun updateEntity(entity: E) {
        responsiveDelegates.values.forEach { responsiveDelegate ->
            responsiveDelegate.updateDTOProperty(hostingDTO.dataModel, entity)
        }
    }

    /***
     * createChildByData child DTO creation with parent finalized
     */
    internal fun createChildByData(){
        getRelationDelegates(hostingDTO.cardinality).forEach {relationDelegate->
            relationDelegate.createByData()
        }
    }




//    internal fun <F_DTO2: ModelDTO, FD2: DataModel, FE2: LongEntity> createByData(
//        childDTO: CommonDTO<F_DTO2, FD2, FE2>,
//        bindFn: (FE2) -> Unit
//    ){
//        val insertedEntity = childDTO.daoService.saveWithParent { entity->
//            childDTO.bindingHub.responsiveDelegates.values.forEach {responsiveDelegate->
//                responsiveDelegate.updateDTOProperty(childDTO.dataModel, entity)
//            }
//            childDTO.bindingHub.getRelationDelegates().forEach { relationDelegate ->
//                relationDelegate.createByData()
//            }
//            bindFn.invoke(entity)
//        }
//        childDTO.provideInsertedEntity(insertedEntity)
//      //  childDTO.provideInsertedEntity(insertedEntity)
//    }

    fun createByEntity(){
        responsiveDelegates.values.forEach {responsiveDelegate->
            responsiveDelegate.updateDTOProperty(hostingDTO.getEntity(hostingDTO))
        }
        getRelationDelegates(hostingDTO.cardinality).forEach {relationDelegate->
            relationDelegate.createByEntity()
        }
    }

    fun updateFromData(data:D){
        responsiveDelegates.values.forEach {
            it.updateDTOProperty(data, hostingDTO.getEntity(hostingDTO))
        }
        getRelationDelegates(hostingDTO.cardinality).forEach {relationDelegate->
            relationDelegate.updateFromData(data)
        }
    }
}