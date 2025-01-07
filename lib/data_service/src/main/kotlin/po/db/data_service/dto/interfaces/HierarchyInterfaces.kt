package po.db.data_service.dto.interfaces



sealed interface HierarchyRoot :Hierarchy{

}

sealed interface Hierarchy {

     val className : String
}