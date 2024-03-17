package systolic

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import systolic.SystolicArrayMatrixMult.Matrix

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import scala.util.Random


object MatMulTestData {
  def genIdentity(n: Int): Matrix = {
    var a = ArrayBuffer.fill(n)(ArrayBuffer.fill(n)(0))
    for (i <- 0 until 4) {
      a(i)(i) = 1
    }
    return a
  }

  def genRandomMatrix(n: Int, minValue: Int = -10, maxValue: Int = 10): Matrix = {
    ArrayBuffer.fill(n, n)(Random.nextInt(maxValue - minValue + 1) + minValue)
  }

  def genZeroMatrix(n: Int, minValue: Int = -10, maxValue: Int = 10): Matrix = {
    ArrayBuffer.fill(n, n)(0)
  }


  def genOnesRow(n: Int): Matrix = ArrayBuffer(ArrayBuffer.fill(n)(1))

  def genOnesCol(n: Int): Matrix = ArrayBuffer.fill(n)(ArrayBuffer(1))

  val testa = ArrayBuffer(ArrayBuffer(1, 4, 7),
                          ArrayBuffer(2, 5, 8),
                          ArrayBuffer(3, 6, 9))
  val testb = ArrayBuffer(ArrayBuffer(1, 2, 3),
                          ArrayBuffer(4, 5, 6),
                          ArrayBuffer(7, 8, 9))
  val testRes = ArrayBuffer(ArrayBuffer(66, 78, 90),
                          ArrayBuffer(78, 93, 108),
                          ArrayBuffer(90, 108, 126))
}

class SystolicMulModelTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "simple matrix"
  it should "counting" in {
    assert(SystolicArrayMatrixMult(3, MatMulTestData.testa, MatMulTestData.testb)==MatMulTestData.testRes)
    true
  }

  // Test identity matrix multiplication
  it should "identity" in {
    val identity4x4 = MatMulTestData.genIdentity(4)
    assert(SystolicArrayMatrixMult(4, identity4x4, identity4x4)==identity4x4)
    true
  }


  // Test multiplication with a zero matrix
  it should "result in a zero matrix when multiplied by a zero matrix" in {
    for (n <- 2 to 6) {
      val zeroMatrix = MatMulTestData.genZeroMatrix(n) // Assuming this generates an nxn zero matrix
      val randomMatrix = MatMulTestData.genRandomMatrix(n)
      assert(SystolicArrayMatrixMult(n, zeroMatrix, randomMatrix) == zeroMatrix)
      assert(SystolicArrayMatrixMult(n, randomMatrix, zeroMatrix) == zeroMatrix)
    }
  }

  // Test multiplication with negative numbers
  "A matrix with negative numbers" should "correctly multiply" in {
    val testMatrixA = ArrayBuffer(ArrayBuffer(-1, -2), ArrayBuffer(-3, -4))
    val testMatrixB = ArrayBuffer(ArrayBuffer(5, 6), ArrayBuffer(7, 8))
    val expected = ArrayBuffer(ArrayBuffer(-19, -22), ArrayBuffer(-43, -50))
    assert(SystolicArrayMatrixMult(2, testMatrixA, testMatrixB) == expected)
  }
}

class SystolicMulTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Test cases of chisel"
  it should "new counting" in {
    val params = new SystolicArrayParams(3, 3, (a: Int, b: Int) => a * b)

    test(new MatMulSystolic(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.ready.expect(true.B)
      dut.io.out.valid.expect(false.B)
      dut.clock.step()
      for(i <- 0 until 3){
        for(j <- 0 until 3){
          dut.io.in.bits.a(i)(j).poke(MatMulTestData.testa(i)(j).S)
          dut.io.in.bits.b(i)(j).poke(MatMulTestData.testb(i)(j).S)
        }
      }
      dut.clock.step()
      dut.io.in.ready.expect(false.B)
      dut.io.out.valid.expect(false.B)
      dut.clock.step(3*2+2)
      for(i <- 0 until 3){
        for(j <- 0 until 3){
          dut.io.out.bits(i)(j).expect(MatMulTestData.testRes(i)(j).S)
        }
      }
    }
    true
  }
}