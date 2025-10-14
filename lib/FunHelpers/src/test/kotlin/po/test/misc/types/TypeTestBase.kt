package po.test.misc.types

import po.misc.types.token.TypeToken

abstract class TypeTestBase {


    fun  createIntComponents(count: Int): List<ComponentInt> {
        val result = mutableListOf<ComponentInt>()
        for (i in 1..count){
            result.add(ComponentInt(i))
        }
        return result
    }



    fun  createHoldersComponentInt(count: Int): List<TypeHolder2<ComponentInt, SealedInheritor>>{
        val list = mutableListOf<TypeHolder2<ComponentInt, SealedInheritor>>()

        for (i in 1..count){
            val newTypeData = TypeToken.create<TypeHolder2<ComponentInt, SealedInheritor>>()
            val holder =  TypeHolder2(newTypeData,  ComponentInt(i),   SealedInheritor("So sealed"))

            list.add(holder)
        }
        return list
    }

    fun  createHoldersComponentStr(count: Int): List<TypeHolder2<ComponentString, SealedInheritor>>{
        val list = mutableListOf<TypeHolder2<ComponentString, SealedInheritor>>()
        for (i in 1..count){
            val newTypeData = TypeToken.create<TypeHolder2<ComponentString, SealedInheritor>>()

            val holder = TypeHolder2(
                newTypeData,
                ComponentString("String_$i"),
                SealedInheritor("Sealed in company with ComponentString")
            )

            list.add(holder)
        }
        return list
    }

}