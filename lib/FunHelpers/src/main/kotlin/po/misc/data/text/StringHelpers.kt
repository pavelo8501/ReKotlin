package po.misc.data.text


fun String.applyIfNotEmpty(block:String.()-> String): String{
    if(this.isNotEmpty()){
       return this.block()
    }
    return this
}

fun String?.applyIfNull(block:String.()-> String): String{
    return this?.block()?:""
}