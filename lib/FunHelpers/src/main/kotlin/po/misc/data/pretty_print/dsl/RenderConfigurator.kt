package po.misc.data.pretty_print.dsl

import po.misc.data.pretty_print.Placeholder
import po.misc.data.pretty_print.PrettyGrid
import po.misc.data.pretty_print.RenderableElement
import po.misc.data.pretty_print.parts.grid.RenderableType
import po.misc.data.pretty_print.templates.TemplatePlaceholder
import po.misc.data.pretty_print.parts.loader.DataProvider
import po.misc.types.token.filterTokenized

/**
 * Collects and resolves external templates against placeholders declared
 * in a [PrettyGrid] during a render pass.
 *
 * `RenderConfigurator` acts as a **binding layer** between:
 * - templates defined outside the grid
 * - placeholders declared inside the grid
 *
 * During rendering, it matches templates to placeholders by their
 * [po.misc.types.token.TypeToken] and initializes the corresponding data providers.
 *
 * This mechanism allows multiple placeholders of different types to be
 * resolved independently and in declaration order, without treating
 * missing or delayed bindings as fatal errors.
 *
 * Typical usage:
 * ```
 * grid.render(record) {
 *     renderWith(userTemplate, user)
 *     renderWith(orderTemplate, order)
 * }
 * ```
 *
 * Resolution is:
 * - type-safe (via [po.misc.types.token.TypeToken])
 * - order-preserving
 * - non-throwing
 *
 * Designed for soft-failure rendering pipelines where partial output
 * is acceptable.
 */
class RenderConfigurator(){

    /**
     * Represents a template provided from outside the grid along with
     * the data provider used to supply its rendering host.
     *
     * An [ExternalTemplate] is resolved against all compatible
     * [TemplatePlaceholder] instances in the grid.
     *
     * @param providedTemplate the renderable template to bind
     * @param provider the data provider supplying values for rendering
     */
    class ExternalTemplate<T : Any>(
       val providedTemplate: RenderableElement<T, T>,
       val provider: DataProvider<T, T>
    ){
        /**
         * Resolves this external template against all compatible placeholders
         * in the given [grid].
         *
         * Placeholders are matched by [po.misc.types.token.TypeToken]. For each matching
         * placeholder:
         * - the template is provided to the placeholder
         * - the placeholder's value loader is initialized with the provider
         *
         * Resolution respects the placeholder's lifecycle rules
         * (e.g. single-use vs reusable).
         *
         * @param grid the grid whose placeholders should be resolved
         * @return the list of placeholders that were resolved
         */
        fun resolveTemplate(grid: PrettyGrid<*>): List<Placeholder<T>> {
            val placeholders = grid.renderPlan[RenderableType.Placeholder]
            val filtered = placeholders.filterTokenized<Placeholder<T>, T>(providedTemplate.typeToken)
            filtered.forEach {
                it.provideRenderable(providedTemplate, provider)
            }
            return filtered
        }
    }

    @PublishedApi
    internal val templatesBacking: MutableList<ExternalTemplate<*>> = mutableListOf<ExternalTemplate<*>>()
    val templates:List<ExternalTemplate<*>> = templatesBacking

    /**
     * Resolves all registered external templates against the placeholders
     * declared in the given [PrettyGrid].
     *
     * This method is typically invoked internally as part of the grid's
     * render process.
     *
     * Resolution is performed in the order templates were registered.
     */
    internal fun resolveAll(grid: PrettyGrid<*>){
        templates.forEach {
            it.resolveTemplate(grid)
        }
    }

    /**
     * Registers a template to be rendered using the given data [provider].
     *
     * The template will be matched against compatible placeholders during
     * rendering and supplied with values from the provider.
     *
     * @param template the renderable template to bind
     * @param provider the data provider supplying rendering values
     */
    fun <T : Any> renderWith(template: RenderableElement<T, T>, provider: DataProvider<T, T>){
        templatesBacking.add( ExternalTemplate(template, provider))
    }

    /**
     * Registers a template to be rendered using a concrete receiver instance.
     * This is a convenience overload that wraps the receiver into a
     * [DataProvider] internally.
     * @param template the renderable template to bind
     * @param receiver the concrete object used for rendering
     */
    inline fun <reified T: Any> renderWith(template: RenderableElement<T, T>, receiver:T) {
        val provider =  DataProvider<T, T>({ receiver } )
        renderWith(template, provider)
    }
}