package po.test.misc.types.token

import po.misc.types.token.TokenFactory
import po.misc.types.token.Tokenized
import po.misc.types.token.TypeToken


abstract class TokenTestBase: TokenFactory {

    class ParametrizedClass<T>(
        val parameter: T
    )

    class DoubleParametrized<T, V>(
        val parameterT: T,
        val parameterV: V
    )

    class TokenizedClass<T>(
        val parameter: T,
        override val typeToken: TypeToken<T>
    ): Tokenized<T>

    class SomeClass(
        val stringValue:String = "String value",
        val intValue:Int = 42
    )
}