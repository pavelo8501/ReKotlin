package po.exposify.test.setup



private fun sectionModel(parent : TestPage,  name: String): TestSection{
    val testClassItems = listOf<TestClassItem>(TestClassItem(1,"class_1"), TestClassItem(2,"class_2"))
    return  TestSection(
        "TestSection/$name",
        "TestSection/$name Description",
        "",
        testClassItems,
        1,
        parent.id)
}

private fun pageModel(name: String, pageClasses: List<TestClassItem>): TestPage{
    return  TestPage("TestPage/$name", 1, pageClasses)
}


fun pageModels(quantity: Int, pageClasses : List<TestClassItem> = emptyList<TestClassItem>()): List<TestPage>{
    val result =  mutableListOf<TestPage>()
    for(index  in 1 .. quantity){
        result.add(pageModel(index.toString(), pageClasses))
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