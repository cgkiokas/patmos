/*
 * Simple I/O 
 *
 * Authors: 
 *
 */

package io

import Chisel._

import ocp._
import patmos.Constants.{ADDR_WIDTH, DATA_WIDTH}

object VoterIO  {
  // var voterCount = -1

  // def init(params: Map[String, String]) = {
  //   //voterCount = getPosIntParam(params, "voterCount")
  // }

  // def create(params: Map[String, String]) : VoterIO = {
  //   Module(new VoterIO())
  // }

  // trait Pins {
  //   val voterIOPins = new Bundle() {
  //     //val resultDataReg  = (Reg(Bits(width = DATA_WIDTH)),OUTPUT)
  //   }
  //}
  def init(params: Map[String, String]) = {}

  def create(params: Map[String, String]): VoterIO = Module(new VoterIO())

  trait Pins 
  {
    val voterIOPins = new Bundle() {
      val resultData  = Bits(INPUT, DATA_WIDTH)
    }
  }
}

class VoterIO() extends CoreDevice() {

  override val io = new CoreDeviceIO() with VoterIO.Pins
  //val resultDataReg  = Reg(Bits(width = DATA_WIDTH))
  val voterReg = Reg(init = Bits(0, DATA_WIDTH))

  // Default response
  val respReg = Reg(init = OcpResp.NULL)
  respReg := OcpResp.NULL

  // Write 
  // when(io.ocp.M.Cmd === OcpCmd.WR) {
  //   respReg := OcpResp.DVA
  //   voterReg := io.ocp.M.Data(DATA_WIDTH-1, 0)
  // }

  // Read
  when(io.ocp.M.Cmd === OcpCmd.RD) {
    respReg := OcpResp.DVA
  }

  // Connections to master
  io.ocp.S.Resp := respReg
  io.ocp.S.Data := voterReg

  // Connection to pins
  //voterReg := io.ocp.M.Data(DATA_WIDTH-1, 0)
  voterReg := io.voterIOPins.resultData 
  //resultDataReg := Reg(next = voterReg)
}
