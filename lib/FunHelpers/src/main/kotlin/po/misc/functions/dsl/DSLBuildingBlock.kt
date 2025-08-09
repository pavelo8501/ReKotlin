package po.misc.functions.dsl

import po.misc.containers.Containable

interface DSLBuildingBlock: Containable {

}

inline fun <T:DSLBuildingBlock> T.runOnReceiver(block:T.()-> Unit):T{
  this.block()
   return this
}