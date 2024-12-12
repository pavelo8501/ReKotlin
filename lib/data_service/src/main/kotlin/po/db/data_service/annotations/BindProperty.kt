package po.db.data_service.annotations

import org.jetbrains.exposed.sql.statements.StatementResult
import java.lang.reflect.Type
import java.util.Objects
import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ClassBinder(val key: String){

}

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class PropertyBinder (val key: String = "")
