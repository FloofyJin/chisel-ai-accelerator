package systolic

import chisel3._
import chisel3.internal.firrtl.Width
import chisel3.util._


case class MatMulParams(m: Int, k: Int, n: Int, parallelism: Int = 1, cyclesPerTransfer: Int = 1) {
  // A (m x k) X B (k x n) = C (m x n)
  val aRows: Int = m
  val aCols: Int = k
  val bRows: Int = k
  val bCols: Int = n
  val cRows: Int = m
  val cCols: Int = n
  // Implementation details
  val w: Width = 32.W
  // Only relevant for MatMulMC (multi-cycle transfer)
  require((aRows * aCols) % cyclesPerTransfer == 0)
  val aElementsPerTransfer: Int = (aRows * aCols) / cyclesPerTransfer
  if (cyclesPerTransfer != 1) {
    require(aElementsPerTransfer <= aCols)
    require(aCols % aElementsPerTransfer == 0)
  }
  require((bRows * bCols) % cyclesPerTransfer == 0)
  val bElementsPerTransfer: Int = (bRows * bCols) / cyclesPerTransfer
  if (cyclesPerTransfer != 1) {
    require(bElementsPerTransfer <= bCols)
    require(bCols % bElementsPerTransfer == 0)
  }
  if ((cRows * cCols) > cyclesPerTransfer)
    require((cRows * cCols) % cyclesPerTransfer == 0)
  val cElementsPerTransfer: Int = ((cRows * cCols) / cyclesPerTransfer).max(1)
  if (cyclesPerTransfer != 1) {
    require(cElementsPerTransfer <= cCols)
    require(cCols % cElementsPerTransfer == 0)
  }
  require(cCols >= parallelism)
  require(cCols % parallelism == 0)
}

