package patmos



import Chisel._


class Voter extends Module{
  val io = IO(new VoterIO())


  //Voter logic result = a and b or a and c or b and c
  val res = new Result()

  //Vote on result data
  res.data = (io.a.data & io.b.data) |
    (io.a.data & io.c.data) |
    (io.b.data & io.c.data)

  //Synchronize on data valid signal
  res.valid = io.a.valid & io.b.valid & io.c.valid

  io.votedResult := res


  when (io.votedResult.data === 0.U & res.valid)
  {
    io.fault := true.B
  }.otherwise
  {
    io.fault := false.B
  }



}
