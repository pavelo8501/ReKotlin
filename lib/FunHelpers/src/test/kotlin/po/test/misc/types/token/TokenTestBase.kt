package po.test.misc.types.token

import po.misc.types.token.TokenFactory




abstract class TokenTestBase: TokenFactory {

    class SomeClass(
        val stringValue:String = "String value",
        val intValue:Int = 42
    )
}