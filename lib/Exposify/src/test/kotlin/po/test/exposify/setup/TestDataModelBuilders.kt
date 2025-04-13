package po.test.exposify.setup



private fun sectionModel(
    parent : TestPage,
    name: String,
    updatedBy: Long,
    sectionClasses : List<TestClassItem> = listOf<TestClassItem>(TestClassItem(1,"class_1"), TestClassItem(2,"class_2"))): TestSection
{

    return  TestSection(
        "TestSection/$name",
        "TestSection/$name Description",
        "",
        sectionClasses,
        updatedBy,
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


fun pageModelsWithSections(pageCount: Int, updatedBy : Long, sectionsCount: Int,  pageClasses : List<TestClassItem> = emptyList<TestClassItem>()): List<TestPage>{
    val pages =  pageModels(pageCount, updatedBy)
    pages.forEach {
        for(index  in 1 .. sectionsCount){
            it.sections.add(sectionModel(it,index.toString(), updatedBy))
        }
    }
    return  pages
}