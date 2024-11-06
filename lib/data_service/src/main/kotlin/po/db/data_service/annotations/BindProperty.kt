package po.db.data_service.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class BindProperty(
    val propName: String,
    val dbType: KClass<*> = Any::class // Optional, if you need type info
)