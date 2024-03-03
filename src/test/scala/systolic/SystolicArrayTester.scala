package systolic

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

import systolic.SystolicArrayMatrixMult.Matrix

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable


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

  val testa = ArrayBuffer(ArrayBuffer(1, 2, 3),
                          ArrayBuffer(4, 5, 6),
                          ArrayBuffer(7, 8, 9))
  val testb = ArrayBuffer(ArrayBuffer(1, 4, 7),
                          ArrayBuffer(2, 5, 8),
                          ArrayBuffer(3, 6, 9))
  val testRes = ArrayBuffer(ArrayBuffer(66, 78, 90),
                          ArrayBuffer(78, 93, 108),
                          ArrayBuffer(90, 108, 126))
}

class SystolicMulModelTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Test cases of scala"
  it should "counting" in {
    assert(SystolicArrayMatrixMult(3, MatMulTestData.testb, MatMulTestData.testa)==MatMulTestData.testRes)
    true
  }
  it should "identity" in {
    val identity4x4 = MatMulTestData.genIdentity(4)
    assert(SystolicArrayMatrixMult(4, identity4x4, identity4x4)==identity4x4)
    true
  }
}

class SystolicMulTester extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "Test cases of chisel"
  it should "new counting" in {
    val params = new SystolicArrayParams(3, 3, (a: Int, b: Int) => a * b)

    test(new MatMulSystolic(params)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.io.in.valid.poke(true.B)
      dut.io.in.ready.poke(true.B)
      for(i <- 0 until 3){
        for(j <- 0 until 3){
          dut.io.in.bits.aBlock(i).poke(MatMulTestData.testa(i)(j).S)
          dut.io.in.bits.bBlock(j).poke(MatMulTestData.testb(i)(j).S)
        }
      }
      dut.clock.step()
      dut.io.in.ready.expect(false.B)
      dut.io.outBlock.valid.expect(false.B)
    }
    true
  }
}