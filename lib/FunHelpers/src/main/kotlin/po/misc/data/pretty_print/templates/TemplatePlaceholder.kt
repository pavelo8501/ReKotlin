package po.misc.data.pretty_print.templates

import po.misc.callbacks.callable.ReceiverCallable
import po.misc.data.pretty_print.Placeholder
import po.misc.data.pretty_print.PrettyGridBase
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.TemplateHost
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.pretty_print.parts.grid.RenderPlan
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.ElementProvider
import po.misc.data.pretty_print.parts.options.Orientation
import po.misc.data.pretty_print.parts.options.PrettyHelper
import po.misc.data.pretty_print.parts.options.NamedTemplate
import po.misc.data.pretty_print.parts.render.KeyParameters
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.data.pretty_print.parts.render.RenderParameters
import po.misc.data.styles.Colour
import po.misc.data.styles.SpecialChars
import po.misc.types.token.TypeToken

/**
 * Defines how a [TemplatePlaceholder] reacts when a new renderable
 * is provided after it has already been resolved.
 *
 * Lifecycle policies control whether placeholders are **single-assignment**
 * or **re-bindable** during subsequent render passes.
 */
enum class Lifecycle {
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
 * [RenderableElement] provided at runtime.
 *
 * `TemplatePlaceholder` allows templates to be declared ahead of time and
 * resolved later during rendering, without treating missing or replaced
 * bindings as fatal errors.
 *
 * The behavior of re-binding is controlled by [lifecycle]:
 * - [Lifecycle.SingleUse] prevents overwriting once resolved
 * - [Lifecycle.Reusable] allows re-binding on subsequent renders
 *
 * This class is designed for **soft-failure rendering pipelines**, where
 * incomplete or partially resolved templates should degrade gracefully
 * rather than interrupt execution.
 *
 * @param lifecycle defines how the placeholder behaves when reassigned
 * @param hostingGrid the builder scope that owns this placeholder
 * @param typeToken the expected host type used during rendering
 */
class TemplatePlaceholder<T: Any>(
    val lifecycle: Lifecycle,
    val hostingGrid: PrettyGridBase<*>,
    override val receiverType: TypeToken<T>,
): Placeholder<T>, PrettyHelper {

    override val sourceType:TypeToken<T> = receiverType
    override var options: RowOptions = RowOptions(Orientation.Horizontal)
    override val typeToken: TypeToken<T> = receiverType

    override val name: String get() = "TemplatePlaceholder$typeName"

    //override val valueType: TypeToken<T> get() = receiverType

    /**
     * The currently assigned renderable responsible for actual rendering.
     * May be `null` if the placeholder has not yet been resolved.
     */
    var delegate: TemplateHost<T, T>? = null

    override val index: Int  get() = delegate?.index ?: 0
    override val keyParameters: KeyParameters get() = delegate?.keyParameters ?: KeyParameters()

    override val renderableType: RenderableType = RenderableType.Row

    override var enabled: Boolean = false
    override var renderPlan: RenderPlan<T, T> = RenderPlan(this)
    override val dataLoader: DataLoader<T, T> = DataLoader("TemplatePlaceholder", receiverType, receiverType)

    override val templateID: NamedTemplate get() = delegate?.templateID?:run {
        generateRowID(this, hostingGrid.hashCode() + hashCode())
    }

    private fun cleanup(){
        enabled = false
        delegate = null
        options =  RowOptions(Orientation.Horizontal)
    }

    private fun applyProvider(provider: ReceiverCallable<T, T>) {
        dataLoader.add(provider)
    }
    private fun attachDelegate(element: TemplateHost<T, T>, dataProvider: ReceiverCallable<T, T>?) {
        delegate = element
        options = element.options
        renderPlan = RenderPlan(element)
        if (dataProvider != null) {
            dataLoader.add(dataProvider)
        } else {
            dataLoader.apply(element.dataLoader.elementRepository).apply(element.dataLoader.listRepository)
        }
    }
    fun render(receiver: T, opts: CommonRowOptions?): String {
        return delegate?.renderFromSource(receiver, opts) ?:run { SpecialChars.EMPTY }
    }
    override fun renderFromSource(source: T, opts: CommonRowOptions? ): String = render(receiver =  source, opts)

    override fun renderInScope(parameter: RenderParameters): RenderCanvas{
        TODO("Not implemented in TemplatePlaceholder. Need to decide if receiver can be obtained from upper container")
    }

    fun render(receiverList: List<T>, opts: CommonRowOptions?): String {
        val rendered = mutableListOf<String>()
        val useOptions = toRowOptions(opts, options)
        receiverList.forEach { receiver ->
            val render = renderPlan.render(receiver).styled
            rendered.add(render)
        }
        return rendered.joinToString(SpecialChars.NEW_LINE)
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
        if(!dataLoader.canResolve){
            return SpecialChars.EMPTY
        }
        val resolved = dataLoader.resolveList()
        val render = if(resolved.isNotEmpty()){
              render(resolved, toRowOptions(opts, options))
        }else{
            SpecialChars.EMPTY
        }
        return render
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
    override fun provideRenderable(
        element: TemplateHost<T, T>,
        provider: ReceiverCallable<T, T>?
    ): TemplatePlaceholder<T> {
        if(delegate != element && delegate != null){
            if(lifecycle == Lifecycle.SingleUse){
                "TemplatePlaceholder<$typeToken> is being overwritten".output(Colour.Yellow)
                return this
            }
        }
        attachDelegate(element, provider)
        enabled = true
        return this
    }

    override fun initLoader(provider: ElementProvider<T, T>): Unit{
        dataLoader.apply(provider)
    }
    fun initLoader(provider: ReceiverCallable<T, T>) {
        dataLoader.add(provider)
    }
    override fun copy(usingOptions: CommonRowOptions?): TemplatePlaceholder<T> {
       return delegate?.let {element->
            TemplatePlaceholder(lifecycle, hostingGrid, typeToken).also {placeholder->
                val renderable = element.copy(usingOptions)

                dataLoader.elementRepository.callables.values.forEach {
                    placeholder.provideRenderable(renderable, it)
                }
            }
        }?:run {
            TemplatePlaceholder(lifecycle, hostingGrid, typeToken).also {
                it.dataLoader.apply(dataLoader.copy())
            }
        }
    }

    fun resolve(receiverList: List<T>): List<T> {
       return dataLoader.resolveList(receiverList)
    }

    override fun toString(): String = "Placeholder<${typeToken.typeName}>($lifecycle)"
}