package cmp

import Chisel._
import io._
import ocp.OcpCoreSlavePort
import patmos.Constants.{ADDR_WIDTH, DATA_WIDTH}
import patmos._

object VoterCmp {

  trait Pins {
    val voterCmpPins = new Bundle() {
      // val resultDataReg  = Vec.fill(3) {Bits(INPUT,DATA_WIDTH)}
      // val outputDataReg  = Vec.fill(3) {Bits(OUTPUT,DATA_WIDTH)}
      val resultDataReg  = Vec.fill(3) {new VoterResult().asInput}
      val outputDataReg  = Vec.fill(3) {new VoterResult().asOutput}
    }
  }

}


class VoterCmp(nrCores: Int) extends Module {
  val io = new CmpIO(nrCores) with VoterCmp.Pins


  //val voterDev = (Module(new VoterIO()).io)
  val res = Bits(width = DATA_WIDTH)

  val countReg = Reg(init = UInt(0, 32))
  countReg := countReg + UInt(1)
  when(countReg === 500000.U){
     res := (io.voterCmpPins.resultDataReg(0).data & 42.U) |
            (io.voterCmpPins.resultDataReg(0).data & 32.U) |   
            (42.U & 32.U)

  }.otherwise
  {
     res := (io.voterCmpPins.resultDataReg(0).data & io.voterCmpPins.resultDataReg(1).data) |
              (io.voterCmpPins.resultDataReg(0).data & io.voterCmpPins.resultDataReg(2).data) |   
              (io.voterCmpPins.resultDataReg(1).data & io.voterCmpPins.resultDataReg(2).data)
  }



  
  val valid = io.voterCmpPins.resultDataReg(0).valid & io.voterCmpPins.resultDataReg(1).valid & io.voterCmpPins.resultDataReg(2).valid
  //Wire one device IO device per core
  for (i <- 0 until nrCores) {
    
  // voterDev.ocp.M := io.cores(0).M
  // io.cores(0).S := voterDev.ocp.S
    io.voterCmpPins.outputDataReg(i).data := res
    io.voterCmpPins.outputDataReg(i).valid := valid
  //voterDev.voterIOPins.resultData := res
  //voterDevs(i).voterIOPins.resultData := io.voterCmpPins.resultDataReg(i)
  }


}