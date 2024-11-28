package po.db.data_service.builder


import po.db.data_service.dto.DataTransferObjectsParent


data class DataModel(val id : Int = 5 , val name : String = "Some Data"){

}
//class DataTransferModel(override val id: Long) : DataTransferObjectsParent<DataTransferModel>(){
//    override fun configDTOModel(context: ConfigurationContext.() -> Unit) {
//        val a = 10
//    }
//}
//
//object DataTransferContainerTest {
//    val dataObject = DataModel(10,"Some_name")
//
//    fun startDTOTests() {
//        val model = DataTransferModel(1)
//
//    }
//}
//
//class  BindingsContext(){
//    fun bindProperties():String{
//        val someData : String = "Some DAta"
//        return  someData
//    }
//}

//class  DataTransferModel(override val id : Long): DataTransferObjectsParent<DataTransferModel>(), MarkerInterface{
//    abstract class DataProcessor {
//        abstract fun processData(
//            config: ProcessingContext.() -> Unit,
//            onResult: (String) -> Unit
//        )
//    }
//  override fun configDTOModel(context: ConfigurationContext.() -> Unit) {
//
//  }
//}

//class ProcessingContext {
//      private var prefix = ""
//      private var suffix = ""
//      fun setPrefix(value: String) { prefix = value }
//      fun setSuffix(value: String) { suffix = value }
//      fun process(input: String): String {
//        return "$prefix$input$suffix".uppercase()
//      }
//}

//ABSTRACT WITH CLOSURE
//abstract class DataProcessor {
//    abstract fun processData(
//        config: ProcessingContext.() -> Unit,
//        onResult: (String) -> Unit
//    )
//}
//
//class ProcessingContext {
//    private var prefix = ""
//    private var suffix = ""
//
//    fun setPrefix(value: String) { prefix = value }
//    fun setSuffix(value: String) { suffix = value }
//
//    fun process(input: String): String {
//        return "$prefix$input$suffix".uppercase()
//    }
//}
//
//class StringProcessor : DataProcessor() {
//    override fun processData(config: ProcessingContext.() -> Unit, onResult: (String) -> Unit) {
//        val context = ProcessingContext().apply(config) // Apply the config lambda
//        val result = context.process("Input Data")
//        sonReult(result) // Handle the result
//    }
//}
//
//// Usage
//val processor = StringProcessor()
//
//processor.processData(
//config = {
//    setPrefix("[")
//    setSuffix("]")
//},
//onResult = { result ->
//    println("Processed Data: $result")
//}
//)


