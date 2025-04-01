package po.exposify.binders.relationship.models

import po.exposify.binders.enums.Cardinality
import po.exposify.binders.relationship.BindingContainer2
import po.exposify.binders.relationship.BindingKeyBase2
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import kotlin.reflect.KProperty1

data class PropertyInfo<DTO, DATA>(
    val name: String,
    val cardinality: Cardinality,
    val isNullable: Boolean = false,
    val bindingKey: BindingKeyBase2,
    private val hostingContainer : BindingContainer2<DTO, DATA, *, *>
) where DTO : ModelDTO, DATA: DataModel{

    var processed : Boolean = false
    var inBlueprint: KProperty1<DATA, *>?  = null

}