package po.db.data_service.classes.components



//abstract class BaseDTO<DTO : CommonDTO (
//   open val entity: DTO,
//   open val className: String
//)
//
//class DTOFactory {
//   fun <DTO : DATA> createDTO(entity: CommonDTO, className: String): BaseDTO<DTO> {
//      return object : BaseDTO<DTO>(entity, className) {}
//   }
//}

inline fun <reified T> Any.safeCast(): T? {
    return this as? T
}

inline fun <reified T> printType() {
    println(T::class) // Access runtime type
}

inline fun <reified T, reified U> initializeContexts(
    receiverInstance: T,
    paramInstance: U,
    block: T.(U) -> Unit
) {
    println("T is: ${T::class}, U is: ${U::class}")
    receiverInstance.block(paramInstance)
}

// Usage:
//initializeContexts("RootDTO", 42) { num ->
//    println("this = $this")  // "RootDTO"
//    println("num = $num")     // 42
//}

inline fun <reified T, reified U> withTwoContexts(
    receiver: T,
    param: U,
    block: T.(U) -> Unit
) {
    // T and U are reified here.
    receiver.block(param)
}