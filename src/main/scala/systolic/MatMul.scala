package systolic

import chisel3._
import chisel3.util.log2Ceil
import chisel3.util._ 

import scala.collection.mutable.ArrayBuffer

class PEModule() extends Module {
  val io = IO(new Bundle {
    val inA = Input(SInt(32.W)) // Input a with width from params
    val inB = Input(SInt(32.W)) // Input b with width from params
    val out = Output(SInt(32.W)) // Output result with width from params
  })
  val product = Reg(SInt(32.W))
  // Perform multiplication and store the result
  product := io.inA * io.inB
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

    val rowGrid = Reg(Vec(params.rows, Vec(params.cols, SInt(32.W))))
    val colGrid = Reg(Vec(params.rows, Vec(params.cols, SInt(32.W))))
    val res = RegInit(VecInit(Seq.fill(params.rows)(VecInit(Seq.fill(params.cols)(0.S(32.W))))))
    // Dynamic PE grid initialization based on params
    val peGrid = Seq.tabulate(params.rows, params.cols) { (i, j) =>
        Module(new PEModule())
    }

    val state = RegInit(Calc.idle)
    val ovalid = RegInit(false.B)
    val iready = RegInit(true.B)

    val counter = RegInit(0.U(32.W))
    def loadDataShiftAndProcess() = {
        // Iterate over the PE grid
        for (i <- 0 until params.rows) {
            for (j <- 0 until params.cols) {
                // Load data into the first row/column, considering the systolic operation cycle
                // Example correction concept, not direct replacement
                when(counter === 0.U) {
                    val rowIndex = Mux(counter >= i.U, (params.cols.U - 1.U - counter) % params.cols.U, 0.U) // Ensuring result is UInt
                    val colIndex = Mux(counter >= j.U, (params.rows.U - 1.U - counter) % params.rows.U, 0.U) // Ensuring result is UInt

                    rowGrid(i)(0) := Mux(counter >= i.U, io.in.bits.a(i)(rowIndex), 0.S)
                    colGrid(0)(j) := Mux(counter >= j.U, io.in.bits.b(colIndex)(j), 0.S)
                }


                // Shift data in the grid
                when(counter > 0.U && counter < (params.rows + params.cols - 2).U) {
                    rowGrid(i)(j) := Mux(j.U === 0.U, 0.S, rowGrid(i)(j - 1))
                    colGrid(i)(j) := Mux(i.U === 0.U, 0.S, colGrid(i - 1)(j))
                }

                // Connect PE inputs and accumulate results, considering the propagation delay
                val pe = peGrid(i)(j)
                pe.io.inA := rowGrid(i)(j)
                pe.io.inB := colGrid(i)(j)

                when(counter >= (i.U + j.U)) {
                    // Ensure we accumulate results after the data has had time to propagate through the PEs
                    res(i)(j) := res(i)(j) + pe.io.out
                }
            }
        }

        // Update the counter to progress through the systolic operation cycles
        when(counter < (params.rows + params.cols - 2).U) {
            counter := counter + 1.U
        }.otherwise {
            counter := 0.U // Reset counter or handle completion
        }
    }


    switch(state) {
        is(Calc.idle) {
            when(io.in.fire) {
                state := Calc.multiplying
                counter := 0.U
            }
        }
        is(Calc.multiplying) {
            loadDataShiftAndProcess() // Handles loading, shifting, and processing

            // Transition to finished state or continue processing based on your design
            when(counter === (params.rows + params.cols - 2).U) {
                state := Calc.finished
            }
        }

        is(Calc.finished) {
            io.out.valid := true.B
        }
    }


    io.in.ready := iready
    io.out.valid := ovalid
    io.out.bits := res





    def perform() = {
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
    for(r <- 0 until params.rows){
      val tmpc = (params.cols-1+r).U-counter
      rowGrid(r)(0) := Mux(counter >= r.U && counter < (params.cols+r).U,io.in.bits.a(r.U)(tmpc), 0.S)
      // when(counter >= r.U && counter < (params.cols+r).U){
      //   printf(p"rowGrid(${r.U}, ${0.U}) = a(${r})(${tmpc}) => ${io.in.bits.a(r)(tmpc)}\n")
      // }
    }
    for(c <- 0 until params.cols){
      val tmpr = (params.rows-1+c).U-counter
      colGrid(0)(c) := Mux(counter >= c.U && counter < (params.rows+c).U, io.in.bits.b(tmpr)(c.U), 0.S)
      // when(counter >= c.U && counter < (params.rows+c).U){
      //   printf(p"colGrid(${0}, ${c.U}) = b(${tmpr})(${c}) => ${io.in.bits.b(tmpr)(c)}\n")
      // }
    }
    }
    for (i <- 0 until params.rows) {
      for (j <- 0 until params.cols) {
        peGrid(i)(j).io.inA := rowGrid(i)(j)
        peGrid(i)(j).io.inB := colGrid(i)(j)
        res(i)(j) := res(i)(j) + peGrid(i)(j).io.out //issue is taht peGrid out is initially undefined so I cant set it
        printf(p"${rowGrid(i)(j)}, ${colGrid(i)(j)}, ${res(i)(j) + peGrid(i)(j).io.out}\n")
      }
    }
  
}