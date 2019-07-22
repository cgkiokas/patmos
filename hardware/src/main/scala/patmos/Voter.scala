package patmos



import Chisel._


class Voter extends Module{
  val io = IO(new VoterIO())


  //Voter logic result = A and B or A and C or B and C
  val res = new Result()

  //Vote on result data
  res.data = (io.a.data & io.b.data) |
             (io.a.data & io.c.data) |
             (io.b.data & io.c.data)

  //Synchronize on data valid signal
  res.valid = io.a.valid & io.b.valid & io.c.valid

  io.votedResult := res

  //
  //Error detection ~A and B and C or A and ~B and C or A and B and ~C
//  val err_detect = !io.a.data & io.b.data & io.c.data |
//                   io.a.data & !io.b.data & io.c.data |
//                   io.a.data & io.b.data & !io.c.data

  val err_detect = io.a.data =/= io.b.data =/= io.c.data

  when (err_detect === 1.U)
  {
    io.fault := true.B
  }.otherwise
  {
    io.fault := false.B
  }



}
