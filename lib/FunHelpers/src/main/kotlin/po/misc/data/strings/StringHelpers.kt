package po.misc.data.strings

import po.misc.data.styles.SpecialChars


fun  String?.ifNotBlank(block: (String)-> String): String{
    return if(this != null && this.isNotBlank()){
        block.invoke(this)
    }else{
        SpecialChars.EMPTY
    }
}