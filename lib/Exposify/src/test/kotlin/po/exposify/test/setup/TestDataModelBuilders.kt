package po.exposify.test.setup

class TestDataModelBuilders {
}


fun pageModels(quantity: Int): List<TestPage>{
    val result =  mutableListOf<TestPage>()
    for(index  in 1 .. quantity){
        result.add(pageModel(index.toString()))
    }
    return  result
}