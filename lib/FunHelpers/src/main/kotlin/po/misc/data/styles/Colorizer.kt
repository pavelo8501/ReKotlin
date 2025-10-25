package po.misc.data.styles

import po.misc.data.HasValue
import po.misc.interfaces.ValueBase


interface Colorizer {


    fun String.applyColour(colour: Colour): String{
        return Colour.applyColour(this, colour)
    }

    fun  String.colorizeIf(colour: Colour, negativeCaseColour: Colour? = null,   predicate: ()-> Boolean): String{
      return  if(predicate.invoke()){
            this.colorize(colour)
        }else{
          negativeCaseColour?.let {
              this.colorize(it)
          }?:this
        }
    }

    fun  HasValue.colorizeIf(colour: Colour, negativeCaseColour: Colour? = null,   predicate: ()-> Boolean): String{
        return  if(predicate.invoke()){
            this.value.colorize(colour)
        }else{
            negativeCaseColour?.let {
                this.value.colorize(it)
            }?:this.value
        }
    }

    fun  String.applyColourIf(colour: Colour, predicate: ()-> Boolean): String{
        return  if(predicate.invoke()){
            this.applyColour(colour)
        }else{
            this
        }
    }
}