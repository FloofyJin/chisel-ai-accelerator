package systolic

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import systolic.SystolicArrayMatrixMult.Matrix

import scala.collection.mutable.ArrayBuffer


object MatMulTestData {
  def genIdentity(n: Int): Matrix = {
    var a = ArrayBuffer.fill(n)(ArrayBuffer.fill(n)(0))
    for (i <- 0 until 4) {
      a(i)(i) = 1
    }
    return a
  }


  def genOnesRow(n: Int): Matrix = ArrayBuffer(ArrayBuffer.fill(n)(1))

  def genOnesCol(n: Int): Matrix = ArrayBuffer.fill(n)(ArrayBuffer(1))

  val out2x2 = Seq(Seq(50, 60),
                   Seq(114,140))
  val out4x4 = Seq(Seq(11, 14, 17, 20),
                   Seq(23, 30, 37, 44),
                   Seq(35, 46, 57, 68),
                   Seq(47, 62, 77, 92))
}

class SystolicMulModelTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Test cases of scala "
  it should "array" in {
    val identity4x4 = MatMulTestData.genIdentity(4)
    SystolicArrayMatrixMult(4, identity4x4, identity4x4)
    true
  }
}