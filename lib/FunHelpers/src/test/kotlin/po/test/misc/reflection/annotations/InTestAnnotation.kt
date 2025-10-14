package po.test.misc.reflection.annotations


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class InTestAnnotation( val order: Int,)
