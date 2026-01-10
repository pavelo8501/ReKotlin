package po.misc.data.styles



fun CharSequence.contains(colour: StyleCode, ignoreCase: Boolean = false): Boolean =
     indexOf(colour.code, ignoreCase = ignoreCase) >= 0