package po.misc.reflection.anotations

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.LOCAL_VARIABLE)
@Retention(AnnotationRetention.RUNTIME)
annotation class ManagedProperty()




@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class BuilderProperty()



