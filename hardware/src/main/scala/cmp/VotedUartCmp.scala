/*
 * A multicore Uart wrapper
 *
 * Author: Torur Biskopsto Strom (torur.strom@gmail.com)
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

// class CmpIO(corecnt : Int) extends Bundle
// {
//   val cores = Vec(corecnt, new OcpCoreSlavePort(ADDR_WIDTH, DATA_WIDTH))

//   override def clone = new CmpIO(corecnt).asInstanceOf[this.type]
// }

class VotedUartCmp(corecnt: Int, clk_freq: Int, baud_rate: Int, fifoDepth: Int) extends Module {

  val io = new CmpIO(corecnt) with VotedUartCmp.Pins
  
  val uart = Module(new Uart(clk_freq,baud_rate,fifoDepth))
  
  io.votedUartCmpPins <> uart.io.uartPins

//     val Cmd = Bits(width = 3)
//   val Addr = Bits(width = addrWidth)
//   val Data = Bits(width = dataWidth)
// val res = (a & b |
//             (a & c |   
//             (b & c)
  

    //uart.io.ocp.M := PriorityMux(io.cores.map(e => (e.M.Cmd =/= OcpCmd.IDLE, e.M)))

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

    for (i <- 0 until corecnt) {    
        io.cores(i).S.Data := uart.io.ocp.S.Data
        io.cores(i).S.Resp := uart.io.ocp.S.Resp
        when(cmdReg =/= Bool(true)) {
            io.cores(i).S.Resp := OcpResp.NULL
        }
    }
}
