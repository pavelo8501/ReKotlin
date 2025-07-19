package po.misc.reflection.objects.builders

//
//fun <E: Enum<E>, T: Composed> T.createClassSurrogate(
//    key:E,
//    manager: ObjectManager<E, *>
//): KSurrogate<E, T> {
//    val objectClass = this::class as KClass<T>
//    val properties = objectClass.memberProperties.mapNotNull { kProperty ->
//        kProperty.get(this)?.let { value ->
//            val clazz = value::class as KClass<Any>
//            val property =  createPropertyIO(kProperty as KProperty1<T, Any>, this,  value)
//            property.provideReceiver(this)
//            property
//        }
//    }
//    val responsive = KSurrogate<E, T>(key, this, manager.hooks, properties)
//    manager.addSurrogate(key, responsive)
//    return responsive
//}

//
//inline fun <T, reified V: Any, E: Enum<E>> T.composable(
//    dataManager: ObjectManager<E, T>
//): ComposableProperty<T, V, E> where T: Composed, T: IdentifiableContext {
//    return ComposableProperty(this, dataManager, V::class)
//}
//
//inline fun <T, reified D: Composed,  reified V: Any, E: Enum<E>> T.composable(
//    dataProperty: KMutableProperty1<D, V>,
//    dataManager: ObjectManager<E, T>
//): ComposableProperty<T, V, E> where T: Composed, T: IdentifiableContext{
//
//    val valueClass = V::class
//    val dataClass = D::class
//    val propertyInfo =  dataProperty.toPropertyInfo(dataClass)
//    propertyInfo.setValueClass(valueClass)
//    val ioProperty = PropertyIO(propertyInfo, PropertyIOBase.PropertyType.StaticallySet, null)
//    dataManager.storeProperty(ioProperty)
//    return ComposableProperty(this, dataManager, V::class, ioProperty)
//}