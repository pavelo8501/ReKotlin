package po.db.data_service.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class BindProperty(
    val propName: String,
)