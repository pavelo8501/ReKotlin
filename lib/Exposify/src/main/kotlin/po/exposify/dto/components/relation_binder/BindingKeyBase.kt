package po.exposify.dto.components.relation_binder

import po.exposify.dto.enums.Cardinality
import po.exposify.classes.DTOClass
import po.exposify.dto.interfaces.ModelDTO

sealed class BindingKeyBase(val ordinance: Cardinality) {
    open class  OneToMany<CHILD_DTO: ModelDTO>(
        val childModel :DTOClass<CHILD_DTO>
    ):BindingKeyBase(Cardinality.ONE_TO_MANY)
    open class  OneToOne<CHILD_DTO: ModelDTO>(
        val childModel : DTOClass<CHILD_DTO>
    ):BindingKeyBase(Cardinality.ONE_TO_ONE)
    open class  ManyToMany<CHILD_DTO: ModelDTO>(
        val childModel : DTOClass<CHILD_DTO>
    ):BindingKeyBase(Cardinality.MANY_TO_MANY)
    companion object{
        fun <CHILD_DTO: ModelDTO> createOneToManyKey(
            childModel : DTOClass<CHILD_DTO>
        ): OneToMany<CHILD_DTO>{
            return  OneToMany<CHILD_DTO>( childModel)

        }

        fun <CHILD_DTO: ModelDTO> createOneToOneKey(
            childModel : DTOClass<CHILD_DTO>
        ): OneToOne<CHILD_DTO>{
            return  object : OneToOne<CHILD_DTO>(childModel) {}
        }
    }
}