/*
 * Patmos Core Lockstep Class
 *
 * Authors: Christos Gkiokas(gkiokasc@gmail.com)
 *
 */

package patmos

import Chisel._
import java.io.File

import Constants._
import util._
import io._
import datacache._
import ocp.{OcpCoreSlavePort, _}
import argo._





/**
 * Module for one Patmos core.
 */
class PatmosCoreLockstep( binFile: String,nr: Int, cnt: Int, aegeanCompatible: Boolean) extends PatmosCore(binFile,nr,cnt,aegeanCompatible) {
   println("LOCKSTEP ENABLED")


//  override val io = IO(Config.getPatmosCoreIO(nr))
//
//  override val icache =
//    if (ICACHE_SIZE <= 0)
//      Module(new NullICache())
//    else if (ICACHE_TYPE == ICACHE_TYPE_METHOD && ICACHE_REPL == CACHE_REPL_FIFO)
//      Module(new MCache())
//    else if (ICACHE_TYPE == ICACHE_TYPE_LINE && ICACHE_ASSOC == 1)
//      Module(new ICache())
//    else {
//      throw new Error("Unsupported instruction cache configuration:" +
//        " type \"" + ICACHE_TYPE + "\"" +
//        " (must be \"" + ICACHE_TYPE_METHOD + "\" or \"" + ICACHE_TYPE_LINE + "\")" +
//        " associativity " + ICACHE_ASSOC +
//        " with replacement policy \"" + ICACHE_REPL + "\"")
//      Module(new NullICache()) // return at least a dummy cache
//    }
//
//  override val fetch = Module(new Fetch(binFile))
//  override val decode = Module(new Decode())
//  override val execute = Module(new Execute())
//  override val memory = Module(new Memory())
//  override val writeback = Module(new WriteBack())
//  override val exc = Module(new Exceptions())
//  override val iocomp = Module(new InOut(nr, cnt, aegeanCompatible))
//  override val dcache = Module(new DataCache())

  val voter = Module(new Voter())

  var testBinFile = "/home/patmos/t-crest/patmos/tmp/bootable-bootloader.bin"
  var testBinFile2= "/home/patmos/t-crest/patmos/tmp/bootable-blinking.bin"

  val fetch_1 = Module(new Fetch(binFile))
  val decode_1 = Module(new Decode())
  val execute_1 = Module(new Execute())

  val writeback_1 = Module(new WriteBack())
  val exc_1 = Module(new Exceptions())
  val dcache_1 = Module(new DataCache())

  val fetch_2 = Module(new Fetch(testBinFile2))
  val decode_2 = Module(new Decode())
  val execute_2 = Module(new Execute())

  val writeback_2 = Module(new WriteBack())
  val exc_2 = Module(new Exceptions())
  val dcache_2 = Module(new DataCache())



  //connect icache
  icache.io.feicache.addrOdd := fetch_1.io.feicache.addrOdd
  icache.io.feicache.addrEven := fetch_1.io.feicache.addrEven

  icache.io.feicache.addrOdd := fetch_2.io.feicache.addrOdd
  icache.io.feicache.addrEven := fetch_2.io.feicache.addrEven


  fetch_1.io.icachefe.base := icache.io.icachefe.base
  fetch_1.io.icachefe.instrEven := icache.io.icachefe.instrEven
  fetch_1.io.icachefe.instrOdd := icache.io.icachefe.instrOdd
  fetch_1.io.icachefe.memSel := icache.io.icachefe.memSel
  fetch_1.io.icachefe.relBase := icache.io.icachefe.relBase
  fetch_1.io.icachefe.reloc := icache.io.icachefe.reloc
  fetch_1.io.icachefe.relPc := icache.io.icachefe.relPc


  fetch_2.io.icachefe.base := icache.io.icachefe.base
  fetch_2.io.icachefe.instrEven := icache.io.icachefe.instrEven
  fetch_2.io.icachefe.instrOdd := icache.io.icachefe.instrOdd
  fetch_2.io.icachefe.memSel := icache.io.icachefe.memSel
  fetch_2.io.icachefe.relBase := icache.io.icachefe.relBase
  fetch_2.io.icachefe.reloc := icache.io.icachefe.reloc
  fetch_2.io.icachefe.relPc := icache.io.icachefe.relPc


  icache.io.exicache.callRetAddr := execute_1.io.exicache.callRetAddr
  icache.io.exicache.callRetBase := execute_1.io.exicache.callRetBase
  icache.io.exicache.doCallRet := execute_1.io.exicache.doCallRet

  icache.io.exicache.callRetAddr := execute_2.io.exicache.callRetAddr
  icache.io.exicache.callRetBase := execute_2.io.exicache.callRetBase
  icache.io.exicache.doCallRet := execute_2.io.exicache.doCallRet

  fetch.io.fault_flag := voter.io.fault
  fetch_1.io.fault_flag := voter.io.fault
  fetch_2.io.fault_flag := voter.io.fault

  io.fault := voter.io.fault

  decode_1.io.fedec <> fetch_1.io.fedec
  execute_1.io.decex <> decode_1.io.decex
  decode_2.io.fedec <> fetch_2.io.fedec
  execute_2.io.decex <> decode_2.io.decex

//Voter connect
  memory.io.exmem.rd(0).addr := execute.io.exmem.rd(0).addr

  voter.io.a := execute.io.exmem.rd(0)
  voter.io.b := execute_1.io.exmem.rd(0)
  voter.io.c := execute_2.io.exmem.rd(0)

  memory.io.exmem.rd(0).data := voter.io.votedResult.data
  memory.io.exmem.rd(0).valid := voter.io.votedResult.valid

  memory.io.fault := voter.io.fault

  memory.io.exmem.mem.addr := execute_1.io.exmem.mem.addr
  memory.io.exmem.mem.brcf := execute_1.io.exmem.mem.brcf
  memory.io.exmem.mem.byte := execute_1.io.exmem.mem.byte
  memory.io.exmem.mem.call := execute_1.io.exmem.mem.call
  memory.io.exmem.mem.callRetAddr := execute_1.io.exmem.mem.callRetAddr
  memory.io.exmem.mem.callRetBase := execute_1.io.exmem.mem.callRetBase
  memory.io.exmem.mem.data := execute_1.io.exmem.mem.data
  memory.io.exmem.mem.hword := execute_1.io.exmem.mem.hword
  memory.io.exmem.mem.illOp := execute_1.io.exmem.mem.illOp
  memory.io.exmem.mem.load := execute_1.io.exmem.mem.load
  memory.io.exmem.mem.nonDelayed := execute_1.io.exmem.mem.nonDelayed
  memory.io.exmem.mem.ret := execute_1.io.exmem.mem.ret
  memory.io.exmem.mem.store := execute_1.io.exmem.mem.store
  memory.io.exmem.mem.trap := execute_1.io.exmem.mem.trap
  memory.io.exmem.mem.typ := execute_1.io.exmem.mem.typ
  memory.io.exmem.mem.xcall := execute_1.io.exmem.mem.xcall
  memory.io.exmem.mem.xret := execute_1.io.exmem.mem.xret
  memory.io.exmem.mem.xsrc := execute_1.io.exmem.mem.xsrc
  memory.io.exmem.mem.zext := execute_1.io.exmem.mem.zext

  memory.io.exmem.mem.addr := execute_2.io.exmem.mem.addr
  memory.io.exmem.mem.brcf := execute_2.io.exmem.mem.brcf
  memory.io.exmem.mem.byte := execute_2.io.exmem.mem.byte
  memory.io.exmem.mem.call := execute_2.io.exmem.mem.call
  memory.io.exmem.mem.callRetAddr := execute_2.io.exmem.mem.callRetAddr
  memory.io.exmem.mem.callRetBase := execute_2.io.exmem.mem.callRetBase
  memory.io.exmem.mem.data := execute_2.io.exmem.mem.data
  memory.io.exmem.mem.hword := execute_2.io.exmem.mem.hword
  memory.io.exmem.mem.illOp := execute_2.io.exmem.mem.illOp
  memory.io.exmem.mem.load := execute_2.io.exmem.mem.load
  memory.io.exmem.mem.nonDelayed := execute_2.io.exmem.mem.nonDelayed
  memory.io.exmem.mem.ret := execute_2.io.exmem.mem.ret
  memory.io.exmem.mem.store := execute_2.io.exmem.mem.store
  memory.io.exmem.mem.trap := execute_2.io.exmem.mem.trap
  memory.io.exmem.mem.typ := execute_2.io.exmem.mem.typ
  memory.io.exmem.mem.xcall := execute_2.io.exmem.mem.xcall
  memory.io.exmem.mem.xret := execute_2.io.exmem.mem.xret
  memory.io.exmem.mem.xsrc := execute_2.io.exmem.mem.xsrc
  memory.io.exmem.mem.zext := execute_2.io.exmem.mem.zext


  memory.io.exmem.pc := execute_1.io.exmem.pc
  memory.io.exmem.pc := execute_2.io.exmem.pc

  fetch_1.io.pc_reset := execute_1.io.exmem.pc
  fetch_2.io.pc_reset := execute_2.io.exmem.pc


  memory.io.exmem.base := execute_1.io.exmem.base
  memory.io.exmem.base := execute_2.io.exmem.base

  memory.io.exmem.relPc := execute_1.io.exmem.relPc
  memory.io.exmem.relPc := execute_2.io.exmem.relPc


  writeback_1.io.memwb.pc := memory.io.memwb.pc
  writeback_1.io.memwb.rd := memory.io.memwb.rd
  writeback_2.io.memwb.pc := memory.io.memwb.pc
  writeback_2.io.memwb.rd := memory.io.memwb.rd
  // RF write connection
  decode_1.io.rfWrite <> writeback_1.io.rfWrite
  decode_2.io.rfWrite <> writeback_2.io.rfWrite
  // This is forwarding of registered result
  // Take care that it is the plain register
  execute_1.io.exResult := memory.io.exResult
  execute_2.io.exResult := memory.io.exResult

  execute_1.io.memResult <> writeback_1.io.memResult
  execute_2.io.memResult <> writeback_2.io.memResult



  // Connect stack cache
  execute_1.io.exsc <> dcache_1.io.scIO.exsc
  dcache_1.io.scIO.scex <> execute_1.io.scex

  execute_2.io.exsc <> dcache_2.io.scIO.exsc
  dcache_2.io.scIO.scex <> execute_2.io.scex

  memory.io.scacheIllMem := dcache_1.io.scIO.illMem
  memory.io.scacheIllMem := dcache_2.io.scIO.illMem

  // We branch in EX
  fetch_1.io.exfe <> execute_1.io.exfe
  fetch_2.io.exfe <> execute_2.io.exfe
  // We call in MEM
  fetch_1.io.memfe.addr := memory.io.memfe.addr
  fetch_1.io.memfe.callRetBase := memory.io.memfe.callRetBase
  fetch_1.io.memfe.callRetPc := memory.io.memfe.callRetPc
  fetch_1.io.memfe.data := memory.io.memfe.data
  fetch_1.io.memfe.doCallRet := memory.io.memfe.doCallRet
  fetch_1.io.memfe.store := memory.io.memfe.store

  fetch_2.io.memfe.addr := memory.io.memfe.addr
  fetch_2.io.memfe.callRetBase := memory.io.memfe.callRetBase
  fetch_2.io.memfe.callRetPc := memory.io.memfe.callRetPc
  fetch_2.io.memfe.data := memory.io.memfe.data
  fetch_2.io.memfe.doCallRet := memory.io.memfe.doCallRet
  fetch_2.io.memfe.store := memory.io.memfe.store
  // We store the return base in EX (in cycle corresponding to MEM)
  fetch_1.io.feex <> execute_1.io.feex
  fetch_2.io.feex <> execute_2.io.feex



  // Connect exception unit
  exc_1.io.ocp.M := iocomp.io.excInOut.M
  iocomp.io.excInOut.S := exc_1.io.ocp.S

  exc_1.io.intrs := iocomp.io.intrs
  exc_1.io.excdec <> decode_1.io.exc

  exc_1.io.memexc.call := memory.io.exc.call
  exc_1.io.memexc.exc := memory.io.exc.exc
  exc_1.io.memexc.excAddr := memory.io.exc.excAddr
  exc_1.io.memexc.excBase := memory.io.exc.excBase
  exc_1.io.memexc.ret := memory.io.exc.ret
  exc_1.io.memexc.src := memory.io.exc.src

  exc_2.io.ocp.M := iocomp.io.excInOut.M
  iocomp.io.excInOut.S := exc_2.io.ocp.S

  exc_2.io.intrs := iocomp.io.intrs
  exc_2.io.excdec <> decode_2.io.exc

  exc_2.io.memexc.call := memory.io.exc.call
  exc_2.io.memexc.exc := memory.io.exc.exc
  exc_2.io.memexc.excAddr := memory.io.exc.excAddr
  exc_2.io.memexc.excBase := memory.io.exc.excBase
  exc_2.io.memexc.ret := memory.io.exc.ret
  exc_2.io.memexc.src := memory.io.exc.src

  // Connect data cache
  dcache.io.master.M := memory.io.globalInOut.M
  memory.io.globalInOut.S := dcache.io.master.S



  // Enable signals for memory stage, method cache and stack cache
  memory.io.ena_in := icache.io.ena_out && !dcache_1.io.scIO.stall
  icache.io.ena_in := memory.io.ena_out && !dcache_1.io.scIO.stall
  dcache_1.io.scIO.ena_in := memory.io.ena_out && icache.io.ena_out

  memory.io.ena_in := icache.io.ena_out && !dcache_2.io.scIO.stall
  icache.io.ena_in := memory.io.ena_out && !dcache_2.io.scIO.stall
  dcache_2.io.scIO.ena_in := memory.io.ena_out && icache.io.ena_out

  // Enable signal

  fetch_1.io.ena := enable
  decode_1.io.ena := enable
  execute_1.io.ena := enable
  writeback_1.io.ena := enable
  exc_1.io.ena := enable

  fetch_2.io.ena := enable
  decode_2.io.ena := enable
  execute_2.io.ena := enable
  writeback_2.io.ena := enable
  exc_2.io.ena := enable


  // Flush signal

  decode_1.io.flush := flush || brflush
  execute_1.io.flush := flush

  decode_2.io.flush := flush || brflush
  execute_2.io.flush := flush

  // Software resets
  icache.io.invalidate := exc_1.io.invalICache
  icache.io.invalidate := exc_2.io.invalICache
  dcache_1.io.invalDCache := exc_1.io.invalDCache
  dcache_2.io.invalDCache := exc_2.io.invalDCache

//  // Make privileged mode visible internally and externally
//  iocomp.io.superMode := exc.io.superMode
//  mmu.io.superMode := exc.io.superMode
//  io.superMode := exc.io.superMode
//
//  // Internal "I/O" data
//  iocomp.io.internalIO.perf.ic := icache.io.perf
  iocomp.io.internalIO.perf.dc := dcache_1.io.dcPerf
  iocomp.io.internalIO.perf.sc := dcache_1.io.scPerf
  iocomp.io.internalIO.perf.wc := dcache_1.io.wcPerf
  iocomp.io.internalIO.perf.dc := dcache_2.io.dcPerf
  iocomp.io.internalIO.perf.sc := dcache_2.io.scPerf
  iocomp.io.internalIO.perf.wc := dcache_2.io.wcPerf
//  iocomp.io.internalIO.perf.mem.read := (io.memPort.M.Cmd === OcpCmd.RD &&
//    io.memPort.S.CmdAccept === Bits(1))
//  iocomp.io.internalIO.perf.mem.write := (io.memPort.M.Cmd === OcpCmd.WR &&
//    io.memPort.S.CmdAccept === Bits(1))
//
//  // The inputs and outputs
//  io.comConf <> iocomp.io.comConf
//  io.comSpm <> iocomp.io.comSpm
//  io.memPort <> mmu.io.phys
//  Config.connectAllIOPins(io, iocomp.io)

  // Keep signal alive for debugging
  debug(enableReg)



}