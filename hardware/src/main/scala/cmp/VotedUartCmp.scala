/*
 * A Uart wrapper with voting for Lockstep multicore
 *
 * Author: Christos Gkiokas (gkiokasc@gmail.com)
 */

package cmp

import Chisel._
import ocp._
import patmos.Constants._
import patmos._
import io.Uart

object VotedUartCmp {
  trait Pins {
    val votedUartCmpPins = new Bundle() {
      val tx = Bits(OUTPUT, 1)
      val rx = Bits(INPUT, 1)
    }
  }
}


class VotedUartCmp(corecnt: Int, clk_freq: Int, baud_rate: Int, fifoDepth: Int) extends Module {

  val io = new CmpIO(corecnt) with VotedUartCmp.Pins
  
  //connect voted uart to existing uart component
  val uart = Module(new Uart(clk_freq,baud_rate,fifoDepth))
  io.votedUartCmpPins <> uart.io.uartPins

  //Vote on the incoming ocp signals
  val votedCmd = (io.cores(0).M.Cmd & io.cores(1).M.Cmd) | 
                  (io.cores(0).M.Cmd & io.cores(2).M.Cmd) |
                  (io.cores(1).M.Cmd & io.cores(2).M.Cmd)

  val votedAddr = (io.cores(0).M.Addr & io.cores(1).M.Addr) | 
                  (io.cores(0).M.Addr & io.cores(2).M.Addr) |
                  (io.cores(1).M.Addr & io.cores(2).M.Addr)

  val votedData = (io.cores(0).M.Data & io.cores(1).M.Data) | 
                  (io.cores(0).M.Data & io.cores(2).M.Data) |
                  (io.cores(1).M.Data & io.cores(2).M.Data)

  val votedEn = (io.cores(0).M.ByteEn & io.cores(1).M.ByteEn) | 
                  (io.cores(0).M.ByteEn & io.cores(2).M.ByteEn) |
                  (io.cores(1).M.ByteEn & io.cores(2).M.ByteEn)

  //Inject the voted results into normal uart
  uart.io.ocp.M.Cmd := votedCmd
  uart.io.ocp.M.Addr := votedAddr
  uart.io.ocp.M.Data := votedData
  uart.io.ocp.M.ByteEn := votedEn
  

  val cmdReg = Reg(init = Bool(false))

  when(votedCmd =/= OcpCmd.IDLE) {
      cmdReg := Bool(true)
  }.elsewhen(uart.io.ocp.S.Resp === OcpResp.DVA) {
      cmdReg := Bool(false)
  }

  //TODO: Rx part
  for (i <- 0 until corecnt) {    
      io.cores(i).S.Data := uart.io.ocp.S.Data
      io.cores(i).S.Resp := uart.io.ocp.S.Resp
      when(cmdReg =/= Bool(true)) {
          io.cores(i).S.Resp := OcpResp.NULL
      }
  }
}
