package po.misc.interfaces

import po.misc.types.TypeRecord


interface Identifiable{
    val componentName: String
    val completeName: String
}

interface IdentifiableModule : Identifiable {
    val moduleName: String
    override val componentName: String get()= moduleName
}


//interface ValidatableComponent<T: Any> : Identifiable {
//    val typeRecord: TypeRecord<T>
//    val mapperItem : PropertyMapperItem<T>
//
//    override val componentName: String get() = typeRecord.simpleName
//    override val value: Int  get()= typeRecord.element.value
//}
