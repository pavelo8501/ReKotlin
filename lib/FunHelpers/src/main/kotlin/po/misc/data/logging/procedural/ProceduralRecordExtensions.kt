package po.misc.data.logging.procedural


/**
 * If name not provided last entry without assigned result will get returned
 */
fun ProceduralRecord.findEntry(entryName: String? = null): ProceduralEntry?{

    fun searchByName(entryName: String): ProceduralEntry?{
       return proceduralEntries.firstOrNull { it.stepName ==  entryName}
    }

    fun searchLastActive(): ProceduralEntry?{
       return proceduralEntries.lastOrNull { it.stepResult == null  }
    }

    return  if(entryName != null){
        searchByName(entryName)
    }else{
        searchLastActive()
    }
}