package po.misc.data.pretty_print.templates

import po.misc.types.token.TokenFactory

/**
 * Marker interface for companion objects used in semantic renderable lookup.
 *
 * Implementations of this interface are used as keys in [po.misc.data.pretty_print.parts.grid.RenderPlan]
 * operator-based access.
 *
 * This enables expressive queries such as:
 * ```
 * grid.templateMap[Placeholder]
 * ```
 */
interface TemplateCompanion : TokenFactory