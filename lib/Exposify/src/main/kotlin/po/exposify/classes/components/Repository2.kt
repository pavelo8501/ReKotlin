package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.binder.BindingContainer2
import po.exposify.binder.MultipleChildContainer2
import po.exposify.binder.SingleChildContainer2
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO2
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO


class SingleRepository2<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    parent : CommonDTO2<DTO, *, *>,
    childClass: DTOClass2<CHILD_DTO>,
    private val binding : SingleChildContainer2<DTO, CHILD_DATA, CHILD_ENTITY>
): RepositoryBase2<DTO, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>(parent, childClass, binding)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity {

    override val repoName: String =  "Repository[${parent.registryItem.typeKeyCombined}/Single]"

    val childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>
        get() = binding.childModel


    override fun setReferenced(childEntity:CHILD_ENTITY, parentEntity: LongEntity){
        binding.referencedOnProperty?.set(childEntity, parentEntity)
    }

    override fun getReferences(parentEntity: LongEntity): List<CHILD_ENTITY> {
        binding.byProperty.get(parentEntity)?.let {
            return  listOf<CHILD_ENTITY>(it)
        }?: return emptyList()
    }

    override fun extractDataModel(dataModel:DataModel): List<CHILD_DATA>{
        val result =  childModel.factory.extractDataModel(binding.sourcePropertyWrapper.extractNullable(), dataModel)
        return if(result!=null){
            listOf<CHILD_DATA>(result)
        }else{
            emptyList()
        }
    }
}

class MultipleRepository2<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(
    parent : CommonDTO2<DTO, *, *>,
    childClass: DTOClass2<CHILD_DTO>,
    private val binding : MultipleChildContainer2<DTO,CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>
): RepositoryBase2<DTO, CHILD_DTO, CHILD_DATA, CHILD_ENTITY>(parent, childClass, binding)
    where DTO: ModelDTO, CHILD_DTO: ModelDTO,  CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
{

        override val repoName: String =  "Repository[${parent.registryItem.typeKeyCombined}/Multiple]"
        override fun setReferenced(childEntity:CHILD_ENTITY, parentEntity: LongEntity){
            binding.referencedOnProperty?.set(childEntity, parentEntity)
        }
        override fun getReferences(parentEntity: LongEntity): List<CHILD_ENTITY> {
            binding.byProperty.get(parentEntity).let {
                return   it.toList()
            }
        }
        override fun extractDataModel(dataModel: DataModel): List<CHILD_DATA>
                = binding.childModel.factory.extractDataModel(binding.sourceProperty, dataModel)
        }


sealed class RepositoryBase2<DTO, CHILD_DTO,  CHILD_DATA, CHILD_ENTITY>(
    val parent : CommonDTO2<DTO, *, *>,
    val childClass: DTOClass2<CHILD_DTO>,
    private val  bindingContainer : BindingContainer2<DTO, CHILD_DATA, CHILD_ENTITY>
) where DTO : ModelDTO, CHILD_DTO: ModelDTO,  CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity {

    abstract val repoName: String

    abstract fun extractDataModel(dataModel: DataModel): List<CHILD_DATA?>
    abstract fun setReferenced(childEntity: CHILD_ENTITY, parentEntity: LongEntity)
    abstract fun getReferences(parentEntity: LongEntity): List<CHILD_ENTITY>

    var initialized: Boolean = false
    val dtoList = mutableListOf<CommonDTO2<CHILD_DTO, CHILD_DATA, CHILD_ENTITY>>()

    val childFactory: DTOFactory2<CHILD_DTO, *>
        get() {
            return childClass.config.dtoFactory
        }

}