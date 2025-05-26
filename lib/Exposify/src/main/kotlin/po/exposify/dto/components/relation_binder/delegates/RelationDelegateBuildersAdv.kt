package po.exposify.dto.components.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.components.relation_binder.RelationshipBinder
import po.exposify.dto.components.relation_binder.components.getOrCreateOneToManyContainer
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrInitEx
import kotlin.reflect.KProperty1



/**
 * Binds a one-to-many relationship between this [CommonDTO] and a child DTO.
 *
 * This delegate initializes and registers a container and repository to manage a collection
 * of child DTOs related to this parent. It automatically triggers child DTO setup and binds
 * it through the [RelationshipBinder].
 *
 * @param childClass The [DTOClass] of the child DTOs in the 1:N relationship.
 * @param ownDataModels The property in the parent [DATA] model that holds the child [CD] list.
 * @param foreignEntities The property in the parent [ENTITY] referencing the child [FE] entities.
 *
 * @return A [OneToManyDelegate] that exposes the bound child DTOs as a read-only list.
 */
fun <DTO, DATA, ENTITY, C_DTO, CD,  FE> CommonDTO<DTO, DATA, ENTITY>.oneToManyOfAdv(
    childClass: DTOClass<C_DTO, CD, FE>,
    ownDataModels: KProperty1<DATA, MutableList<out CD>>,
    ownEntities: KProperty1<ENTITY, SizedIterable<FE>>,
): OneToManyDelegateAdv<DTO, DATA, ENTITY, C_DTO, CD, FE>
        where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO, C_DTO: ModelDTO,  CD: DataModel, FE: LongEntity
{
    val thisDtoClass = this.dtoClass
    when(thisDtoClass){
        is RootDTO<DTO, DATA, ENTITY>->{
            if(!childClass.initialized){
                childClass.initialization()
                thisDtoClass.config.addHierarchMemberIfAbsent(childClass)
            }
        }
        else -> {}
    }

    val castedOwnDataModels = ownDataModels.castOrInitEx<KProperty1<DATA, MutableList<CD>>>()
    val bindingDelegate = OneToManyDelegateAdv(this, childClass,  castedOwnDataModels, ownEntities)
    val container =  childClass.getOrCreateOneToManyContainer(this.dtoClass, bindingDelegate)
    this.createRepository(container)
    return bindingDelegate
}

