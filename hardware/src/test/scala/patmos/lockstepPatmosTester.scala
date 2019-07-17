package patmos

import Chisel.{Module, Tester, UInt, chiselMainTest}
import sys.process._
import scala.language.postfixOps


class lockstepPatmosTester(pat: Patmos) extends Tester(pat) {

  println("Patmos start")

  for (i <- 0 until 100) {
    step(1) // false as third argument disables printout
    // The PC printout is a little off on a branch
    val pc = peek(pat.cores(0).memory.io.memwb.pc) - 2
    print(pc + " - ")
    //for (j <- 0 until 32)
      //print(peek(pat.cores(0).decode.rf.rf(UInt(j))) + " ")
    println()
  }
}

object lockstepPatmosTester extends App {

  override def main(args: Array[String]): Unit = {
    val pathToVCD = "generated/lockstepPatmosTester"
    val nameOfVCD = "Patmos.vcd"

    val chiselArgs = Array("--genHarness", "--test", "--backend", "c",
      "--compile", "--vcd", "--targetDir", "generated/" + this.getClass.getSimpleName.dropRight(1))
    val configFile = args(0)
    val binFile = args(1)
    val datFile = args(2)

    chiselMainTest(chiselArgs, () => Module(new Patmos(configFile, binFile, datFile))) { pat => new lockstepPatmosTester(pat) }

    "gtkwave " + pathToVCD + "/" + nameOfVCD + " " + pathToVCD + "/" + "view.sav" !
  }
}