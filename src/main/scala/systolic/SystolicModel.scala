/*
 * File: SystolicModel.scala
 * Author: Rian Borah, Jinsung Park
 *
 * Date: 1 March, 2024
 *
 * Main file for the Systolic Array Scala Model Implementation for
 * CSE-228A Final Project - Winter 2024
 */

/*******************************************************************************
 * MODULE #INCLUDE                                                             *
 ******************************************************************************/
package systolic

import scala.collection.mutable.ArrayBuffer

/*******************************************************************************
 * PROCESSING ELEMENT MODULE DEFINITION                                        *
 ******************************************************************************/

/*
* @class ProcessingElement
* @param operation: operation to be performed by the PE on the inputs
* @return:
* @brief: Processing Element Module to perform computations in the systolic array
*
* @author: Rian Borah, 2024.03.02
* */

// Could change from operation on Ints to UInts for chisel
class ProcessingElementModel(operation: (Int, Int) => Int){
    private var value: Int = 0
    def execute(inputA: Int, inputB: Int): Int = {
        value = operation(inputA, inputB)
        value
    }
    def getValue: Int = {
        value
    }
}

/*******************************************************************************
 * SYSTOLIC ARRAY MODULE DEFINITION                                            *
 ******************************************************************************/

/*
* @class SystolicArrayParams
* @param rows: No. of rows in the systolic array
* @param cols: No. of columns in the systolic array
* @param peOperation: operation to be performed by the PE on the inputs
* @return:
* @brief: Parameters for the Systolic Array Module
*
* @author: Rian Borah, 2024.03.02
* */
class SystolicArrayParams(
    val rows: Int,
    val cols: Int,
    val peOperation: (Int, Int) => Int
)

/*
* @class SystolicArrayModel
* @param p: SystolicArrayParams
* @return:
* @brief: Scala Model for the Systolic Array
*
* @author: Rian Borah, 2024.03.02
* */
class SystolicArrayModel(p: SystolicArrayParams) {
    // Initialize a 2D array of Processing Element Models
    private val peGrid: ArrayBuffer[ArrayBuffer[ProcessingElementModel]] =
      ArrayBuffer.fill(p.rows, p.cols)(new ProcessingElementModel(p.peOperation))

    /*
    * @function performOperation
    * @param inputA: Row from input Matrix A
    * @param inputB: Column from input Matrix B
    * @return: resultGrid
    * @brief: Perform the operation on the inputs using the systolic array
    *
    * @author: Rian Borah, 2024.03.02
    * */
    def performOperation(inputA: ArrayBuffer[Int], inputB: ArrayBuffer[Int]): ArrayBuffer[ArrayBuffer[Int]] = {
        require(inputA.length == p.rows && inputB.length == p.cols, "Input dimensions must match the systolic array dimensions.")

        // Temporary storage for the result
        val resultGrid: ArrayBuffer[ArrayBuffer[Int]] = ArrayBuffer.fill(p.rows, p.cols)(0)

        // Execute the operation on each PE based on the inputs
        for (row <- 0 until p.rows; col <- 0 until p.cols) {
          val result = peGrid(row)(col).execute(inputA(row), inputB(col))
          resultGrid(row)(col) = result
        }

        resultGrid
    }

    /*
    * @function displayGrid
    * @param: None
    * @return: None
    * @brief: Display the current state of the systolic array
    *
    * @author: Rian Borah, 2024.03.02
    * */
    def displayGrid(): Unit = {
        println("Systolic Array State:")
        peGrid.foreach(row => println(row.map(_.getValue).mkString(" ")))
    }
}

object SystolicArrayModel {
    def apply(params: SystolicArrayParams): SystolicArrayModel = new SystolicArrayModel(params)
}

// Example usage
object SystolicArrayModelExample extends App {
    private val params = new SystolicArrayParams(4, 4, (a: Int, b: Int) => a * b)

    private val systolicArrayModel = SystolicArrayModel(params)
    private val inputA = ArrayBuffer(1, 2, 3, 4) // Example input for rows
    private val inputB = ArrayBuffer(1, 2, 3, 4) // Example input for columns

    systolicArrayModel.performOperation(inputA, inputB)
    systolicArrayModel.displayGrid()
}