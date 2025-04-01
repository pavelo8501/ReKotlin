package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.binders.relationship.MultipleChildContainer2
import po.exposify.binders.relationship.SingleChildContainer2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.extensions.update
import po.exposify.dto.interfaces.ModelDTO


class SingleRepository2<DTO, DATA, ENTITY,  CHILD_DTO>(
    val parent : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass2<CHILD_DTO>,
    private val binding : SingleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>
): RepositoryBase2<DTO,CHILD_DTO>(childClass)
        where DTO: ModelDTO, CHILD_DTO: ModelDTO,  DATA: DataModel, ENTITY : LongEntity  {

    override val repoName: String =  "Repository[${parent.registryItem.typeKeyCombined}/Single]"

    override suspend fun update() {
        val dataModel = binding.sourcePropertyWrapper.extract().get(parent.dataModel)

    }

    val childModel : DTOClass2<CHILD_DTO>
        get() = binding.childClass

}

class MultipleRepository2<DTO, DATA, ENTITY, CHILD_DTO>(
    val parent : CommonDTO<DTO, DATA, ENTITY>,
    val childClass: DTOClass2<CHILD_DTO>,
    private val binding : MultipleChildContainer2<DTO, DATA, ENTITY, CHILD_DTO>,
    private val casted: MultipleChildContainer2<DTO, DataModel, LongEntity, CHILD_DTO>
): RepositoryBase2<DTO, CHILD_DTO>(childClass)
    where DTO: ModelDTO, CHILD_DTO: ModelDTO, DATA: DataModel, ENTITY : LongEntity {

    override val repoName: String = "Repository[${parent.registryItem.typeKeyCombined}/Multiple]"
    override suspend fun update() {
        val dataModels =  binding.ownDataModelsProperty.get(parent.dataModel)
        dataModels.filter{it.id == 0L}.let {
           val result =  childClass.update<CHILD_DTO>(it)

        }

    }
}


class RootRepository2<DTO, CHILD_DTO>(
    val  dtoClass: DTOClass2<CHILD_DTO>, override val repoName: String
): RepositoryBase2<DTO, CHILD_DTO>(dtoClass) where DTO : ModelDTO, CHILD_DTO : ModelDTO
{
    override suspend fun update() {
        TODO("Not yet implemented")
    }

}


sealed class RepositoryBase2<DTO, CHILD_DTO>(
    private val forClass: DTOClass2<CHILD_DTO>,
) where DTO : ModelDTO, CHILD_DTO: ModelDTO
{
    abstract val repoName: String

    var initialized: Boolean = false
    val childFactory: DTOFactory2<CHILD_DTO, DataModel, LongEntity>
        get() {
            return forClass.config.dtoFactory
        }
   abstract suspend fun update()
}