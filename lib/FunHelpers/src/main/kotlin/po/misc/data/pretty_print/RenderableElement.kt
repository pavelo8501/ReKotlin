package po.misc.data.pretty_print

import po.misc.callbacks.callable.ReceiverCallable
import po.misc.data.pretty_print.parts.common.RenderData
import po.misc.data.pretty_print.parts.common.RenderMarker
import po.misc.data.pretty_print.parts.grid.RenderPlan
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.parts.loader.DataLoader
import po.misc.data.pretty_print.parts.loader.ElementProvider
import po.misc.data.pretty_print.parts.options.CommonRowOptions
import po.misc.data.pretty_print.parts.options.RowOptions
import po.misc.data.pretty_print.parts.options.NamedTemplate
import po.misc.data.pretty_print.parts.render.KeyParameters
import po.misc.data.pretty_print.parts.render.RenderCanvas
import po.misc.data.pretty_print.parts.render.RenderParameters
import po.misc.data.pretty_print.parts.render.RenderSnapshot
import po.misc.data.pretty_print.templates.TemplateCompanion
import po.misc.interfaces.named.NamedComponent
import po.misc.types.token.Tokenized
import po.misc.types.token.TokenizedResolver
import po.misc.types.token.TypeToken
import kotlin.reflect.KClass

sealed interface TemplatePart<T>: Tokenized<T>, RenderParameters{
    val enabled: Boolean
    val templateID: NamedTemplate
    val renderableType : RenderableType
    val keyParameters: KeyParameters
    val receiverType:TypeToken<T>
    override val typeToken: TypeToken<T> get() = receiverType

   // val index: Int get() = keyParameters.index
    fun copy(usingOptions: CommonRowOptions? = null):TemplatePart<T>

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
sealed interface RenderableElement<S, T> : TemplatePart<T>, TokenizedResolver<S, T>{

    val options: RowOptions
    override val sourceType: TypeToken<S>
    override val receiverType:TypeToken<T>
    override val typeToken: TypeToken<T> get() = receiverType
    val dataLoader: DataLoader<S, T>

    fun renderFromSource(marker: RenderMarker, source:S, opts: CommonRowOptions? = null): RenderCanvas
    fun renderFromSource(source:S, opts: CommonRowOptions? = null):String
}

interface TemplateHost<S, V>: TemplatePart<V>, TokenizedResolver<S, V>, NamedComponent{
    val options: RowOptions
    val renderPlan : RenderPlan<S, V>
    val dataLoader: DataLoader<S, V>
    override val receiverType:TypeToken<V>
    override val typeToken: TypeToken<V> get() = receiverType
    override fun copy(usingOptions: CommonRowOptions? ):TemplateHost<S, V>
    fun renderFromSource(source:S, opts: CommonRowOptions? = null):String
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
    fun initLoader(provider: ElementProvider<T, T>)
    fun provideRenderable(element: TemplateHost<T, T>, provider: ReceiverCallable<T, T>? = null):Placeholder<T>
    fun render(opts: CommonRowOptions? = null): String

    fun renderInScope(parameter: RenderParameters):RenderCanvas

    override fun copy(usingOptions: CommonRowOptions?):Placeholder<T>
    /**
     * Companion object used for semantic lookup in [po.misc.data.pretty_print.parts.grid.RenderPlan].
     *
     * Example:
     * ```
     * val placeholders = grid.templateMap[Placeholder]
     * ```
     */
    companion object : TemplateCompanion<Placeholder<*>>{
        override val templateClass: KClass<Placeholder<*>> = Placeholder::class
        override val renderType: RenderableType = RenderableType.Placeholder
    }
}

