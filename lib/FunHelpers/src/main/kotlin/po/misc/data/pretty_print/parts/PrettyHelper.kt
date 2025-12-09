package po.misc.data.pretty_print.parts

@PublishedApi
internal interface PrettyHelper {

    companion object {

        fun toRowOptions(input: CommonRowOptions): RowOptions =
            when (input) {
                is RowOptions -> input
                is RowPresets -> input.asRowOptions()
            }

        fun toRowOptions(input: CommonRowOptions?, default : CommonRowOptions? = null): RowOptions {

          val options =  if(default == null){
                input?.let(::toRowOptions) ?: run {
                    RowOptions()
                }
            }else{
                when(default){
                    is RowOptions ->{
                        if(default.useNoEdit){
                            default
                        }else{
                            input?.let(::toRowOptions) ?: run { default }
                        }
                    }
                    is RowPresets ->{
                        input?.let(::toRowOptions) ?: run {
                            toRowOptions(default)
                        }
                    }
                }
            }
            return options
        }

        fun toRowOptionsOrNull(input: CommonRowOptions?):RowOptions?{
            if(input == null){
                return null
            }
           return toRowOptions(input)
        }


        fun toOptions(input: PrettyOptions): Options =
             when (input) {
                is Options->  input
                else -> input.asOptions()
            }

        fun toOptions(input: PrettyOptions?, default: Options = Options()): Options =
            input?.let(::toOptions) ?: default

        fun toOptionsOrNull(input: PrettyOptions?): Options? {
            if(input == null){
                return null
            }
            return toOptions(input)
        }
        fun toKeyedOptions(input: CommonCellOptions): KeyedOptions =
            when (input) {
                is KeyedOptions -> input
                is KeyedPresets -> input.toOptions()
                is CellPresets -> KeyedOptions(input.toOptions())
                is CellOptions -> KeyedOptions(input)
            }
        fun toKeyedOptions(input: CommonCellOptions?, default: KeyedOptions = KeyedOptions()): KeyedOptions =
            input?.let(::toKeyedOptions) ?: default

        fun toKeyedOptionsOrNull(commonCellOptions: CommonCellOptions?): KeyedOptions? {
            if(commonCellOptions == null){
                return null
            }
            return toKeyedOptions(commonCellOptions)
        }

    }
}