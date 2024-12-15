package po.db.data_service.scope.service.controls

import po.db.data_service.scope.service.controls.service_registry.ServiceRegistryItem
import po.db.data_service.scope.service.controls.service_registry.ServiceRegistryItemBuilder
import po.db.data_service.scope.service.controls.service_registry.serviceRegistry

class ListBuilderL<T>(initial: Collection<T> = emptyList()) {
    private val _list: MutableList<T> = initial.toMutableList()
    val list: List<T> get() = _list

    fun add(element: T) = apply { _list.add(element) }

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

    fun <DATA_MODEL : Any, ENTITY : Any> ListBuilderL<ServiceRegistryItem<DATA_MODEL, ENTITY>>.addServiceRegistryItem(
        init: ServiceRegistryItemBuilder<DATA_MODEL, ENTITY>.() -> Unit
    ) = apply {
        val serviceRegistryItem = serviceRegistry(init)
        add(serviceRegistryItem)
    }

    fun build(): List<T> = list
}