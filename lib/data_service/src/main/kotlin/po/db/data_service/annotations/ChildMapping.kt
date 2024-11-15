package po.db.data_service.annotations

import po.db.data_service.services.models.ChildBindingClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)

annotation class ChildMapping( val mappingName: String)


data class ChildMappingExt(
    val parentContainer: ChildBindingClass
){

}
