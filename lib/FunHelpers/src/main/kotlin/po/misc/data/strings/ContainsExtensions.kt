package po.misc.data.strings

import po.misc.data.styles.StyleCode
import po.misc.data.text_span.TextSpan
import po.misc.interfaces.named.Named

fun CharSequence.contains(named: Named, ignoreCase: Boolean = false): Boolean =
    indexOf(named.name, ignoreCase = ignoreCase) >= 0


fun CharSequence.contains(colour: StyleCode, ignoreCase: Boolean = false): Boolean =
    indexOf(colour.code, ignoreCase = ignoreCase) >= 0

fun TextSpan.contains(colour: StyleCode, ignoreCase: Boolean = false): Boolean =
    styled.indexOf(colour.code, ignoreCase = ignoreCase) >= 0

fun TextSpan.contains(other: CharSequence, ignoreCase: Boolean = false): Boolean =
    plain.contains(other, ignoreCase = ignoreCase)