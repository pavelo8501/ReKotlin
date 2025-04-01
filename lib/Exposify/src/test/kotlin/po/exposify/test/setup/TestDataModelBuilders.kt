package po.exposify.test.setup

fun pageModels(quantity: Int, ): List<TestPage>{
    val result =  mutableListOf<TestPage>()
    for(index  in 1 .. quantity){
        result.add(pageModel(index.toString()))
    }
    return  result
}


fun pageModelsWithSections(pageCount: Int, sectionsCount: Int): List<TestPage>{
    val pages =  pageModels(pageCount)
    pages.forEach {
        for(index  in 1 .. sectionsCount){
            it.sections.add(sectionModel(it, index.toString()))
        }
    }
    return  pages
}