package po.exposify.dto.components.relation_binder

import po.exposify.dto.enums.Cardinality
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO

sealed class BindingKeyBase(val ordinance: Cardinality) {

    open class  OneToMany<DTO: ModelDTO>(
        val dtoClass :DTOClass<DTO>
    ):BindingKeyBase(Cardinality.ONE_TO_MANY)
    open class  OneToOne<DTO: ModelDTO>(
        val dtoClass : DTOClass<DTO>
    ):BindingKeyBase(Cardinality.ONE_TO_ONE)
    open class  ManyToMany<DTO: ModelDTO>(
        val dtoClass : DTOClass<DTO>
    ):BindingKeyBase(Cardinality.MANY_TO_MANY)

    companion object{
        fun <DTO: ModelDTO> createOneToManyKey(
            dtoClass : DTOClass<DTO>
        ): OneToMany<DTO>{
            return  OneToMany(dtoClass)

        }

        fun <DTO: ModelDTO> createOneToOneKey(
            dtoClass : DTOClass<DTO>
        ): OneToOne<DTO>{
            return  object : OneToOne<DTO>(dtoClass) {}
        }
    }
}