package po.misc.exceptions



object ExceptionLocator : ExceptionLocatorBase(){
    override val helperPackages: MutableList<HelperPackage> = mutableListOf(
        HelperPackage("po.misc"),
        HelperPackage("kotlin"),
        HelperPackage("java")
    )
}