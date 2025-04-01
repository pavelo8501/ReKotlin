package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel



//class SingleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
//  //  parent : CommonDTO<DATA, ENTITY>,
//    private val binding : SingleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
//): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, binding)
//        where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity {
//
//    override val repoName: String =  "Repository/Single]"
//
//    val childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>
//        get() = binding.childModel
//
//
//    override fun setReferenced(childEntity:CHILD_ENTITY, parentEntity:ENTITY){
//        binding.referencedOnProperty?.set(childEntity, parentEntity)
//    }
//
//    override fun getReferences(parentEntity: ENTITY): List<CHILD_ENTITY> {
//        binding.byProperty.get(parentEntity)?.let {
//            return  listOf<CHILD_ENTITY>(it)
//        }?: return emptyList()
//    }
//
//    override fun extractDataModel(dataModel:DATA): List<CHILD_DATA>{
//        val result =  childModel.factory.extractDataModel(binding.sourcePropertyWrapper.extract(), dataModel)
//        return if(result!=null){
//            listOf<CHILD_DATA>(result)
//        }else{
//            emptyList()
//        }
//    }
//}
//
//class MultipleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
//    parent : CommonDTO<DATA, ENTITY>,
//    private val binding : MultipleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
//): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, binding)
//    where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
//{
//
//    override val repoName: String =  "Repository[/Multiple]"
//
//    override fun setReferenced(childEntity:CHILD_ENTITY, parentEntity:ENTITY){
//        binding.referencedOnProperty?.set(childEntity, parentEntity)
//    }
//
//    override fun getReferences(parentEntity: ENTITY): List<CHILD_ENTITY> {
//        binding.byProperty.get(parentEntity).let {
//            return   it.toList()
//        }
//    }
//
//    override fun extractDataModel(dataModel:DATA): List<CHILD_DATA>
//            = binding.childModel.factory.extractDataModel(binding.sourceProperty, dataModel)
//}
//
//
//
//
//sealed class RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
//    val parent : CommonDTO<DATA, ENTITY>,
//  //  val childDTOClass: DTOClass<CHILD_DATA, CHILD_ENTITY>,
//  //  val bindingKey : BindingKeyBase,
//    private val  bindingContainer : BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
//) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity{
//
//    abstract val repoName : String
//
//    abstract fun extractDataModel(dataModel:DATA):List<CHILD_DATA?>
//    abstract fun setReferenced(childEntity:CHILD_ENTITY, parentEntity:ENTITY)
//    abstract fun getReferences(parentEntity:ENTITY): List<CHILD_ENTITY>
//
//    var initialized: Boolean = false
//    val dtoList = mutableListOf<CommonDTO<CHILD_DATA, CHILD_ENTITY>>()
//
//    val childFactory: DTOFactory<*,*>
//        get(){return  bindingContainer.childModel.factory }
//
//    init {
////        parent.onUpdate={
////            println("OnUpdate Callback invoked by parent in ${repoName}")
////            onInitHostedByData.forEach {
////                println("Invoking stored by ${it.first.sourceModel.className} Fn in ${repoName}")
////                it.second()
////            }
////        }
//
////        parent.onUpdateFromEntity={
////            println("OnUpdateFromEntity Callback invoked by parent in ${repoName}")
////            onInitHostedByEntity.forEach {
////                println("Invoking stored by ${it.first.sourceModel.className} Fn in ${repoName}")
////            }
////        }
////
////        parent.onDelete={
////            println("OnDelete callback invoked by parent in ${repoName}")
////        }
//    }
//
//    suspend fun <CHILD_ENTITY : LongEntity> select(
//        entity:CHILD_ENTITY){
//       // parent.dtoClass.
//
//    //    childDTO.onUpdateFromEntity?.invoke(entity)
//    }
//
//
////     suspend fun <CHILD_ENTITY : LongEntity> initByEntity(
////        entity:CHILD_ENTITY,
////       // childDTO: HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>){
////      //  childDTO.onUpdateFromEntity?.invoke(entity)
////    }
//
//
////    private  val onInitHostedByData = mutableListOf<
////            Pair<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
////                    suspend ()-> Unit>>()
////
////    private suspend fun subscribeOnInitByData(
////        subscriber: HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
////        callback: suspend ()-> Unit){
////        onInitHostedByData.add(Pair(subscriber, callback))
////    }
////
////    private val onInitHostedByEntity = mutableListOf<
////            Pair<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
////                        (ENTITY)-> Unit>>()
////
////
////    suspend fun deleteAll(){
////        println("DeleteAll called in : $repoName")
////        dtoList.forEach {
////            it.sourceModel.daoService.delete(it)
////        }
////    }
////
////
////    suspend fun deleteAllRecursively() {
////        println("DeleteAllRecursively called on : $repoName")
////        // Recursively delete all child repositories first
////        dtoList.forEach { childHostDTO ->
////            childHostDTO.deleteInRepositories()
////        }
////        println("Calling deleteAll from : $repoName")
////        // After all child deletions, delete current repository data
////        deleteAll()
////    }
////
////
////    /**
////     * Propagate a call to the parent repository.
////     */
////    suspend fun propagateOnUpdateByData(
////        childDTO: HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>){
////        childDTO.onUpdate?.invoke()
////    }
////
////    suspend fun propagateOnInitByEntity(
////        entity:CHILD_ENTITY,
////        childDTO: HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>){
////        childDTO.onUpdateFromEntity?.invoke(entity)
////    }
////
////    fun propagateOnDelete(childDTO : HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>){
////        childDTO.subscribeOnDelete{
//            childDTO.repositories.values.forEach {
//
//            }
//        }
//    }
//
//    suspend  fun initialize(entity:ENTITY){
//        println("Initialize  in  $repoName")
//        getReferences(entity).forEach { childEntity ->
//            factory.createDataModel().let { dataModel ->
//                createHosted(dataModel).let { hosted ->
//                    hosted.update(childEntity, UpdateMode.ENTITY_TO_MODEL)
//                    dtoList.add(hosted)
//                    childModel.bindings.values.forEach {
//                        it.applyBindingToHost(hosted)
//                        hosted.initializeRepositories(hosted.entityDAO)
//                    }
//                }
//            }
//        }
//        initialized = true
//        println("Repository initialized for ${parent.sourceModel.className} with id ${parent.getInjectedModel().id}")
//    }
//
//    suspend fun initialize(dataModel:DATA){
//        extractDataModel(dataModel).forEach { childData ->
//            createHosted(childData).let { hosted ->
//                dtoList.add(hosted)
//                childModel.bindings.values.forEach {
//                    it.applyBindingToHost(hosted)
//                    hosted.initializeRepositories()
//                }
//            }
//        }
//        initialized = true
//        println("Repository initialized for ${parent.sourceModel.className} with id ${parent.getInjectedModel().id}")
//        println("ByData Subscriptions count ${onInitHostedByData.count()}")
//        // "This needs to be refactored. Calling to loop through repos inside a repo creates complexity"
//        parent.repositories.values.forEach {
//
//            it.subscribeOnInitByData(parent) {
//                dtoList.forEach { dto ->
//                    if (!dto.isSaved) {
//                        dto.sourceModel.daoService.saveNew(dto) {
//                            setReferenced(it, parent.entityDAO)
//                        }
//                        propagateOnUpdateByData(dto)
//                    } else {
//                        dto.sourceModel.daoService.updateExistent(dto)
//                        propagateOnUpdateByData(dto)
//                    }
//                }
//            }
//        }
//        parent.subscribeOnDelete {
//            dtoList.forEach { dto ->
//                propagateOnDelete(dto)
//            }
//        }
//    }
//
//    protected fun createHosted(childData : CHILD_DATA):HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>{
//        return HostDTO.createHosted<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>(childData, childModel)
//    }

//}



//class SingleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
//    parent :  HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
//    childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
//    bindingKey : BindingKeyBase.OneToOne<CHILD_DATA, CHILD_ENTITY>,
//    val binding : SingleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
//): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, childModel,  bindingKey, binding)
//        where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
//{
//    override val repoName: String =  "Repository ${parent.sourceModel.className}/Single"
//
////    fun initialize(){
////        parent.subscribeOnInitByData(parent) {
////            extractDataModel(it.toDataModel())?.let {childData ->
////                createHosted(childData).let { hosted ->
////                        hosted.setChildBindings()
////                        childModel.daoService.saveNew(hosted) { childEntity ->
////                            binding.referencedOnProperty.set(childEntity, it.entityDAO)
////                        }
////                        dtoList.add(hosted)
////                    }
////                }
////        }
////    }
//
//    fun getSourceProperty(): KProperty1<DATA, CHILD_DATA?>{
//        return binding.sourceProperty.extractNullable()
//    }
//
//    override fun setReferenced(childEntity:CHILD_ENTITY, parentEntity:ENTITY){
//        binding.referencedOnProperty?.set(childEntity, parentEntity)
//    }
//
//    override fun getReferences(parentEntity: ENTITY): List<CHILD_ENTITY> {
//        binding.byProperty.get(parentEntity)?.let {
//            return  listOf<CHILD_ENTITY>(it)
//        }?: return emptyList()
//    }
//
//    fun submitSingleDataModel(dataModel:DATA){
//        binding.sourceProperty.set(dataModel, dtoList[0].getInjectedModel() )
//        val a = 10
//    }
//
//    override fun extractDataModel(dataModel:DATA): List<CHILD_DATA>{
//        val result =  childModel.factory.extractDataModel(binding.sourceProperty.extractNullable(), dataModel)
//        return if(result!=null){
//            listOf<CHILD_DATA>(result)
//        }else{
//            emptyList()
//        }
//    }
//
//}
//
//class MultipleRepository<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
//    parent : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
//    childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
//    bindingKey : BindingKeyBase.OneToMany<CHILD_DATA, CHILD_ENTITY>,
//    val binding : MultipleChildContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
//): RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(parent, childModel, bindingKey, binding)
//    where  DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity
//{
//
//    override val repoName: String =  "Repository ${parent.sourceModel.className}/Multiple"
//
//    override fun setReferenced(childEntity:CHILD_ENTITY, parentEntity:ENTITY){
//        binding.referencedOnProperty?.set(childEntity, parentEntity)
//    }
//
//    inline fun <reified DATA : DataModel> submitMultipleDataModels(
//
//        dataModel:DATA
//        )
//    {
//        val childDataClass = DATA::class
//        val dataClass = DATA::class
//        dtoList.forEach {
//            @Suppress("UNCHECKED_CAST")
//            (binding.sourceProperty as MutableList<CHILD_DATA>).add(it.getInjectedModel() as CHILD_DATA)
//            val a = 10
//        }
//    }
//
//    fun getSourceProperty(): KProperty1<DATA, Iterable<CHILD_DATA>>{
//        return binding.sourceProperty
//    }
//
//
//    override fun getReferences(parentEntity: ENTITY): List<CHILD_ENTITY> {
//        binding.byProperty.get(parentEntity).let {
//            return  it.toList()
//        }
//    }
//
//    override fun extractDataModel(dataModel:DATA): List<CHILD_DATA>
//            = childModel.factory.extractDataModel(binding.sourceProperty, dataModel)
//}

//sealed class RepositoryBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>(
//    val parent : HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
//    val childModel : DTOClass<CHILD_DATA, CHILD_ENTITY>,
//    val bindingKey : BindingKeyBase,
//    binding : BindingContainer<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
//) where DATA : DataModel, ENTITY : LongEntity, CHILD_DATA: DataModel, CHILD_ENTITY: LongEntity{
//
//    abstract val repoName : String
//
//    abstract fun extractDataModel(dataModel:DATA):List<CHILD_DATA>
//    abstract fun setReferenced(childEntity:CHILD_ENTITY, parentEntity:ENTITY)
//    abstract fun getReferences(parentEntity:ENTITY): List<CHILD_ENTITY>
//
//    var initialized: Boolean = false
//    val dtoList = mutableListOf<HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>>()
//
//    val factory: Factory<CHILD_DATA, CHILD_ENTITY>
//        get(){return  childModel.factory }
//
//    init {
//        parent.onUpdate={
//            println("OnUpdate Callback invoked by parent in ${repoName}")
//            onInitHostedByData.forEach {
//                println("Invoking stored by ${it.first.sourceModel.className} Fn in ${repoName}")
//                it.second()
//            }
//        }
//
//        parent.onUpdateFromEntity={
//            println("OnUpdateFromEntity Callback invoked by parent in ${repoName}")
//            onInitHostedByEntity.forEach {
//                println("Invoking stored by ${it.first.sourceModel.className} Fn in ${repoName}")
//            }
//        }
//
//        parent.onDelete={
//            println("OnDelete callback invoked by parent in ${repoName}")
//        }
//    }
//
//    private  val onInitHostedByData = mutableListOf<
//            Pair<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
//                    suspend ()-> Unit>>()
//
//   private suspend fun subscribeOnInitByData(
//        subscriber: HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
//        callback: suspend ()-> Unit){
//        onInitHostedByData.add(Pair(subscriber, callback))
//    }
//
//    private val onInitHostedByEntity = mutableListOf<
//            Pair<HostDTO<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
//                (ENTITY)-> Unit>>()
//
//
//    suspend fun deleteAll(){
//        println("DeleteAll called in : $repoName")
//        dtoList.forEach {
//            it.sourceModel.daoService.delete(it)
//        }
//    }
//
//
//    suspend fun deleteAllRecursively() {
//        println("DeleteAllRecursively called on : $repoName")
//        // Recursively delete all child repositories first
//        dtoList.forEach { childHostDTO ->
//            childHostDTO.deleteInRepositories()
//        }
//        println("Calling deleteAll from : $repoName")
//        // After all child deletions, delete current repository data
//        deleteAll()
//    }
//
//
//    /**
//     * Propagate a call to the parent repository.
//     */
//    suspend fun propagateOnUpdateByData(
//        childDTO: HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>){
//        childDTO.onUpdate?.invoke()
//    }
//
//    suspend fun propagateOnInitByEntity(
//        entity:CHILD_ENTITY,
//        childDTO: HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>){
//        childDTO.onUpdateFromEntity?.invoke(entity)
//    }
//
//    fun propagateOnDelete(childDTO : HostDTO<CHILD_DATA, CHILD_ENTITY,DATA, ENTITY>){
//        childDTO.subscribeOnDelete{
//            childDTO.repositories.values.forEach {
//
//            }
//        }
//    }
//
//   suspend  fun initialize(entity:ENTITY){
//        println("Initialize  in  $repoName")
//        getReferences(entity).forEach { childEntity ->
//            factory.createDataModel().let { dataModel ->
//               createHosted(dataModel).let { hosted ->
//                   hosted.update(childEntity, UpdateMode.ENTITY_TO_MODEL)
//                   dtoList.add(hosted)
//                   childModel.bindings.values.forEach {
//                       it.applyBindingToHost(hosted)
//                       hosted.initializeRepositories(hosted.entityDAO)
//                   }
//               }
//            }
//        }
//        initialized = true
//        println("Repository initialized for ${parent.sourceModel.className} with id ${parent.getInjectedModel().id}")
//    }
//
//   suspend fun initialize(dataModel:DATA){
//        extractDataModel(dataModel).forEach { childData ->
//            createHosted(childData).let { hosted ->
//                dtoList.add(hosted)
//                childModel.bindings.values.forEach {
//                    it.applyBindingToHost(hosted)
//                    hosted.initializeRepositories()
//                }
//            }
//        }
//        initialized = true
//        println("Repository initialized for ${parent.sourceModel.className} with id ${parent.getInjectedModel().id}")
//        println("ByData Subscriptions count ${onInitHostedByData.count()}")
//        // "This needs to be refactored. Calling to loop through repos inside a repo creates complexity"
//        parent.repositories.values.forEach {
//
//            it.subscribeOnInitByData(parent) {
//                dtoList.forEach { dto ->
//                    if (!dto.isSaved) {
//                        dto.sourceModel.daoService.saveNew(dto) {
//                            setReferenced(it, parent.entityDAO)
//                        }
//                        propagateOnUpdateByData(dto)
//                    } else {
//                        dto.sourceModel.daoService.updateExistent(dto)
//                        propagateOnUpdateByData(dto)
//                    }
//                }
//            }
//        }
//        parent.subscribeOnDelete {
//            dtoList.forEach { dto ->
//                propagateOnDelete(dto)
//            }
//        }
//    }
//
//    protected fun createHosted(childData : CHILD_DATA):HostDTO<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>{
//       return HostDTO.createHosted<CHILD_DATA, CHILD_ENTITY, DATA, ENTITY>(childData, childModel)
//    }
//
//}