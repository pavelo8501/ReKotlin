package po.misc.data.pretty_print.parts

@PublishedApi
internal interface PrettyHelper {
    companion object {


        fun toRowOptions(input: CommonRowOptions): RowOptions =
            when (input) {
                is RowOptions -> input
                is RowPresets -> input.asRowOptions()
            }
        fun toRowOptions(input: CommonRowOptions?, default : RowOptions = RowOptions()): RowOptions =
            input?.let(::toRowOptions) ?: default

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