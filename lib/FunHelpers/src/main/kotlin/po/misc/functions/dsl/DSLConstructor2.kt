package po.misc.functions.dsl







@DSLBlockMarker
abstract class DSLBlockBase2<T: Any>(
    private val block: DSLBlockBase2<T>.(T)-> Unit
){
    val subBlocks = mutableListOf<DSLBlockBase2<T>>()




}


class BuilderDSLBlock<T: Any>(
    private val block: DSLBlockBase2<T>.(T)-> Unit
):DSLBlockBase2<T>(block){

}


class DSLConstructor2<T: Any>(
    private val constructLambda: DSLConstructor2<T>.(T)-> Unit
){

    val dslBlocks = mutableListOf<DSLBlockBase2<T>>()

    fun createBlock(block:DSLBlockBase2<T>.(T)-> Unit){
        dslBlocks.add(BuilderDSLBlock(block))
    }

}

