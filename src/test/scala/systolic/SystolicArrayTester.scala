package systolic

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import systolic.MatMulModel.Matrix


object MatMulTestData {
  def genIdentity(n: Int): Matrix = Seq.tabulate(n,n) { (i,j) => if (i==j) 1 else 0 }

  def genOnesRow(n: Int): Matrix = Seq(Seq.fill(n)(1))

  def genOnesCol(n: Int): Matrix = Seq.fill(n)(Seq(1))

  val in2x4  = Seq(Seq(1,2,3,4),
                   Seq(5,6,7,8))
  val in4x2  = Seq(Seq(1,2),
                   Seq(3,4),
                   Seq(5,6),
                   Seq(7,8))
  val out2x2 = Seq(Seq(50, 60),
                   Seq(114,140))
  val out4x4 = Seq(Seq(11, 14, 17, 20),
                   Seq(23, 30, 37, 44),
                   Seq(35, 46, 57, 68),
                   Seq(47, 62, 77, 92))
}

class SystolicMulModelTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Test cases of scala "
  it should "multiply identity (4x4)" in {
    val n = 4
    val identity4x4 = MatMulTestData.genIdentity(n)
    val p = MatMulParams(n,n,n)
    assert(MatMulModel(p, identity4x4, identity4x4) == identity4x4)
  }

  it should "multiply identity x in4x2" in {
    assert(MatMulModel(MatMulParams(4,4,2), MatMulTestData.genIdentity(4), MatMulTestData.in4x2) == MatMulTestData.in4x2)
  }

  it should "multiply identity x in2x4" in {
    assert(MatMulModel(MatMulParams(2,2,4), MatMulTestData.genIdentity(2), MatMulTestData.in2x4) == MatMulTestData.in2x4)
  }

  it should "multiply in2x4 x in4x2" in {
    assert(MatMulModel(MatMulParams(2,4,2), MatMulTestData.in2x4, MatMulTestData.in4x2) == MatMulTestData.out2x2)
  }

  it should "multiply in4x2 x in2x4" in {
    assert(MatMulModel(MatMulParams(4,2,4), MatMulTestData.in4x2, MatMulTestData.in2x4) == MatMulTestData.out4x4)
  }
}