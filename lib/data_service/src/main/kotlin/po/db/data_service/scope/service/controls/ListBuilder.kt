package po.db.data_service.scope.service.controls

import po.db.data_service.scope.service.controls.service_registry.ServiceRegistryItem
import po.db.data_service.scope.service.controls.service_registry.ServiceRegistryItemBuilder
import po.db.data_service.scope.service.controls.service_registry.serviceRegistry

/**
 * Represents a builder for creating immutable lists dynamically.
 *
 * This class provides a fluent API to add elements conditionally or in bulk.
 *
 * @param T The type of elements in the list.
 */
open class ListBuilder<T>(initial: Collection<T> = emptyList()) {
    private val _list: MutableList<T> = initial.toMutableList()
    /**
     * The immutable list built by this builder.
     */
    val list: List<T> get() = _list

    /**
     * Adds an element to the list.
     *
     * @param element The element to be added.
     * @return The current instance of the builder for chaining.
     */
    fun add(element: T) = apply { _list.add(element) }

    /**
     * Adds an element to the list if the provided [predicate] evaluates to `true`.
     *
     * @param element The element to be conditionally added.
     * @param predicate A function that takes the element and the current list as arguments
     * and returns `true` if the element should be added.
     * @return The current instance of the builder for chaining.
     */
    fun addIf(element: T, predicate: (T, List<T>) -> Boolean) = apply {
        if (predicate(element, _list)) _list.add(element)
    }

    fun addUnless(element: T, predicate: (T, List<T>) -> Boolean) = apply {
        if (!predicate(element, _list)) _list.add(element)
    }

    fun addAll(elements: Collection<T>) = apply { _list.addAll(elements) }

    fun clear() = apply { _list.clear() }

    fun <R> addTransformed(element: R, transform: (R) -> T) = apply {
        _list.add(transform(element))
    }

    fun build(): List<T> = list
}