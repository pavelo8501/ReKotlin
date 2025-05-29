package po.exposify.dto.components.bindings.relation_binder.delegates

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.dto.DTOClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.components.SingleRepository
import po.exposify.dto.enums.Cardinality
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.InitException
import po.exposify.extensions.castOrInitEx
import po.exposify.extensions.castOrOperationsEx
import po.lognotify.classes.task.models.TaskConfig
import po.lognotify.extensions.subTask
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


fun <DTO, DATA, ENTITY, F_DTO, CD,  FE> CommonDTO<DTO, DATA, ENTITY>.oneToOneOf(
    childClass: DTOClass<F_DTO, CD, FE>,
    ownDataModel: KMutableProperty1<DATA, out CD?>,
    ownEntity: KMutableProperty1<ENTITY, FE>,
    foreignEntity: KMutableProperty1<FE, ENTITY>
): OneToOneDelegate<DTO, DATA, ENTITY, F_DTO, CD, FE>
        where DATA:DataModel, ENTITY : LongEntity, DTO : ModelDTO, F_DTO: ModelDTO,  CD: DataModel, FE: LongEntity
        = subTask("oneToOneOf", TaskConfig(actor = this.dtoName)){
    val thisDtoClass = this.dtoClass
    when(thisDtoClass){
        is RootDTO<DTO, DATA, ENTITY>->{
            if(!childClass.initialized){
                childClass.initialization()
                thisDtoClass.config.addHierarchMemberIfAbsent(childClass)
            }
        }
        is DTOClass -> {
            if(!childClass.initialized){
                childClass.initialization()
                thisDtoClass.findHierarchyRoot().config.addHierarchMemberIfAbsent(childClass)
            }
        }
    }

    val castedOwnDataModel = ownDataModel.castOrInitEx<KMutableProperty1<DATA, CD>>()
    val bindingDelegate = OneToOneDelegate(this, childClass, castedOwnDataModel, ownEntity, foreignEntity)
    //this.createRepository(container)
    bindingDelegate
}.resultOrException()


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
fun <DTO, DATA, ENTITY, F_DTO, FD,  FE> CommonDTO<DTO, DATA, ENTITY>.oneToManyOf(
    childClass: DTOClass<F_DTO, FD, FE>,
    ownDataModels: KProperty1<DATA, MutableList<out FD>>,
    ownEntities: KProperty1<ENTITY, SizedIterable<FE>>,
    foreignEntity: KMutableProperty1<FE, ENTITY>
): OneToManyDelegate<DTO, DATA, ENTITY, F_DTO, FD, FE>
        where  DTO : ModelDTO, DATA:DataModel, ENTITY : LongEntity, F_DTO: ModelDTO,  FD: DataModel, FE: LongEntity
 = subTask("oneToManyOf", TaskConfig(actor = this.dtoName)) {
    val thisDtoClass = this.dtoClass
    when(thisDtoClass){
        is RootDTO<DTO, DATA, ENTITY>->{
            if(!childClass.initialized){
                childClass.initialization()
                thisDtoClass.config.addHierarchMemberIfAbsent(childClass)
            }
        }
        is DTOClass -> {
            if(!childClass.initialized){
                childClass.initialization()
                val root = thisDtoClass.findHierarchyRoot()
                root.config.addHierarchMemberIfAbsent(childClass)
            }
        }
    }
    val castedOwnDataModels = ownDataModels.castOrInitEx<KProperty1<DATA, MutableList<FD>>>()
    val bindingDelegate = OneToManyDelegate(this, childClass, castedOwnDataModels, ownEntities, foreignEntity)
    bindingDelegate
}.resultOrException()

