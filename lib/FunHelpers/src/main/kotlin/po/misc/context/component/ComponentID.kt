package po.misc.context.component

import po.misc.callbacks.signal.Signal
import po.misc.data.PrettyPrint
import po.misc.data.logging.Verbosity
import po.misc.data.styles.Colour
import po.misc.data.styles.colorize
import po.misc.debugging.ClassResolver
import po.misc.debugging.models.ClassInfo
import po.misc.debugging.models.GenericInfo
import po.misc.types.token.TypeToken


/**
 * Represents a unique identity assigned to a [Component].
 *
 * A `ComponentID` encapsulates:
 * - `name` – human-readable alias of the component.
 * - `classInfo` – resolved type information including generic parameters.
 * - `verbosity` – minimum severity level for log output.
 *
 * ### Generic Awareness
 * `ComponentID` supports generic type annotation via [addParamInfo], allowing components
 * to display full type structure in reports or runtime diagnostics.
 *
 * ### Usage
 * Instances are typically auto-created by [Component.componentID], but may be customized
 * if a component instance must represent a logical role (e.g., “DatabasePool#1”) rather
 * than a raw class name.
 *
 * @property name Display name of the component.
 * @property classInfo Structural information used in type-friendly formatting.
 * @property verbosity Controls minimum log level for this component.
 */
class ComponentID(
    private var name: String?,
    var classInfo: ClassInfo,
    var verbosity: Verbosity = Verbosity.Info,
): PrettyPrint {

    constructor(
        provider: () -> String,
        component: Component,
        verbosity: Verbosity = Verbosity.Info
    ):this(null, ClassResolver.classInfo(component), verbosity){
        nameProvider = provider
    }

    private var nameProvider : (() -> String)? = null

    private val resolvedName: String by lazy {
        val nameFromProvider = nameProvider?.invoke()
        nameFromProvider ?: name ?: classInfo.simpleName
    }

    val componentName: String get() = resolvedName

    fun updateNameProvider(provider: (() -> String)?) {
        nameProvider = provider
    }

    fun useName(nameToUse: String){
        name = nameToUse
    }

    fun addParamInfo(genericInfo: GenericInfo): ComponentID{
        classInfo.genericInfoBacking.add(genericInfo)
        return this
    }

    fun addParamInfo(parameterName: String,  typeToken: TypeToken<*>): ComponentID{
        val info =  classInfo.addParamInfo(parameterName, typeToken)
        classInfo.genericInfoBacking.add(info)
        return this
    }

    override val formattedString: String get() =  "${componentName.colorize(Colour.Magenta)} " +
            classInfo.formattedString

    override fun toString(): String = componentName + classInfo
}
