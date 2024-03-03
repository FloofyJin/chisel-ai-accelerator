package systolic

import chisel3._
import chisel3.util.log2Ceil
import chisel3.util._ 

class MatMulSystolic(params: SystolicArrayParams) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Bundle {
      val aBlock = Vec(3, SInt(32.W))
      val bBlock = Vec(3, SInt(32.W))
    }))
    val outBlock = Valid(Vec(3, SInt(32.W)))
  })

}