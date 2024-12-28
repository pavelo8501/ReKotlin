package po.db.data_service.constructors

//class ClassBlueprintContainer {
//
//}

data class ClassBlueprintContainer(
   var dtoModel: ClassBlueprint? = null,
   var dataModel: ClassBlueprint? = null,
){
   fun addDto(dtoModel: ClassBlueprint){
      this.dtoModel = dtoModel
   }

   fun addData(dataModel: ClassBlueprint){
      this.dataModel = dataModel
   }
}