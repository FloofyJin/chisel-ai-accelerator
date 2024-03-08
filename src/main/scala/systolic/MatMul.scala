package systolic

import chisel3._
import chisel3.util.log2Ceil
import chisel3.util._ 

import scala.collection.mutable.ArrayBuffer

// object Calc extends ChiselEnum {
//   val idle, multiplying, finished= Value
// }

class MatMulSystolic(params: SystolicArrayParams) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Bundle {
      val a = Vec(params.rows, Vec(params.cols, SInt(32.W)))
      val b = Vec(params.rows, Vec(params.cols, SInt(32.W)))
    }))
    val out = Valid(Vec(params.rows, Vec(params.cols, SInt(32.W))))
  })

  val rowGrid = Reg(Vec(params.rows, Vec(params.cols, SInt(32.W))))
  val colGrid = Reg(Vec(params.rows, Vec(params.cols, SInt(32.W))))
  val res = RegInit(VecInit(Seq.fill(params.rows)(VecInit(Seq.fill(params.cols)(0.S(32.W))))))

  val peGrid: ArrayBuffer[ArrayBuffer[ProcessingElementModel]] =
      ArrayBuffer.fill(params.rows, params.cols)(new ProcessingElementModel(params.peOperation))

  // val state = RegInit(Calc.idle)
  val ovalid = RegInit(false.B)
  val iready = RegInit(true.B)

  // val Counter = Counter(params.rows*2+1)

  // switch(state){
  //   is(Calc.idle){
  //     iready := true.B
  //     ovalid := false.B
  //     when(io.in.fire){
  //       state := Calc.multiplying
  //     }
  //   }
  //   is(Calc.multiplying){
  //     iready := false.B
  //     ovalid := false.B
  //     res(0.U)(0.U) := 1.S
  //     printf("Multiplying\n")
  //   }
  //   is(Calc.finished){
  //     printf("Finished\n")
  //     ovalid := true.B
  //     iready := false.B
  //   }
  // }

  // io.in.ready := iready
  // io.out.valid := ovalid
  // io.out.bits := res

  // def perform() = {
    // for(i <- params.rows -1 to 0 by -1){
    //   for(j <- params.cols -1 to 0 by -1){
    //     when(j < params.cols -1){
    //       rowGrid(i)(j+1) := rowGrid(i)(j)
    //     }
    //     when(i < params.rows-1){
    //       colGrid(i+1)(j) := ColGrid(i)(j)
    //     }
    //   }
    // }
  // }
  
}