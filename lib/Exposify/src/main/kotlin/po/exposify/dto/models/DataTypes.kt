package po.exposify.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dao.helpers.getExposifyEntityCompanion
import po.exposify.dao.models.TableColumnMap
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.interfaces.Copyable
import po.misc.types.Tokenized
import po.misc.types.Typed
import po.misc.types.type_data.TypeData
import po.misc.types.containers.ThreeTypeRegistry
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.KType


class EntityType<E: LongEntity>(
    val entityClass: ExposifyEntityClass<E>,
    override val typeToken: TypeToken<E>,
): Tokenized<E> {

    val kClass: KClass<E> get() = typeToken.kClass
    val kType: KType get() = typeToken.kType


    var  tableColumnMap: TableColumnMap =  TableColumnMap()

    val isInitialized: Boolean get() = tableColumnMap.propertySize > 0

    fun provideColumnMetadata(columnMap:  TableColumnMap){
        tableColumnMap = columnMap
    }

    fun copy():EntityType<E>{
        return EntityType(entityClass, typeToken).also {
            it.tableColumnMap = tableColumnMap.copy()
        }
    }

    companion object{
        inline fun <reified E: LongEntity> create(
            entityClass: ExposifyEntityClass<E>
        ):EntityType<E>{
           return EntityType(entityClass, TypeToken.create())
        }
    }
}

class CommonDTOType<DTO: ModelDTO, D: DataModel,  E: LongEntity>(
    val dtoType: TypeToken<DTO>,
    val dataType: TypeToken<D>,
    val entityTypeData: TypeToken<E>,
    val entityType: EntityType<E>,
    val commonType: TypeToken<CommonDTO<DTO,  D, E>>,
    private val dtoBaseType:TypeToken<DTOBase<DTO, D, E>>,

): ThreeTypeRegistry<DTO, D, E>(dtoType, dataType,   entityTypeData), Copyable<CommonDTOType<DTO, D, E>>{

    val baseKClass: KClass<DTOBase<DTO, D, E>> get() = dtoBaseType.kClass
    val baseKType: KType get() = dtoBaseType.kType

    fun initializeColumnMetadata(){
       val propertyMap =  entityType.entityClass.createTablePropertyMap()
       entityType.provideColumnMetadata(propertyMap)
    }

    override fun copy(): CommonDTOType<DTO, D, E> {
        val newEntityType = entityType.copy()
        return CommonDTOType(dtoType, dataType,entityTypeData,  newEntityType, commonType,  dtoBaseType)
    }

    override fun toString(): String = "CommonDTOType<${dtoType.typeName}, ${dataType.typeName}, ${entityType}>"


    companion object {
        inline fun <reified DTO, reified D, reified E> create(
            dtoClass: DTOBase<DTO, D, E>
        ): CommonDTOType<DTO, D, E> where DTO : ModelDTO, D : DataModel, E : LongEntity {

            val entityClass = getExposifyEntityCompanion<E>(this)
            val entityType = EntityType.create<E>(entityClass)
            val dtoBaseType: TypeToken<DTOBase<DTO, D, E>> = TypeToken.create()
            println("DTOBaseType ${dtoBaseType.typeName}")
            val commonType = TypeToken.create<CommonDTO<DTO, D, E>>()
            return CommonDTOType(
                dtoType = TypeToken.create<DTO>(),
                dataType = TypeToken.create<D>(),
                entityType = entityType,
                commonType = commonType,
                dtoBaseType = dtoBaseType,
                entityTypeData = TypeToken.create<E>()
            )
        }
    }

}