package patmos



import Chisel._


class Voter extends Module{
  val io = IO(new VoterIO())
  val res = new Result()
  val err_detect = Bool()

  //Voter logic result = A and B or A and C or B and C
  //Vote on result data
  res.data = (io.a.data & io.b.data) |
             (io.a.data & io.c.data) |
             (io.b.data & io.c.data)

  //Synchronize on data valid signal
  res.valid = io.b.valid & io.c.valid & io.a.valid

  io.votedResult := res

  //non-recoverable error detection
  err_detect := io.a.data =/= io.b.data & io.a.data =/= io.c.data & io.b.data =/= io.c.data

  when (err_detect === true.B )
  {
    io.fault := true.B
  }.otherwise
  {
    io.fault := false.B
  }



}
