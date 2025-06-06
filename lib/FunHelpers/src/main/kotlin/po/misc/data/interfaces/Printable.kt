package po.misc.data.interfaces


interface Printable {

}

interface PrintableProvider<T:Printable> {
    val template: T.()-> String
}
