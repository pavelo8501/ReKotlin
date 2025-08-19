package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.castOrInit
import po.exposify.extensions.castOrOperations
import po.misc.types.castOrManaged
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


fun <F, FD,  FE, DTO, D, E> CommonDTO<F, FD,  FE>.oneToOneOf(
    dtoClass: DTOClass<F, FD,  FE>,
    ownDataModel: KMutableProperty1<D, out FD?>,
    ownEntity: KMutableProperty1<FE, E>,
    foreignEntity: KMutableProperty1<E, FE>
): OneToOneDelegate<F, FD,  FE, DTO, D, E>
        where  DTO : ModelDTO, D:DataModel, E: LongEntity, F: ModelDTO,  FD: DataModel, FE: LongEntity
{
    TODO("Refactor")
  //  val castedOwnDataModel = ownDataModel.castOrManaged<KMutableProperty1<FD, D?>>(this)

   // return OneToOneDelegate(this , dtoClass, castedOwnDataModel, ownEntity, foreignEntity)
}


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
fun <DTO, D, E, F, FD, FE> CommonDTO<DTO, D, E>.oneToManyOf(
    dtoClass: DTOClass<F, FD,  FE>,
    ownDataModels: KProperty1<D, MutableList<out FD>>,
    ownEntities: KProperty1<E, SizedIterable<FE>>,
    foreignEntity: KMutableProperty1<FE, out E>
): OneToManyDelegate<DTO, D, E, F, FD, FE>
    where  DTO : ModelDTO, D:DataModel, E: LongEntity, F: ModelDTO,  FD: DataModel, FE: LongEntity
{
    val castedOwnDataModels = ownDataModels.castOrManaged<KProperty1<D, MutableList<FD>>>(this)

    return OneToManyDelegate(this, dtoClass, castedOwnDataModels, ownEntities, foreignEntity.castOrOperations(dtoClass))
}

