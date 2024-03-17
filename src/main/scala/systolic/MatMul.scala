package systolic

import chisel3._
import chisel3.util.log2Ceil
import chisel3.util._ 

import scala.collection.mutable.ArrayBuffer

object OpType extends ChiselEnum {
  val Add, Mul, And, Or, Xor = Value

}

class PEModule(opType: OpType.Type) extends Module {
  val io = IO(new Bundle {
    val inA = Input(SInt(32.W)) // Input a with width from params
    val inB = Input(SInt(32.W)) // Input b with width from params
    val out = Output(SInt(32.W)) // Output result with width from params
  })
  val product = Reg(SInt(32.W))
  def determine_calculate(op: OpType.Type): SInt = op match {
    case OpType.Add => io.inA + io.inB
    case OpType.Mul => io.inA * io.inB
    case _ => 0.S
  }
  product := determine_calculate(opType)
  // Output the product
  io.out := product
}

object Calc extends ChiselEnum {
  val idle, multiplying, finished= Value
}

class MatMulSystolic(params: SystolicArrayParams) extends Module {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Bundle {
      val a = Vec(params.rows, Vec(params.cols, SInt(32.W)))
      val b = Vec(params.rows, Vec(params.cols, SInt(32.W)))
    }))
    val out = Valid(Vec(params.rows, Vec(params.cols, SInt(32.W))))
  })

  // grid of systolic arrays in operation
  val rowGrid = Reg(Vec(params.rows, Vec(params.cols, SInt(32.W))))
  val colGrid = Reg(Vec(params.rows, Vec(params.cols, SInt(32.W))))
  val res = RegInit(VecInit(Seq.fill(params.rows)(VecInit(Seq.fill(params.cols)(0.S(32.W))))))

  // processing elements
  // currently you can change the operation type
  // eg. OpType.Add, OpType.Mul, OpType.And, OpType.Or, OpType.Xor
  val peGrid = Array.fill(params.rows, params.cols)(Module(new PEModule(OpType.Mul)))

  val state = RegInit(Calc.idle)
  val ovalid = RegInit(false.B)
  val iready = RegInit(true.B)

  // counts cycle
  val counter = RegInit(0.U(32.W))

  switch(state){
    is(Calc.idle){
      iready := true.B
      ovalid := false.B
      when(io.in.fire){
        state := Calc.multiplying
      }
    }
    is(Calc.multiplying){
      iready := false.B
      ovalid := false.B
      perform()
      // Update the counter to progress through the systolic operation cycles
      when(counter < (params.rows + params.cols + 2).U) {
          counter := counter + 1.U
      }.otherwise {
          counter := 0.U // Reset counter or handle completion
      }
      // Transition to finished state or continue processing based on your design
      when(counter === (params.rows*2).U){
        state := Calc.finished
      }
    }
    is(Calc.finished){
      ovalid := true.B
      iready := false.B
    }
  }

  io.in.ready := iready
  io.out.valid := ovalid
  io.out.bits := res

  def perform() = {
    // Shift the previous row/column following the systolic operation cycle
    for (row <- params.rows -1 to 0 by -1) {
      for (col <- params.cols -1 to 0 by -1) { // Avoid out-of-bounds access in the last column
        if(col < params.cols - 1){
          rowGrid(row)(col+1) := rowGrid(row)(col)
        }
        if(row < params.rows - 1){
          colGrid(row+1)(col) := colGrid(row)(col)
        }
      }
    }
    // Load data into the first column, considering the systolic operation cycle
    for(r <- 0 until params.rows){
      val tmpc = (params.cols-1+r).U-counter
      rowGrid(r)(0) := Mux(counter >= r.U && counter < (params.cols+r).U,io.in.bits.a(r.U)(tmpc), 0.S)
    }
    // Load data into the first row, considering the systolic operation cycle
    for(c <- 0 until params.cols){
      val tmpr = (params.rows-1+c).U-counter
      colGrid(0)(c) := Mux(counter >= c.U && counter < (params.rows+c).U, io.in.bits.b(tmpr)(c.U), 0.S)
    }
  }
  // processing element should calculate every element in their respective grid
  // Connect PE inputs and accumulate results, considering the propagation delay
  for (i <- 0 until params.rows) {
    for (j <- 0 until params.cols) {
      peGrid(i)(j).io.inA := rowGrid(i)(j)
      peGrid(i)(j).io.inB := colGrid(i)(j)
      res(i)(j) := res(i)(j) + peGrid(i)(j).io.out
      // printf(p"${rowGrid(i)(j)}, ${colGrid(i)(j)}, ${res(i)(j) + peGrid(i)(j).io.out}\n")
    }
  }
  
}