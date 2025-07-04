package po.lognotify.debug.interfaces

import po.lognotify.debug.models.InputParameter
import po.misc.data.processors.DataProcessor

interface DebugProvider {
   val inputParams: MutableList<InputParameter>
  // fun provideDataProcessor(processor: DataProcessor<*>)
}