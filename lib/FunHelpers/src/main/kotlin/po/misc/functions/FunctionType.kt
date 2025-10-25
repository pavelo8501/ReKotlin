package po.misc.functions



sealed interface FunctionType

object Throwing: FunctionType


sealed interface FunctionResultType


object NonNullable: FunctionResultType

object Nullable: FunctionResultType

/**
 * For overloads with result type of Unit
 */
object NoResult: FunctionResultType

/**
 * For overloads with result type of Nothing
 */
object NoReturn: FunctionResultType
