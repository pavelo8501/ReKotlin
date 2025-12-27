package po.misc.data.pretty_print.templates

import po.misc.collections.asList
import po.misc.data.output.output
import po.misc.data.pretty_print.Placeholder
import po.misc.data.pretty_print.PrettyRow
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.TemplateHost
import po.misc.data.pretty_print.dsl.BuilderScope
import po.misc.data.pretty_print.parts.grid.RenderPlan
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.template.NamedTemplate
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.functions.Throwing
import po.misc.types.safeCast
import po.misc.types.token.TypeToken

/**
 * Defines how a [TemplatePlaceholder] reacts when a new renderable
 * is provided after it has already been resolved.
 *
 * Lifecycle policies control whether placeholders are **single-assignment**
 * or **re-bindable** during subsequent render passes.
 */
enum class PlaceholderLifecycle {
    /**
     * The placeholder may be resolved only once.
     *
     * If an attempt is made to provide a different renderable after the
     * placeholder has already been resolved, the overwrite is ignored and
     * execution continues without throwing.
     *
     * This mode is intended for templates where stability and determinism
     * are preferred over flexibility.
     */
    SingleUse,

    /**
     * The placeholder may be resolved multiple times.
     *
     * Each subsequent render pass may replace the previously assigned
     * renderable, allowing dynamic template substitution.
     *
     * This mode is intended for reusable or dynamic template layouts.
     */
    Reusable
}

/**
 * A renderable placeholder that delegates its rendering logic to another
 * [po.misc.data.pretty_print.RenderableElement] provided at runtime.
 *
 * `TemplatePlaceholder` allows templates to be declared ahead of time and
 * resolved later during rendering, without treating missing or replaced
 * bindings as fatal errors.
 *
 * The behavior of re-binding is controlled by [lifecycle]:
 * - [PlaceholderLifecycle.SingleUse] prevents overwriting once resolved
 * - [PlaceholderLifecycle.Reusable] allows re-binding on subsequent renders
 *
 * This class is designed for **soft-failure rendering pipelines**, where
 * incomplete or partially resolved templates should degrade gracefully
 * rather than interrupt execution.
 *
 * @param lifecycle defines how the placeholder behaves when reassigned
 * @param hostingBuilder the builder scope that owns this placeholder
 * @param typeToken the expected host type used during rendering
 */
class TemplatePlaceholder<T: Any>(
    val lifecycle: PlaceholderLifecycle,
    val hostingBuilder: BuilderScope<*>,
    override val receiverType: TypeToken<T>,
): Placeholder<T>, PrettyHelper {

    var options: RowOptions = RowOptions(Orientation.Horizontal)
    override val typeToken: TypeToken<T> = receiverType
    override val valueType: TypeToken<T> get() = receiverType

    /**
     * The currently assigned renderable responsible for actual rendering.
     * May be `null` if the placeholder has not yet been resolved.
     */
    var delegate: RenderableElement<T, T>? = null

    override val renderableType: RenderableType = RenderableType.Row


    override var enabled: Boolean = false
    override val renderPlan: RenderPlan<T, T> = RenderPlan(this)
    var dataLoader: DataLoader<T, T>? = null

    override val id: NamedTemplate get() = delegate!!.id

    private fun cleanup(){
        enabled = false
        delegate = null
        options =  RowOptions(Orientation.Horizontal)
    }

    private fun setLoader(loader: DataLoader<T, T>?) {
        if(loader != null){
            dataLoader = loader
        }
    }
    private fun attachDelegate(element: RenderableElement<T, T>) {
        delegate = element
        options = element.options

        when(element){
            is PrettyRow<*> -> {
                val casted = element.safeCast<PrettyRow<T>>()
                setLoader(casted?.dataLoader)
            }
        }
    }

    fun render(receiver: T, opts: RowOptions): String {
        return delegate?.render(receiver.asList(), opts) ?:run { SpecialChars.EMPTY }
    }

    override fun render(receiverList: List<T>, opts: CommonRowOptions?): String {
       val useOptions = toRowOptions(opts, options)
       return renderPlan.renderList(receiverList, useOptions)
    }

    /**
     * Renders the placeholder using the currently assigned delegate, if any.
     *
     * If no delegate is available or the delegate cannot load a value,
     * an empty string is returned.
     *
     * Rendering is performed lazily and delegates all host-specific logic
     * to the resolved [RenderableElement].
     *
     * @param opts optional rendering options
     * @return rendered output or an empty string if unresolved
     */
    override fun render(opts: CommonRowOptions?): String {
        val loader = dataLoader?.safeCast<DataLoader<T, T>>()
        val rowOptions = PrettyHelper.toRowOptionsOrNull(opts)
        if(loader != null && loader.canResolve){
           val host = loader.resolveValue(Throwing)
           val render = render(host, rowOptions?:options)
           return render
        }
        return SpecialChars.EMPTY
    }

    /**
     * Provides a concrete [RenderableElement] to be used by this placeholder.
     *
     * If the placeholder has not yet been resolved, the given [element] becomes
     * the active delegate and is used for subsequent rendering.
     *
     * If the placeholder is already resolved:
     * - In [PlaceholderLifecycle.SingleUse] mode, attempts to replace the
     *   existing delegate are ignored and a warning is emitted.
     * - In [PlaceholderLifecycle.Reusable] mode, the delegate is replaced.
     *
     * This method never throws and is intentionally tolerant of re-binding,
     * making it suitable for templating systems where partial or repeated
     * resolution is expected.
     *
     * @param element the renderable to associate with this placeholder
     */
    override fun provideRenderable(element: RenderableElement<T, T>, provider: DataProvider<T, T>?) {
        if(delegate != element && delegate != null){
            if(lifecycle == PlaceholderLifecycle.SingleUse){
                "TemplatePlaceholder<$typeToken> is being overwritten".output(Colour.Yellow)
                return
            }
        }
        attachDelegate(element)
        if(provider != null){
            initLoader(provider)
        }
        enabled = true
    }

    override fun initLoader(provider: DataProvider<T, T>) {
        dataLoader?.applyCallables(provider)
    }

    override fun copy(usingOptions: CommonRowOptions?): TemplatePlaceholder<T> {

        val copy = TemplatePlaceholder(lifecycle, hostingBuilder, typeToken)
        val delegateToCopy = delegate
        if (delegateToCopy != null) {
            val delegateCopy = delegateToCopy.copy(usingOptions)
            copy.delegate = delegateCopy
        }
        return copy
    }

    override fun resolve(receiverList: List<T>): List<T> {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "Placeholder<${typeToken.typeName}>($lifecycle)"
}