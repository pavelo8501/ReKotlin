package po.misc.data.pretty_print

import po.misc.data.pretty_print.parts.grid.RenderPlan
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.template.NamedTemplate
import po.misc.data.pretty_print.templates.TemplateCompanion
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken

sealed interface TemplatePart<T, V>: TokenizedResolver<T, V>{
    val enabled: Boolean
    val id: NamedTemplate
    val renderableType : RenderableType

    fun resolve(receiverList: List<T>): List<V>
    fun copy(usingOptions: CommonRowOptions? = null):TemplatePart<T, V>
}

/**
 * Base abstraction for all elements that can participate in rendering.
 *
 * A [RenderableElement] represents a renderable unit that:
 * - is associated with a specific host type via [TypeToken]
 * - may be enabled or disabled
 * - can render itself against a concrete host instance
 *
 * Implementations are expected to degrade gracefully when rendering
 * conditions are not met.
 */
sealed interface RenderableElement<T, V> : TemplatePart<T, V>{
    val options: RowOptions
    /**
     * Renders the placeholder on the given [receiverList] using the resolved delegate.
     * If the placeholder has not been resolved, an empty string is returned.
     */
    fun render(receiverList: List<T>, opts: CommonRowOptions? = null): String
    override fun copy(usingOptions: CommonRowOptions? ):RenderableElement<T, V>

    fun shouldRender():Boolean{
        return enabled
    }
}

interface TemplateHost<T, V>: TemplatePart<T, V>{
    val renderPlan : RenderPlan<T, V>
    override fun copy(usingOptions: CommonRowOptions? ): TemplateHost<T, V>

    fun render(receiverList: List<T>, opts: CommonRowOptions? = null): String
}

/**
 * A renderable element that acts as a deferred binding point within a template.
 *
 * A [Placeholder] does not render content directly. Instead, it delegates
 * rendering to another [RenderableElement] provided at runtime.
 *
 * Placeholders are resolved via [po.misc.data.pretty_print.dsl.RenderConfigurator] and matched by
 * [TypeToken], allowing templates to be composed dynamically.
 *
 * Missing or unresolved placeholders do not cause failures and render
 * as empty output.
 */
interface Placeholder<T> : TemplateHost<T, T> {

    override val renderPlan : RenderPlan<T, T>
    fun initLoader(provider: DataProvider<T, T>)
    fun provideRenderable(element: RenderableElement<T, T>, provider: DataProvider<T, T>? = null)
    fun render(opts: CommonRowOptions? = null): String
    override fun copy(usingOptions: CommonRowOptions? ):Placeholder<T>
    /**
     * Companion object used for semantic lookup in [po.misc.data.pretty_print.parts.grid.RenderPlan].
     *
     * Example:
     * ```
     * val placeholders = grid.templateMap[Placeholder]
     * ```
     */
    companion object : TemplateCompanion
}

