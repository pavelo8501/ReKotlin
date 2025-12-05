package po.misc.data.pretty_print.parts

import po.misc.data.pretty_print.presets.PrettyPresets

interface PrettyHelper {

    companion object {

        fun toRowOptions(commonRowOptions: CommonRowOptions): RowOptions {
            return when (commonRowOptions) {
                is RowOptions -> commonRowOptions
                is RowPresets -> commonRowOptions.toOptions()
            }
        }
        fun toRowOptionsOrDefault(commonRowOptions: CommonRowOptions?): RowOptions {
            if(commonRowOptions == null){
               return RowOptions()
            }
            return toRowOptions(commonRowOptions)
        }

        fun toRowOptionsOrDefault(commonRowOptions: CommonRowOptions?, defaultOptions: RowOptions): RowOptions {
            if(commonRowOptions == null){
                return defaultOptions
            }
            return toRowOptions(commonRowOptions)
        }


        fun toCellOptions(commonCellOptions: CommonCellOptions): CellOptions {
            return when (commonCellOptions) {
                is CellOptions -> commonCellOptions
                is PrettyPresets -> commonCellOptions.toOptions()
                is KeyedCellOptions -> commonCellOptions.toCellOptions()
            }
        }
        fun toCellOptionsOrDefault(commonCellOptions: CommonCellOptions?,  defaultValue: CellOptions? = null): CellOptions {
            if(commonCellOptions == null){
                return defaultValue?: CellOptions()
            }
            return  toCellOptions(commonCellOptions)
        }

        fun toCellOptions(commonRowOptions: CommonRowOptions): CellOptions {
            return when (commonRowOptions) {
                is RowOptions -> CellOptions(commonRowOptions)
                is RowPresets -> CellOptions(commonRowOptions.toOptions())
            }
        }
        fun toCellOptionsOrDefault(commonRowOptions: CommonRowOptions?, defaultValue: RowOptions? = null): CellOptions {
            if(commonRowOptions == null){
                return  CellOptions(defaultValue?: RowOptions(Orientation.Horizontal))
            }
            return toCellOptions(commonRowOptions)
        }

        fun toCellOptionsOrNull(commonRowOptions: CommonRowOptions?): CellOptions? {
            if(commonRowOptions == null){
                return null
            }
            return toCellOptions(commonRowOptions)
        }

        fun toCellOptionsOrDefault(commonCellOptions: CommonCellOptions?, defaultValue: RowOptions? = null): CellOptions {
            if(commonCellOptions == null){
                return  CellOptions(defaultValue?: RowOptions(Orientation.Horizontal))
            }
            return toCellOptions(commonCellOptions)
        }

        fun toKeyedCellOptions(commonCellOptions: CommonCellOptions): KeyedCellOptions {
            return when (commonCellOptions) {
                is KeyedCellOptions -> commonCellOptions
                is CellOptions -> KeyedCellOptions(commonCellOptions)
            }
        }
        fun toKeyedCellOptionsOrDefault(commonCellOptions: CommonCellOptions?, defaultValue: KeyedCellOptions? = null): KeyedCellOptions {
            if(commonCellOptions == null){
               return defaultValue?: KeyedCellOptions()
            }
            return toKeyedCellOptions(commonCellOptions)
        }

    }
}