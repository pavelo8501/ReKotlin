package po.lognotify.extensions

//
//suspend inline fun <reified T: IdentifiableContext, R: Any?> T.subTasksAsync(
//    taskName: String,
//    config: TaskConfig = TaskConfig(isDefault = true),
//    debugProxy: DebugProxy<*,*>? = null,
//    noinline block: suspend  T.(TaskHandler<R>)-> R
//): TaskResult<R> {
//
//
//    var effectiveConfig = config
//    val rootTask = TasksManaged.LogNotify.taskDispatcher.activeRootTask()
//    val moduleName: String = effectiveConfig.moduleName.emptyAsNull()?:this.contextName
//    if(rootTask != null && config.isDefault){
//        effectiveConfig = rootTask.config
//    }
//    val result = if(rootTask != null){
//        val subTask = rootTask.createChild<T, R>(taskName, moduleName, effectiveConfig, this)
//        withContext(subTask.coroutineContext){
//            try {
//                subTask.start()
//                debugProxy?.provideDataProcessor(subTask.dataProcessor)
//                val value =  block.invoke(this, subTask.handler)
//                onTaskResult<T, R>(subTask, value)
//                TaskResult(subTask, value)
//            }catch (throwable: Throwable){
//              val managed =  handleException(throwable, subTask, null)
//              createFaultyResult(managed, subTask)
//            }finally {
//                subTask.complete()
//            }
//        }
//    }else{
//        runTaskAsync(taskName, config, block)
//    }
//    return result
//}
//
//
//inline fun <reified T: IdentifiableContext, R: Any?> T.subsTask(
//    taskName: String,
//    config: TaskConfig = TaskConfig(isDefault = true),
//    debugProxy: DebugProxy<*,*>? = null,
//    block: T.(TaskHandler<R>) -> R
//): TaskResult<R>{
//
//    var effectiveConfig = config
//    val moduleName: String = effectiveConfig.moduleName.emptyAsNull()?:this.contextName
//    val rootTask = TasksManaged.LogNotify.taskDispatcher.activeRootTask()
//    if(rootTask != null && config.isDefault){
//        effectiveConfig = rootTask.config
//    }
//    val result =  rootTask?.let {
//       val subTask = it.createChild<T, R>(taskName, moduleName, effectiveConfig, this)
//       subTask.start()
//       debugProxy?.provideDataProcessor(subTask.dataProcessor)
//       val subTaskResult = try {
//            val value = block.invoke(this, subTask.handler)
//            onTaskResult<T,R>(subTask, value)
//        }catch (throwable: Throwable) {
//           val snapshot = takePropertySnapshot<T, LogOnFault>(this)
//           val managed = handleException(throwable, subTask, snapshot)
//           val subtaskFaultyResult =  if(managed.handler == HandlerType.CancelAll){
//               rootTask.complete(managed)
//           }else{
//               createFaultyResult(managed, subTask)
//           }
//           subtaskFaultyResult
//        } finally {
//            subTask.complete()
//        }
//        subTaskResult
//    }?:this.runTask(taskName, config, block =  block)
//    return result
//}


