package po.exposify.test.setup



private fun sectionModel(parent : TestPage,  name: String): TestSection{
    val sectionClasses = listOf<TestClassItem>(TestClassItem(1,"class_1"), TestClassItem(2,"class_2"))
    val sectionMetaTags = listOf<TestMetaTag>(TestMetaTag(1, "tag_key_1", "tag_value_1"))
    return  TestSection(
        "TestSection/$name",
        "TestSection/$name Description",
        "",
        sectionClasses,
        sectionMetaTags,
        1,
        parent.id)
}

private fun pageModel(name: String, pageClasses: List<TestClassItem>, updatedBy: Long): TestPage{
    return  TestPage("TestPage/$name", 1, pageClasses, updatedBy)
}


fun pageModels(quantity: Int, updatedBy : Long,  pageClasses : List<TestClassItem> = emptyList<TestClassItem>()): List<TestPage>{
    val result =  mutableListOf<TestPage>()
    for(index  in 1 .. quantity){
        result.add(pageModel(index.toString(), pageClasses, updatedBy))
    }
    return  result
}


fun pageModelsWithSections(pageCount: Int, updatedBy : Long, sectionsCount: Int): List<TestPage>{
    val pages =  pageModels(pageCount, updatedBy)
    pages.forEach {
        for(index  in 1 .. sectionsCount){
            it.sections.add(sectionModel(it, index.toString()))
        }
    }
    return  pages
}