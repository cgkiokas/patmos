/*
 * A Majority voter component for lockstep patmos
 *
 * Author: Christos Gkiokas (gkiokasc@gmail.com)
 */
package cmp

import Chisel._
import io._
import ocp.OcpCoreSlavePort
import patmos.Constants.{ADDR_WIDTH, DATA_WIDTH}
import patmos._

object VoterCmp {

  trait Pins {
    val voterCmpPins = new Bundle() {
      val resultData  = Vec.fill(3) {new VoterResult().asInput}
      val outputData  = Vec.fill(3) {new VoterResult().asOutput}
      val fault  = Bits(OUTPUT, 1)
    }
  }

}


class VoterCmp(nrCores: Int) extends Module {
  val io = new CmpIO(nrCores) with VoterCmp.Pins
  
  //Voter logic result = a and b or a and c or b and c
  val res = (io.voterCmpPins.resultData(0).data & io.voterCmpPins.resultData(1).data) |
             (io.voterCmpPins.resultData(0).data & io.voterCmpPins.resultData(2).data) |   
             (io.voterCmpPins.resultData(1).data & io.voterCmpPins.resultData(2).data)

  //Fault flag for the led demo
  when (res === 0.U)
  {
    io.voterCmpPins.fault := 1.U
  }.otherwise
  {
    io.voterCmpPins.fault := 0.U
  }
  
  //Data valid signal synchronization
  val valid = io.voterCmpPins.resultData(0).valid & io.voterCmpPins.resultData(1).valid & io.voterCmpPins.resultData(2).valid
  
  //Wire one device IO device per core
  for (i <- 0 until nrCores) {
    io.voterCmpPins.outputData(i).data := res
    io.voterCmpPins.outputData(i).valid := valid
  }


}