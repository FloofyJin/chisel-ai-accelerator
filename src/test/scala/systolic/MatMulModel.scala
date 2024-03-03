package systolic

object MatMulModel {
  type Matrix = Seq[Seq[Int]]

  // def apply(p: MatMulParams, a: Matrix, b: Matrix): Matrix = {
  //   assert(a.size == p.aRows)
  //   assert(a.head.size == p.aCols)
  //   assert(b.size == p.bRows)
  //   assert(b.head.size == p.bCols)

  //   val transposedB = b.transpose
  //   val result = for {
  //       rowA <- a
  //     } yield {
  //         for {
  //             colB <- transposedB
  //           } yield {
  //               rowA.zip(colB).map { case (elemA, elemB) => elemA * elemB }.sum
  //             }
  //           }
            
  // // Perform matrix multiplication using systolic array


  // println(s"${result}")
  // result
  // }
}
